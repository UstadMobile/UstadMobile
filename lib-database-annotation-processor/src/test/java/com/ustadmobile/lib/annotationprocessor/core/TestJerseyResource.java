package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableDao;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableEntity;
import com.ustadmobile.lib.db.sync.SyncResponse;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.ForbiddenException;
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
                .packages("com.ustadmobile.lib.annotationprocessor.core.db")
                .register(MultiPartFeature.class);
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
        server.shutdownNow();
    }

    @Test
    public void testIt(){
        ArrayList<ExampleSyncableEntity> localChangeList = new ArrayList<>();

        SyncResponse<ExampleSyncableEntity> response = target
                .path("ExampleSyncableDao/handleIncomingSync")
                .queryParam("userId", ExampleDatabase.VALID_AUTH_TOKEN_USER_UID).request()
                .header("X-Auth-Token", ExampleDatabase.VALID_AUTH_TOKEN)
                .post(Entity.entity(localChangeList, MediaType.APPLICATION_JSON),
                        new GenericType<SyncResponse<ExampleSyncableEntity>>() {});

        Assert.assertNotNull(response);
    }

    @Test
    public void testPrimitiveReturnType() {
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.clearAll();
        db.setMaster(true);

        ExampleSyncableEntity e1 = new ExampleSyncableEntity();
        e1.setLocalChangeSeqNum(42);


        long insertedUid = target.path("ExampleSyncableDao/insertRest").request()
                .post(Entity.entity(e1, MediaType.APPLICATION_JSON),
                        new GenericType<Long>() {});


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

    @Test
    public void testLiveDataReturn() {
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.clearAll();

        ExampleSyncableEntity e2 = new ExampleSyncableEntity();
        e2.setTitle("LiveData Return");
        long insertedUid = db.getExampleSyncableDao().insert(e2);

        String result = target.path("ExampleSyncableDao/findTitleLive")
                .queryParam("uid", String.valueOf(insertedUid))
                .request().get(String.class);
        Assert.assertEquals("Expected result for getTitleByUidLive returned", "LiveData Return",
                result);
    }

    @Test
    public void testLiveDataList() {
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.clearAll();
        ExampleSyncableEntity e1 = new ExampleSyncableEntity();
        e1.setTitle("e1");
        ExampleSyncableEntity e2 = new ExampleSyncableEntity();
        e2.setTitle("e2");

        target.path("ExampleSyncableDao/insertRestListAndReturnIds").request()
                .post(Entity.entity(Arrays.asList(e1, e2), MediaType.APPLICATION_JSON),
                        new GenericType<List<Long>>() {});


        List<ExampleSyncableEntity> resultList = target.path("ExampleSyncableDao/findAllLive")
                .request().get(new GenericType<List<ExampleSyncableEntity>>(){});
        Assert.assertEquals("LiveData list has two entities which were inserted", 2,
                resultList.size());
    }

    @Test
    public void testListInsert() {
        ExampleDatabase.getInstance(null).clearAll();

        ExampleSyncableEntity e1 = new ExampleSyncableEntity();
        e1.setTitle("Entity 1");
        ExampleSyncableEntity e2 = new ExampleSyncableEntity();
        e2.setTitle("Entity 2");

        List<Long> response = target
                .path("ExampleSyncableDao/insertRestListAndReturnIds").request()
                .post(Entity.entity(Arrays.asList(e1, e2), MediaType.APPLICATION_JSON),
                        new GenericType<List<Long>>() {});

        //Note: we would test looking up other entities, but the underlying JDBC implementation
        // of getGeneratedKeys does not always return everything when we use executeBatch
        ExampleSyncableDao dao = ExampleDatabase.getInstance(null).getExampleSyncableDao();
        Assert.assertNotNull("Entity e1 was inserted", dao.findByUid(response.get(0)));
    }


    @Test
    public void givenValidAuth_whenMethodWithAuthenticatedUserCalled_thenShouldProvideValue() {
        ExampleDatabase.getInstance(null).clearAll();
        ExampleSyncableEntity e1 = new ExampleSyncableEntity();
        e1.setTitle("Entity 1");
        Long response = target.path("ExampleSyncableDao/insertRest")
                .request().post(Entity.entity(e1, MediaType.APPLICATION_JSON),
                        new GenericType<Long>() {});

        String title = target.path("ExampleSyncableDao/getTitleByUidAuthenticated")
                .queryParam("uid", response).queryParam("personUid",
                        ExampleDatabase.VALID_AUTH_TOKEN_USER_UID)
                .request().header("X-Auth-Token", ExampleDatabase.VALID_AUTH_TOKEN)
                .get(String.class);

        Assert.assertEquals("Given valid authentication, authenticated method answers",
                "Entity 1", title);
    }

    @Test(expected = ForbiddenException.class)
    public void givenInvalidAuth_whenMethodWithAutehtnicatedUserCalled_thenShouldReturn403Status(){
        ExampleDatabase.getInstance(null).clearAll();
        ExampleSyncableEntity e1 = new ExampleSyncableEntity();
        e1.setTitle("Entity 1");
        Long response = target.path("ExampleSyncableDao/insertRest")
                .request().post(Entity.entity(e1, MediaType.APPLICATION_JSON),
                        new GenericType<Long>() {});

        target.path("ExampleSyncableDao/getTitleByUidAuthenticated")
                .queryParam("uid", response).queryParam("personUid",
                    ExampleDatabase.VALID_AUTH_TOKEN_USER_UID)
                .request().header("X-Auth-Token", "invalid")
                .get(String.class);
    }

}
