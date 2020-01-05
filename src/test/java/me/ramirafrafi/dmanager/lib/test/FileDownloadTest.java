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

/**
 *
 * @author Admin
 */
public class FileDownloadTest {
    static Properties props;
    
    public static void main(String arg[]) throws Exception {
        loadProps();
        
        FileDownload fileDownload = new FileDownload(loadFileURL(), "");
        fileDownload.download();
    }
    
    public static String loadFileURL() {
        return props.getProperty("fileUrl");
    }

    private static void loadProps() throws FileNotFoundException, IOException {
        props = new Properties();
        props.load(new FileInputStream("test.properties"));
    }
}
