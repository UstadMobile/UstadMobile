package com.ustadmobile.port.web.resource;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.lib.db.entities.Person;

import org.glassfish.jersey.media.multipart.FormDataParam;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("DaoName")
public class HelloWorldResource {

    @GET
    @Path("/queryMethod")
    @Produces({"application/javascript", MediaType.APPLICATION_JSON})
    public Person sayhello(@QueryParam("param") String param) {
        Person somePerson = new Person();
        somePerson.setUsername(param);
        somePerson.setEmailAddr("joe@joe.com");

        return somePerson;
    }

    //This works but it isn't how things are supposed to be according to JAX-RS spec, might be a problem for restygwt
    @POST
    @Path(("/insertMethod"))
    @Produces(MediaType.TEXT_PLAIN)
    public String sayMyName(@FormDataParam("person") Person person) {
        return person.toString() +" blah";
    }

    @POST
    @Path("/insertTest")
    @Produces(MediaType.TEXT_PLAIN)
    public String sayOtherName(@QueryParam("idList") List<Integer> idList) {
        return "Hello all " + idList.size() + " of you";
    }


    @GET
    @Path("/say")
    @Produces(MediaType.TEXT_PLAIN)
    public String say() {
        return "I think something else";
    }


}
