package com.ustadmobile.sharedse.xapi

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContextXObjectStatementJoinDao
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.core.util.parse8601Duration
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.RESULT_SUCCESS
import com.ustadmobile.core.contentformats.xapi.ContextDeserializer
import com.ustadmobile.core.contentformats.xapi.StatementDeserializer
import com.ustadmobile.core.contentformats.xapi.StatementSerializer
import com.ustadmobile.core.db.dao.ContextXObjectStatementJoinDaoCommon
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.port.sharedse.contentformats.xapi.endpoints.XapiStatementEndpointImpl
import com.ustadmobile.test.util.ext.bindDbAndRepoWithEndpoint
import com.ustadmobile.util.test.checkJndiSetup
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.json.*
import io.ktor.utils.io.*
import okhttp3.OkHttpClient
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class TestStatementEndpoint {

    val contextWithObject = "/com/ustadmobile/port/sharedse/xapi/contextWithObject"
    val fullstatement = "/com/ustadmobile/port/sharedse/xapi/fullstatement"
    val simpleStatement = "/com/ustadmobile/port/sharedse/xapi/simpleStatement"
    val subStatement = "/com/ustadmobile/port/sharedse/xapi/substatement"
    val statementWithLearnerGroup =  "/com/ustadmobile/port/sharedse/xapi/statementWithLearnerGroup"

    val statementWithProgress = "/com/ustadmobile/port/sharedse/xapi/statementWithProgress.json"

    private lateinit var repo: UmAppDatabase

    private lateinit var endpoint: XapiStatementEndpoint

    private lateinit var gson: Gson

    private lateinit var di: DI

    private lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient

    val context = Any()

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Before
    fun setup() {
        checkJndiSetup()
        val endpointScope = EndpointScope()
        val endpointUrl = Endpoint("http://localhost:8087/")

        okHttpClient = OkHttpClient()
        httpClient = HttpClient(OkHttp){
            install(ContentNegotiation)
            install(HttpTimeout)
            engine {
                preconfigured = okHttpClient
            }
        }

        di = DI {
            bindDbAndRepoWithEndpoint(endpointScope, clientMode = true)

            bind<HttpClient>() with singleton {
                httpClient
            }

            bind<OkHttpClient>() with singleton {
                okHttpClient
            }

            bind<Gson>() with singleton {
                val builder = GsonBuilder()
                builder.registerTypeAdapter(Statement::class.java, StatementSerializer())
                builder.registerTypeAdapter(Statement::class.java, StatementDeserializer())
                builder.registerTypeAdapter(ContextActivity::class.java, ContextDeserializer())
                builder.create()
            }
            bind<XapiStatementEndpoint>() with singleton {
                XapiStatementEndpointImpl(endpointUrl, di)
            }
        }

        gson = di.direct.instance()
        repo = di.on(endpointUrl).direct.instance(tag = DoorTag.TAG_REPO)
        endpoint = di.on(endpointUrl).direct.instance()
    }

    fun tearDown() {
        httpClient.close()
    }

    @Test
    @Throws(IOException::class)
    fun givenValidStatement_whenParsed_thenDbAndStatementShouldMatch() {

        val tmpFile = temporaryFolder.newFile()
        javaClass.getResourceAsStream(simpleStatement).writeToFile(tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val statement = gson.fromJson(content, Statement::class.java)
        endpoint.storeStatements(listOf(statement), "")

        val entity = repo.statementDao.findByStatementId("fd41c918-b88b-4b20-a0a5-a4c32391aaa0")
        val agent = repo.agentDao.getAgentByAnyId("", "mailto:user@example.com", "", "", "")
        val verb = repo.verbDao.findByUrl("http://example.com/xapi/verbs#sent-a-statement")
        val xobject = repo.xObjectDao.findByObjectId("http://example.com/xapi/activity/simplestatement")

        Assert.assertEquals("joined to agent", entity!!.agentUid, agent!!.agentUid)
        Assert.assertEquals("mailto:user@example.com", agent.agentMbox)

        Assert.assertEquals("joined to verb", entity.statementVerbUid, verb!!.verbUid)
        Assert.assertEquals("joined to object", entity.xObjectUid, xobject!!.xObjectUid)

    }

    @Test
    @Throws(IOException::class)
    fun givenValidStatementWithContext_whenParsed_thenDbAndStatementShouldMatch() {

        val tmpFile = temporaryFolder.newFile()
        javaClass.getResourceAsStream(contextWithObject).writeToFile(tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val statement = gson.fromJson(content, Statement::class.java)
        endpoint.storeStatements(listOf(statement), "")

        val entity = repo.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        val agent = repo.agentDao.getAgentByAnyId("", "mailto:sally@example.com", "", "", "")
        val verb = repo.verbDao.findByUrl("http://adlnet.gov/expapi/verbs/experienced")
        val xobject = repo.xObjectDao.findByObjectId("http://example.com/activities/solo-hang-gliding")
        val parent = repo.xObjectDao.findByObjectId("http://example.com/activities/hang-gliding-class-a")
        val contextJoin = repo.contextXObjectStatementJoinDao
                .findByStatementAndObjectUid(entity!!.statementUid, parent!!.xObjectUid)

        Assert.assertEquals("joined to agent", entity.agentUid, agent!!.agentUid)
        Assert.assertEquals("mailto:sally@example.com", agent.agentMbox)

        Assert.assertEquals("joined to verb", entity.statementVerbUid, verb!!.verbUid)
        Assert.assertEquals("joined to object", entity.xObjectUid, xobject!!.xObjectUid)

        Assert.assertEquals("context statement joined with parent flag", ContextXObjectStatementJoinDaoCommon.CONTEXT_FLAG_PARENT.toLong(), contextJoin!!.contextActivityFlag.toLong())
        Assert.assertEquals("context statement joined matches with objectuid", parent.xObjectUid, contextJoin.contextXObjectUid)
        Assert.assertEquals("context statement joined matches with statement", entity.statementUid, contextJoin.contextStatementUid)
    }


    @Test
    fun givenStatementWithProgress_whenStored_thenDbAndStatementShouldMatch() {
        val statementStr = this::class.java.getResourceAsStream(statementWithProgress).bufferedReader().use {
            it.readText()
        }

        val entry = ContentEntry().apply {
            entryId = "http://demo.com/"
            contentEntryUid = repo.contentEntryDao.insert(this)
        }

        endpoint.storeStatements(listOf(gson.fromJson(statementStr, Statement::class.java)),
                "", contentEntryUid = entry.contentEntryUid)

        val statementEntity = repo.statementDao.findByStatementId("442f1133-bcd0-42b5-957e-4ad36f9414e0")
        val xObject = repo.xObjectDao.findByXobjectUid(statementEntity!!.xObjectUid)

        Assert.assertEquals("Statement entity has correctly assigned contententryuid",
                entry.contentEntryUid, xObject?.objectContentEntryUid)
        Assert.assertEquals("Statement entity has progress set as per JSON",
                17, statementEntity?.extensionProgress)
        Assert.assertEquals("Statement has preset Verb UID as expected",
                VerbEntity.FIXED_UIDS["http://adlnet.gov/expapi/verbs/progressed"],
                statementEntity?.statementVerbUid)
        Assert.assertEquals("Statement has contentEntryUid set", entry.contentEntryUid,
                statementEntity?.statementContentEntryUid)
        Assert.assertEquals("progress was updated", 17, statementEntity.extensionProgress)
    }

    @Test
    fun givenStatementWithProgress_whenProgressAlreadyAvailable_thenProgressShouldUpdate() {
        val statementStr = this::class.java.getResourceAsStream(statementWithProgress).bufferedReader().use {
            it.readText()
        }

        val entry = ContentEntry().apply {
            entryId = "http://demo.com/"
            contentEntryUid = repo.contentEntryDao.insert(this)
        }


        endpoint.storeStatements(listOf(gson.fromJson(statementStr, Statement::class.java)), "",
                contentEntryUid = entry.contentEntryUid)

        val statementEntity = repo.statementDao.findByStatementId("442f1133-bcd0-42b5-957e-4ad36f9414e0")
        val xObject = repo.xObjectDao.findByXobjectUid(statementEntity!!.xObjectUid)

        Assert.assertEquals("Statement entity has correctly assigned contententryuid",
                entry.contentEntryUid, xObject?.objectContentEntryUid)
        Assert.assertEquals("Statement entity has progress set as per JSON",
                17, statementEntity?.extensionProgress)
        Assert.assertEquals("Statement has preset Verb UID as expected",
                VerbEntity.FIXED_UIDS["http://adlnet.gov/expapi/verbs/progressed"],
                statementEntity?.statementVerbUid)
        Assert.assertEquals("Statement has contentEntryUid set", entry.contentEntryUid,
                statementEntity?.statementContentEntryUid)
        Assert.assertEquals("progress was updated", 17, statementEntity!!.extensionProgress)
    }


    @Test
    fun givenFullValidStatementWithContext_whenParsed_thenDbAndStatementShouldMatch() {
        val tmpFile = temporaryFolder.newFile()
        javaClass.getResourceAsStream(fullstatement).writeToFile(tmpFile)
        val content = String(Files.readAllBytes(Paths.get(tmpFile.absolutePath)))

        val statement = gson.fromJson(content, Statement::class.java)
        endpoint.storeStatements(listOf(statement), "")

        val entity = repo.statementDao.findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee")
        val agent = repo.agentDao.getAgentByAnyId("", "mailto:teampb@example.com", "", "", "")
        val verb = repo.verbDao.findByUrl("http://adlnet.gov/expapi/verbs/attended")
        val xobject = repo.xObjectDao.findByObjectId("http://www.example.com/meetings/occurances/34534")
        val instructor = repo.agentDao.getAgentByAnyId("", "", "13936749", "http://www.example.com", "")
        val authority = repo.agentDao.getAgentByAnyId("", "", "anonymous", "http://cloud.scorm.com/", "")
        val team = repo.agentDao.getAgentByAnyId("", "mailto:teampb@example.com", "", "", "")
        val parent = repo.xObjectDao.findByObjectId("http://www.example.com/meetings/series/267")
        val contextJoin = repo.contextXObjectStatementJoinDao
                .findByStatementAndObjectUid(entity!!.statementUid, parent!!.xObjectUid)

        Assert.assertEquals("joined to agent", agent?.agentUid, entity?.agentUid)
        Assert.assertEquals("mailto:teampb@example.com", agent?.agentMbox)

        Assert.assertEquals("joined to verb", verb?.verbUid, entity?.statementVerbUid)
        Assert.assertEquals("joined to object", xobject?.xObjectUid, entity.xObjectUid)

        Assert.assertEquals("context registration matched", "ec531277-b57b-4c15-8d91-d292c5b2b8f7", entity.contextRegistration)
        Assert.assertEquals("context platform matched", "Example virtual meeting software", entity.contextPlatform)
        Assert.assertEquals("context statement matched", "6690e6c9-3ef0-4ed3-8b37-7f3964730bee", entity.contextStatementId)

        Assert.assertEquals("joined to instructor", instructor!!.agentUid, entity?.instructorUid)
        Assert.assertEquals("13936749", instructor?.agentAccountName)

        Assert.assertEquals("joined to authority", authority?.agentUid, entity?.authorityUid)
        Assert.assertEquals("joined to team", team?.agentUid, entity?.teamUid)

        Assert.assertEquals("context statement joined matches with statement", contextJoin?.contextStatementUid, entity?.statementUid)
        Assert.assertEquals("context statement joined matches with objectuid", parent.xObjectUid, contextJoin?.contextXObjectUid)
        Assert.assertEquals("context statement joined with parent flag",
            ContextXObjectStatementJoinDaoCommon.CONTEXT_FLAG_PARENT.toLong(), contextJoin?.contextActivityFlag?.toLong())


        Assert.assertEquals("result success matched", RESULT_SUCCESS, entity.resultSuccess)
        Assert.assertTrue("result completion matched", entity.resultCompletion)
        Assert.assertEquals("result response matched", "We agreed on some example actions.", entity.resultResponse)
        Assert.assertEquals("result duration matched", parse8601Duration("PT1H0M0S"),
                entity.resultDuration)

    }


    @Test
    @Throws(IOException::class)
    fun givenValidStatementWithSubStatement_whenParsed_thenDbAndStatementShouldMatch() {

        val statement = gson.fromJson(javaClass.getResourceAsStream(subStatement).readString(), Statement::class.java)
        endpoint.storeStatements(listOf(statement), "")

        val entity = repo.statementDao.findByStatementId("fd41c918-b88b-4b20-a0a5-a4c32391aaa0")
        val agent = repo.agentDao.getAgentByAnyId("", "mailto:test@example.com", "", "", "")
        val verb = repo.verbDao.findByUrl("http://example.com/planned")
        val subActor = repo.agentDao.getAgentByAnyId("", "mailto:test@example.com", "", "", "")
        val subVerb = repo.verbDao.findByUrl("http://example.com/visited")
        val subobject = repo.xObjectDao.findByObjectId("http://example.com/website")

        Assert.assertEquals("joined to agent", agent?.agentUid, entity?.agentUid)
        Assert.assertEquals("mailto:test@example.com", agent?.agentMbox)

        Assert.assertEquals("joined to verb", verb?.verbUid, entity?.statementVerbUid)

        Assert.assertEquals("joined to substatement actor", subActor?.agentUid, entity?.subStatementActorUid)
        Assert.assertEquals("mailto:test@example.com", subActor?.agentMbox)

        Assert.assertEquals("joined to substatement verb", subVerb?.verbUid, entity?.substatementVerbUid)
        Assert.assertEquals("joined to substatement object", subobject?.xObjectUid, entity?.subStatementObjectUid)
        Assert.assertEquals("with substatment, object should be null", 0L, entity?.xObjectUid)




    }


    @Test
    fun givenValidStatementWithLearnerGroup_whenParsed_thenDbAndStatementShouldMatch(){

        val entry = ContentEntry().apply {
            entryId = "http://demo.com/"
            contentEntryUid = repo.contentEntryDao.insert(this)
        }

        val learnerGroup = LearnerGroup().apply{
            learnerGroupUid = 1
            repo.learnerGroupDao.insert(this)
        }

        val statement = gson.fromJson(javaClass.getResourceAsStream(statementWithLearnerGroup).readString(), Statement::class.java)
        endpoint.storeStatements(listOf(statement), "", entry.contentEntryUid)

        val entity = repo.statementDao.findByStatementId("442f1133-bcd0-42b5-957e-4ad36f9414e0")
        val agent = repo.agentDao.getAgentByAnyId("", "", "group:1", "http://localhost/", "")

        Assert.assertEquals("group registered as accountName ","group:1", agent?.agentAccountName)
        Assert.assertEquals("entity has learnerGroupUid", learnerGroup.learnerGroupUid, entity!!.statementLearnerGroupUid)
        Assert.assertEquals("Statement entity has progress set as per JSON",
                17, entity?.extensionProgress)
        Assert.assertEquals("Statement has preset Verb UID as expected",
                VerbEntity.FIXED_UIDS["http://adlnet.gov/expapi/verbs/progressed"],
                entity?.statementVerbUid)
        Assert.assertEquals("Statement has contentEntryUid set", entry.contentEntryUid,
                entity?.statementContentEntryUid)

    }


    @Test
    fun givenAgentEntity_daoReturnsTheCorrectAgent() {
        val agentDao = repo.agentDao
        val agentEntity = AgentEntity()
        agentEntity.agentMbox = "samih@ustadmobile.com"
        agentEntity.agentOpenid = null
        agentEntity.agentUid = agentDao.insert(agentEntity)

        val secondAgent = AgentEntity()
        secondAgent.agentMbox = null
        secondAgent.agentOpenid = "mike@ustadmobile.com"
        secondAgent.agentUid = agentDao.insert(secondAgent)

        val thirdAgent = AgentEntity()
        thirdAgent.agentMbox = "sd@ustad.com"
        thirdAgent.agentOpenid = null
        thirdAgent.agentUid = agentDao.insert(thirdAgent)

        val entity = agentDao.getAgentByAnyId(
                agentEntity.agentOpenid,
                agentEntity.agentMbox,
                agentEntity.agentAccountName,
                agentEntity.agentHomePage,
                agentEntity.agentMbox_sha1sum)

        Assert.assertEquals("not same mbox", agentEntity.agentMbox, entity?.agentMbox)
    }


}
