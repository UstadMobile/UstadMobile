package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.UmAppDatabase
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.Test
import com.ustadmobile.core.impl.di.commonJvmDiModule
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.ParentalConsentManagementView
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.*
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.serialization.json.Json
import org.mockito.kotlin.*
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.rest.ext.insertDefaultSite
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.content.*
import io.ktor.serialization.gson.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.response.*
import org.slf4j.event.Level


class PersonAuthRegisterRouteTest {

    private lateinit var mockNotificationSender: NotificationSender

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var serverDi: DI

    private lateinit var endpointScope: EndpointScope

    @Before
    fun setup() {
        endpointScope = EndpointScope()
        mockNotificationSender = mock { }
        Napier.takeLogarithm()
        Napier.base(DebugAntilog())

        serverDi = DI {
            import(commonJvmDiModule)

            import(commonTestKtorDiModule(endpointScope, temporaryFolder))

            bind<NotificationSender>() with singleton {
                mockNotificationSender
            }

            onReady {
                val repo: UmAppDatabase by di.on(Endpoint("localhost")).instance(tag = DoorTag.TAG_REPO)
                repo.insertDefaultSite()
            }

            registerContextTranslator { _: ApplicationCall ->
                Endpoint("localhost")
            }
        }
    }

    private fun testPersonAuthRegisterApplication(
        block: ApplicationTestBuilder.(httpClient: HttpClient) -> Unit
    ) {
        testApplication {
            environment {
                config = MapApplicationConfig("ktor.environment" to "test")
            }

            val client = createClient {
                install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                    gson()
                }
            }

            application {
                install(ContentNegotiation) {
                    gson()
                }
                install(CallLogging) {
                    level = Level.DEBUG
                }

                di {
                    extend(serverDi)
                }

                routing {
                    personAuthRegisterRoute()
                }
            }

            block(client)
        }
    }

    @Test
    fun givenRegisterRequestFromMinor_whenRegisterCalled_thenShouldSendEmailAndReply(

    ) = testPersonAuthRegisterApplication { client ->
        val registerPerson = PersonWithAccount().apply {
            this.newPassword = "test"
            firstNames = "Bob"
            lastName = "Jones"
            username = "bobjones"
            dateOfBirth = systemTimeInMillis() - 1000
        }
        val registerParent = PersonParentJoin().apply {
            this.ppjEmail = "parent@email.com"
        }

        runBlocking {
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(
                    person = registerPerson,
                    parent = registerParent,
                    endpointUrl = "https://org.ustadmobile.app/"
                ))
            }
        }

        verifyBlocking(mockNotificationSender) {
            sendEmail(eq("parent@email.com"), any(), argWhere {
                it.contains("https://org.ustadmobile.app/umapp/#/${ParentalConsentManagementView.VIEW_NAME}")
            })
        }
    }

    @Test
    fun givenRegisterPersonWithAuth_whenRegisterCalled_thenShouldGenerateAuth(

    ) = testPersonAuthRegisterApplication { client ->
        val registerPerson = PersonWithAccount().apply {
            this.newPassword = "test"
            firstNames = "Bob"
            lastName = "Jones"
            username = "bobjones"
            dateOfBirth = systemTimeInMillis() - (20 * 365 * 24 * 60 * 60 * 1000L) //approx 20 years
            newPassword = "secret23"
            confirmedPassword = "secret23"
        }

        val httpResponse = runBlocking {
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(
                    person = registerPerson,
                    parent = null,
                    endpointUrl = "https://org.ustadmobile.app/"
                ))
            }
        }

        val createdAccount: PersonWithAccount = runBlocking { httpResponse.body() }


        val pbkdf2Params: Pbkdf2Params = serverDi.direct.instance()

        runBlocking {
            val db: UmAppDatabase = serverDi.direct.on(Endpoint("localhost")).instance(tag = DoorTag.TAG_DB)
            val personAuth2 = db.personAuth2Dao.findByPersonUid(createdAccount.personUid)
            val salt = db.siteDao.getSite()?.authSalt ?: throw IllegalStateException("No auth salt!")
            Assert.assertEquals("PersonAuth2 created with valid hashed password",
                "secret23".doublePbkdf2Hash(salt, pbkdf2Params).encodeBase64(),
                personAuth2?.pauthAuth)
        }
    }

    @Test
    fun givenValidCredentials_whenLoginCalled_thenShouldReturnAccount(

    )  = testPersonAuthRegisterApplication { client ->
        val repo: UmAppDatabase by serverDi.on(Endpoint("localhost")).instance(tag= DoorTag.TAG_REPO)
        val pbkdf2Params: Pbkdf2Params by serverDi.instance()

        val person = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                username = "mary"
                dateOfBirth = systemTimeInMillis() - (20 * 365 * 24 * 60 * 60 * 1000L)
            })
        }

        runBlocking {
            repo.insertPersonAuthCredentials2(person.personUid, "secret23", pbkdf2Params)
        }

        val httpResponse = runBlocking {
            client.post("/auth/login?username=mary&password=secret23") {
                header("X-nid", "123")
            }
        }

        Assert.assertEquals("Response is 200 OK", HttpStatusCode.OK, httpResponse.status)
        val umAccount: UmAccount = runBlocking { httpResponse.body() }

        Assert.assertEquals("Received expected account object",
            "mary", umAccount.username)
    }



    @Test
    fun givenInvalidCredentials_whenLoginCalled_thenShouldRespondForbidden(

    )  = testPersonAuthRegisterApplication { client ->
        val repo: UmAppDatabase by serverDi.on(Endpoint("localhost")).instance(tag= DoorTag.TAG_REPO)
        val pbkdf2Params: Pbkdf2Params by serverDi.instance()

        val person = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                username = "mary"
                dateOfBirth = systemTimeInMillis() - (20 * 365 * 24 * 60 * 60 * 1000L)
            })
        }

        runBlocking {
            repo.insertPersonAuthCredentials2(person.personUid, "secret23", pbkdf2Params)
        }

        val httpResponse = runBlocking {
            client.post("/auth/login?username=mary&password=wrong") {
                header("X-nid", "123")
            }
        }
        Assert.assertEquals("Resposne is Forbidden", HttpStatusCode.Forbidden,
            httpResponse.status)
    }

    @Test
    fun givenParentalConsentIsRequiredButNotGranted_whenLoginCalled_thenShouldRespondFailedDepdency(

    ) = testPersonAuthRegisterApplication {
        val repo: UmAppDatabase by serverDi.on(Endpoint("localhost")).instance(tag= DoorTag.TAG_REPO)
        val pbkdf2Params: Pbkdf2Params by serverDi.instance()

        val person = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                username = "mary"
                dateOfBirth = systemTimeInMillis() - (5 * 365 * 24 * 60 * 60 * 1000L)
            })
        }

        runBlocking {
            repo.insertPersonAuthCredentials2(person.personUid, "secret23", pbkdf2Params)
            repo.personParentJoinDao.insertAsync(PersonParentJoin().apply {
                ppjMinorPersonUid = person.personUid
                ppjStatus = PersonParentJoin.STATUS_UNSET
            })
        }

        val response = runBlocking {
            client.post("/auth/login?username=mary&password=secret23") {
                header("X-nid", "123")
            }
        }

        Assert.assertEquals("Response is Precondition Failed",
            HttpStatusCode.FailedDependency, response.status)
    }

}