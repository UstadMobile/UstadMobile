package com.ustadmobile.port.android.impl.qr;

import com.ustadmobile.core.omr.OMRImageSource;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mike on 7/11/16.
 */
public class NV21OMRImageSource implements OMRImageSource {

    protected int nv21Width;

    protected int nv21Height;

    protected byte[] nv21Buffer;

    public NV21OMRImageSource(int nv21Width, int nv21Height) {
        this.nv21Width = nv21Width;
        this.nv21Height = nv21Height;
    }

    public void setNV21Buffer(byte[] nv21Buffer) {
        this.nv21Buffer = nv21Buffer;
    }


    @Override
    public void setBuffer(byte[] buf) {
        nv21Buffer = buf;
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

    @Override
    public byte[] getBuffer() {
        return nv21Buffer;
    }

    @Override
    public OMRImageSource copy() {
        OMRImageSource copy = new NV21OMRImageSource(nv21Width, nv21Height);
        copy.setBuffer(nv21Buffer);
        return copy;
    }

    public void decodeGrayscale(int[] buf, int cropX, int cropY, int cropWidth, int cropHeight) {
        int lineStart;
        int l;
        for(int y = 0; y < cropHeight; y++) {
            lineStart = (cropY + y) * nv21Width;
            for(int x = 0; x < cropWidth; x++) {
                l = nv21Buffer[lineStart + (x + cropX)] & 0xFF;
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
    private int[] decodeGrayscale() {
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
