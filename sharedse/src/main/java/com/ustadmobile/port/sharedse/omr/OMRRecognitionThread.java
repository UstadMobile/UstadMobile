/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.sharedse.omr;

/**
 *
 * @author mike
 */
public class OMRRecognitionThread {
    
    private AttendanceSheetImage sheet;
    
    private static final int MIN_SLEEP_TIME = 50;
    
    private byte[] lastBufferChecked;
    
    private boolean running;
    
    public OMRRecognitionThread(AttendanceSheetImage sheet) {
        this.sheet = sheet;
    }
    
    public void run() {
        
        while(running) {
            
        }
    }
    
}
