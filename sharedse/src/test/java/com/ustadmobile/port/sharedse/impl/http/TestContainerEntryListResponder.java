package com.ustadmobile.port.sharedse.impl.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5;
import com.ustadmobile.port.sharedse.container.ContainerManager;
import com.ustadmobile.port.sharedse.util.UmFileUtilSe;
import com.ustadmobile.sharedse.SharedSeTestConfig;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestContainerEntryListResponder {

    private static final String RES_FOLDER = "/com/ustadmobile/port/sharedse/container/";

    private static final String[] RES_FILENAMES = new String[]{"testfile1.png", "testfile2.png"};

    private Container container;

    private ContainerManager containerManager;

    private UmAppDatabase appDatabase;

    private UmAppDatabase appRepo;

    @Before
    public void setupDb() throws IOException {
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                SharedSeTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true);

        appDatabase = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        appDatabase.clearAllTables();
        appRepo = appDatabase.getRepository("http://localhost/dummy/", "");

        File tmpDir = File.createTempFile("testresponder", "tmpdir");
        tmpDir.delete();
        tmpDir.mkdirs();

        File containerTmpDir = File.createTempFile("testresponder", "containerfiles");
        containerTmpDir.delete();
        containerTmpDir.mkdirs();

        Map<File, String> fileMap = new HashMap<>();
        for(String filename : RES_FILENAMES) {
            File resFile = new File(tmpDir, filename);
            UmFileUtilSe.extractResourceToFile(RES_FOLDER + filename,
                    resFile);
            fileMap.put(resFile, filename);
        }

        container = new Container();
        container.setContainerUid(appDatabase.getContainerDao().insert(container));

        containerManager = new ContainerManager(container, appDatabase, appRepo,
                containerTmpDir.getAbsolutePath());
        containerManager.addEntries(fileMap, true);
    }

    @Test
    public void givenContainerWithFiles_whenGetRequestedMade_thenShouldReturnFileList()
            throws IOException{
        ContainerEntryListResponder responder = new ContainerEntryListResponder();

        NanoHTTPD.IHTTPSession mockSession = mock(NanoHTTPD.IHTTPSession.class);

        Map<String, List<String>> mockParamMap = new HashMap<>();
        mockParamMap.put(ContainerEntryListResponder.PARAM_CONTAINER_UID,
                Arrays.asList(String.valueOf(container.getContainerUid())));
        when(mockSession.getParameters()).thenReturn(mockParamMap);

        RouterNanoHTTPD.UriResource mockUriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(mockUriResource.initParameter(0, UmAppDatabase.class)).thenReturn(appDatabase);


        NanoHTTPD.Response response = responder.get(mockUriResource, null, mockSession);


        Assert.assertNotNull("Response is not null", response);
        String responseStr = UMIOUtils.readStreamToString(response.getData());
        List<ContainerEntryWithMd5> containerEntryList = new Gson().fromJson(responseStr,
                new TypeToken<List<ContainerEntryWithMd5>>(){}.getType());
        Assert.assertEquals("List has two entries", 2, containerEntryList.size());
    }

    @Test
    public void givenContainerUidWithNoFiles_whenGetRequestMade_thenShouldReturn404NotFound() {
        ContainerEntryListResponder responder = new ContainerEntryListResponder();

        NanoHTTPD.IHTTPSession mockSession = mock(NanoHTTPD.IHTTPSession.class);

        Map<String, List<String>> mockParamMap = new HashMap<>();
        mockParamMap.put(ContainerEntryListResponder.PARAM_CONTAINER_UID,
                Arrays.asList(String.valueOf(0)));
        when(mockSession.getParameters()).thenReturn(mockParamMap);

        RouterNanoHTTPD.UriResource mockUriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(mockUriResource.initParameter(0, UmAppDatabase.class)).thenReturn(appDatabase);

        NanoHTTPD.Response response = responder.get(mockUriResource, null, mockSession);
        Assert.assertEquals("When making a request for a container that has no entries, 404 status " +
                "is returns", NanoHTTPD.Response.Status.NOT_FOUND, response.getStatus());

    }



}
