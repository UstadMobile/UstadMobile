package com.ustadmobile.lib.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

public class InputStreamStreamingResponse implements StreamingOutput {

    private InputStream in;

    private int bufSize = 1024;

    public InputStreamStreamingResponse(InputStream in) {
        this.in = in;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        byte[] buf = new byte[1024];

        try {
            int bytesRead;
            while((bytesRead = in.read(buf)) != -1) {
                output.write(buf, 0, bytesRead);
            }
            output.flush();
        }catch(IOException e) {
            in.close();
            throw e;
        }
    }
}
