/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ramirafrafi.dmanager.lib.test;

import me.ramirafrafi.dmanager.lib.FileDownload;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Admin
 */
public class DownloadPoolTest {
    static Properties props;
    
    static public void main(String args[]) throws Exception {
        loadProps();
        
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(new FileDownload(props.getProperty("fileUrl1"), ""));
        executorService.submit(new FileDownload(props.getProperty("fileUrl2"), ""));
        executorService.submit(new FileDownload(props.getProperty("fileUrl3"), ""));
        executorService.submit(new FileDownload(props.getProperty("fileUrl4"), ""));
        executorService.shutdown();
    }

    private static void loadProps() throws FileNotFoundException, IOException {
        props = new Properties();
        props.load(new FileInputStream("test.properties"));
    }
}
