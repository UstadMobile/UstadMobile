package com.ustadmobile.core.controller

import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
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
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import java.util.*


class XapiPackageContentPresenterTest {

    private lateinit var learnerGroup: LearnerGroup
    private lateinit var context: Any

    val account = UmAccount(42, "username", "fefe1010fe",
            "http://localhost/")

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var xapiContainer: Container

    private lateinit var mockedView: XapiPackageContentView

    private val contentEntryUid = 1234L

    private lateinit var di: DI

    private lateinit var endpoint: Endpoint

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Suppress("BlockingMethodInNonBlockingContext")
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

        runBlocking {
            repo.addEntriesToContainerFromZipResource(xapiContainer.containerUid, this::class.java,
                "/com/ustadmobile/core/contentformats/XapiPackage-JsTetris_TCAPI.zip",
                ContainerAddOptions(temporaryFolder.newFolder().toDoorUri()))
        }

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
            val umAccountActor = Json.decodeFromString(UmAccountActor.serializer(), paramsProvided["actor"]!!)
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
            val umAccountGroupActor = Json.decodeFromString(UmAccountGroupActor.serializer(),
                paramsProvided["actor"]!!)
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
