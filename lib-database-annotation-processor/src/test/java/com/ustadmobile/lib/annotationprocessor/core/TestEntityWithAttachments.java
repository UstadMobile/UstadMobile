package com.ustadmobile.lib.annotationprocessor.core;

import com.ustadmobile.lib.annotationprocessor.core.db.ExampleDatabase;

import org.apache.commons.io.FileUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;


public class TestEntityWithAttachments {

    private File attachmentsDir;

    private HttpServer httpServer;

    public static final String TEST_URI = "http://localhost:8089/api/";

    @Before
    public void before() throws IOException{
        attachmentsDir = File.createTempFile("ExampleDatabase", "attachments");
        attachmentsDir.delete();
        attachmentsDir.mkdir();

        httpServer = startServer();
    }

    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .register(MultiPartFeature.class)
                .packages("com.ustadmobile.lib.annotationprocessor.core.db");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
    }

    @After
    public void tearDown() throws IOException{
        FileUtils.deleteDirectory(attachmentsDir);
        httpServer.shutdownNow();
    }

    @Test
    public void givenAttachmentSet_whenRetrieved_thenShouldBeIdentical() throws IOException {
        byte[] byteBuf = new byte[]{1,2,3,4,5,6,7,8};
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.setAttachmentsDir(attachmentsDir.getAbsolutePath());
        db.getExampleSyncableEntityWithAttachmentDao().setAttachment(1,
                new ByteArrayInputStream(byteBuf));
        Assert.assertTrue(true);
        String fileName = db.getExampleSyncableEntityWithAttachmentDao().getAttachmentUri(1);
        Assert.assertTrue("Attachment file exists", new File(fileName).exists());
    }

    @Test
    public void givenAttachmentSetByMovingFile_whenRetrieved_thenShouldBeIdentical() throws IOException {
        byte[] byteBuf = new byte[]{1,2,3,4,5,6,7,8};
        ExampleDatabase db = ExampleDatabase.getInstance(null);
        db.setAttachmentsDir(attachmentsDir.getAbsolutePath());

        File tmpFile = File.createTempFile("tmp", "file");
        FileOutputStream fout = new FileOutputStream(tmpFile);
        fout.write(byteBuf);
        fout.flush();
        fout.close();

        db.getExampleSyncableEntityWithAttachmentDao().setAttachment(1, tmpFile);

        InputStream attachmentIn = db.getExampleSyncableEntityWithAttachmentDao().
                getAttachmentStream(1);
        byte[] newBuf = new byte[byteBuf.length];
        attachmentIn.read(newBuf, 0, byteBuf.length);
        attachmentIn.close();
        Assert.assertTrue("Attachment in is the same as written to tmp file",
                Arrays.equals(byteBuf, newBuf));
    }

}
