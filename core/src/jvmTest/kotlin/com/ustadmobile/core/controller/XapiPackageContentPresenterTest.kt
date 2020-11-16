package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.tincan.UmAccountActor
import com.ustadmobile.core.tincan.UmAccountGroupActor
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNER_GROUP_UID
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.Runnable
import kotlinx.serialization.json.Json
import org.junit.*
import org.kodein.di.*
import org.mockito.Mockito.*
import org.mockito.Mockito.timeout
import java.io.File
import java.util.*
import java.util.concurrent.CountDownLatch


class XapiPackageContentPresenterTest {

    private lateinit var learnerGroup: LearnerGroup
    private lateinit var context: Any

    val account = UmAccount(42, "username", "fefe1010fe",
            "http://localhost/")

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var xapiTmpFile: File

    private var containerDirTmp: File? = null

    private lateinit var xapiContainer: Container

    private lateinit var mockedView: XapiPackageContentView


    private val mountLatch = CountDownLatch(1)

    private val contentEntryUid = 1234L

    private lateinit var di: DI

    private lateinit var endpoint: Endpoint

    @Before
    fun setup() {
        val endpointUrl = account.endpointUrl!!
        endpoint = Endpoint(endpointUrl)

        di = DI {
            import(ustadTestRule.diModule)
        }

        di.direct.instance<UstadAccountManager>().activeAccount = account

        val db: UmAppDatabase by di.on(endpoint).instance(tag = TAG_DB)
        val repo: UmAppDatabase by di.on(endpoint).instance(tag = TAG_REPO)

        context = Any()

        xapiContainer = Container().also {
            it.containerContentEntryUid = contentEntryUid
        }
        xapiContainer.containerUid = repo.containerDao.insert(xapiContainer)

        xapiTmpFile = File.createTempFile("testxapipackagecontentpresenter",
                "xapiTmpFile")
        extractTestResourceToFile("/com/ustadmobile/core/contentformats/XapiPackage-JsTetris_TCAPI.zip",
                xapiTmpFile)

        containerDirTmp = UmFileUtilSe.makeTempDir("testxapipackagecontentpresenter",
                "containerDirTmp")
        val containerManager = ContainerManager(xapiContainer, db, repo,
                containerDirTmp!!.absolutePath)
        addEntriesFromZipToContainer(xapiTmpFile.absolutePath, containerManager)

        mockedView = mock{
            on { runOnUiThread(any())}.doAnswer{
                Thread(it.getArgument<Any>(0) as Runnable).start()
            }
        }

        learnerGroup = LearnerGroup().apply {
            learnerGroupUid = 1
            learnerGroupName = "Test"
            repo.learnerGroupDao.insert(this)
        }

        LearnerGroupMember().apply {
            learnerGroupMemberLgUid = learnerGroup.learnerGroupUid
            learnerGroupMemberPersonUid = account.personUid
            learnerGroupMemberRole = LearnerGroupMember.PRIMARY_ROLE
            repo.learnerGroupMemberDao.insert(this)
        }

        Person().apply {
            personUid = 1
            admin = true
            username = "Student"
            firstNames = "Test"
            lastName = "Student"
            repo.personDao.insert(this)
        }

        LearnerGroupMember().apply {
            learnerGroupMemberLgUid = learnerGroup.learnerGroupUid
            learnerGroupMemberPersonUid = 1
            learnerGroupMemberRole = LearnerGroupMember.PARTICIPANT_ROLE
            repo.learnerGroupMemberDao.insert(this)
        }

        GroupLearningSession().apply {
            groupLearningSessionUid = 1
            groupLearningSessionContentUid = contentEntryUid
            groupLearningSessionLearnerGroupUid = learnerGroup.learnerGroupUid
            repo.groupLearningSessionDao.insert(this)
        }

    }

    @After
    fun tearDown() {
        xapiTmpFile.delete()
        UmFileUtilSe.deleteRecursively(containerDirTmp!!)
    }


