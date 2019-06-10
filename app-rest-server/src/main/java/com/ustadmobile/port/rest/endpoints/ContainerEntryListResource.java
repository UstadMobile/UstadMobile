package com.ustadmobile.port.rest.endpoints;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;


@Path("ContainerEntryList")
public class ContainerEntryListResource {

    @Path("findByContainerWithMd5")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ContainerEntryWithMd5> findByContainerWithMd5(@QueryParam("containerUid") long containerUid) {
        List<ContainerEntryWithMd5> entryList = UmAppDatabase.getInstance(null)
                .getContainerEntryDao().findByContainerWithMd5(containerUid);

        if(entryList.isEmpty())
            throw new WebApplicationException("No entries found: container does not exist or has no entries",
                    404);


        return entryList;
    }

}
