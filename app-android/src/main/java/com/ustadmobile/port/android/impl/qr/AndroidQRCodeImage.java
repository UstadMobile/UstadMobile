package com.ustadmobile.port.android.impl.qr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

import jp.sourceforge.qrcode.data.QRCodeImage;

/**
 * Implementation of QRCodeImage for Android backed by bitmap
 *
 * Created by mike on 10/25/15.
 */
public class AndroidQRCodeImage implements QRCodeImage{

    private Bitmap bitmap;

    public AndroidQRCodeImage(InputStream in) {
        bitmap = BitmapFactory.decodeStream(in);
    }

    public AndroidQRCodeImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public int getWidth() {
        return bitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return bitmap.getHeight();
    }

    @Override
    public int getPixel(int x, int y) {
        int val = bitmap.getPixel(x, y);
        //String hexVal = Integer.toHexString(val);
        //int black = Color.rgb(0,0, 0);
        //int ablack = Color.argb(255, 0, 0, 0);
        return val;
    }
}
