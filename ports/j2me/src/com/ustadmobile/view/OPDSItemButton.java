/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.view;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Graphics;

/**
 *
 * @author varuna
 */
public class OPDSItemButton extends Button {

    private final int progressHeight = 5;
    private final byte progressTransparency = (byte)80;
    private int progressPercentage;
    
    public OPDSItemButton(String text){
        this.setText(text);
    }
    
    public OPDSItemButton(Command cmd){
        this.setCommand(cmd);
    }

    public void paint(Graphics g) {
        super.paint(g); 

        int x = this.getWidth();
        int y = this.getHeight();
        int yAxis = y - progressHeight;

        float percentageWidth = ((float)x/100) * progressPercentage;
        
        g.fillRect(getX(), getY() + (y - progressHeight), (int)percentageWidth, 
                progressHeight, progressTransparency);
       
     }
    
    public void updateProgress(int progressPercentage){
        this.progressPercentage = progressPercentage;
        //Repaint
        this.repaint();
        
    }
    
    
}
