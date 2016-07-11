package com.ustadmobile.port.android.impl.qr;

import com.ustadmobile.core.omr.OMRImageSource;

/**
 * Created by mike on 7/11/16.
 */
public class NV21OMRImageSource implements OMRImageSource {

    private int nv21Width;

    private int nv21Height;

    private byte[] nv21Buffer;

    public NV21OMRImageSource(int nv21Width, int nv21Height) {
        this.nv21Width = nv21Width;
        this.nv21Height = nv21Height;
    }

    public void setNV21Buffer(byte[] nv21Buffer) {
        this.nv21Buffer = nv21Buffer;
    }


    @Override
    public void getGrayscaleImage(int[][] buf, int x, int y, int width, int height) {

    }

    @Override
    public int getWidth() {
        return nv21Width;
    }

    @Override
    public int getHeight() {
        return nv21Height;
    }

    public void decodeGreyscale(int[] buf, int cropX, int cropY, int cropWidth, int cropHeight) {
        int lineStart;
        int l;
        for(int y = 0; y < cropHeight; y++) {
            lineStart = (cropY + y) * nv21Width;
            for(int x = 0; x < cropWidth; x++) {
                l = nv21Buffer[lineStart + x + cropX] & 0xFF;
                buf[(y*cropWidth)+x] = 0xff000000 | l<<16 | l<<8 | l;
            }
        }
    }

    /**
     * Taken from
     * http://stackoverflow.com/questions/5272388/extract-black-and-white-image-from-android-cameras-nv21-format
     *
     * @return
     */
    private int[] decodeGreyscale() {
        int pixelCount = nv21Width * nv21Height;
        int[] out = new int[pixelCount];
        int l;
        for (int i = 0; i < pixelCount; ++i) {
            l = nv21Buffer[i] & 0xFF;
            out[i] = 0xff000000 | l<<16 | l<<8 | l;
        }
        return out;
    }
}
