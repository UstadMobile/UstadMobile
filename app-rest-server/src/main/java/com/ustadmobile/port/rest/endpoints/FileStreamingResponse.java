package com.ustadmobile.port.rest.endpoints;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

public class FileStreamingResponse implements StreamingOutput {

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {

    }
}
