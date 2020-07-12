//package com.ustadmobile.core.controller
//
//import com.nhaarman.mockitokotlin2.*
//import com.ustadmobile.core.container.ContainerManager
//import com.ustadmobile.core.util.SystemImplRule
//import com.ustadmobile.core.util.UmAppDatabaseClientRule
//import com.ustadmobile.core.view.ContentEntryExportView
//import com.ustadmobile.core.view.UstadView
//import com.ustadmobile.util.test.AbstractContentEntryExportTest
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.runBlocking
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.kodein.di.DI
//import org.mockito.ArgumentMatchers
//import java.io.File
//import java.util.concurrent.TimeUnit
//
//class ContentEntryExportPresenterTest : AbstractContentEntryExportTest(){
//
//
//    private lateinit var mockView: ContentEntryExportView
//
//    private lateinit var presenter: ContentEntryExportPresenter
//
//    private val context = Any()
//
//    private var arguments: HashMap<String, String> = hashMapOf()
//
//
//    @JvmField
//    @Rule
//    var systemImplRule = SystemImplRule()
//
//    @JvmField
//    @Rule
//    var clientDbRule = UmAppDatabaseClientRule(useDbAsRepo = true)
//
//    private lateinit var di: DI
//
//    @Before
//    fun setUp(){
//        insertContainer(clientDbRule.db, clientDbRule.repo)
//        val containerTmpDir = File.createTempFile("testcontainerdir", "tmp")
//        containerTmpDir.delete()
//        containerTmpDir.mkdir()
//        val containerManager = ContainerManager(container!!, clientDbRule.db, clientDbRule.repo,
//                containerTmpDir.absolutePath)
//
//        val pathList = mutableListOf("path1.txt", "path2.txt", "path3.txt", "path4.txt")
//        val fileSources = mutableListOf<ContainerManager.FileEntrySource>()
//        pathList.forEach {
//            fileSources.add(ContainerManager.FileEntrySource(
//                    File.createTempFile("tmp", it), it))
//        }
//        runBlocking {
//            containerManager.addEntries(*fileSources.toTypedArray())
//        }
//
//        arguments[UstadView.ARG_CONTENT_ENTRY_UID] = contentEntryUid.toString()
//        arguments[ContentEntryExportView.ARG_CONTENT_ENTRY_TITLE] = "Sample title"
//
//        mockView = mock{
//            on { runOnUiThread(ArgumentMatchers.any()) }.doAnswer { invocation ->
//                Thread(invocation.getArgument<Any>(0) as Runnable).start()
//                Unit
//            }
//        }
//
//        di = DI {
//            import(clientDbRule.diModule)
//            import(systemImplRule.diModule)
//        }
//
//    }
//
//    @After
//    fun cleanUp(){
//        val exportedFile = File(presenter.destinationZipFile)
//        if(exportedFile.exists()){
//            exportedFile.delete()
//        }
//    }
//
//
//
//    @Test
//    fun givenExportOptionSelected_whenPreparing_thenShouldCheckFilePermission(){
//        presenter = ContentEntryExportPresenter(context,arguments, mockView, di)
//        presenter.onCreate(null)
//
//        //verify(mockView, timeout(TimeUnit.SECONDS.toMillis(5))).checkFilePermissions()
//    }
//
//
//    @Test
//    fun givenExportDialogIsShown_whenPositiveButtonIsClicked_thenShouldStartExportingByShowingProgress(){
//        runBlocking {
//            presenter = ContentEntryExportPresenter(context,arguments, mockView, di )
//            presenter.onCreate(null)
//            delay(TimeUnit.SECONDS.toMillis(3))
//            presenter.handleClickPositive()
//
//            verify(mockView, timeout(TimeUnit.SECONDS.toMillis(3))).prepareProgressView(eq(true))
//        }
//    }
//
//
//    @Test
//    fun givenExportDialogIsShown_whenNegativeButtonIsClicked_shouldQuitExportingAndDismissDialog(){
//        runBlocking {
//            presenter = ContentEntryExportPresenter(context,arguments, mockView, di)
//            presenter.onCreate(null)
//            delay(TimeUnit.SECONDS.toMillis(3))
//            presenter.handleClickNegative()
//
//            verify(mockView, timeout(TimeUnit.SECONDS.toMillis(2))).dismissDialog()
//        }
//    }
//
//
//}