/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ramirafrafi.dmanager.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

/**
 *
 * @author Admin
 */
public class FileDownload implements StatefulRunnable {

    private String fileUrl = null;
    private String downloadDir = null;
    private String fileName = null;
    private String contentType = null;
    private long contentLength = -1;
    private State status = State.STOPPED;
    private File localFile = null;
    private long downloaded = 0;
    private FileOutputStream outputStream = null;
    private AsyncHttpClient httpClient = null;
    private FileDownloadListener listener = new FileDownloadListener() {
        @Override
        public void onDownload(FileDownload fileDownload) {
        }

        @Override
        public void onCompleted(State status, Response response) {
        }

        @Override
        public void onAdvance(long downloaded) {
        }

        @Override
        public void onError(FileDownload fileDownload) {
        }

        @Override
        public void onHangon(FileDownload fileDownload) {
        }
    };
    private final AsyncHandler<Object> asyncHandler = new AsyncCompletionHandler<Object>() {
        @Override
        public AsyncHandler.State onBodyPartReceived(HttpResponseBodyPart hrbp) throws Exception {
            ByteBuffer buffer = hrbp.getBodyByteBuffer();
            outputStream.getChannel().write(buffer);
            downloaded += hrbp.length();
            listener.onAdvance(downloaded);
            return status == me.ramirafrafi.dmanager.lib.State.STOPPED
                    ? AsyncHandler.State.ABORT : AsyncHandler.State.CONTINUE;
        }

        @Override
        public Object onCompleted(Response response) throws Exception {
            if (status == me.ramirafrafi.dmanager.lib.State.DOWNLOADING) {
                status = me.ramirafrafi.dmanager.lib.State.COMPLETE;
            }

            listener.onCompleted(status, response);
            close();

            return response;
        }

        @Override
        public void onThrowable(Throwable t) {
            try {
                status = me.ramirafrafi.dmanager.lib.State.ERROR;

                listener.onError(FileDownload.this);
                close();
            } catch (IOException ex) {
                Logger.getLogger(FileDownload.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    public FileDownload(String fileUrl, String downloadDir) throws IOException, MalformedURLException, InterruptedException, ExecutionException, MimeTypeException {
        this.fileUrl = fileUrl;
        this.downloadDir = downloadDir;
        this.requestInfos();
        this.initLocalFile();
    }

    public FileDownload(String fileUrl, String downloadDir, String fileName) throws IOException, MalformedURLException, InterruptedException, ExecutionException, MimeTypeException {     
        this.fileUrl = fileUrl;
        this.downloadDir = downloadDir;
        this.fileName = fileName;
        this.requestInfos();
        this.initLocalFile();
    }

    @Override
    public void run() {
        try {
            this.download().get();
        } catch (InterruptedException | ExecutionException | IOException ex) {
            Logger.getLogger(FileDownload.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop() {
        if (this.status != State.COMPLETE) {
            this.status = State.STOPPED;
        }
    }

    @Override
    public void hangon() {
        if (this.status == State.STOPPED || this.status == State.ERROR) {
            this.status = State.PENDING;
            listener.onHangon(this);
        }
    }

    @Override
    public State getStatus() {
        return status;
    }

    public String getFileName() {
        return fileName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public String getFilePath() {
        return this.downloadDir + File.separator + this.fileName;
    }

    public void setListener(FileDownloadListener listener) {
        this.listener = listener;
    }

    public Future<Object> download() throws FileNotFoundException, IOException {
        if (this.status == State.STOPPED || this.status == State.PENDING
                || this.status == State.ERROR) {
            this.status = State.DOWNLOADING;

            this.setupOutputStream();
            this.setupHttpClient();

            Request request = this.httpClient
                    .prepareGet(this.fileUrl)
                    .setRequestTimeout(Duration.ofDays(365))
                    .setReadTimeout(Duration.ofMinutes(2))
                    .setHeader("Range", "bytes=" + this.downloaded + "-")
                    .build();

            this.listener.onDownload(this);

            return this.httpClient.executeRequest(request, this.asyncHandler);
        }
        return null;
    }

    public void close() throws IOException {
        this.flushOutputStream();
        this.closeHttpClient();
        this.closeOutputStream();
    }

    private void setupOutputStream() throws FileNotFoundException {
        if (null == this.outputStream) {
            if (-1 == this.contentLength) {
                this.outputStream = new FileOutputStream(this.localFile, false);
            } else {
                this.outputStream = new FileOutputStream(this.localFile, true);
            }
        }
    }

    private void setupHttpClient() {
        if (null == this.httpClient) {
            this.httpClient = Dsl.asyncHttpClient();
        }
    }

    private void requestInfos() throws ExecutionException, MimeTypeException, InterruptedException, MalformedURLException, IOException {
        this.setupHttpClient();

        Request request = Dsl.head(this.fileUrl).build();
        Future<Response> responseFuture = this.httpClient.executeRequest(request);
        Response response = responseFuture.get();
        this.closeHttpClient();

        this.setupFilename(response);
        try {
            this.contentLength = Long.parseLong(response.getHeader("Content-Length"));
        } catch (NumberFormatException e) {
        }
        this.contentType = response.getHeader("Content-Type");

    }

    private void closeOutputStream() throws IOException {
        if (null != this.outputStream) {
            this.outputStream.close();
            this.outputStream = null;
        }
    }

    private void closeHttpClient() throws IOException {
        if (null != this.httpClient) {
            this.httpClient.close();
            this.httpClient = null;
        }
    }

    private void setupFilename(Response response) throws MimeTypeException, MalformedURLException {
        if (null == this.fileName) {
            String disposition = response.getHeader("Content-Disposition");
            if (null == disposition) {
                String[] arr = (new URL(fileUrl)).getPath().split("/");
                try {
                    this.fileName = arr[arr.length - 1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    String contType = response.getHeader("Content-Type");
                    if (contType.contains(";")) {
                        contType = contType.substring(0, contType.indexOf(";"));
                    }

                    String ext = MimeTypes.getDefaultMimeTypes()
                            .forName(contType)
                            .getExtension();
                    this.fileName = RandomStringUtils.randomAlphanumeric(32) + ext;
                }
            } else {
                this.fileName = disposition.substring(disposition.indexOf("filename=") + 9)
                        .replace("\"", "");
            }
        }
    }

    private void flushOutputStream() throws IOException {
        if (null != this.outputStream) {
            this.outputStream.flush();
        }
    }

    private void initLocalFile() {
        this.localFile = new File(this.getFilePath());
        this.downloaded = localFile.exists() ? localFile.length() : 0;
        if (downloaded == contentLength) {
            status = State.COMPLETE;
        }
    }
}
