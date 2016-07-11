/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.model;

import com.ustadmobile.core.omr.OMRImageSource;
import com.ustadmobile.core.omr.OMRRecognizer;
import java.util.concurrent.locks.ReentrantLock;
import jp.sourceforge.qrcode.geom.Point;
import jp.sourceforge.qrcode.util.DebugCanvas;

/**
 *
 * Represents an scannable attendance sheet image.  
 * 
 * @author mike
 */
public class AttendanceSheetImage {
    
    private OMRImageSource imageSource;
    
    private int[] expectedPageArea;
    
    /**
     * Area of rectangles that will be searched through for finder patterns
     */
    private int[][] finderPatternSearchAreas;
    
    /**
     * Default page width to be expected : This is only really used to calculate
     * the aspect ratio: 
     */
    public static final int DEFAULT_PAGE_WIDTH = 210;
    
    public static final int DEFAULT_PAGE_HEIGHT = 297;
    
    /**
     * Distance from the horizontal edge of the page to the center of the finder
     * pattern.
     * 
     * An A4 page is 595.3 pts wide and the PDF generator has 485 pts between 
     * horizontally between the finder pattern centers.
     */
    public static final float DEFAULT_PAGE_X_DISTANCE = ((595.27559055118f - 485)/2)/595.27559055118f;
    
    /**
     * Distance from the vertical edge of the page to the center of the finder
     * pattern.
     * 
     * An A4 page is 841.9 pts tall and the PDF generator area has 722 pts between
     * vertically between the finder pattern centers.
     */
    public static final float DEFAULT_PAGE_Y_DISTANCE = ((841.8897637795275f - 722)/2)/841.8897637795275f;
    
    /**
     * Default distances from the edge of the page to the center of the finder
     * pattern : TOP, LEFT, BOTTOM, RIGHT
     */
    public static final float[] DEFAULT_PAGE_DISTANCES = new float[]{
        DEFAULT_PAGE_Y_DISTANCE, DEFAULT_PAGE_X_DISTANCE,
        DEFAULT_PAGE_Y_DISTANCE, DEFAULT_PAGE_X_DISTANCE
    };
    
    public static final float DEFAULT_PAGE_AREA_MARGIN = 0.1f;
    
    
    /**
     * The size of the area (eg. the width or height of the square) in which
     * to scan for the finder pattern to determine if the image is ready to scan.
     * 
     * This is expressed as a percentage of the width of the 
     */
    public static final float DEFAULT_FINDER_PATTERN_SIZE = 40f/595.27559055118f;
    
    private float pageAreaMargin;
    
    private int pageWidth;
    
    private int pageHeight;
    
    private float[] pageDistances;
    
    private float finderPatternSize;
    
    /**
     * Thread that runs to check if the current image is a sheet with finder 
     * patterns etc.
     */
    private Thread recognitionThread;
    
    
    private ReentrantLock recognitionLock;
    
    
    public AttendanceSheetImage(float margin, int pageWidth, int pageHeight, float[] pageDistances, float finderPatternSize) {
        this.pageAreaMargin = margin;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.pageDistances = pageDistances;
        this.finderPatternSize = finderPatternSize;
    }
    
    public AttendanceSheetImage() {
        this(DEFAULT_PAGE_AREA_MARGIN, DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT,
            DEFAULT_PAGE_DISTANCES, DEFAULT_FINDER_PATTERN_SIZE);
    }
    
    
    /**
     * Gives the page area margin.  This is a float between 0 and 1 (default 0.1) 
     * of the minimum distance between the edge of the image and the edge of the 
     * page within the image as a proportion of the most constrained side.
     * 
     * If the aspect ratio of the page area is smaller
     * than that of the image it's contained in (e.g. the page area is relatively
     * wide) this applies to the x axis, otherwise the y axis.  
     * 
     * @return page area margin as above
     */
    public float getPageAreaMargin() {
        return pageAreaMargin;
    }
    
    
    public void setImageSource(OMRImageSource source) {
        imageSource = source;
        expectedPageArea = OMRRecognizer.getExpectedPageArea(pageWidth, pageHeight, 
            source.getWidth(), source.getHeight(), pageAreaMargin);
        finderPatternSearchAreas = OMRRecognizer.getFinderPatternSearchAreas(
            expectedPageArea, pageDistances, finderPatternSize);
    }
    
    public OMRImageSource getImageSource() {
        return imageSource;
    }
    
    public final void updateImageSource(byte[] buf) {
        try {
            recognitionLock.lock();
            imageSource.setBuffer(buf);
        }finally {
            recognitionLock.unlock();
        }
    }
    
    /**
     * Draw on the given debug canvas the areas of the image as understood 
     * here
     * 
     * @param dc 
     */
    public void drawAreas(DebugCanvas dc) {
        drawRect(expectedPageArea, dc, 0xFFFF0000);
    }
    
    private void drawRect(int[] rect, DebugCanvas dc, int color) {
        dc.drawPolygon(new Point[] {
            new Point(rect[OMRRecognizer.X], rect[OMRRecognizer.Y]),//top left
            new Point(rect[OMRRecognizer.X] + rect[OMRRecognizer.WIDTH], //top right
                rect[OMRRecognizer.Y]),
            new Point(rect[OMRRecognizer.X] + rect[OMRRecognizer.WIDTH],//bottom right
                rect[OMRRecognizer.Y] + rect[OMRRecognizer.HEIGHT]),
            new Point(rect[OMRRecognizer.X], 
                rect[OMRRecognizer.Y] + rect[OMRRecognizer.HEIGHT]) //bottom left
        }, color);
    }
    
    public void startChecking() {
        
    }
    
    
    public boolean isAligned() {
        int i;
        for(i = 0; i < finderPatternSearchAreas.length; i++) {
            
        }
        
        return false;
    }
    
    public class RecognitionThread extends Thread{
        
        public void run() {
            ReentrantLock lock = AttendanceSheetImage.this.recognitionLock;
            try {
                lock.lock();
                
            }finally {
                lock.unlock();
            }
        }
        
    }
    
}
