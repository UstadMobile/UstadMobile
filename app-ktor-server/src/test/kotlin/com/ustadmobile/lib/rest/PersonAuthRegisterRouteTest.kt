package com.ustadmobile.lib.rest

import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.Test
import com.ustadmobile.core.impl.di.CommonJvmDiModule
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.viewmodel.parentalconsentmanagement.ParentalConsentManagementViewModel
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.*
import com.ustadmobile.door.util.systemTimeInMillis
import org.mockito.kotlin.*
import com.ustadmobile.lib.db.entities.*
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.serialization.gson.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.ktor.di
import io.ktor.server.config.*
import io.ktor.server.plugins.callloging.*
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
            import(CommonJvmDiModule)

            import(commonTestKtorDiModule(endpointScope))

            bind<NotificationSender>() with singleton {
                mockNotificationSender
            }

            bind<AddNewPersonUseCase>() with scoped(endpointScope).singleton {
                AddNewPersonUseCase(
                    db = instance(tag = DoorTag.TAG_DB),
                    repo = null,
                )
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

    //Disabled until consent screens are brought back
    @Suppress("unused")
    //@Test
    fun givenRegisterRequestFromMinor_whenRegisterCalled_thenShouldSendEmailAndReply(

    ) = testPersonAuthRegisterApplication { client ->
        val registerPerson = Person().apply {
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
                    newPassword = "test",
                    parent = registerParent,
                    endpointUrl = "https://org.ustadmobile.app/"
                ))
            }
        }

        verifyBlocking(mockNotificationSender) {
            sendEmail(eq("parent@email.com"), any(), argWhere {
                it.contains("https://org.ustadmobile.app/umapp/#/${ParentalConsentManagementViewModel.DEST_NAME}")
            })
        }
    }

    @Test
    fun givenRegisterPersonWithAuth_whenRegisterCalled_thenShouldGenerateAuth(

    ) = testPersonAuthRegisterApplication { client ->
        val registerPerson = Person().apply {
            firstNames = "Bob"
            lastName = "Jones"
            username = "bobjones"
            dateOfBirth = systemTimeInMillis() - (20 * 365 * 24 * 60 * 60 * 1000L) //approx 20 years
        }

        val httpResponse = runBlocking {
            client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(
                    person = registerPerson,
                    newPassword = "secret23",
                    parent = null,
                    endpointUrl = "https://org.ustadmobile.app/"
                ))
            }
        }

        val createdAccount: Person = runBlocking { httpResponse.body() }


        val pbkdf2Params: Pbkdf2Params = serverDi.direct.instance()

        runBlocking {
            val db: UmAppDatabase = serverDi.direct.on(Endpoint("localhost")).instance(tag = DoorTag.TAG_DB)
            val personAuth2 = db.personAuth2Dao().findByPersonUid(createdAccount.personUid)
            val salt = db.siteDao().getSite()?.authSalt ?: throw IllegalStateException("No auth salt!")
            Assert.assertEquals("PersonAuth2 created with valid hashed password",
                "secret23".doubleEncryptWithPbkdf2V2(salt, pbkdf2Params.iterations, pbkdf2Params.keyLength)
                    .encodeBase64(),
                personAuth2?.pauthAuth)
        }
    }

    @Test
    fun givenValidCredentials_whenLoginCalled_thenShouldReturnAccount(

    )  = testPersonAuthRegisterApplication { client ->
        val db: UmAppDatabase by serverDi.on(Endpoint("localhost")).instance(tag= DoorTag.TAG_DB)
        val pbkdf2Params: Pbkdf2Params by serverDi.instance()
        val httpClient: HttpClient by serverDi.instance()

        val person = runBlocking {
            db.insertPersonAndGroup(Person().apply {
                username = "mary"
                dateOfBirth = systemTimeInMillis() - (20 * 365 * 24 * 60 * 60 * 1000L)
            })
        }

        runBlocking {
            val salt = db.siteDao().getSiteAuthSaltAsync()!!
            db.personAuth2Dao().insertAsync(
                PersonAuth2().apply {
                    pauthUid = person.personUid
                    pauthMechanism = PersonAuth2.AUTH_MECH_PBKDF2_DOUBLE
                    pauthAuth = "secret23".doubleEncryptWithPbkdf2V2(
                        salt, pbkdf2Params.iterations, pbkdf2Params.keyLength
                    ).encodeBase64()
                }
            )
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
        val db: UmAppDatabase by serverDi.on(Endpoint("localhost")).instance(tag= DoorTag.TAG_DB)
        val pbkdf2Params: Pbkdf2Params by serverDi.instance()
        val httpClient: HttpClient by serverDi.instance()

        val person = runBlocking {
            db.insertPersonAndGroup(Person().apply {
                username = "mary"
                dateOfBirth = systemTimeInMillis() - (20 * 365 * 24 * 60 * 60 * 1000L)
            })
        }

        runBlocking {
            val salt = db.siteDao().getSiteAuthSaltAsync()!!
            db.personAuth2Dao().insertAsync(
                PersonAuth2().apply {
                    pauthUid = person.personUid
                    pauthMechanism = PersonAuth2.AUTH_MECH_PBKDF2_DOUBLE
                    pauthAuth = "secret23".doubleEncryptWithPbkdf2V2(
                        salt, pbkdf2Params.iterations, pbkdf2Params.keyLength
                    ).encodeBase64()
                }
            )
        }

        val httpResponse = runBlocking {
            client.post("/auth/login?username=mary&password=wrong") {
                header("X-nid", "123")
            }
        }
        Assert.assertEquals("Resposne is Forbidden", HttpStatusCode.Forbidden,
            httpResponse.status)
    }

    //Disabled until consent screens are re-introduced
    @Suppress("unused")
    //@Test
    fun givenParentalConsentIsRequiredButNotGranted_whenLoginCalled_thenShouldRespondFailedDepdency(

    ) = testPersonAuthRegisterApplication {
        val db: UmAppDatabase by serverDi.on(Endpoint("localhost")).instance(tag= DoorTag.TAG_DB)
        val pbkdf2Params: Pbkdf2Params by serverDi.instance()
        val httpClient: HttpClient by serverDi.instance()

        val person = runBlocking {
            db.insertPersonAndGroup(Person().apply {
                username = "mary"
                dateOfBirth = systemTimeInMillis() - (5 * 365 * 24 * 60 * 60 * 1000L)
            })
        }

        runBlocking {
            val salt = db.siteDao().getSiteAuthSaltAsync()!!
            db.personAuth2Dao().insertAsync(
                PersonAuth2().apply {
                    pauthUid = person.personUid
                    pauthMechanism = PersonAuth2.AUTH_MECH_PBKDF2_DOUBLE
                    pauthAuth = "secret23".doubleEncryptWithPbkdf2V2(
                        salt, pbkdf2Params.iterations, pbkdf2Params.keyLength
                    ).encodeBase64()
                }
            )
            db.personParentJoinDao().upsertAsync(PersonParentJoin().apply {
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