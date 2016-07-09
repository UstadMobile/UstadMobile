/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.core.omr;

import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.data.QRCodeImage;
import jp.sourceforge.qrcode.geom.Line;
import jp.sourceforge.qrcode.geom.Point;
import jp.sourceforge.qrcode.pattern.FinderPattern;
import jp.sourceforge.qrcode.reader.QRCodeImageReader;
import jp.sourceforge.qrcode.util.DebugCanvas;

/**
 *
 * @author mike
 */
public class OMRRecognizer {

    
    /**
     * Top corner for both arrays of points and rectangle coordinates 
     */
    public static final int TOP = 0;
    
    
    /**
     * Left corner for both arrays of points and rectangle coordinates 
     */
    public static final int LEFT = 1;
    
    /**
     * Bottom corner for arrays of points
     */
    public static final int BOTTOM = 2;
    
    /**
     * Right corner for arrays of points
     */
    public static final int RIGHT = 3;
    
    
    /**
     * X coordinate for rectangle cordinates returned as int array
     */
    public static final int X = 0;
    
    /**
     * Y coordinate for rectangle coordinates return as int array
     */
    public static final int Y = 1;
    
    /**
     * Width for rectangle coordinates returned as an int array
     */
    public static final int WIDTH = 2;

    /**
     * Height for rectangle coordinates return as an int array
     */
    public static final int HEIGHT = 3;
    
    //measured in inkscape
    //Most of these are for test cases and these arent the ones in use.
    public static final float AREA_WIDTH = 607f;
    
    public static final float AREA_HEIGHT = 902f;
    
    public static final float OMR_AREA_OFFSET_X = (311f/AREA_WIDTH);
    
    public static final float OMR_AREA_OFFSET_Y = (37.5f/AREA_HEIGHT);
    
    public static final float OM_WIDTH = 26f/AREA_WIDTH;
    
    public static final float OM_HEIGHT = 20f/AREA_HEIGHT;
    
    public static final float OM_ROW_HEIGHT = 25.8f/AREA_HEIGHT;

    /**
     * Method that finds the intersect between boundaries and x & y axis given to it.
     * @param boundaries
     * @param x
     * @param y
     * @param dc
     * @return
     */
    public static Point txPoint(Line[] boundaries, float x, float y, DebugCanvas dc) {
        Line vertical = new Line(boundaries[TOP].getMidpoint(x), boundaries[BOTTOM].getMidpoint(x));
        Line horizontal = new Line(boundaries[LEFT].getMidpoint(y), boundaries[RIGHT].getMidpoint(y));

        /*if (dc!= null) {
            dc.drawLine(vertical, 0xFF00FFFF); //cyan
            dc.drawLine(horizontal, 0xFFFF00FF); //magenta
        }*/

        Point intersect = vertical.getIntersect(horizontal);
        return intersect;
    }
    
    /**
     * From the sorted array of points make an array in the order
     * of top, left, bottom, right of lines that represent the bounds
     * defined by the finder pattern
     * s
     * @param centers Point[][] array in 
     * @return 
     */
    public static Line[] getBoundaryLines(Point[][] centers) {
        return new Line[] {
            new Line(centers[0][0], centers[1][0]),
            new Line(centers[0][0], centers[0][1]),
            new Line(centers[0][1], centers[1][1]),
            new Line(centers[1][0], centers[1][1])
        };
    }
    
