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
import jp.sourceforge.qrcode.pattern.FinderPattern;
import jp.sourceforge.qrcode.reader.QRCodeImageReader;
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
    private int[][] fpSearchAreas;
    
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
    
    /**
     * The default threshold to determine if the pixel is to be counted as black
     * or white
     */
    public static final int DEFAULT_GS2BITMAP_THRESHOLD = 128;
    
    private int gs2BitmapThreshold;
    
    private float pageAreaMargin;
    
    private int pageWidth;
    
    private int pageHeight;
    
    private float[] pageDistances;
    
    private float finderPatternSize;
    
    private float searchAreaFactor = 3f;
    
    /**
     * Thread that runs to check if the current image is a sheet with finder 
     * patterns etc.
     */
    private Thread recognitionThread;
    
    
    private ReentrantLock recognitionLock;
    
    /**
     * Buffer used to get the grayscale version of an area in which we are looking
     * for the finder pattern
     */
    private int[][] searchPatternGsBuf;
    
    /**
     * Buffer used to get the bitmap version of an area in which we are looking
     * for the finder pattern
     */
    private boolean[][] searchPatternBmBuf;
    
    /**
     * Array of the finder patterns that have been found
     */
    private Point[] finderPatterns;
    
    private DebugSaveRequestListener debugSaveListener;
    
    private SheetRecognizedListener recognizedListener;
    
    public AttendanceSheetImage(float margin, int pageWidth, int pageHeight, float[] pageDistances, float finderPatternSize) {
        this.pageAreaMargin = margin;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.pageDistances = pageDistances;
        this.finderPatternSize = finderPatternSize;
        recognitionLock = new ReentrantLock();
        gs2BitmapThreshold = DEFAULT_GS2BITMAP_THRESHOLD;
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
    
    public void setOnDebugSaveRequestListener(DebugSaveRequestListener listener) {
        this.debugSaveListener = listener;
    }
    
    public void setOnSheetRecognizedListener(SheetRecognizedListener listener) {
        this.recognizedListener = listener;
    }
    
    
    public void setImageSource(OMRImageSource source) {
        imageSource = source;
        calcExpectedAreas(source.getWidth(), source.getHeight());
    }
    
    /**
     * Determine on the basis of the width and height of the image stream where
     * the finder patterns would be found.
     * 
     * @param width
     * @param height 
     */
    public void calcExpectedAreas(int width, int height) {
        expectedPageArea = OMRRecognizer.getExpectedPageArea(pageWidth, pageHeight, 
            width, height, pageAreaMargin);
        fpSearchAreas = OMRRecognizer.getFinderPatternSearchAreas(
            expectedPageArea, pageDistances, finderPatternSize * searchAreaFactor);
        int fpSizePx = fpSearchAreas[0][OMRRecognizer.WIDTH];
        searchPatternGsBuf = new int[fpSizePx][fpSizePx];
        searchPatternBmBuf = new boolean[fpSizePx][fpSizePx];
        finderPatterns = new Point[fpSearchAreas.length];
    }
    
    public OMRImageSource getImageSource() {
        return imageSource;
    }
    
    public int[][] getFinderPatternSearchAreas() {
        return fpSearchAreas;
    }
    
    public int[] getExpectedPageArea() {
        return expectedPageArea;
    }
    
    public int getPageWidth() {
        return pageWidth;
    }
    
    public int getPageHeight() {
        return pageHeight;
    }
    
    public float[] getPageDistances() {
        return pageDistances;
    }
    
    public float getFinderPatternSize() {
        return finderPatternSize;
    }
    
    /**
     * Update the buffer data for the image source in a thread safe way
     * 
     * @param buf 
     */
    public final void updateImageSource(byte[] buf) {
        try {
            recognitionLock.lock();
            imageSource.setBuffer(buf);
        }finally {
            recognitionLock.unlock();
        }
    }
    
    /**
     * Determines the size of the search area in which we look for the finder
     * pattern.  The size of the finder pattern is multiplied by this to determine
     * the area of the image in which to check.
     * 
     * The bigger this is the less accurate the alignment of the page to the 
     * expected page area needs to be: but as it gets bigger checking the image
     * will get slower
     * 
     * @return Finder pattern search area factor as described above
     */
    public float getSearchAreaFactor() {
        return searchAreaFactor;
    }

    /**
     * Sets the finder pattern search area factor as per getSearchAreaFactor.
     * For this to be effective it must be called before calling setImageSource
     * 
     * @see AttendanceSheetImage#getSearchAreaFactor() 
     * 
     * @param searchAreaFactor The new factor to use.  
     */
    public void setSearchAreaFactor(float searchAreaFactor) {
        this.searchAreaFactor = searchAreaFactor;
    }
    
    /**
     * Draw on the given debug canvas the areas of the image as understood 
     * here
     * 
     * @param dc 
     */
    public void drawAreas(DebugCanvas dc) {
        drawRect(expectedPageArea, dc, 0xFFFF0000);
        for(int i = 0; i < fpSearchAreas.length; i++) {
            drawRect(fpSearchAreas[i], dc, 0xFF0000FF);
        }
    }
    
    public ReentrantLock getLock() {
        return recognitionLock;
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
         recognitionThread = new RecognitionThread();
         recognitionThread.start();
    }
    
    public int isAligned(OMRImageSource src) {
        Point[] foundPoints;
        int numFound = 0;
        for(int i = 0; i < fpSearchAreas.length; i++) {
            src.getGrayscaleImage(searchPatternGsBuf, 
                fpSearchAreas[i][OMRRecognizer.X], 
                fpSearchAreas[i][OMRRecognizer.Y],
                fpSearchAreas[i][OMRRecognizer.WIDTH],
                fpSearchAreas[i][OMRRecognizer.HEIGHT]);
            AttendanceSheetImage.grayScaleToBitmap(searchPatternGsBuf,
                searchPatternBmBuf, gs2BitmapThreshold);
            foundPoints = FinderPattern.findCenters(searchPatternBmBuf);
            if(foundPoints == null || foundPoints.length == 0) {
                //do nothing for now
            }else {
                finderPatterns[i] = foundPoints[0];
                numFound++;
            }
        }
        
        return numFound;
    }
    
    /**
     * Convert the given grayscale image to a boolean bitmap array
     * 
     * @param gs 2 dimensional array of 32bit pixels - expected to be grayscale
     * @param out 2 dimensional boolean array to put the image into
     * @param threshold If a pixel is darker than the given threshold (e.g. black) 
     * it's assigned to true in the out buffer, false otherwise
     */
    public static void grayScaleToBitmap(int[][] gs, boolean[][] out, int threshold) {
        int x, y;
        for(x = 0; x < gs.length; x++) {
            for(y = 0; y < gs[0].length; y++) {
                out[x][y] = (gs[x][y] & 0xFF) < threshold;
            }
        }
    }

    
    public class RecognitionThread extends Thread{
        
        private boolean threadActive = true;
        
        private OMRImageSource src;
        
        public static final int NEWBUF_SLEEPWAIT = 30;
        
        
        public RecognitionThread() {
            try {
                AttendanceSheetImage.this.recognitionLock.lock();
                this.src = AttendanceSheetImage.this.imageSource.copy();
            }finally {
                AttendanceSheetImage.this.recognitionLock.unlock();
            }
            
        }
        
        public void run() {
            ReentrantLock lock = AttendanceSheetImage.this.recognitionLock;
            
            boolean running = true;
            boolean aligned = false;
            byte[] lastChecked = null;
            byte[] newBuf;
            int numPoints;
            int life = 0;
            
            long lastSaved = 0;
            long timeNow = 0;
            while(running && !aligned) {
                try {
                    lock.lock();
                    newBuf = AttendanceSheetImage.this.imageSource.getBuffer();
                }finally {
                    lock.unlock();
                }
                
                if(newBuf != lastChecked) {
                    //check the image
                    src.setBuffer(newBuf);
                    numPoints = AttendanceSheetImage.this.isAligned(src);
                    if(numPoints > 0) {
                        life = life+42;
                    }
                    aligned = numPoints == 4;
                    lastChecked = newBuf;
                }else {
                    //sleep and wait
                    try { Thread.sleep(NEWBUF_SLEEPWAIT); }
                    catch(InterruptedException e) {}
                }
                
                
                //check and see if we should still be running
                try {
                    lock.lock();
                    if(AttendanceSheetImage.this.debugSaveListener != null) {
                        timeNow = System.currentTimeMillis();
                        if(timeNow - lastSaved > 10000 && newBuf != null) {
                            AttendanceSheetImage.this.debugSaveListener.saveDebugImage(AttendanceSheetImage.this, src);
                            lastSaved = timeNow;
                        }
                    }
                    
                    running = threadActive;
                }finally {
                    lock.unlock();
                }
                
                
            }
            
            if(recognizedListener != null) {
                recognizedListener.sheetRecognized(AttendanceSheetImage.this);
            }
        }
        
        public void stopProcesing() {
            try {
                AttendanceSheetImage.this.recognitionLock.lock();
                threadActive = false;
            }finally {
                AttendanceSheetImage.this.recognitionLock.unlock();
            }
        }
        
    }
    
    public static interface DebugSaveRequestListener {
        public void saveDebugImage(AttendanceSheetImage sheet, OMRImageSource imgSrc);
    }
    
    public static interface SheetRecognizedListener {
        public void sheetRecognized(AttendanceSheetImage sheet);
    }
}
