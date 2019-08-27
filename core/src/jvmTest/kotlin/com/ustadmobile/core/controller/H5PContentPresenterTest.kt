package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.H5PContentView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

class H5PContentPresenterTest {

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var h5pTmpFile: File

    private lateinit var containerTmpDir: File

    private lateinit var h5pContentView: H5PContentView

    private lateinit var httpd: EmbeddedHTTPD

    private lateinit var h5pContainer: Container

    private val context = Any()

    @Before
    @Throws(IOException::class)
    fun setup() {
        checkJndiSetup()
        db = UmAppDatabase.getInstance(context)
        db.clearAllTables()
        repo = db

        h5pContentView = mock()

        h5pTmpFile = File.createTempFile("H5PContentPresenterTest", "true-false.h5p")
        containerTmpDir = UmFileUtilSe.makeTempDir("H5PContentPresenterTest", "containerTmp")

        extractTestResourceToFile("/com/ustadmobile/core/contentformats/H5P-true-false.h5p",
                h5pTmpFile)

        h5pContainer = Container()
        h5pContainer.containerUid = repo.containerDao.insert(h5pContainer)
        val containerManager = ContainerManager(h5pContainer, db, repo,
                containerTmpDir.absolutePath)
        val zipFile = ZipFile(h5pTmpFile)
        addEntriesFromZipToContainer(h5pTmpFile.absolutePath,
                containerManager)
        zipFile.close()

        Mockito.doAnswer { invocation ->
            Thread(invocation.getArgument<Any>(0) as Runnable).start()
            null
        }.`when`<H5PContentView>(h5pContentView).runOnUiThread(any())

        httpd = EmbeddedHTTPD(0, context, db, repo)
        httpd.start()
    }

    @After
    fun after() {
        h5pTmpFile.delete()
        containerTmpDir.delete()
    }

    @Test
    fun `Given Valid H5P file WHEN onCreate called THEN should call setTitle`() {
        val h5PPresenter = H5PContentPresenter(context,
                mapOf(UstadView.ARG_CONTAINER_UID to h5pContainer.containerUid.toString()),
                h5pContentView, httpd.containerMounter)

        h5PPresenter.onCreate(null)

        verify(h5pContentView, timeout(TimeUnit.MINUTES.toMillis(2))).setContentTitle("True/False Question")

        //A higher fidelity test is run using Espresso testing
        verify(h5pContentView, timeout(15000)).setContentHtml(any(), any())
    }

}