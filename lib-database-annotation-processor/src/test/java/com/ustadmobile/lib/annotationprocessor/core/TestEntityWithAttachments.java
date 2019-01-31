package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableEntityWithAttachment;
import com.ustadmobile.lib.annotationprocessor.core.db.ExampleSyncableEntityWithAttachmentDao;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;


public class TestEntityWithAttachments {

    private File serverAttachmentsDir;

    private File clientAttachmentsDir;

    private HttpServer httpServer;

    public static final String TEST_URI = "http://localhost:8089/api/";

    ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
    ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");
    ExampleDatabase clientRepo = clientDb.getRepository(TEST_URI,
            ExampleDatabase.VALID_AUTH_TOKEN);

    ExampleSyncableEntityWithAttachmentDao clientDao = clientDb
            .getExampleSyncableEntityWithAttachmentDao();
    ExampleSyncableEntityWithAttachmentDao clientRepoDao = clientRepo
            .getExampleSyncableEntityWithAttachmentDao();

    @Before
    public void before() throws IOException{
        serverAttachmentsDir = File.createTempFile("ExampleDatabase", "attachments");
        serverAttachmentsDir.delete();
        serverAttachmentsDir.mkdir();

        clientAttachmentsDir = File.createTempFile("ExampleDatabase", "clientAttachments");
        clientAttachmentsDir.delete();
        clientAttachmentsDir.mkdir();

        httpServer = startServer();

        serverDb = ExampleDatabase.getInstance(null);
        clientDb = ExampleDatabase.getInstance(null, "db1");
        clientRepo = clientDb.getRepository(TEST_URI,
                ExampleDatabase.VALID_AUTH_TOKEN);

        clientDao = clientDb.getExampleSyncableEntityWithAttachmentDao();
        clientRepoDao = clientRepo.getExampleSyncableEntityWithAttachmentDao();

        serverDb.clearAll();
        clientDb.clearAll();

        serverDb.setAttachmentsDir(serverAttachmentsDir.getAbsolutePath());
        clientDb.setAttachmentsDir(clientAttachmentsDir.getAbsolutePath());
    }

    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .register(MultiPartFeature.class)
                .packages("com.ustadmobile.lib.annotationprocessor.core.db");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
    }

    @After
    public void tearDown() throws IOException{
        FileUtils.deleteDirectory(serverAttachmentsDir);
        FileUtils.deleteDirectory(clientAttachmentsDir);
        httpServer.shutdownNow();
    }

    @Test
    public void givenAttachmentSet_whenRetrieved_thenShouldBeIdentical() throws IOException {
        byte[] byteBuf = new byte[]{1,2,3,4,5,6,7,8};
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.setAttachmentsDir(serverAttachmentsDir.getAbsolutePath());
        db.getExampleSyncableEntityWithAttachmentDao().setAttachment(1,
                new ByteArrayInputStream(byteBuf));
        Assert.assertTrue(true);
        String fileName = db.getExampleSyncableEntityWithAttachmentDao().getAttachmentUri(1);
        Assert.assertTrue("Attachment file exists", new File(fileName).exists());
    }

    @Test
    public void givenAttachmentSetByMovingFile_whenRetrieved_thenShouldBeIdentical()
            throws IOException {
        byte[] byteBuf = new byte[]{1,2,3,4,5,6,7,8};
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.setAttachmentsDir(serverAttachmentsDir.getAbsolutePath());

        File tmpFile = File.createTempFile("tmp", "file");
        FileOutputStream fout = new FileOutputStream(tmpFile);
        fout.write(byteBuf);
        fout.flush();
        fout.close();

        db.getExampleSyncableEntityWithAttachmentDao().setAttachmentFile(1, tmpFile);

        InputStream attachmentIn = db.getExampleSyncableEntityWithAttachmentDao().
                getAttachmentStream(1);
        byte[] newBuf = new byte[byteBuf.length];
        attachmentIn.read(newBuf, 0, byteBuf.length);
        attachmentIn.close();
        Assert.assertTrue("Attachment in is the same as written to tmp file",
                Arrays.equals(byteBuf, newBuf));
    }

    @Test
    public void givenAttachment_whenUploadedViaRepository_thenShouldBeOnServerAttachmentDir()
            throws IOException {
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");

        byte[] buf = new byte[]{1,2,3,4,5};
        ByteArrayInputStream bin = new ByteArrayInputStream(buf);

        clientDb.getRepository(TEST_URI, ExampleDatabase.VALID_AUTH_TOKEN)
                .getExampleSyncableEntityWithAttachmentDao().uploadAttachment(1, bin);

        InputStream inFromServer = serverDb.getExampleSyncableEntityWithAttachmentDao()
                .getAttachmentStream(1);
        byte[] fromServer = IOUtils.readFully(inFromServer, buf.length);
        Assert.assertTrue("After upload via retrofit interface, server " +
                "has same attachment", Arrays.equals(buf, fromServer));
    }

    @Test
    public void givenAttachmentOnServer_whenRetrievedViaRepository_thenShouldBeIdentical()
            throws IOException {
        ExampleDatabase serverDb = ExampleDatabase.getInstance(null);
        ExampleDatabase clientDb = ExampleDatabase.getInstance(null, "db1");

        byte[] buf = new byte[]{1,2,3,4,5};

        serverDb.getExampleSyncableEntityWithAttachmentDao().setAttachment(1,
                new ByteArrayInputStream(buf));

        InputStream inFromRepo = clientDb.getRepository(TEST_URI, ExampleDatabase.VALID_AUTH_TOKEN)
                .getExampleSyncableEntityWithAttachmentDao().downloadAttachment(1);
        byte[] repoBuf = IOUtils.readFully(inFromRepo, buf.length);
        Assert.assertTrue("After setting attachment on server, download from client " +
                "via retrofit interface is identical", Arrays.equals(buf, repoBuf));

    }

    @Test
    public void givenAttachmentSentLocally_whenSynced_thenShouldBeOnServer() throws IOException{
        byte[] buf = new byte[]{1,2,3,4,5};
        ExampleSyncableEntityWithAttachment entity = new ExampleSyncableEntityWithAttachment();
        entity.setFilename("test");
        long insertedUid = clientRepo.getExampleSyncableEntityWithAttachmentDao().insert(entity);
        clientRepoDao.setAttachment(insertedUid, new ByteArrayInputStream(buf));

        clientDao.syncWith(clientRepoDao, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                10, 10);

        InputStream serverAttachmentIn = serverDb.getExampleSyncableEntityWithAttachmentDao()
                .getAttachmentStream(insertedUid);
        byte[] bufFromServer = IOUtils.readFully(serverAttachmentIn, buf.length);
        Assert.assertTrue(Arrays.equals(buf, bufFromServer));
    }

    @Test
    public void givenAttachmentOnServer_whenSynced_thenShouldBeOnClient() throws IOException{
        byte[] buf = new byte[]{1,2,3,4,5};

        ExampleSyncableEntityWithAttachment entity = new ExampleSyncableEntityWithAttachment();
        entity.setFilename("test");
        ExampleDatabase serverDummyRepo = serverDb.getRepository("http://localhost/",
                "");
        long insertedUid = serverDummyRepo.getExampleSyncableEntityWithAttachmentDao()
                .insert(entity);
        serverDummyRepo.getExampleSyncableEntityWithAttachmentDao().setAttachment(insertedUid,
                new ByteArrayInputStream(buf));

        clientDao.syncWith(clientRepoDao, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                10, 10);

        InputStream clientAttachmentIn = clientDao.getAttachmentStream(insertedUid);
        byte[] bufFromClient = IOUtils.readFully(clientAttachmentIn, buf.length);
        Assert.assertTrue(Arrays.equals(buf, bufFromClient));
    }

    @Test
    public void givenMoreAttachmentsOnClient_whenSynced_thenShouldBeOnServerAndNotUploadedAgain()
            throws IOException{
        int numEntities = 3;
        byte[][] clientBuf = new byte[numEntities][];
        long[] insertedEntitUids = new long[numEntities];
        for(int i = 0; i < numEntities; i++) {
            clientBuf[i] = new byte[]{(byte)i, (byte)(i + 1), (byte)(i + 2), (byte)(i + 3),
                    (byte)(i + 4)};
            ExampleSyncableEntityWithAttachment entity = new ExampleSyncableEntityWithAttachment();
            entity.setFilename("entity " + i);
            insertedEntitUids[i] = clientRepoDao.insert(entity);
            clientRepoDao.setAttachment(insertedEntitUids[i], new ByteArrayInputStream(clientBuf[i]));
        }

        ExampleSyncableEntityWithAttachmentDao clientRepoDaoSpy = Mockito.spy(clientRepoDao);
        clientDao.syncWith(clientRepoDaoSpy, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                100, 100);

        //Sync again when there is no new data to ensure that the sync system does not run too many times
        clientDao.syncWith(clientRepoDaoSpy, ExampleDatabase.VALID_AUTH_TOKEN_USER_UID,
                100, 100);

        Mockito.verify(clientRepoDaoSpy, Mockito.times(numEntities + 1))
                .handleIncomingSync(any(), anyLong(), anyLong(), anyLong(), anyInt(), anyInt());

        for(int i = 0; i < numEntities; i++) {
            InputStream serverIn = serverDb.getExampleSyncableEntityWithAttachmentDao()
                    .getAttachmentStream(insertedEntitUids[i]);
            Assert.assertTrue("Entity attachment # " + i + " equal on server",
                    Arrays.equals(clientBuf[i], IOUtils.readFully(serverIn, clientBuf[i].length)));
        }
    }


}
