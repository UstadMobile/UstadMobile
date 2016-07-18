/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.model;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.omr.OMRImageSource;
import com.ustadmobile.core.omr.OMRRecognizer;
import com.ustadmobile.core.omr.PerspectiveTransform;
import java.util.concurrent.locks.ReentrantLock;
import jp.sourceforge.qrcode.geom.Line;
import jp.sourceforge.qrcode.geom.Point;
import jp.sourceforge.qrcode.pattern.FinderPattern;
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
     * The width (units irrelevant) between the centers of the finders pattern - left to right
     * 
     * Default number is in pts as used by the sheet creator
     */
    public static final int DEFAULT_ZONE_WIDTH = 485;
    
    /**
     * The height (units irrelevant) between the centers of the finder patterns - top to bottom
     * 
     * Default number is in pts as used by the sheet creator
     */
    public static final int DEFAULT_ZONE_HEIGHT = 722;
    
    /**
     * The default left offset between the left centers of the finder patterns 
     * and the center of the first OMR mark in the first page column
     */
    public static final float DEFAULT_OMR_OFFSET_X_1 = (12+144);//The margin plus name row width
    
    /**
     * The default left offset between the left centers of the finder pattern and
     * the first center of the first OMR mark in the second page column
     */
    public static final float DEFAULT_OMR_OFFSET_X_2 = (237.44f+144);//the margin plus name row width
    
    /**
     * The default distance between the top of the page's finder patterns and the
     * middle of the OMR space.
     */
    public static final float DEFAULT_OMR_OFFSET_Y = 31.6f;
    
    /**
     * Default distance between optical marks on the x axis - same units as ZONE_WIDTH / ZONE_HEIGHT
     */
    public static final float DEFAULT_OM_DISTANCE_X = 20.8f;
    
    /**
     * Default distance between optical marks on the y axis - same units as ZONE_WIDTH / ZONE_HEIGHT
     */
    public static final float DEFAULT_OM_DISTANCE_Y = 20.651441242f;
    
    
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
    
    private float zoneWidth;
    
    private float zoneHeight;
    
    private float[] pageDistances;
    
    private float finderPatternSize;
    
    private float searchAreaFactor = 3f;
    
    /**
     * Thread that runs to check if the current image is a sheet with finder 
     * patterns etc.
     */
    private RecognitionThread recognitionThread;
    
    
    private ReentrantLock recognitionLock;
    
    /**
     * Buffer used to get the grayscale version of an area in which we are looking
     * for the finder pattern
     */
    private int[][][] searchPatternGsBuf;
    
    /**
     * Buffer used to get the bitmap version of an area in which we are looking
     * for the finder pattern
     */
    private boolean[][][] searchPatternBmBuf;
    
    /**
     * Array of the finder patterns that have been found : in order of fpSearchAreas
     */
    private Point[] finderPatterns;
    
    /**
     * Two dimensional array of finder patterns as follows:
     *  { 
     *    { TOP LEFT, BOTTOM LEFT },
     *    { BOTTOM LEFT, BOTTOM RIGHT} 
     *  }
     */
    private Point[][] recognizedFinderPatterns;
    
    private DebugSaveRequestListener debugSaveListener;
    
    private SheetRecognizedListener recognizedListener;
    
    /**
     * Once the image has been recognized as a sheet by finding all the finder
     * patterns this will be used to keep a reference to that exact frame
     */
    private OMRImageSource recognizedImage;
    
    private boolean focusMoving = false;
    
    public AttendanceSheetImage(float margin, int pageWidth, int pageHeight, float zoneWidth, float zoneHeight, float[] pageDistances, float finderPatternSize) {
        this.pageAreaMargin = margin;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.pageDistances = pageDistances;
        this.finderPatternSize = finderPatternSize;
        this.zoneWidth = zoneWidth;
        this.zoneHeight = zoneHeight;
        recognitionLock = new ReentrantLock();
        gs2BitmapThreshold = DEFAULT_GS2BITMAP_THRESHOLD;
    }
    
    public AttendanceSheetImage() {
        this(DEFAULT_PAGE_AREA_MARGIN, DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT,
            DEFAULT_ZONE_WIDTH, DEFAULT_ZONE_HEIGHT, DEFAULT_PAGE_DISTANCES, 
            DEFAULT_FINDER_PATTERN_SIZE);
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
        searchPatternGsBuf = new int[fpSearchAreas.length][fpSizePx][fpSizePx];
        searchPatternBmBuf = new boolean[fpSearchAreas.length][fpSizePx][fpSizePx];
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
    
    /**
     * The Threshold between 0 and 255 for considering a pixel as either black
     * or white.
     * 
     * @return Threshold value as above.
     */
    public int getGrayscaleThreshold() {
        return gs2BitmapThreshold;
    }
    
    /**
     * The width between the vertical centers of the finder patterns : left and 
     * right lines
     * 
     * The units are arbitary as they are used for a perspective transform
     * but they should be consistent between zoneWidth, zoneHeight and parameters
     * passed to getOMRsByRow
     * 
     * @return  Zone width as above
     */
    public float getZoneWidth() {
        return zoneWidth;
    }
    
    /**
     * The height between the horizontal centers of the finder patterns : top
     * and bottom lines
     * 
     * The units are arbitary as they are used for a perspective transform
     * but they should be consistent between zoneWidth, zoneHeight and parameters
     * passed to getOMRsByRow
     * 
     * @return Zone height as above
     */
    public float getZoneHeight() {
        return zoneHeight;
    }
    
    /**
     * Expected distances between the edge of the page and the centers of finder
     * patterns: in the order : TOP LEFT BOTTOM RIGHT
     * 
     * Expressed as a percentage of the dimension: Eg. TOP and BOTTOM are float
     * values between 0 and 1 as a percentage of the height, LEFT and RIGHT
     * are float values between 0 and 1 as a percentage of the width.
     * 
     * @return Array of floats representing the expected distance between the edge
     * of the page and the center of the finder patterns
     */
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
     * For those devices that know when the focus is moving it's best to wait
     * until the focus has stopped moving.  
     * 
     * When this is set to true it will effectively suspend checking the image
     * 
     * @param focusMoving
     */
    public void setSourceFocusMoving(boolean focusMoving) {
        try {
            recognitionLock.lock();
            this.focusMoving = focusMoving;
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
    
    public void stopChecking() {
        recognitionThread.stopProcesing();;
    }
    
    public int isAligned(OMRImageSource src) {
        Point[] foundPoints;
        int i, numFound = 0;
        short[] minMaxBuf = new short[2];
        short[] minMax = new short[]{-1, -1};
        
        for(i = 0; i < fpSearchAreas.length; i++) {
            src.getGrayscaleImage(searchPatternGsBuf[i], 
                fpSearchAreas[i][OMRRecognizer.X], 
                fpSearchAreas[i][OMRRecognizer.Y],
                fpSearchAreas[i][OMRRecognizer.WIDTH],
                fpSearchAreas[i][OMRRecognizer.HEIGHT],
                minMaxBuf);
            
            if(minMaxBuf[OMRImageSource.MINMAX_BUF_MIN] < minMax[OMRImageSource.MINMAX_BUF_MIN] || minMax[OMRImageSource.MINMAX_BUF_MIN] == -1) {
                minMax[OMRImageSource.MINMAX_BUF_MIN] = minMaxBuf[OMRImageSource.MINMAX_BUF_MIN];
            }
            
            if(minMaxBuf[OMRImageSource.MINMAX_BUF_MAX] > minMax[OMRImageSource.MINMAX_BUF_MAX] || minMax[OMRImageSource.MINMAX_BUF_MAX] == -1) {
                minMax[OMRImageSource.MINMAX_BUF_MAX] = minMaxBuf[OMRImageSource.MINMAX_BUF_MAX];
            }
            
        }
        
        int gsThreshold = minMax[OMRImageSource.MINMAX_BUF_MIN] + ((minMax[OMRImageSource.MINMAX_BUF_MAX] -
            minMax[OMRImageSource.MINMAX_BUF_MIN])/2);
        
        for(i = 0; i < fpSearchAreas.length; i++) {
            AttendanceSheetImage.grayScaleToBitmap(searchPatternGsBuf[i],
                searchPatternBmBuf[i], gsThreshold);
            foundPoints = FinderPattern.findCenters(searchPatternBmBuf[i]);
            if(foundPoints == null || foundPoints.length == 0) {
                return numFound;//if one point is missing - there is no point to checking further
            }else {
                finderPatterns[i] = foundPoints[0];
                numFound++;
            }
        }
        
        //Temporarily don't do this - more testing needed - leave the default level
        //this.gs2BitmapThreshold = gsThreshold;
        handleImageRecognized(src);
        
        return numFound;
    }
    
    /**
     * Handles what needs to be done when the image has been recognized as a valid
     * sheet with all finder patterns located
     * 
     * We need to save a reference to the frame recognized, calculate out where
     * finder patterns are.
     * 
     * @param src 
     */
    private void handleImageRecognized(OMRImageSource src) {
        /*
          Translate the finder points to their position on the master image itself
          as they were found within the fpSearchArea
         */
        for(int i = 0; i < finderPatterns.length; i++) {
            finderPatterns[i].translate(fpSearchAreas[i][OMRRecognizer.X], 
                fpSearchAreas[i][OMRRecognizer.Y]);
        }
        
        recognizedFinderPatterns = OMRRecognizer.sortCenters(finderPatterns, 
            imageSource.getWidth(), imageSource.getHeight());
        
        try {
            recognitionLock.lock();
            this.recognizedImage = src.copy();
            this.recognizedImage.setBuffer(src.getBuffer());
        }finally {
            recognitionLock.unlock();
        }
    }
    
    public OMRImageSource getRecognizedImage() {
        return recognizedImage;
    }
    
    /**
     * Get Line objects that represent the boundaries : essentially a square
     * where the corners are in the center of the finder pattern
     * 
     * @return 
     */
    public Line[] getBoundaryLines() {
        checkImageRecognized();
        Point[][] ptsSorted = OMRRecognizer.sortCenters(finderPatterns, 
            recognizedImage.getWidth(), recognizedImage.getHeight());
        return OMRRecognizer.getBoundaryLines(ptsSorted);
    }
    
    
    
    
    /**
     * Throw an IllegalStateException 
     */
    private void checkImageRecognized() {
        if(recognizedImage == null) {
            //this method has been called before the image was recognized - not OK!
            throw new IllegalStateException("Method called before image recognized");
        }
    }
    
    
    public boolean[][] getOMRsByRow(OMRImageSource src, int gsThreshold, float shadedThreshold, float offsetX, float offsetY, float omWidth, float omSearchDistance, float rowHeight, int numCols, int numRows, DebugCanvas dc) {
        boolean[][] result = new boolean[numRows][numCols];
        Line[] bounds = getBoundaryLines();
        
        int i, j;
        Point imgPoint;
        
        if(dc != null) {
            dc.drawPolygon(new Point[]{
                recognizedFinderPatterns[0][0], recognizedFinderPatterns[1][0],
                recognizedFinderPatterns[1][1], recognizedFinderPatterns[0][1]
            }, 0xFF0000FF);
            
            for(i = 0; i < finderPatterns.length; i++) {
                dc.drawCross(finderPatterns[i], 0xFFFF0000);
            }
        }
        
        /* 
         search distance in pixels when determining if a spot is an optical mark 
         is the width between top left and top right point multiplied by 
         omSearchDistance arg
        */
        int omSearchDistancePx = Math.round(recognizedFinderPatterns[0][0].distanceOf(
            recognizedFinderPatterns[0][1]) * omSearchDistance);
        int omSearchWidth = omSearchDistancePx*2;
        int[][] gsBuf = new int[omSearchWidth][omSearchWidth];
        boolean[][] bmBuf = new boolean[omSearchWidth][omSearchWidth];
        
        double[] srcPts=  new double[numRows*numCols*2];
        double[] imgPts = new double[srcPts.length];
        
        int ptIndex;
        for(i = 0; i < numRows; i++) {
            for(j = 0; j < numCols; j++) {
                ptIndex = ((i*numCols) + j) * 2;
                srcPts[ptIndex] = (offsetX + (j*omWidth));
                srcPts[ptIndex + 1] = (offsetY + (i*rowHeight));
            }
        }
        PerspectiveTransform tx = getPerspectiveTransform(getZoneWidth(), getZoneHeight());
        
        tx.transform(srcPts, 0, imgPts, 0, numRows*numCols);
        
        for(i = 0; i < numRows; i++) {
            for(j = 0; j < numCols; j++) {
                ptIndex = ((i*numCols) + j) * 2;
                imgPoint = new Point((int)Math.round(imgPts[ptIndex]),
                    (int)Math.round(imgPts[ptIndex + 1]));
                
                result[i][j] = isOMRMark(src, imgPoint, omSearchDistancePx,
                    gsThreshold, shadedThreshold, gsBuf, bmBuf, dc);
                if(dc != null) {
                    if (result[i][j] == true){
                        dc.drawCross(imgPoint, 0xFF00FF00); //Green
                    }else{
                        dc.drawCross(imgPoint, 0xFFFF0000); //red
                    }

                }
            }
        }
        
        return result;
    }
    
    public PerspectiveTransform getPerspectiveTransform(double width, double height) {
        return getPerspectiveTransform(new Point[] {
            recognizedFinderPatterns[0][0], recognizedFinderPatterns[1][0],
            recognizedFinderPatterns[1][1], recognizedFinderPatterns[0][1]
        }, width, height);
    }
    
    /**
     * 
     * @param corners Array of points in the order of Top left, top right, bottom right, bottom left
     * @return 
     */
    public PerspectiveTransform getPerspectiveTransform(Point[] corners, double width, double height) {
        PerspectiveTransform tx = PerspectiveTransform.getQuadToQuad(
            corners[0].getX(), corners[0].getY(), corners[1].getX(), corners[1].getY(),
            corners[2].getX(), corners[2].getY(), corners[3].getX(), corners[3].getY(),
            0, 0, width,0,        width,height,      0,height);
        try {
            tx = tx.createInverse();
        }catch(CloneNotSupportedException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 90, null, e);
            tx = null;
        }
        
        return tx;
    }
    
    
    
    
    /**
     * Determine if the given point is marked or not
     * 
     * @param src
     * @param pt
     * @param searchDistance
     * @param gsThreshold Treshold between 0 and 255 to determine if grayscale pixel is considered black or white
     * @float shadedThreshold % of pixels looked at that should be black to be considered a marked point
     * @param gsBuf
     * @param bmBuf
     * @return 
     */
    private boolean isOMRMark(OMRImageSource src, Point pt, int searchDistance, int gsThreshold, float shadedThreshold, int[][] gsBuf, boolean[][] bmBuf, DebugCanvas dc) {
        src.getGrayscaleImage(gsBuf, pt.getX()-searchDistance, pt.getY()-searchDistance, 
            searchDistance*2, searchDistance*2, null);
        grayScaleToBitmap(gsBuf, bmBuf, gsThreshold);
        int x, y, count = 0;
        
        for(x = 0; x < bmBuf.length; x++) {
            for(y = 0; y < bmBuf[0].length; y++) {
                if(bmBuf[x][y]) {
                    count++;
                }
            }
        }
        
        if(dc != null) {
            drawRect(new int[]{pt.getX()-searchDistance, pt.getY()-searchDistance, 
                searchDistance*2, searchDistance*2}, dc, 0xFF0000FF);
        }
        
        return ((float)count/((float)bmBuf.length*bmBuf[0].length) > shadedThreshold);
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
            byte[] newBuf = null;
            int numPoints;
            
            long lastSaved = 0;
            long timeNow = 0;
            boolean focusMoving;
            while(running && !aligned) {
                try {
                    lock.lock();
                    focusMoving = AttendanceSheetImage.this.focusMoving;
                    running = threadActive;
                    newBuf = AttendanceSheetImage.this.imageSource.getBuffer();
                }finally {
                    lock.unlock();
                }
                
                if(newBuf != lastChecked && !focusMoving) {
                    //check the image
                    src.setBuffer(newBuf);
                    numPoints = AttendanceSheetImage.this.isAligned(src);
                    aligned = numPoints == 4;
                    lastChecked = newBuf;
                }else {
                    //sleep and wait
                    try { Thread.sleep(NEWBUF_SLEEPWAIT); }
                    catch(InterruptedException e) {}
                }
                
                
                //see if we need to save debug imagery
                if(AttendanceSheetImage.this.debugSaveListener != null) {
                    timeNow = System.currentTimeMillis();
                    if(timeNow - lastSaved > 10000 && newBuf != null) {
                        try {
                            lock.lock();
                            AttendanceSheetImage.this.debugSaveListener.saveDebugImage(
                                AttendanceSheetImage.this, src);
                            lastSaved = timeNow;
                        }finally {
                            lock.unlock();
                        }
                    }
                }
            }
            
            if(recognizedListener != null && aligned) {
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
