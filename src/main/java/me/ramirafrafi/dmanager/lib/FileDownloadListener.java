/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ramirafrafi.dmanager.lib;

import org.asynchttpclient.Response;

/**
 *
 * @author Admin
 */
public interface FileDownloadListener {
    public void onDownload(FileDownload fileDownload);
    public void onCompleted(State status, Response response);
    public void onAdvance(long downloaded);
    public void onError(FileDownload fileDownload);
    public void onHangon(FileDownload fileDownload);
}
