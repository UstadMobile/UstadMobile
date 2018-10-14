package com.ustadmobile.port.web.resource;

import com.ustadmobile.lib.db.entities.Person;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("hello")
public class HelloWorldResource {

    @GET
    @Produces({"application/javascript", MediaType.APPLICATION_JSON})
    public Person sayhello(String param) {
        Person somePerson = new Person();
        somePerson.setUsername("bobjones");
        somePerson.setEmailAddr("joe@joe.com");

        return somePerson;
    }

    @GET
    @Path("/say")
    @Produces(MediaType.TEXT_PLAIN)
    public String say() {
        return "I think something else";
    }


}
