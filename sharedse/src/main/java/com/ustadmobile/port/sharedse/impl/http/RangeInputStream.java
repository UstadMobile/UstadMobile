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

package com.ustadmobile.port.sharedse.impl.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple InputStream filter to support range requests.  It bounds the source input stream to serve
 * from a given start byte up until a given end byte.
 *
 * Created by mike on 12/18/15.
 */
public class RangeInputStream extends FilterInputStream {

    private long pos;

    private boolean markSupported;

    private long resetPos;

    private long resetInvalidate;

    private long end;

    /**
     * Parse a range header, and determine the bytes that should be sent to answer a request.
     *
     * @param header Value of the "range" header (if any). This can be null if there is no such header.
     *
     * @param totalLength The total length of the response in it's entirety (e.g. the file size).
     *                    This method will use it to determine the end position.
     *
     * @return a two value long array of the first byte (inclusive) and last byte (TODO: check this)
     */
    public static long[] parseRangeRequest(String header, long totalLength) {
        long[] range = null;
        if(header != null  && header.startsWith("bytes=")) {
            range = new long[] {0, -1};
            header = header.substring("bytes=".length());
            int dashPos = header.indexOf('-');
            try {
                if(dashPos > 0) {
                    range[0] = Integer.parseInt(header.substring(0,dashPos));
                }

                if(dashPos == header.length()-1) {
                    range[1] = totalLength-1;
                }else if(dashPos > 0) {
                    range[1] = Integer.parseInt(header.substring(dashPos+1));

                }
            }catch(NumberFormatException nfe) {

            }
            if(range[0] < 0 || range[1] > totalLength) {
                range[0] = -1;//Error flag
            }
        }

        return range;
    }


    /**
     * Create a RangeInputStream that bounds the given source input streamL this is useful for
     * fulfilling range http requests from any abstract InputStream.
     *
     * @param in Source InputStream
     * @param start The first byte to serve: The stream will skip this many bytes from the in stream
     * @param end The end of the range to serve up to.  After this even if the source stream has more
     *            bytes this stream will return -1 to signal the end of the stream
     * @throws IOException
     */
    public RangeInputStream(InputStream in, long start, long end) throws IOException {
        super(in);
        markSupported = in.markSupported();
        resetPos = -1;
        resetInvalidate = -1;

        this.end = end;
        pos = 0;

        //skip can skip up to the requested number of bytes to skip
        long startBytesSkipped = 0;
        while(startBytesSkipped < start){
            startBytesSkipped += skip(start - startBytesSkipped);
        }
    }

    @Override
    public int read() throws IOException {
        if(pos <= end) {
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
        byteCount = (int)Math.min((end +1)- pos, byteCount);
        if(byteCount > 0) {
            int bytesRead = in.read(buffer, byteOffset, byteCount);
            pos += bytesRead;
            return bytesRead;
        }else {
            return -1;
        }
    }

    @Override
    /**
     * ===WORKAROUND===
     *
     * NanoHTTPD is using InputStream.available incorrectly: as if it provides the
     * pending number of bytes in the stream.  As per the Java documentation it in
     * fact is only supposed to reply with how many bytes it can deliver before
     * blocking
     */
    public int available() throws IOException {
//        return (int)((end +1)- pos);
        return super.available();
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
