package com.ustadmobile.port.android.impl.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple InputStream filter to support range requests.
 *
 * Created by mike on 12/18/15.
 */
public class RangeInputStream extends FilterInputStream {

    private int pos;

    private boolean markSupported;

    int resetPos;

    int resetInvalidate;

    int start;

    int end;

    long startBytesSkipped;


    public RangeInputStream(InputStream in, int start, int end) throws IOException {
        super(in);
        markSupported = in.markSupported();
        resetPos = -1;
        resetInvalidate = -1;

        this.start = start;
        this.end = end;
        pos = 0;

        //skip can skip up to the requested number of bytes to skip
        startBytesSkipped = 0;
        while(startBytesSkipped < start){
            startBytesSkipped += skip(start - startBytesSkipped);
        }
    }

    @Override
    public int read() throws IOException {
        if(pos < end) {
            pos++;
            return in.read();
        }else {
            return -1;
        }
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }



    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        byteCount = Math.min(end - pos, byteCount);
        if(byteCount > 0) {
            int bytesRead = in.read(buffer, byteOffset, byteCount);
            pos += bytesRead;
            return bytesRead;
        }else {
            return -1;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        if(resetPos != -1 && pos < resetInvalidate) {
            in.reset();
            pos = resetPos;
        }
    }

    @Override
    public long skip(long byteCount) throws IOException {
        int skipped = (int)in.skip(byteCount);
        pos += skipped;
        return skipped;
    }

    @Override
    public synchronized void mark(int readlimit) {
        if(markSupported) {
            resetPos = pos;
            resetInvalidate = pos + readlimit;
        }

        super.mark(readlimit);
    }
}
