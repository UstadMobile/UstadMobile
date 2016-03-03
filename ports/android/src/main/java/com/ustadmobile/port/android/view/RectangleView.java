package com.ustadmobile.port.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.graphics.Paint.Style;
import android.view.View;

/**
 * Created by varuna on 22/02/16.
 */
public class RectangleView extends View{

    Paint paint;

    int parentWidth;
    int parentHeight;

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
        // this is the canvas where you will draw all your stuff

        paint.setStyle(Style.STROKE); // set style to paint
        paint.setColor(0xFFE80926); // set color - its red.
        paint.setStrokeWidth(10); // set stroke width

        float offset = 50;
        float lineLength = 150;

        float maxX;
        float maxY;
        float minX;
        float minY;

        if (parentHeight >= parentWidth) {

            maxX = parentWidth - offset;
            maxY = (1.414f * maxX); //Keep it A4
            minX = offset;
            minY = (1.414f * minX); //Keep it A4
        }else{
            maxY = parentHeight - offset;
            maxX = (maxY / 1.414f); //Keep it A4;
            minY = offset;
            minX = (minY / 1.414f); //Keep it A4;
        }

        //top left
        canvas.drawLine((minX),(minY),(minX),
                (minY) + lineLength,paint);
        canvas.drawLine((minX),(minY),
                (minX) + lineLength,(minY),paint);

        //bottom left
        canvas.drawLine((minX ),(minY + maxY),(minX ),(minY + maxY) - lineLength,paint);
        canvas.drawLine((minX ),(minY + maxY),(minX ) + lineLength,(minY + maxY) ,paint);

        //top right
        canvas.drawLine((maxX),(minY),(maxX) - lineLength,(minY),paint);
        canvas.drawLine((maxX),(minY),(maxX),(minY) + lineLength,paint);

        //bottom right
        canvas.drawLine((maxX),(minY + maxY),(maxX),(minY + maxY) - lineLength,paint);
        canvas.drawLine((maxX),(minY + maxY),(maxX) - lineLength,(minY + maxY),paint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        parentHeight = MeasureSpec.getSize(heightMeasureSpec);

    }

}
