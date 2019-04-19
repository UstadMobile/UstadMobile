package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.test.core.impl.PlatformTestUtil
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.H5PContentView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.container.ContainerManager
import com.ustadmobile.port.sharedse.impl.http.ClassResourcesResponder
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.zip.ZipFile

class H5PContentPresenterTest {

    private var db: UmAppDatabase? = null

    private var repo: UmAppDatabase? = null

    private var h5pTmpFile: File? = null

    private var containerTmpDir: File? = null

    private var h5pContentView: H5PContentView? = null

    private var httpd: EmbeddedHTTPD? = null

    private var h5pContainer: Container? = null

    @Before
    @Throws(IOException::class)
    fun setup() {
        db = UmAppDatabase.getInstance(PlatformTestUtil.targetContext)
        db?.clearAllTables()
        repo = db?.getRepository("http://localhost/dummy/", "")

        h5pContentView = mock()

        h5pTmpFile = File.createTempFile("H5PContentPresenterTest", "true-false.h5p")
        containerTmpDir = UmFileUtilSe.makeTempDir("H5PContentPresenterTest", "containerTmp")

        UmFileUtilSe.extractResourceToFile(
                "/com/ustadmobile/core/contentformats/H5P-true-false.h5p", h5pTmpFile)

        h5pContainer = Container()
        h5pContainer?.containerUid = repo?.containerDao!!.insert(h5pContainer)
        val containerManager = ContainerManager(h5pContainer, db, repo,
                containerTmpDir!!.absolutePath)
        val zipFile = ZipFile(h5pTmpFile)
        containerManager.addEntriesFromZip(zipFile,
                ContainerManager.OPTION_COPY or ContainerManager.OPTION_UPDATE_TOTALS)
        zipFile.close()

        Mockito.doAnswer { invocation ->
            Thread(invocation.getArgument<Any>(0) as Runnable).start()
            null
        }.`when`<H5PContentView>(h5pContentView).runOnUiThread(ArgumentMatchers.any<Runnable>())

        Mockito.doAnswer {
            val mountedUrl = UMFileUtil.joinPaths(
                    httpd!!.localHttpUrl,
                    httpd!!.mountContainer(it.arguments[0] as Long, null, emptyList()))
            (it.arguments[1] as UmCallback<String?>).onSuccess(mountedUrl)
        }.`when`<H5PContentView>(h5pContentView).mountH5PContainer(any(), any())

        Mockito.doAnswer {
            httpd!!.addRoute("/android-assets/", ClassResourcesResponder::class.java)
            (it.arguments[0] as UmCallback<String>).onSuccess("/android-assets/http/")
        }.`when`<H5PContentView>(h5pContentView).mountH5PDist(any())

        httpd = EmbeddedHTTPD(0, PlatformTestUtil.targetContext, db, repo)
        httpd!!.start()
    }

    @After
    fun after() {
        h5pTmpFile?.delete()
        containerTmpDir?.delete()
    }

    @Test
    fun `Given Valid H5P file WHEN onCreate called THEN should call setTitle`() {
        val h5PPresenter = H5PContentPresenter(PlatformTestUtil.targetContext,
                mapOf(UstadView.ARG_CONTAINER_UID to h5pContainer!!.containerUid.toString()),
                h5pContentView!!)

        h5PPresenter.onCreate(null)

        verify(h5pContentView, timeout(15000))?.mountH5PContainer(
                eq(h5pContainer!!.containerUid), any())
        verify(h5pContentView, timeout(15000))?.mountH5PDist(any())

        verify(h5pContentView, timeout(15000))?.setTitle("True/False Question")

        //A higher fidelity test is run using Espresso testing
        verify(h5pContentView, timeout(15000))?.setContentHtml(any(), any())
    }

}