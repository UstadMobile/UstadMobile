/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.impl.qr;

import com.sun.lwuit.Image;
import jp.sourceforge.qrcode.data.QRCodeImage;

/**
 *
 * @author mike
 */
public class J2MEQRCodeImage implements QRCodeImage{
    
    private int[] imgRGB;
    
    private int width;
    
    private int height;
    
    public J2MEQRCodeImage(Image img){
        imgRGB = img.getRGBCached();
        width = img.getWidth();
        height = img.getHeight();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPixel(int x, int y) {
        return imgRGB[x + y*width];
    }
    
}