    /**
     * Process the image to find out which row items were marked.  QR Code square
     * finder patterns need to be on the corners.  Where + is a QR code finder
     * pattern and there is an area of marks that could be filled in by the
     * user represented by o
     * 
     * ------------------------------------------------------
     *  +-------------------------------------------------- + 
     *  |                                                   |
     *  |                                                   |
     *  |                          ------------------       |
     *  |                          |  o o o o o     |       |
     *  |                          |  o o o o o     |       |
     *  |                          |  o o o o o     |       |
     *  |                          |  o o o o o     |       |
     *  |                          |  o o o o o     |       |
     *  |                          ------------------       |
     *  |                                                   |
     *  +-------------------------------------------------- +
     * -------------------------------------------------------
     * 
     * @param img 2 dimensional boolean array representing the image (true = black, false=white)
     * @param bounds Boundary lines in the order top, left, bottom, right that 
     * @param offsetX represents the distance from the left boundary line to the 
     * center of the first circle where an optical mark would be: this is a number
     * between 0 and 1 as a percentage of the distance between the finder patterns
     * from the left line.
     * 
     * @param offsetY represents the distance from the top boundary line to the 
     * center of the first circle where an optical mark would be: this is a number
     * between 0 and 1 as a percentage of the distance between the finder patterns
     * from the top line.
     * 
     * @param omWidth The width of an optical mark as measured from the center of
     * one mark to the center of the next mark to the left: as a float between 0 
     * and 1 as a percentage of the width of the area within the finder patterns
     * 
     * @param rowHeight The height of an optical mark area as measured from the
     * center of one mark to the center of the next mark below
     * 
     * @param numCols the number of columns of evenly spaced areas for optical
     * marks to occur
     * 
     * @param numRows the number of rows of evenly spaced area for optical marks
     * to occur
     * 
     * @param dc Debug canvas that can be used to show an image of the processing
     * going on
     * 
     * @return A two dimensional boolean array representing the marks as they
     * were filled in - in the form of returnValue[row][col] : will be true if a
     * mark was found: false otherwise
     */
    public static boolean[][] getMarks(boolean[][] img, Line[] bounds, float offsetX, float offsetY, float omWidth, float omHeight, float rowHeight, int numCols, int numRows, DebugCanvas dc) {
        boolean[][] result = new boolean[numRows][numCols];
        
        int i;
        int j;
        
        Point stPoint = txPoint(bounds, offsetX, offsetY, dc);
        
        if(dc != null) {
            dc.drawCross(stPoint, 0xFFFF0000); //red
        }
        
        for(i = 0; i < numRows; i++) {
            for(j = 0; j < numCols; j++) {
                float xPos = offsetX + (j*omWidth);
                float yPos = offsetY + (i*rowHeight);
                Point imgPoint = txPoint(bounds, xPos, yPos, dc);
                result[i][j] = img[imgPoint.getX()][imgPoint.getY()];
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
    
    /**
     * Process the image to find out which row items were marked.  QR Code square
     * finder patterns need to be on the corners.  Where + is a QR code finder
     * pattern and there is an area of marks that could be filled in by the
     * user represented by o
     * 
     * ------------------------------------------------------
     *  +-------------------------------------------------- + 
     *  |                                                   |
     *  |                                                   |
     *  |                          ------------------       |
     *  |                          |  o o o o o     |       |
     *  |                          |  o o o o o     |       |
     *  |                          |  o o o o o     |       |
     *  |                          |  o o o o o     |       |
     *  |                          |  o o o o o     |       |
     *  |                          ------------------       |
     *  |                                                   |
     *  +-------------------------------------------------- +
     * -------------------------------------------------------
     * 
     * @param img 2 dimensional boolean array representing the image (true = black, false=white)
     * @param offsetX represents the distance from the left boundary line to the 
     * center of the first circle where an optical mark would be: this is a number
     * between 0 and 1 as a percentage of the distance between the finder patterns
     * from the left line.
     * 
     * @param offsetY represents the distance from the top boundary line to the 
     * center of the first circle where an optical mark would be: this is a number
     * between 0 and 1 as a percentage of the distance between the finder patterns
     * from the top line.
     * 
     * @param omWidth The width of an optical mark as measured from the center of
     * one mark to the center of the next mark to the left: as a float between 0 
     * and 1 as a percentage of the width of the area within the finder patterns
     * 
     * @param rowHeight The height of an optical mark area as measured from the
     * center of one mark to the center of the next mark below
     * 
     * @param numCols the number of columns of evenly spaced areas for optical
     * marks to occur
     * 
     * @param numRows the number of rows of evenly spaced area for optical marks
     * to occur
     * 
     * @param dc Debug canvas that can be used to show an image of the processing
     * going on
     * 
     * @return A two dimensional boolean array representing the marks as they
     * were filled in - in the form of returnValue[row][col] : will be true if a
     * mark was found: false otherwise
     */
    public static boolean[][] getMarks(boolean[][] img, float offsetX, float offsetY, float omWidth, float omHeight, float rowHeight, int numCols, int numRows, DebugCanvas dc) {
        Point[] centerList = FinderPattern.findCenters(img);
        if (dc != null){
            for (int c=0; c<centerList.length; c++){
                dc.drawCross(centerList[c], 0xFF00FF00); //green
            }
        }
        Point[][] centers = sortCenters(centerList, img.length, img[0].length);
        Line[] bounds = getBoundaryLines(centers);
        if (dc != null){
            dc.drawLines(bounds, 0xFF0000FF); //blue
        }
        return getMarks(img, bounds, offsetX, offsetY, omWidth, omHeight, rowHeight, numCols, numRows, dc);
    }
    
    /**
     * Find centers on the image efficiently by examining only those parts of the 
     * image that are expected to contain a corner finder pattern.  
     * 
     * @param img QRCodeImage implementation used to get RGB values from a specific x/y coordinate
     * @param expectedCenters x,y coordinates on the image where the center should be if it's in position
     * @param searchSize The width/height of the area to search around the expectedCenter
     *   e.g. search will start at expectedCorner[i].x - (searchSize/2) for a width of searchSize, same for y axis
     * @param foundCenters Array of points that will be filled with points found from the centers
     */
    public static boolean getCenters(QRCodeImage img, Point[] expectedCenters, int searchSize, Point[] foundCenters) {
        int imgArr[][];
        boolean imgBool[][];
        Point[] tmpCenters;
        
        int x;
        int y;
        for(int i = 0; i < expectedCenters.length ;i++) {
            x = expectedCenters[i].getX() - (searchSize/2);
            y = expectedCenters[i].getY() - (searchSize/2);
            imgArr = QRCodeDecoder.imageToIntArray(img, x, y, searchSize, searchSize);
            imgBool = QRCodeImageReader.filterImage(imgArr);
            tmpCenters = FinderPattern.findCenters(imgBool);
            if(tmpCenters == null || tmpCenters.length == 0) {
                return false;
            }else {
                foundCenters[i] = tmpCenters[0];
            }
        }
        
        return true;
    }
    
    
    /**
     * Convert the given QRCodeImage to a bitmap array
     * 
     * @param img
     * @return 
     */
    public static boolean[][] convertImgToBitmap(QRCodeImage img) {
        int[][] imgArr = QRCodeDecoder.imageToIntArray(img);
        boolean[][] bitMap = QRCodeImageReader.filterImage(imgArr);
        //int[][] imgArr = imageToIntArray(img);
        //boolean[][] bitMap = filterImage(imgArr);

        return bitMap;
    }

    /**
     * Figure out which point is which and return an ordered array
     * 
     * @param centers all finder pattern centers which were found
     * @param width 
     * @param height
     * 
     * @return 
     */
    static Point[][] sortCenters(Point[] centers, int width, int height) {
        Point[][] retVal = new Point[2][2];
        
        for(int i = 0; i < centers.length; i++) {
            if(centers[i].getX() < width/2) {
                if(centers[i].getY() < height / 2) {
                    retVal[0][0] = centers[i];
                }else {
                    retVal[0][1] = centers[i];
                }
            }else {
                if(centers[i].getY() < height / 2) {
                    retVal[1][0] = centers[i];
                }else {
                    retVal[1][1] = centers[i];
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Determines where we expect the centers to be.  We will have a zone width
     * and height (e.g. A4 paper) and an image width and height (e.g. the image
     * from the camera).
     * 
     * Within the image we will have an expected margin - if the zone is relatively
     * wide this is determined by how it fits into the x plane (e.g. a wide screen
     * phone in portrait mode); 
     * 
     * @param zoneWidth Width of the zone we are looking for finder patterns (units are irrelevant - only used to calculate the aspect ratio)
     * @param zoneHeight Height of the zone we are looking for finder patterns (units are irrelevant - only used to calculate the aspect ratio)
     * @param imgWidth Width of the image in which we are expecting the zone
     * @param imgHeight Height of the image in which we are expecting the zone
     * @param margin Float representing the percentage margin to apply either to width/height: whichever is the tighter fit.
     * 
     * @return int array of x, y, width, height where centers should be within this image if it's in the middle
     */
    public static int[] getExpectedPageArea(int zoneWidth, int zoneHeight, int imgWidth, int imgHeight, float margin) {
        float zoneRatio = (float)zoneWidth/(float)zoneHeight;
        float imgRatio = (float)imgWidth/(float)imgHeight;
                
        int marginX;
        int marginY;
        float scale;
        if(zoneRatio > imgRatio) {
            //the recognition zone is relatively wider than the image it's to be found within
            marginX = Math.round(margin * imgWidth);
            scale = (float)(imgWidth - (marginX*2))/(float)zoneWidth;
            marginY = Math.round((imgHeight -  (zoneHeight * scale))/2);
        }else {
            marginY = Math.round(margin * imgHeight);
            scale = (float)(imgHeight - (marginY*2))/(float)zoneHeight;
            marginX = Math.round((imgWidth-  (zoneWidth * scale))/2);
        }
        
        return new int[]{marginX, marginY, imgWidth-(marginX*2), imgHeight-(marginY*2)};
    }
    

}
