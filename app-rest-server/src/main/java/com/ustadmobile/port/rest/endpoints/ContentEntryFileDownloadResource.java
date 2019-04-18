package com.ustadmobile.port.rest.endpoints;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithStatus;

import java.io.File;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@Path("ContentEntryFile")
public class ContentEntryFileDownloadResource {



    @Path("{contentEntryFileUid}")
    @GET
    public Response getFile(@Context HttpHeaders headers,
                            @PathParam("contentEntryFileUid") long contentEntryFileUid) {
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        ContentEntryFileWithStatus file = db.getContentEntryFileDao().findByUidWithStatus(
                contentEntryFileUid);

        if(file == null || file.getEntryStatus() == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        Response.ResponseBuilder responseBuilder;
        List<String> rangeRequestHeaders = headers.getRequestHeader("range");
        if(rangeRequestHeaders != null && !rangeRequestHeaders.isEmpty()) {
            //TODO: change this to actually handle the range request
            responseBuilder = Response.ok();
        }else {
            responseBuilder = Response.ok();
        }

        responseBuilder.header("Content-Length", file.getFileSize());
        responseBuilder.header("Content-Type", file.getMimeType());
        responseBuilder.entity(new File(file.getEntryStatus().getFilePath()));

        return responseBuilder.build();
    }


}
