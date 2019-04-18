package com.ustadmobile.port.rest.endpoints;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;

import org.apache.commons.io.FileUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

public class TestContentEntryFileDownloadResource {

    protected static HttpServer server;

    public static final String TEST_URI = "http://localhost:8089/api/";

    private static File contentTmpFile;

    private UmAppDatabase appDb;

    private ContentEntryFile contentEntryFile;


    @BeforeClass
    public static void startServer() throws IOException {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages("com.ustadmobile.port.rest.endpoints")
                .register(MultiPartFeature.class);
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
        server.start();
    }

    @BeforeClass
    public static void copyTmpFile() throws IOException{
        contentTmpFile = File.createTempFile("TestContentEntryFileDownloadResource", "file1");
        InputStream entryFileIn = TestContentEntryFileDownloadResource.class
                .getResourceAsStream("thelittlechicks.epub");
        OutputStream fileOut = new FileOutputStream(contentTmpFile);
        UMIOUtils.readFully(entryFileIn, fileOut);
        fileOut.close();
    }

    @AfterClass
    public static void cleanupClass() {
        contentTmpFile.delete();
        server.shutdownNow();
    }

    @Before
    public void setup() {
        appDb = UmAppDatabase.getInstance(null);
        appDb.clearAllTables();

        contentEntryFile = new ContentEntryFile(contentTmpFile.length());
        contentEntryFile.setMimeType("application/epub+zip");
        contentEntryFile.setContentEntryFileUid(
                appDb.getContentEntryFileDao().insert(contentEntryFile));

        ContentEntryFileStatus entryStatus = new ContentEntryFileStatus();
        entryStatus.setCefsContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
        entryStatus.setFilePath(contentTmpFile.getAbsolutePath());
        appDb.getContentEntryFileStatusDao().insert(entryStatus);
    }


    @Test
    public void givenFileExists_whenDownloaded_thenShouldDownloadAndBeTheSameFile()
            throws IOException{
        File downloadTmp = File.createTempFile("TestContentEntryFileDownloadResource",
                "dltmp");
        FileUtils.copyURLToFile(new URL(TEST_URI + "ContentEntryFile/" +
                contentEntryFile.getContentEntryFileUid()), downloadTmp);

        Assert.assertEquals("Downloaded file is same size as original",
                contentTmpFile.length(), downloadTmp.length());

        Assert.assertNotNull(appDb);
    }


}
