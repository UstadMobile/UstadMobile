package com.ustadmobile.core.controller;

import com.ustadmobile.core.contentformats.epub.opf.OpfDocument;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.EpubContentView;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.port.sharedse.container.ContainerManager;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.util.UmFileUtilSe;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestEpubContentPresenter {

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private File epubTmpFile;

    private File containerDirTmp;

    private EpubContentView mockEpubView;

    private Container epubContainer;

    private EmbeddedHTTPD httpd;

    private OpfDocument opf;

    @Before
    public void setup() throws IOException, XmlPullParserException {
        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        repo = db.getRepository("http://localhost/dummy/", "");
        db.clearAllTables();

        epubContainer = new Container();
        epubContainer.setContainerUid(repo.getContainerDao().insert(epubContainer));

        epubTmpFile = File.createTempFile("testepubcontentpresenter", "epubTmpFile");

        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/core/contentformats/epub/test.epub",
                epubTmpFile);

        containerDirTmp = UmFileUtilSe.makeTempDir("testpubcontentpresenter", "containerDirTmp");
        ContainerManager containerManager = new ContainerManager(epubContainer, db, repo,
                containerDirTmp.getAbsolutePath());

        ZipFile epubZipFile = new ZipFile(epubTmpFile);
        containerManager.addEntriesFromZip(epubZipFile, ContainerManager.OPTION_COPY);
        epubZipFile.close();

        httpd = new EmbeddedHTTPD(0, PlatformTestUtil.getTargetContext(), db, repo);
        httpd.start();

        mockEpubView = mock(EpubContentView.class);

        doAnswer((invocation -> {
            new Thread(() -> {
                String mountedUrl = UMFileUtil.joinPaths(httpd.getLocalHttpUrl(),
                        httpd.mountContainer(invocation.getArgument(0), null));
                UmCallbackUtil.onSuccessIfNotNull(invocation.getArgument(1), mountedUrl);
            }).start();

            return null;
        })).when(mockEpubView).mountContainer(eq(epubContainer.getContainerUid()), any());

        doAnswer(invocation -> {
            new Thread((Runnable)invocation.getArgument(0)).start();
            return null;
        }).when(mockEpubView).runOnUiThread(any());

        //Used for verification purposes
        InputStream opfIn = containerManager.getInputStream(containerManager.getEntry("OEBPS/package.opf"));
        opf = new OpfDocument();
        opf.loadFromOPF(UstadMobileSystemImpl.getInstance().newPullParser(opfIn, "UTF-8"));
        opfIn.close();
    }

    @After
    public void tearDown() {
        epubTmpFile.delete();
        UmFileUtilSe.deleteRecursively(containerDirTmp);
    }

    @Test
    public void givenValidEpub_whenCreated_shouldSetTitleAndSpineHrefs() throws IOException {
        Hashtable args = new Hashtable();
        args.put(EpubContentView.ARG_CONTAINER_UID, epubContainer.getContainerUid());

        AtomicReference<Object> hrefListReference = new AtomicReference<>();

        doAnswer(invocation -> {
            hrefListReference.set(invocation.getArgument(0));
            return null;
        }).when(mockEpubView).setSpineUrls(any());

        EpubContentPresenter presenter = new EpubContentPresenter(PlatformTestUtil.getTargetContext(),
                args, mockEpubView);
        presenter.onCreate(null);

        verify(mockEpubView, timeout(5000)).mountContainer(eq(epubContainer.getContainerUid()),
                any());
        verify(mockEpubView, timeout(5000)).setContainerTitle(opf.getTitle());
        verify(mockEpubView, timeout(5000)).setSpineUrls(any());

        String[] linearSpineUrls = (String[])hrefListReference.get();
        for(int i = 0; i < linearSpineUrls.length; i++) {
            Assert.assertTrue("Spine itemk " + i + " ends with expected url",
                    linearSpineUrls[i].endsWith(opf.getLinearSpineHREFs()[i]));
            UmHttpResponse response = UstadMobileSystemImpl.getInstance().makeRequestSync(
                    new UmHttpRequest(PlatformTestUtil.getTargetContext(), linearSpineUrls[i]));
            Assert.assertEquals("Making HTTP request to spine url status code is 200 OK", 200,
                    response.getStatus());
        }

    }

    @Test
    public void givenNoOcf_whenCreated_shouldShowErrorMessage() {

    }

}
