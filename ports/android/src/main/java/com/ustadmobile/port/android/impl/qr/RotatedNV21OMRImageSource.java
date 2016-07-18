package com.ustadmobile.port.android.impl.qr;

import com.ustadmobile.core.model.AttendanceSheetImage;
import com.ustadmobile.core.omr.OMRImageSource;

/**
 * The camera preview image is in fact rotated 90 degrees.  When asked for image data this class
 * will transparently handle that for the given area without having to rotate the rest of the image
 * itself.
 *
 * Created by mike on 7/11/16.
 */
public class RotatedNV21OMRImageSource extends NV21OMRImageSource{

    public RotatedNV21OMRImageSource(int nv21Width, int nv21Height) {
        super(nv21Width, nv21Height);
    }

    @Override
    public int getHeight() {
        return nv21Width;
    }

    @Override
    public int getWidth() {
        return nv21Height;
    }

    @Override
    public void decodeGrayscale(int[] buf, int cropX, int cropY, int cropWidth, int cropHeight) {
        int yStart, l, x;
        for(int y = 0; y < cropHeight; y++) {
            yStart = nv21Height - cropX;
            for(x = 0; x < cropWidth; x++) {
                /*
                 source X coordinate = cropY + y (swap x and y axis)
                 source Y coordinate = nv21Height - (cropX +x) - swap axis and invert
                 source position in stream = (yPos * nv21Width) + source x coordinate
                 */
                l = nv21Buffer[((yStart -x)*nv21Width)+(cropY+y)] & 0xFF;
                buf[(y*cropWidth) + x] = 0xff000000 | l<<16 | l<<8 | l;
            }
        }
    }

    @Override
    public void getGrayscaleImage(int[][] buf, int cropX, int cropY, int cropWidth, int cropHeight, short[] minMaxBuf) {
        int yStart, l, x;
        short min = -1, max = -1;
        for(int y = 0; y < cropHeight; y++) {
            yStart = nv21Height - cropX;
            for (x = 0; x < cropWidth; x++) {
                /*
                 source X coordinate = cropY + y (swap x and y axis)
                 source Y coordinate = nv21Height - (cropX +x) - swap axis and invert
                 source position in stream = (yPos * nv21Width) + source x coordinate
                 */
                l = nv21Buffer[((yStart - x) * nv21Width) + (cropY + y)] & 0xFF;
                buf[x][y] = 0xff000000 | l<<16 | l<<8 | l;
                if(l < min || min == -1) {
                    min = (short)l;
                }else if(l > max || max == -1) {
                    max = (short)l;
                }
            }
        }

        if(minMaxBuf != null) {
            minMaxBuf[OMRImageSource.MINMAX_BUF_MIN] = min;
            minMaxBuf[OMRImageSource.MINMAX_BUF_MAX] = max;
        }
    }

    @Override
    public OMRImageSource copy() {
        OMRImageSource copy = new RotatedNV21OMRImageSource(nv21Width, nv21Height);
        copy.setBuffer(nv21Buffer);
        return copy;
    }

}
