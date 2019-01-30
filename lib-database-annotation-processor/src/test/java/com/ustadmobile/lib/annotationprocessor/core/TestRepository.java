package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableDao;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableEntity;
import com.ustadmobile.lib.database.jdbc.UmJdbcDatabase;
import com.ustadmobile.test.http.MockReverseProxyDispatcher;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockWebServer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestRepository {

    private HttpServer server;

    private WebTarget target;

    public static final String TEST_URI = "http://localhost:8089/api/";

    private static final int SYNC_SEND_LIMIT = 100;

    private static final int SYNC_RECEIVE_LIMIT = 100;

    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .register(MultiPartFeature.class)
                .packages("com.ustadmobile.lib.annotationprocessor.core.db");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
    }

    public static ExampleDatabase serverDbStatic;

    @Before
    public void setUp() {
        server = startServer();
    }

    @After
    public void tearDown() {
        server.shutdownNow();
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
        ExampleSyncableDao repo = clientDb.getRepository(TEST_URI,
                ExampleDatabase.VALID_AUTH_TOKEN).getExampleSyncableDao();

        long uid = repo.insert(entity);
        clientDb.getExampleSyncableDao().syncWith(repo, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                SYNC_SEND_LIMIT, SYNC_RECEIVE_LIMIT);

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
        ExampleSyncableDao repo = clientDb.getRepository(TEST_URI,
                ExampleDatabase.VALID_AUTH_TOKEN).getExampleSyncableDao();
        long uid = repo.insert(entity);
        entity.setExampleSyncableUid(uid);
        clientDb.getExampleSyncableDao().syncWith(repo, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                SYNC_SEND_LIMIT, SYNC_RECEIVE_LIMIT);
        String entityTitleUpdated = "Syncable " + System.currentTimeMillis() + " updated";
        entity.setTitle(entityTitleUpdated);


        repo.updateEntity(entity);
        clientDb.getExampleSyncableDao().syncWith(repo,ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                SYNC_SEND_LIMIT, SYNC_RECEIVE_LIMIT);


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
                clientDb.getRepository(TEST_URI, ExampleDatabase.VALID_AUTH_TOKEN)
                        .getExampleSyncableDao(), ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                SYNC_SEND_LIMIT, SYNC_RECEIVE_LIMIT);


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
                clientDb.getRepository(TEST_URI, ExampleDatabase.VALID_AUTH_TOKEN)
                        .getExampleSyncableDao(), ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                SYNC_SEND_LIMIT, SYNC_RECEIVE_LIMIT);

        String updatedTitle = "Server Created " + System.currentTimeMillis() + " updated";
        insertedEntity.setTitle(updatedTitle);
        target.path("ExampleSyncableDao/updateEntity")
                .request().post(Entity.entity(insertedEntity, MediaType.APPLICATION_JSON));


        clientDb.getExampleSyncableDao().syncWith(
                clientDb.getRepository(TEST_URI, ExampleDatabase.VALID_AUTH_TOKEN)
                        .getExampleSyncableDao(), ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                    SYNC_SEND_LIMIT, SYNC_RECEIVE_LIMIT);


        Assert.assertEquals("Title updated on client database after update on server", updatedTitle,
                clientDb.getExampleSyncableDao().getTitleByUid(insertUid));
    }

    @Test
    public void givenMoreEntitiesChangedLocallyThenSendLimit_whenSynced_thenShouldComplete() {
        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
        serverDbStatic = serverDb;

        ExampleDatabase clientRepo = clientDb.getRepository(TEST_URI,
                ExampleDatabase.VALID_AUTH_TOKEN);

        ExampleSyncableDao clientRepoDao = spy(clientRepo.getExampleSyncableDao());
        clientDb.clearAll();
        serverDb.clearAll();

        List<ExampleSyncableEntity> entityList = new ArrayList<>(2000);
        for(int i = 0; i < 1950; i++){
            entityList.add(new ExampleSyncableEntity("Entity " + i));
        }

        clientRepoDao.insertList(entityList);
        clientDb.getExampleSyncableDao().syncWith(clientRepoDao,
                ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,100, 100);

        int serverNumEntitiesAfterSync = serverDb.getExampleSyncableDao().findAll().size();
        int clientNumEntitiesAfterSync = serverDb.getExampleSyncableDao().findAll().size();


        clientDb.getExampleSyncableDao().syncWith(clientRepoDao,
                ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,100, 100);

        Assert.assertEquals(serverNumEntitiesAfterSync, clientNumEntitiesAfterSync);

        verify(clientRepoDao, times(21))
                .handleIncomingSync(any(), anyLong(), anyLong(), anyLong(), anyInt(), anyInt());


    }


    @Test
    public void givenMoreEntitiesChangedRemotelyThanReceiveLimit_whenSynced_thenShouldComplete() {
        Client c = ClientBuilder.newClient();
        target = c.target(TEST_URI);

        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
        serverDbStatic = serverDb;

        ExampleDatabase clientRepo = clientDb.getRepository(TEST_URI,
                ExampleDatabase.VALID_AUTH_TOKEN);

        ExampleSyncableDao clientRepoDao = spy(clientRepo.getExampleSyncableDao());


        clientDb.clearAll();
        serverDb.clearAll();

        List<ExampleSyncableEntity> entityList = new ArrayList<>(2000);
        for(int i = 0; i < 1950; i++){
            entityList.add(new ExampleSyncableEntity("Entity " + i));
        }

        List<Long> idsCreated = target.path("ExampleSyncableDao/insertRestListAndReturnIds").request().post(
                Entity.entity(entityList, MediaType.APPLICATION_JSON),
                new GenericType<List<Long>>() {});

        clientDb.getExampleSyncableDao().syncWith(clientRepoDao,
                ExampleDatabase.VALID_AUTH_TOKEN_USER_UID, 100, 100);

        int serverNumEntitiesAfterSync = serverDb.getExampleSyncableDao().findAll().size();
        int clientNumEntitiesAfterSync = serverDb.getExampleSyncableDao().findAll().size();

        Assert.assertEquals("All entities now in client db",
                serverNumEntitiesAfterSync, clientNumEntitiesAfterSync);

        clientDb.getExampleSyncableDao().syncWith(clientRepoDao, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                100, 100);

        //Ensure that sync'd entities don't have to go back over the connection.
        verify(clientRepoDao, times(21))
                .handleIncomingSync(any(), anyLong(), anyLong(), anyLong(), anyInt(), anyInt());
    }


    /**
     * Test of triggers: ensure that when we run a plain UPDATE query using @UmQuery that this changes
     * the change sequence numbers and lastChangedBy such that the update will be sync'd.
     */
    @Test
    public void givenEntityUpdatedOnClientWithUpdateQuery_whenSynced_thenShouldBeUpdatedOnServer() {
        Client c = ClientBuilder.newClient();
        target = c.target(TEST_URI);

        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
        serverDbStatic = serverDb;

        ExampleDatabase clientRepo = clientDb.getRepository(TEST_URI,
                ExampleDatabase.VALID_AUTH_TOKEN);

        ExampleSyncableDao clientRepoDao = spy(clientRepo.getExampleSyncableDao());

        clientDb.clearAll();
        serverDb.clearAll();

        ExampleSyncableEntity entity = new ExampleSyncableEntity();
        String entityTitleOnInsert = "Update test";
        entity.setTitle(entityTitleOnInsert);
        long insertUid = target.path("ExampleSyncableDao/insertRest")
                .request().post(Entity.entity(entity, MediaType.APPLICATION_JSON),
                        Long.class);
        clientDb.getExampleSyncableDao().syncWith(clientRepoDao, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                100, 100);
        String entityTitleInClientDbAfterSync1 = clientDb.getExampleSyncableDao()
                .getTitleByUid(insertUid);
        String newTitle = "Update test " + System.currentTimeMillis();
        clientRepo.getExampleSyncableDao().updateTitle(insertUid, newTitle);


        clientDb.getExampleSyncableDao().syncWith(clientRepoDao,
                ExampleDatabase.VALID_AUTH_TOKEN_USER_UID, 100, 100);


        Assert.assertEquals("After initial sync, entity was synced to client from server",
                entityTitleOnInsert, entityTitleInClientDbAfterSync1);
        Assert.assertEquals("After update using @UmQuery method, new title modified on client " +
                "is reflected on server after sync", newTitle,
                serverDb.getExampleSyncableDao().getTitleByUid(insertUid));
    }

    @Test
    public void givenEntityUpdatedOnServerByQuery_whenSynced_thenShouldBeUpdatedOnClient() {
        Client c = ClientBuilder.newClient();
        target = c.target(TEST_URI);

        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
        serverDbStatic = serverDb;

        ExampleDatabase clientRepo = clientDb.getRepository(TEST_URI,
                ExampleDatabase.VALID_AUTH_TOKEN);

        ExampleSyncableDao clientRepoDao = spy(clientRepo.getExampleSyncableDao());

        clientDb.clearAll();
        serverDb.clearAll();

        ExampleSyncableEntity entity = new ExampleSyncableEntity();
        String entityTitleOnInsert = "Update test";
        entity.setTitle(entityTitleOnInsert);
        long insertUid = clientRepoDao.insert(entity);
        clientDb.getExampleSyncableDao().syncWith(clientRepoDao, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                100, 100);
        String entityTitleInClientDbAfterSync1 = clientDb.getExampleSyncableDao()
                .getTitleByUid(insertUid);

        clientDb.getExampleSyncableDao().syncWith(clientRepoDao,
                ExampleDatabase.VALID_AUTH_TOKEN_USER_UID, 100, 100);

        String entityTitleUpdated = "Update test " + System.currentTimeMillis();

        target.path("ExampleSyncableDao/updateTitle").queryParam("uid", insertUid)
                .queryParam("title", entityTitleUpdated).request().get();

        String entityTitleOnServerAfterUpdate = serverDb.getExampleSyncableDao().getTitleByUid(insertUid);

        clientDb.getExampleSyncableDao().syncWith(clientRepoDao,
                ExampleDatabase.VALID_AUTH_TOKEN_USER_UID, 100, 100);

        Assert.assertEquals("After first sync, entity was syncd to client with initial title",
                entityTitleOnInsert, entityTitleInClientDbAfterSync1);
        Assert.assertEquals("After calling update REST method, title is updated on server",
                entityTitleUpdated, entityTitleOnServerAfterUpdate);
        Assert.assertEquals("After update server and sync, title has been updated on the client",
                entityTitleUpdated, clientDb.getExampleSyncableDao().getTitleByUid(insertUid));
    }





