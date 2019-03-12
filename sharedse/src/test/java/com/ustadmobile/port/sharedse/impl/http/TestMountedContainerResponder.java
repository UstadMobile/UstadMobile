package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.port.sharedse.container.ContainerManager;
import com.ustadmobile.port.sharedse.util.UmFileUtilSe;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestMountedContainerResponder {

    private File containerTmpDir;

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private Container container;

    private ContainerManager containerManager;

    @Before
    public void setup() throws IOException {
        containerTmpDir = UmFileUtilSe.makeTempDir("TestMountedContainerResponder",
                "containerTmpDir");

        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        repo = db.getRepository("http://localhost/dummy/", "");
        db.clearAllTables();

        container = new Container();
        container.setContainerUid(repo.getContainerDao().insert(container));
        containerManager = new ContainerManager(container, db, repo,
                containerTmpDir.getAbsolutePath());
        File tmpExtractFile = new File(containerTmpDir, "testfile1.png");
        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/port/sharedse/container/testfile1.png",
                tmpExtractFile);
        Map<File, String> addMap = new HashMap<>();
        addMap.put(tmpExtractFile, "subfolder/testfile1.png");
        containerManager.addEntries(addMap,
                ContainerManager.OPTION_COPY | ContainerManager.OPTION_UPDATE_TOTALS);
    }

    public void tearDown() throws IOException {
        UmFileUtilSe.deleteRecursively(containerTmpDir);
    }

    @Test
    public void givenContainerMounted_whenGetCalledWithPathThatExists_thenFileContentsShouldMatch()
            throws IOException{
        MountedContainerResponder responder = new MountedContainerResponder();

        NanoHTTPD.IHTTPSession mockSession = mock(NanoHTTPD.IHTTPSession.class);
        String mountPath = "container/" + container.getContainerUid() + "/";
        when(mockSession.getUri()).thenReturn(mountPath + "subfolder/testfile1.png");

        RouterNanoHTTPD.UriResource mockUriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(mockUriResource.initParameter(0, ContainerManager.class))
                .thenReturn(containerManager);
        when(mockUriResource.initParameter(1, List.class)).thenReturn(new ArrayList());
        when(mockUriResource.getUri()).thenReturn(mountPath +
                MountedContainerResponder.URI_ROUTE_POSTFIX);

        NanoHTTPD.Response response = responder.get(mockUriResource, null, mockSession);
        InputStream containerIn = containerManager.getInputStream(
                containerManager.getEntry("subfolder/testfile1.png"));
        Assert.assertArrayEquals("Data returned by URI responder matches actual container entry",
                UMIOUtils.readStreamToByteArray(containerIn),
                UMIOUtils.readStreamToByteArray(response.getData()));
        containerIn.close();

        Assert.assertEquals("Response is 200 OK", NanoHTTPD.Response.Status.OK,
                response.getStatus());

    }

    @Test
    public void givenContainerMountedWithNonExisting_whenGetCalledWithNonExistantPath_thenShouldReturn404() {
        MountedContainerResponder responder = new MountedContainerResponder();

        NanoHTTPD.IHTTPSession mockSession = mock(NanoHTTPD.IHTTPSession.class);
        String mountPath = "container/" + container.getContainerUid() + "/";
        when(mockSession.getUri()).thenReturn(mountPath + "subfolder/doesnotexist.png");

        RouterNanoHTTPD.UriResource mockUriResource = mock(RouterNanoHTTPD.UriResource.class);
        when(mockUriResource.initParameter(0, ContainerManager.class))
                .thenReturn(containerManager);
        when(mockUriResource.initParameter(1, List.class)).thenReturn(new ArrayList());
        when(mockUriResource.getUri()).thenReturn(mountPath +
                MountedContainerResponder.URI_ROUTE_POSTFIX);

        NanoHTTPD.Response response = responder.get(mockUriResource, null, mockSession);
        Assert.assertEquals("Response is 404", NanoHTTPD.Response.Status.NOT_FOUND,
                response.getStatus());
    }

}
