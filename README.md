# dmanager-lib

## Overview
The **dmanager-lib** library makes it easier to manage asynchronous and parallel downloads in Java. It is built on top of [AsyncHttpClient](https://github.com/AsyncHttpClient/async-http-client) library.

## Installation
The library is not yet published into Maven repository, you can download the [JAR file](build/dmanager-lib-1.0-SNAPSHOT-jar-with-dependencies.jar) and add it to your project.

## Documentation
This library defines classes that can be used separately in a flexible manner, this doc takes a tour in explaining them.

### Class [`FileDownload`](src/main/java/me/ramirafrafi/dmanager/lib/FileDownload.java)
Class presenting a single download task, which implements [Runnable](https://docs.oracle.com/javase/8/docs/api/java/lang/Runnable.html):
```java
import me.ramirafrafi.dmanager.lib.FileDownload;

class FileDownloadExample 
{
  public static void main(String[] argv)
    {
      FileDownload fileDownload = new FileDownload(
        "https://example.net/file.zip", // URL of the file to be downloaded
        "/home/user/Downloads",         // Directory in which file will be saved
        "file-custom-name.zip"          // Custom filename for the saved file (Optional)
      );

      new Thread(fileDownload).start(); // Start the file download in a thread as it implements the Runnable interface, the run() method will be called automatically.

      fileDownload.hangon();    // Pauses the download.
      fileDownload.download();  // Resumes the download.
      fileDownload.stop();      // Stops the download.
      fileDownload.close();     // Releases resources.
    } 
}
```

### Interface [`FileDownloadListener`](src/main/java/me/ramirafrafi/dmanager/lib/FileDownloadListener.java)
Interface definition for callbacks to be invoked on different download status changes:
```java
import me.ramirafrafi.dmanager.lib.FileDownload;
import me.ramirafrafi.dmanager.lib.FileDownloadListener;

class FileDownloadListenerExample 
{
  public static void main(String[] argv)
    {
      FileDownload fileDownload = new FileDownload(
        "https://example.net/file.zip",
        "/home/user/Downloads",
        "file-custom-name.zip" 
      );

      new Thread(fileDownload).start();

      fileDownload.setListener(new FileDownloadListener() {
            @Override
            public void onDownload(FileDownload fileDownload) {
                // Download started, no bytes have heen downloaded yet.
            }

            @Override
            public void onCompleted(State status, Response response) {
                // Download completed, all bytes have been downloaded.
            }

            @Override
            public void onAdvance(long downloaded) {
                // Download in progress, `downloaded` is the total bytes downloaded at the moment.
            }
            
            @Override
            public void onError(FileDownload fileDownload) {
                // An error has been occured during download.
            }

            @Override
            public void onHangon(FileDownload fileDownload) {
                // Download has been paused calling `fileDownload.hangon()`
            }
        });
    } 
}
```

### Class [`DownloadManager`](src/main/java/me/ramirafrafi/dmanager/lib/DownloadManager.java)
Class managing a pool of `FileDownload`:
```java
import me.ramirafrafi.dmanager.lib.FileDownload;
import me.ramirafrafi.dmanager.lib.DownloadManager;

class DownloadManagerExample 
{
  public static void main(String[] argv)
    {
      DownloadManager downloadManager = new DownloadManager(
        5,  // Number of downloads that can be run in the same time, DownloadManager allocates internally a `Executors.newFixedThreadPool` (Optional, defaults to 2)
      );

      FileDownload fileDownload1 = new FileDownload(
        "https://example.net/file1.zip",
        "/home/user/Downloads",
        "file1-custom-name.zip" 
      );

      FileDownload fileDownload2 = new FileDownload(
        "https://example.net/file2.zip",
        "/home/user/Downloads",
        "file2-custom-name.zip" 
      );

      downloadManager.newTask(
        fileDownload1,  // Add `fileDownload1` to the managed pool
        true            // When it is `true`, download will be put in the waiting queue if all the pool is used, or will be started immediately.
      );

      downloadManager.newTask(
        fileDownload2,  // Add `fileDownload2` to the managed pool
        false           // When it is `false`, will not be put into queue until `downloadManager.resumeTask(fileDownload2)` is called.
      );
      downloadManager.resumeTask(fileDownload2);    // Starts `fileDownload2`.

      downloadManager.stopTask(fileDownload1);      // Stops `fileDownload1`.
      downloadManager.resumeTask(fileDownload1);    // Resumes `fileDownload1`.

      downloadManager.stopAll();    // Stop all downloads.
    } 
}
```
