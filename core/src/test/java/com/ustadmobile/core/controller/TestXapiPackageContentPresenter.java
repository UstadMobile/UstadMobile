package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.core.tincan.TinCanXML;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.XapiPackageContentView;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.port.sharedse.container.ContainerManager;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.util.UmFileUtilSe;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestXapiPackageContentPresenter {

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private File xapiTmpFile;

    private File containerDirTmp;

    private Container xapiContainer;

    private XapiPackageContentView mockXapiPackageContentView;

    private EmbeddedHTTPD httpd;

    private TinCanXML xapiXml;

    private volatile String lastMountedUrl;

    private CountDownLatch mountLatch = new CountDownLatch(1);

    @Before
    public void setup() throws IOException {
        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        repo = db.getRepository("http://localhost/dummy/", "");
        db.clearAllTables();

        xapiContainer = new Container();
        xapiContainer.setContainerUid(repo.getContainerDao().insert(xapiContainer));

        xapiTmpFile = File.createTempFile("testxapipackagecontentpresenter",
                "xapiTmpFile");
        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/core/contentformats/XapiPackage-JsTetris_TCAPI.zip",
                xapiTmpFile);

        containerDirTmp = UmFileUtilSe.makeTempDir("testxapipackagecontentpresenter",
                "containerDirTmp");
        ContainerManager containerManager = new ContainerManager(xapiContainer, db, repo,
                containerDirTmp.getAbsolutePath());
        ZipFile xapiZipFile = new ZipFile(xapiTmpFile);
        containerManager.addEntriesFromZip(xapiZipFile, ContainerManager.OPTION_COPY);
        xapiZipFile.close();

        httpd = new EmbeddedHTTPD(0, PlatformTestUtil.getTargetContext(), db, repo);
        httpd.start();

        mockXapiPackageContentView = mock(XapiPackageContentView.class);
        doAnswer((invocation -> {
            new Thread(() -> {
                lastMountedUrl = UMFileUtil.joinPaths(httpd.getLocalHttpUrl(),
                        httpd.mountContainer(invocation.getArgument(0), null));
                UmCallbackUtil.onSuccessIfNotNull(invocation.getArgument(1), lastMountedUrl);
                mountLatch.countDown();
            }).start();

            return null;
        })).when(mockXapiPackageContentView)
                .mountContainer(eq(xapiContainer.getContainerUid()), any());


        doAnswer(invocation -> {
            new Thread((Runnable)invocation.getArgument(0)).start();
            return null;
        }).when(mockXapiPackageContentView).runOnUiThread(any());

    }

    @After
    public void tearDown() {
        xapiTmpFile.delete();
        UmFileUtilSe.deleteRecursively(containerDirTmp);
    }

    @Test
    public void givenValidXapiPackage_whenCreated_shouldLoadAndSetTitle() throws InterruptedException{
        Hashtable args = new Hashtable();
        args.put(XapiPackageContentView.ARG_CONTAINER_UID, String.valueOf(xapiContainer.getContainerUid()));

        XapiPackageContentPresenter xapiPresenter = new XapiPackageContentPresenter(
                PlatformTestUtil.getTargetContext(), args, mockXapiPackageContentView);
        xapiPresenter.onCreate(null);

        mountLatch.await(15000, TimeUnit.MILLISECONDS);

        verify(mockXapiPackageContentView).mountContainer(
                eq(xapiContainer.getContainerUid()), any());

        verify(mockXapiPackageContentView, timeout(5000)).loadUrl(
                UMFileUtil.joinPaths(lastMountedUrl, "tetris.html"));

        verify(mockXapiPackageContentView, timeout(15000)).setTitle("Tin Can Tetris Example");
    }

}