//    @Test
    public void givenLocalEntityChangedDuringSync_whenSyncedAgain_thenShouldBePresentOnServer() {
        MockWebServer proxyMock = new MockWebServer();
        MockReverseProxyDispatcher dispatcher = new MockReverseProxyDispatcher(HttpUrl.parse(TEST_URI));
        proxyMock.setDispatcher(dispatcher);
        dispatcher.setLatency(5000);


        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);

        ExampleDatabase clientRepo = clientDb.getRepository(proxyMock.url("/").toString(),
                ExampleDatabase.VALID_AUTH_TOKEN);

        ExampleSyncableDao clientDao = clientDb.getExampleSyncableDao();
        ExampleSyncableDao clientRepoDao = clientRepo.getExampleSyncableDao();
        ExampleSyncableDao serverDao = serverDb.getExampleSyncableDao();

        clientDb.clearAll();
        serverDb.clearAll();

        ExampleSyncableEntity e1 = new ExampleSyncableEntity("Entity 1");
        e1.setExampleSyncableUid(clientRepo.getExampleSyncableDao().insert(e1));

        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            clientDao.syncWith(clientRepoDao, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                    100, 100);
            latch.countDown();
        }).start();
        ExampleSyncableEntity e2 = new ExampleSyncableEntity("Entity 2");
        clientRepoDao.insert(e2);

        boolean e1OnServerAfterSync1 = serverDao.findByUid(e1.getExampleSyncableUid()) != null;
        boolean e2OnServerAfterSync1 = serverDao.findByUid(e2.getExampleSyncableUid()) != null;

        dispatcher.setLatency(0);

        clientDao.syncWith(clientRepoDao, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                100, 100);

        Assert.assertTrue("Entity e1 created before sync was sync'd to server on first sync",
                e1OnServerAfterSync1);
        Assert.assertFalse("Entity e2 created during sync was not sync'd to server on first sync",
                e2OnServerAfterSync1);

        Assert.assertNotNull("Entity e1 on server after sync2",
                serverDao.findByUid(e1.getExampleSyncableUid()));

        Assert.assertNotNull("Entity e2 on server after sync2",
                serverDao.findByUid(e2.getExampleSyncableUid()));
    }

    @Test
    public void givenEntitiesNotSynced_whenDbSyncWithCalled_thenEntitiesAreSynced(){
        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);

        ExampleDatabase clientRepo = clientDb.getRepository(TEST_URI,
                ExampleDatabase.VALID_AUTH_TOKEN);

        List<ExampleSyncableEntity> itemList = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            itemList.add(new ExampleSyncableEntity("Entity " + i));
        }

        clientRepo.getExampleSyncableDao().insertList(itemList);

        clientDb.syncWith(clientRepo, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID, 100, 100);

        Assert.assertEquals("After database sync, all entities have sync'd",
                clientDb.getExampleSyncableDao().findAll().size(),
                serverDb.getExampleSyncableDao().findAll().size());
    }

    @Test
    public void givenSyncedDatabase_whenLocalChangeMade_thenPendingLocalChangesCountShouldBe1() {
        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase masterDb = ExampleDatabase.getInstance(null);

        clientDb.clearAll();
        masterDb.clearAll();

        ExampleDatabase clientRepo = clientDb.getRepository(TEST_URI, ExampleDatabase.VALID_AUTH_TOKEN);
        int numChangesBefore = clientDb.getExampleSyncableDao().countPendingLocalChanges(
                ExampleDatabase.VALID_AUTH_TOKEN_USER_UID, clientDb.getDeviceBits());

        ExampleSyncableEntity e = new ExampleSyncableEntity("title");
        clientRepo.getExampleSyncableDao().insert(e);
        int numChangesAfter = clientDb.getExampleSyncableDao().countPendingLocalChanges(
                ExampleDatabase.VALID_AUTH_TOKEN_USER_UID, clientDb.getDeviceBits());

        Assert.assertEquals("Before any changes made, number of pending changes = 0",
                0, numChangesBefore);
        Assert.assertEquals("After local changes made, number pending changes = 1",
                1, numChangesAfter);
    }

    @Test
    public void givenDatabaseWithLocalChanges_whenSyncCompleted_thenPendingLocalChangesCountShouldBe0() {
        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase masterDb = ExampleDatabase.getInstance(null);

        clientDb.clearAll();
        masterDb.clearAll();

        ExampleDatabase clientRepo = clientDb.getRepository(TEST_URI, ExampleDatabase.VALID_AUTH_TOKEN);
        clientRepo.getExampleSyncableDao().insert(new ExampleSyncableEntity("test entity"));

        int numChangesBefore = clientDb.getExampleSyncableDao().countPendingLocalChanges(
                ExampleDatabase.VALID_AUTH_TOKEN_USER_UID, clientDb.getDeviceBits());

        clientDb.syncWith(clientRepo, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID, 100, 100);

        Assert.assertEquals("After sync completes successfully, number of local changes pending = 0",
                0, clientRepo.getExampleSyncableDao()
                        .countPendingLocalChanges(ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                                clientDb.getDeviceBits()));
        Assert.assertEquals("Before sync 1 change was pending", 1, numChangesBefore);
    }

    @Test
    public void givenDatabaseWithLocalChanges_whenSyncFailed_thenPendingLocalChangesCountShouldStillBe1() {
        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
        ExampleDatabase masterDb = ExampleDatabase.getInstance(null);

        clientDb.clearAll();
        masterDb.clearAll();

        ExampleDatabase clientRepo = clientDb.getRepository(TEST_URI, ExampleDatabase.VALID_AUTH_TOKEN);
        clientRepo.getExampleSyncableDao().insert(new ExampleSyncableEntity("test entity"));

        int numChangesBefore = clientDb.getExampleSyncableDao().countPendingLocalChanges(
                ExampleDatabase.VALID_AUTH_TOKEN_USER_UID, clientDb.getDeviceBits());

        server.shutdownNow();

        clientDb.syncWith(clientRepo, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID, 100,
                100);

        Assert.assertEquals("After sync fails to complete, number of local changes still = 1",
                1, clientRepo.getExampleSyncableDao()
                        .countPendingLocalChanges(ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                                clientDb.getDeviceBits()));
        Assert.assertEquals("Before sync, 1 change was pending", 1, numChangesBefore);

    }



}
