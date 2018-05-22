package com.ustadmobile.port.android.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import jp.sourceforge.qrcode.geom.Line;
import jp.sourceforge.qrcode.geom.Point;
import jp.sourceforge.qrcode.util.DebugCanvas;

/**
 * Created by varuna on 27/02/16.
 */
public class AndroidDebugCanvas extends Canvas implements DebugCanvas {

    Bitmap image;
    Bitmap mutableBitmap;
    Paint paint;
    Canvas canvas;
    static final int strokeWidth = 1;

    public AndroidDebugCanvas(Bitmap bitmap) {
        //super(bitmap);
        this.image = bitmap;
        this.mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        this.canvas = new Canvas(mutableBitmap);
        paint = new Paint();
    }

    public  void println(String string){
        System.out.println(string);
    }

    public  void drawMatrix(boolean[][] matrix) {

    }

    public void drawLine(Line line, int color){

        paint.setStyle(Paint.Style.STROKE); // set style to paint
        paint.setColor(color); // set color
        paint.setStrokeWidth(strokeWidth); // set stroke width


        canvas.drawLine(line.getP1().getX(), line.getP1().getY(),
                line.getP2().getX(), line.getP2().getY(), paint);

    }

    public  void drawLines(Line[] lines, int color){

        paint.setStyle(Paint.Style.STROKE); // set style to paint

        paint.setColor(color); // set color
        paint.setStrokeWidth(strokeWidth); // set stroke width

        for (int i = 0; i < lines.length; i++) {
            canvas.drawLine(lines[i].getP1().getX(), lines[i].getP1().getY(),
                    lines[i].getP2().getX(), lines[i].getP2().getY(), paint);
        }

    }

    public  void drawPolygon(Point[] points, int color) {
        Path p = new Path();
        p.moveTo(points[0].getX(), points[0].getY());
        for(int i = 1; i < points.length; i++) {
            p.lineTo(points[i].getX(), points[i].getY());
        }
        p.lineTo(points[0].getX(), points[0].getY());
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        canvas.drawPath(p, paint);
    }

    @Override
    public void drawCircle(Point point, int radius, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        canvas.drawCircle(point.getX(), point.getY(), radius, paint);
    }

    public  void drawPoints(Point[] points, int color){

    }

    public  void drawPoint(Point point, int color){

    }

    public  void drawCross(Point point, int color){
        int x = point.getX();
        int y = point.getY();

        Line[] lines = {
                new Line(x - 5, y, x + 5, y),new Line(x, y - 5, x ,y + 5)
        };
        drawLines(lines, color);
    }

    public Bitmap getMutableBitmap() {
        return mutableBitmap;
    }

    public Bitmap getImage() {
        return image;
    }
    public void setImage(Bitmap image) {
        this.image = image;
    }

    /**
     * Method to convert a 2D RGB image int array into a 1D image int array.
     * @param image
     * @return
     */
    static int[] rgbTo1DArray(int[][] image){
        int imageWidth = image.length;
        int imageHeight = image[0].length;
        int [] rgbValues = new int[imageWidth*imageHeight];

        for(int i=0; i < imageWidth; i++)
        {
            for(int j=0; j < imageHeight; j++)
            {
                rgbValues[(j * imageWidth) + i] = image[i][j];
            }
        }
        return rgbValues;

    }

    /**
     * Method to convert an RGB 2D int array into a Bitmap image.
     *
     * @param image
     * @return
     */
    public static Bitmap intArrayToImage(int[][] image){
        int [] rgb1D = rgbTo1DArray(image);
        Bitmap postImage = Bitmap.createBitmap(rgb1D, image.length, image[0].length, Bitmap.Config.ARGB_8888);
        return postImage;
    }


    /**
     * Method to convert a boolean (B/W) 2D array into a Bitmap image.
     * @param image
     * @return
     */
    static Bitmap booleanArrayToBitmap(boolean[][] image) {
        int width = image.length;
        int height = image[0].length;
        int[] rgb1D = new int[width*height];
        int lineStart;
        for(int y = 0; y < height; y++) {
            lineStart = y*width;
            for(int x = 0; x < width; x++) {
                rgb1D[lineStart+x] = image[x][y] ? Color.BLACK : Color.WHITE;
            }
        }

        return Bitmap.createBitmap(rgb1D, width, height, Bitmap.Config.ARGB_8888);

        /*
        Bitmap imageFromBool = Bitmap.createBitmap(image.length, image[0].length, Bitmap.Config.ARGB_8888);

        for(int i=0;i<image.length;i++){
            for(int j=0;j<image[0].length;j++){
                if(image[i][j]) imageFromBool.setPixel(i, j, Color.BLACK);
                if(!image[i][j]) imageFromBool.setPixel(i, j, Color.WHITE);
            }
        }
        return imageFromBool;
        */
    }
}

