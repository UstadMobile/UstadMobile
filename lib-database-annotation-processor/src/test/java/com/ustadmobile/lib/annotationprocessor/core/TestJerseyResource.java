package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableEntity;
import com.ustadmobile.lib.db.sync.SyncResponse;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

public class TestJerseyResource {

    private HttpServer server;
    private WebTarget target;

    public static final String TEST_URI = "http://localhost:8087/api";

    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages("com.ustadmobile.lib.annotationprocessor.core.db");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
    }


    @Before
    public void setUp() {
        server = startServer();
        Client c = ClientBuilder.newClient();
        target = c.target(TEST_URI);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testIt(){
        ArrayList<ExampleSyncableEntity> localChangeList = new ArrayList<>();

        SyncResponse<ExampleSyncableEntity> response = target
                .path("ExampleSyncableDao/handlingIncomingSync").request()
                .post(Entity.entity(localChangeList, MediaType.APPLICATION_JSON),
                        new GenericType<SyncResponse<ExampleSyncableEntity>>() {});

        Assert.assertNotNull(response);
    }

    @Test
    public void testPrimitiveReturnType() {
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.clearAll();

        ExampleSyncableEntity e1 = new ExampleSyncableEntity();
        e1.setLocalChangeSeqNum(42);
        long insertedUid = db.getExampleSyncableDao().insert(e1);


        String result = target.path("ExampleSyncableDao/getLocalChangeByUid")
                .queryParam("uid", String.valueOf(insertedUid))
                .request().get(String.class);
        Assert.assertEquals("Expected result for getLocalChangeByUid returned", "42", result);
    }

    @Test
    public void testStringReturnType() {
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.clearAll();

        ExampleSyncableEntity e2 = new ExampleSyncableEntity();
        e2.setTitle("Hello World");
        long insertedUid = db.getExampleSyncableDao().insert(e2);

        String result = target.path("ExampleSyncableDao/getTitleByUid")
                .queryParam("uid", String.valueOf(insertedUid))
                .request().get(String.class);

        Assert.assertEquals("Expected result for getTitleByUid returned", "Hello World",
                result);

    }

    @Test
    public void testAsyncReturn() {
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.clearAll();

        ExampleSyncableEntity e2 = new ExampleSyncableEntity();
        e2.setTitle("Hello Async World");
        long insertedUid = db.getExampleSyncableDao().insert(e2);

        String result = target.path("ExampleSyncableDao/getTitleByUidAsync")
                .queryParam("uid", String.valueOf(insertedUid))
                .request().get(String.class);

        Assert.assertEquals("Expected result for getTitleByUid returned", "Hello Async World",
                result);
    }

}
