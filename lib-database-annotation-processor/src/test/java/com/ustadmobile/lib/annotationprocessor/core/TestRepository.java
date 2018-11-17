package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableDao;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableEntity;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class TestRepository {

    private HttpServer server;

    private WebTarget target;

    public static final String TEST_URI = "http://localhost:8089/api/";

    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages("com.ustadmobile.lib.annotationprocessor.core.db");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
    }

    @Before
    public void setUp() {
        server = startServer();
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void givenEntryInsertedInClientDb_whenSynced_shouldBeInServerDb() {
        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
        serverDb.setMaster(true);

        clientDb.clearAll();
        serverDb.clearAll();

        ExampleSyncableEntity entity = new ExampleSyncableEntity();
        String entityTitle = "Syncable " + System.currentTimeMillis();
        entity.setTitle(entityTitle);
        ExampleSyncableDao repo = clientDb.getRepository(TEST_URI, "").getExampleSyncableDao();

        long uid = repo.insert(entity);
        clientDb.getExampleSyncableDao().syncWith(repo, 0);

        ExampleSyncableEntity entityOnServer = serverDb.getExampleSyncableDao().findByUid(uid);
        Assert.assertNotNull("Entity present in server db", entityOnServer);
        Assert.assertEquals("Entity titles match", entity.getTitle(),
                entityOnServer.getTitle());
    }

    @Test
    public void givenEntryUpdatedOnClientDb_whenSynced_shouldBeUpdatedInServerDb() {
        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
        serverDb.setMaster(true);

        clientDb.clearAll();
        serverDb.clearAll();

        ExampleSyncableEntity entity = new ExampleSyncableEntity();
        String entityTitle = "Syncable " + System.currentTimeMillis();
        entity.setTitle(entityTitle);
        ExampleSyncableDao repo = clientDb.getRepository(TEST_URI, "").getExampleSyncableDao();
        long uid = repo.insert(entity);
        entity.setExampleSyncableUid(uid);
        clientDb.getExampleSyncableDao().syncWith(repo, 0);
        String entityTitleUpdated = "Syncable " + System.currentTimeMillis() + " updated";
        entity.setTitle(entityTitleUpdated);


        repo.updateEntity(entity);
        clientDb.getExampleSyncableDao().syncWith(repo,0);


        Assert.assertEquals("Entity title was updated on server",
                entityTitleUpdated,
                serverDb.getExampleSyncableDao().getTitleByUid(uid));
    }

    @Test
    public void givenEntityCreatedOnServerDb_whenSynced_shouldBeInClientDb() {
        Client c = ClientBuilder.newClient();
        target = c.target(TEST_URI);

        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
        serverDb.setMaster(true);

        clientDb.clearAll();
        serverDb.clearAll();

        ExampleSyncableEntity insertedEntity = new ExampleSyncableEntity();
        String entityTitle = "Server Created " + System.currentTimeMillis();
        insertedEntity.setTitle(entityTitle);

        long insertUid = target.path("ExampleSyncableDao/insertRest")
                .request().post(Entity.entity(insertedEntity, MediaType.APPLICATION_JSON),
                        Long.class);


        clientDb.getExampleSyncableDao().syncWith(
                clientDb.getRepository(TEST_URI, "").getExampleSyncableDao(), 0L);


        Assert.assertNotNull("Entity created on server is present on client",
                clientDb.getExampleSyncableDao().findByUid(insertUid));
        Assert.assertEquals("Entity title on client equals what was created on server",
                entityTitle, clientDb.getExampleSyncableDao().getTitleByUid(insertUid));
    }

    @Test
    public void givenEntityUpdatedOnServer_whenSynced_thenShouldBeUpdatedOnClient() {
        Client c = ClientBuilder.newClient();
        target = c.target(TEST_URI);

        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
        serverDb.setMaster(true);

        clientDb.clearAll();
        serverDb.clearAll();

        ExampleSyncableEntity insertedEntity = new ExampleSyncableEntity();
        String entityTitle = "Server Created " + System.currentTimeMillis();
        insertedEntity.setTitle(entityTitle);

        long insertUid = target.path("ExampleSyncableDao/insertRest")
                .request().post(Entity.entity(insertedEntity, MediaType.APPLICATION_JSON),
                        Long.class);
        insertedEntity.setExampleSyncableUid(insertUid);
        clientDb.getExampleSyncableDao().syncWith(
                clientDb.getRepository(TEST_URI, "").getExampleSyncableDao(), 0L);

        String updatedTitle = "Server Created " + System.currentTimeMillis() + " updated";
        insertedEntity.setTitle(updatedTitle);
        target.path("ExampleSyncableDao/updateEntity")
                .request().post(Entity.entity(insertedEntity, MediaType.APPLICATION_JSON));


        clientDb.getExampleSyncableDao().syncWith(
                clientDb.getRepository(TEST_URI, "").getExampleSyncableDao(), 0L);


        Assert.assertEquals("Title updated on client database after update on server", updatedTitle,
                clientDb.getExampleSyncableDao().getTitleByUid(insertUid));
    }




}
