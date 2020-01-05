/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ramirafrafi.dmanager.lib;

/**
 *
 * @author Admin
 */
public interface StatefulRunnable extends Runnable {
    public void hangon();
    public void stop();
    public State getStatus();
}
