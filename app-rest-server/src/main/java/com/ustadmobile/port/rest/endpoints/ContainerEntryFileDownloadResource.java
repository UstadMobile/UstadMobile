package com.ustadmobile.port.rest.endpoints;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.ContainerEntryFile;

import java.io.File;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;


@Path("ContainerEntryFile")
public class ContainerEntryFileDownloadResource {

    @Path("{containerEntryFileUid}")
    @GET
    public Response getFile(@Context HttpHeaders headers,
                            @PathParam("containerEntryFileUid") long containerEntryFileUid) {
        UmAppDatabase db = UmAppDatabase.getInstance(null);

        ContainerEntryFile file = db.getContainerEntryFileDao().findByUid(containerEntryFileUid);
        if(file == null){
            throw new WebApplicationException("File not found", 404);
        }

        Response.ResponseBuilder responseBuilder;
        List<String> rangeRequestHeaders = headers.getRequestHeader("range");
        if(rangeRequestHeaders != null && !rangeRequestHeaders.isEmpty()) {
            //TODO: change this to actually handle the range request
            responseBuilder = Response.ok();
        }else {
            responseBuilder = Response.ok();
        }

        responseBuilder.header("Content-Length", file.getCeCompressedSize());
        responseBuilder.header("Content-Type", "application/octet");
        responseBuilder.entity(new File(file.getCefPath()));

        return responseBuilder.build();
    }
}
