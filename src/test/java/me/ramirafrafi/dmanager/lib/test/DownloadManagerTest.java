/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ramirafrafi.dmanager.lib.test;

import me.ramirafrafi.dmanager.lib.DownloadManager;
import me.ramirafrafi.dmanager.lib.FileDownload;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Admin
 */
public class DownloadManagerTest {
    static Properties props = null;
    
    static public void main(String args[]) throws Exception {
        loadProps();
        
        FileDownload task1 = new FileDownload(props.getProperty("fileUrl1"), "");
        FileDownload task2 = new FileDownload(props.getProperty("fileUrl2"), "");

        DownloadManager downloadManager = new DownloadManager();
        downloadManager.newTask(task1, true);
        downloadManager.newTask(task2, false);
        
        Thread.sleep(400);
        downloadManager.stopTask(task1);
        downloadManager.resumeTask(task2);
        
        Thread.sleep(10000);
        downloadManager.resumeTask(task1);
        Thread.sleep(400);
        
        downloadManager.shutdown();
    }
    
    private static void loadProps() throws FileNotFoundException, IOException {
        props = new Properties();
        props.load(new FileInputStream("test.properties"));
    }
}
