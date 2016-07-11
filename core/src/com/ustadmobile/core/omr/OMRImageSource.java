/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.omr;


/**
 * Interface representing an image buffer (e.g. from a live preview) that can be 
 * checked to see if it's a valid OMR image with finder patterns etc.  This 
 * requires high speed retrieval of particular cropped areas of the image in
 * grayscale
 * 
 * @author mike
 */
public interface OMRImageSource {
    
    /**
     * Puts the grayscale image into the given buffer
     * 
     * @param buf
     * @param x
     * @param y
     * @param width
     * @param height
     * @return 
     */
    public void getGrayscaleImage(int[][] buf, int x, int y, int width, int height);
    
    /**
     * Returns the width of the image
     * 
     * @return Width of image in pixels
     */
    public int getWidth();
    
    /**
     * Returns the height of the image
     * 
     * @return Height of image in pixels
     */
    public int getHeight();
    
}
