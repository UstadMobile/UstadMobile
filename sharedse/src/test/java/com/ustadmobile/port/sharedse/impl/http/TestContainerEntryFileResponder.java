package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.port.sharedse.container.ContainerManager;
import com.ustadmobile.port.sharedse.util.SharedSeTestUtil;
import com.ustadmobile.sharedse.SharedSeTestConfig;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestContainerEntryFileResponder {

    private static final String RES_FOLDER = "/com/ustadmobile/port/sharedse/container/";

    private static final String[] RES_FILENAMES = new String[]{"testfile1.png", "testfile2.png"};

    private Container container;

    private ContainerManager containerManager;

    private UmAppDatabase appDatabase;

    private UmAppDatabase appRepo;

    @Before
    public void setup() throws IOException {
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                SharedSeTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true);

        appDatabase = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        appDatabase.clearAllTables();
        appRepo = appDatabase.getRepository("http://localhost/dummy/", "");
    }

    @Test
    public void givenExistingContainerEntryFileUid_whenGetCalled_shouldReturnFileContents() throws IOException{
        Container container = new Container();
        container.setContainerUid(appDatabase.getContainerDao().insert(container));
        File containerFileTmpDir = SharedSeTestUtil.makeTempDir("testcontainerentryfileresponder",
                "containerdir");
        ContainerManager containerManager = new ContainerManager(container, appDatabase, appRepo,
                containerFileTmpDir.getAbsolutePath());
        File fileToAdd = File.createTempFile("testcontainerentryfileresponder", "tmpfile");
        SharedSeTestUtil.extractResourceToFile("/com/ustadmobile/port/sharedse/container/",
                fileToAdd);
        Map<File, String> fileMap = new HashMap<>();
        fileMap.put(fileToAdd, "testfile1.png");
        containerManager.addEntries(fileMap, true);

        NanoHTTPD.IHTTPSession mockSession = mock(NanoHTTPD.IHTTPSession.class);
        when(mockSession.getUri()).thenReturn("/ContainerEntryFile/" +
                containerManager.getAllEntries().get(0).getCeCefUid());

        RouterNanoHTTPD.UriResource mockUriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(mockUriResource.initParameter(0, UmAppDatabase.class)).thenReturn(appDatabase);

        NanoHTTPD.Response response = new ContainerEntryFileResponder().get(mockUriResource, null,
                mockSession);

        InputStream containerIn = containerManager.getInputStream(containerManager.getAllEntries().get(0));
        Assert.assertTrue("Response contents equals file contents",
                Arrays.equals(UMIOUtils.readStreamToByteArray(containerIn),
                        UMIOUtils.readStreamToByteArray(response.getData())));
        Assert.assertEquals("Response status is 200 OK", NanoHTTPD.Response.Status.OK,
                response.getStatus());
    }

    @Test
    public void givenNonExistingFileUid_whenGetCalled_shouldReturn404() {
        NanoHTTPD.IHTTPSession mockSession = mock(NanoHTTPD.IHTTPSession.class);
        when(mockSession.getUri()).thenReturn("/ContainerEntryFile/-1");

        RouterNanoHTTPD.UriResource mockUriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(mockUriResource.initParameter(0, UmAppDatabase.class)).thenReturn(appDatabase);

        NanoHTTPD.Response response = new ContainerEntryFileResponder().get(mockUriResource, null,
                mockSession);

        Assert.assertEquals("Response status is 404 not found when file does not exist",
                NanoHTTPD.Response.Status.NOT_FOUND, response.getStatus());
    }


}
