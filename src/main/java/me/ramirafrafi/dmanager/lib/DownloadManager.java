/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ramirafrafi.dmanager.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Admin
 */
public class DownloadManager {    
    protected int nbDownloads;
    protected ExecutorService executorService = null;
    protected Map<StatefulRunnable, CompletableFuture<?>> tasks = null;
    protected Set<StatefulRunnable> downloads = null;
    
    public DownloadManager () {
        this(2);
    }

    public DownloadManager (int nbDownloads) {
        this.nbDownloads = nbDownloads;
        
        this.executorService = Executors.newFixedThreadPool(this.nbDownloads);
        this.tasks = new HashMap<>();
        this.downloads = new HashSet<>();
    }
    
    public CompletableFuture<?> newTask (StatefulRunnable download, boolean startImmediately) {
        if(!downloads.contains(download)) {
            downloads.add(download);
            if (startImmediately) {
                CompletableFuture<?> future = CompletableFuture.runAsync(download, executorService);
                this.tasks.put(download, future);
                download.hangon();
                
                return future;
            }
        }
        
        return null;
    }
    
    public void stopTask (StatefulRunnable download) {
        CompletableFuture<?> future = this.tasks.get(download);
        if (downloads.contains(download) && null != future) {
            if (download.getStatus() == State.PENDING) {
                future.cancel(false);
            }
            download.stop();
        }
    }
    
    public void stopAll () {
        downloads.forEach((download) -> {
            stopTask(download);
        });
    }
    
    public CompletableFuture<?> resumeTask (StatefulRunnable download) {
        if (downloads.contains(download) && (download.getStatus() == State.STOPPED || 
                download.getStatus() == State.ERROR)) {
            CompletableFuture<?> future = CompletableFuture.runAsync(download, executorService);
            this.tasks.put(download, future);
            download.hangon();
            
            return future;
        }
        
        return null;
    }
    
    public void removeTask(StatefulRunnable download) {
        downloads.remove(download);
        tasks.remove(download);
    }
    
    public Set<StatefulRunnable> getDownloads () {
        return this.downloads;
    }
    
    public CompletableFuture<?>[] getFutures() {
        return tasks.values().toArray(CompletableFuture<?>[]::new);
    }
    
    public void shutdown () {
        this.executorService.shutdown();
    }
}