    @Test
    fun givenValidXapiPackage_whenCreated_shouldLoadAndSetTitle() {
        val args = Hashtable<String, String>()
        Assert.assertNotNull(xapiContainer)
        args.put(UstadView.ARG_CONTAINER_UID, xapiContainer.containerUid.toString())
        args[ARG_CONTENT_ENTRY_UID] = contentEntryUid.toString()

        val xapiPresenter = XapiPackageContentPresenter(context, args, mockedView, di)
        xapiPresenter.onCreate(null)

        argumentCaptor<String> {
            verify(mockedView, timeout(5000 )).url = capture()
            val httpd = di.direct.instance<ContainerMounter>() as EmbeddedHTTPD
            Assert.assertTrue("Mounted path starts with url and html name",
                    firstValue.startsWith(httpd.localHttpUrl) && firstValue.contains("tetris.html"))
            val paramsProvided = UMFileUtil.parseURLQueryString(firstValue)
            val umAccountActor = Json.parse(UmAccountActor.serializer(), paramsProvided["actor"]!!)
            Assert.assertEquals("Account actor is as expected",
                    account.username, umAccountActor.account.name)
            val expectedEndpoint = UMFileUtil.resolveLink(firstValue, "/${UMURLEncoder.encodeUTF8(endpoint.url)}/xapi/$contentEntryUid/")
            Assert.assertEquals("Received expected Xapi endpoint: /endpoint/xapi/contentEntryUid",
                    expectedEndpoint, paramsProvided["endpoint"])
            Assert.assertEquals("Received expected activity id",
                    "http://id.tincanapi.com/activity/tincan-prototypes/tetris",
                    paramsProvided["activity_id"])
        }

        verify(mockedView, timeout(15000)).contentTitle = "Tin Can Tetris Example"
    }


    @Test
    fun givenValidXapiPackage_whenCreatedWithGroup_shouldLoadAndSetTitle() {
        val args = Hashtable<String, String>()
        Assert.assertNotNull(xapiContainer)
        args.put(UstadView.ARG_CONTAINER_UID, xapiContainer.containerUid.toString())
        args[ARG_LEARNER_GROUP_UID] = learnerGroup.learnerGroupUid.toString()
        args[ARG_CONTENT_ENTRY_UID] = contentEntryUid.toString()

        val xapiPresenter = XapiPackageContentPresenter(context, args, mockedView, di)
        xapiPresenter.onCreate(null)

        argumentCaptor<String> {
            verify(mockedView, timeout(5000 )).url = capture()
            val httpd = di.direct.instance<ContainerMounter>() as EmbeddedHTTPD
            Assert.assertTrue("Mounted path starts with url and html name",
                    firstValue.startsWith(httpd.localHttpUrl) && firstValue.contains("tetris.html"))
            val paramsProvided = UMFileUtil.parseURLQueryString(firstValue)
            val umAccountGroupActor = Json.parse(UmAccountGroupActor.serializer(), paramsProvided["actor"]!!)
            Assert.assertEquals("Actor object type is group",
                    umAccountGroupActor.objectType, "Group")
            Assert.assertEquals("Actor account name is groupUid",
                    "group:${learnerGroup.learnerGroupUid}",   umAccountGroupActor.account.name)
            Assert.assertEquals("Actor member list is 2",
                   2,  umAccountGroupActor.members.size)
            val expectedEndpoint = UMFileUtil.resolveLink(firstValue, "/${UMURLEncoder.encodeUTF8(endpoint.url)}/xapi/$contentEntryUid/")
            Assert.assertEquals("Received expected Xapi endpoint: /endpoint/xapi/contentEntryUid",
                    expectedEndpoint, paramsProvided["endpoint"])
            Assert.assertEquals("Received expected activity id",
                    "http://id.tincanapi.com/activity/tincan-prototypes/tetris",
                    paramsProvided["activity_id"])
        }

        verify(mockedView, timeout(15000)).contentTitle = "Tin Can Tetris Example"
    }

}
