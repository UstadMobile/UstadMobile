package com.ustadmobile.port.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import com.ustadmobile.port.sharedse.omr.AttendanceSheetImage;
import com.ustadmobile.port.sharedse.omr.OMRRecognizer;

/**
 * This view is to be used as a view to show above the camera preview to guide the user to place
 * the sheet in the correct area.
 *
 * Because the aspect ratio of the preview frames from the camera often does not exactly match the
 * camera preview; Android stretches the preview.  This class should be used by calling
 * setPreviewImgSize as soon as the size of the preview coming from the camera is known.
 *
 * Created by varuna on 22/02/16.
 */
public class RectangleView extends View{

    Paint paint;

    int parentWidth;
    int parentHeight;


    private int[] pageArea;
    private int[][] fpSearchAreas;

    private int previewImgWidth = -1;

    private int previewImgHeight = -1;


    public RectangleView(Context context) {
        super(context);
        paint = new Paint();
    }

    public RectangleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        paint = new Paint();
    }

    public RectangleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if(pageArea == null)
            return;//it's not ready yet

        paint.setStyle(Style.FILL);
        paint.setAlpha(128);



        //Right side (x axis) of the page area
        int pgAreaRight = pageArea[OMRRecognizer.X] + pageArea[OMRRecognizer.WIDTH];

        //Bottom side (y axis) of the page area
        int pgAreaBottom = pageArea[OMRRecognizer.Y] + pageArea[OMRRecognizer.HEIGHT];

        paint.setColor(Color.BLACK);
        paint.setAlpha(128);

        //top margin zone
        canvas.drawRect(0, 0, parentWidth, pageArea[OMRRecognizer.Y], paint);

        //left side
        canvas.drawRect(0, pageArea[OMRRecognizer.Y],
                pageArea[OMRRecognizer.X], pgAreaBottom, paint);

        //right side
        canvas.drawRect(pgAreaRight, pageArea[OMRRecognizer.Y],
                parentWidth, pgAreaBottom, paint);

        //bottom margin zone
        canvas.drawRect(0, pgAreaBottom, parentWidth, parentHeight, paint);



        paint.setStyle(Style.STROKE);
        paint.setAlpha(255);
        paint.setStrokeWidth(4);
        canvas.drawRect(pageArea[OMRRecognizer.X], pageArea[OMRRecognizer.Y],
                pgAreaRight, pgAreaBottom, paint);

        //paint.setColor(Color.BLUE);
        paint.setStrokeWidth(2);
        int boxTop, boxLeft, boxBottom, boxRight;
        for(int i = 0; i < fpSearchAreas.length; i++) {
            boxLeft = Math.max(fpSearchAreas[i][OMRRecognizer.X], pageArea[OMRRecognizer.X]);
            boxTop = Math.max(fpSearchAreas[i][OMRRecognizer.Y], pageArea[OMRRecognizer.Y]);
            boxRight = Math.min(fpSearchAreas[i][OMRRecognizer.X] + fpSearchAreas[i][OMRRecognizer.WIDTH],
                    pgAreaRight);
            boxBottom = Math.min(fpSearchAreas[i][OMRRecognizer.Y] + fpSearchAreas[i][OMRRecognizer.HEIGHT],
                    pgAreaBottom);

            canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, paint);
        }

    }

    public int[] getPageArea() {
        return pageArea;
    }

    /**
     * Set the size of the preview image that is coming from the camera
     *
     * @param width
     * @param height
     */
    public void setPreviewImgSize(int width, int height) {
        this.previewImgWidth = width;
        this.previewImgHeight = height;
        calcAreas();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        calcAreas();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void calcAreas() {
        if(previewImgWidth != -1) {
            pageArea = OMRRecognizer.getExpectedPageArea(210, 297,
                    previewImgWidth, previewImgHeight, 0.1f);

            fpSearchAreas = OMRRecognizer.getFinderPatternSearchAreas(
                    pageArea, AttendanceSheetImage.DEFAULT_PAGE_DISTANCES,
                    AttendanceSheetImage.DEFAULT_FINDER_PATTERN_SIZE* 3f);

            float scaleX = (float)parentWidth/(float)previewImgWidth;
            float scaleY = (float)parentHeight/(float)previewImgHeight;
            pageArea = scaleRect(pageArea, scaleX, scaleY);
            for(int i = 0; i < fpSearchAreas.length; i++) {
                fpSearchAreas[i] = scaleRect(fpSearchAreas[i], scaleX, scaleY);
            }
        }
    }


    private int[] scaleRect(int[] rect, float scaleX, float scaleY) {
        int[] newRect = new int[4];
        newRect[OMRRecognizer.X] = Math.round(rect[OMRRecognizer.X] * scaleX);
        newRect[OMRRecognizer.Y] = Math.round(rect[OMRRecognizer.Y] * scaleY);
        newRect[OMRRecognizer.WIDTH] = Math.round(rect[OMRRecognizer.WIDTH] * scaleX);
        newRect[OMRRecognizer.HEIGHT] = Math.round(rect[OMRRecognizer.HEIGHT] * scaleY);
        return newRect;
    }


}
