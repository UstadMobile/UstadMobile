package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.account.*
import com.ustadmobile.core.db.UmAppDatabase
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.Test
import org.kodein.di.ktor.DIFeature
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.di.commonJvmDiModule
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.ParentalConsentManagementView
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.*
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.serialization.json.Json
import org.mockito.kotlin.*
import org.xmlpull.v1.XmlPullParserFactory
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.ext.toHexString
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.rest.ext.insertDefaultSite
import io.ktor.features.*
import io.ktor.gson.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.ktor.closestDI
import kotlin.random.Random

class PersonAuthRegisterRouteTest {

    private lateinit var mockNotificationSender: NotificationSender

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private fun <R> withTestRegister(testFn: TestApplicationEngine.() -> R) {
        val endpointScope = EndpointScope()
        mockNotificationSender = mock { }
        withTestApplication({
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }

            install(DIFeature) {
                import(commonJvmDiModule)

                bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
                    NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
                }

                bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                    val nodeIdAndAuth : NodeIdAndAuth = instance()
                    DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, "UmAppDatabase")
                        .build().also {
                            it.clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
                        }
                }

                bind<NotificationSender>() with singleton {
                    mockNotificationSender
                }

                bind<Gson>() with singleton {
                    Gson()
                }

                bind<XmlPullParserFactory>(tag  = DiTag.XPP_FACTORY_NSAWARE) with singleton {
                    XmlPullParserFactory.newInstance().also {
                        it.isNamespaceAware = true
                    }
                }

                bind<UstadMobileSystemImpl>() with singleton {
                    UstadMobileSystemImpl(instance(tag = DiTag.XPP_FACTORY_NSAWARE),
                        temporaryFolder.newFolder())
                }

                bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
                    val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
                    val nodeIdAndAuth: NodeIdAndAuth = instance()
                    db.asRepository(
                        RepositoryConfig.repositoryConfig(
                            Any(), "http://localhost/", nodeIdAndAuth.nodeId,
                            nodeIdAndAuth.auth, instance(), instance()))
                }

                bind<Pbkdf2Params>() with singleton {
                    Pbkdf2Params()
                }

                bind<AuthManager>() with scoped(endpointScope).singleton {
                    AuthManager(context, di)
                }

                registerContextTranslator { _: ApplicationCall ->
                    Endpoint("localhost")
                }
            }

            routing {
                personAuthRegisterRoute()
            }
        }) {
            val di: DI by closestDI { this.application }
            val repo: UmAppDatabase by di.on(Endpoint("localhost")).instance(tag = DoorTag.TAG_REPO)
            repo.insertDefaultSite()

            testFn()
        }
    }

    @Test
    fun givenRegisterRequestFromMinor_whenRegisterCalled_thenShouldSendEmailAndReply() = withTestRegister {
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

        handleRequest(HttpMethod.Post, "/auth/register") {
            setBody(Json.encodeToString(RegisterRequest.serializer(), RegisterRequest(
                person = registerPerson,
                parent = registerParent,
                endpointUrl = "https://org.ustadmobile.app/"
            )))
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }.apply {
            verifyBlocking(mockNotificationSender) {
                sendEmail(eq("parent@email.com"), any(), argWhere {
                    it.contains("https://org.ustadmobile.app/umapp/index.html#${ParentalConsentManagementView.VIEW_NAME}")
                })
            }
        }
    }

    @Test
    fun givenRegisterPersonWithAuth_whenRegisterCalled_thenShouldGenerateAuth() = withTestRegister {
        val registerPerson = PersonWithAccount().apply {
            this.newPassword = "test"
            firstNames = "Bob"
            lastName = "Jones"
            username = "bobjones"
            dateOfBirth = systemTimeInMillis() - (20 * 365 * 24 * 60 * 60 * 1000L) //approx 20 years
            newPassword = "secret23"
            confirmedPassword = "secret23"
        }

        handleRequest(HttpMethod.Post, "/auth/register") {
            setBody(Json.encodeToString(RegisterRequest.serializer(), RegisterRequest(
                person = registerPerson,
                parent = null,
                endpointUrl = "https://org.ustadmobile.app/"
            )))

        }.apply {
            val response = response.content!!
            val createdAccount : Person = Json.decodeFromString(PersonWithAccount.serializer(), response)
            val di: DI by closestDI()

            val pbkdf2Params: Pbkdf2Params = di.direct.instance()

            runBlocking {
                val db: UmAppDatabase = di.direct.on(Endpoint("localhost")).instance(tag = DoorTag.TAG_DB)
                val personAuth2 = db.personAuth2Dao.findByPersonUid(createdAccount.personUid)
                val salt = db.siteDao.getSite()?.authSalt ?: throw IllegalStateException("No auth salt!")
                Assert.assertEquals("PersonAuth2 created with valid hashed password",
                    "secret23".doublePbkdf2Hash(salt, pbkdf2Params).encodeBase64(),
                    personAuth2?.pauthAuth)
            }
        }
    }

    @Test
    fun givenValidCredentials_whenLoginCalled_thenShouldReturnAccount()  = withTestRegister {
        val di: DI by closestDI { application }
        val repo: UmAppDatabase by di.on(Endpoint("localhost")).instance(tag= DoorTag.TAG_REPO)
        val pbkdf2Params: Pbkdf2Params by di.instance()

        val person = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                username = "mary"
                dateOfBirth = systemTimeInMillis() - (20 * 365 * 24 * 60 * 60 * 1000L)
            })
        }

        runBlocking {
            repo.insertPersonAuthCredentials2(person.personUid, "secret23", pbkdf2Params)
        }

        handleRequest(HttpMethod.Post, "/auth/login?username=mary&password=secret23") {
            addHeader("X-nid", "123")
        }.apply {
            Assert.assertEquals("Response is 200 OK", HttpStatusCode.OK, response.status())
            val bodyStr = this.response.content!!
            val umAccount = Gson().fromJson(bodyStr, UmAccount::class.java)
            Assert.assertEquals("Received expected account object",
                "mary", umAccount.username)
        }
    }

    @Test
    fun givenInvalidCredentials_whenLoginCalled_thenShouldRespondForbidden()  = withTestRegister {
        val di: DI by closestDI { application }
        val repo: UmAppDatabase by di.on(Endpoint("localhost")).instance(tag= DoorTag.TAG_REPO)
        val pbkdf2Params: Pbkdf2Params by di.instance()

        val person = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                username = "mary"
                dateOfBirth = systemTimeInMillis() - (20 * 365 * 24 * 60 * 60 * 1000L)
            })
        }

        runBlocking {
            repo.insertPersonAuthCredentials2(person.personUid, "secret23", pbkdf2Params)
        }

        handleRequest(HttpMethod.Post, "/auth/login?username=mary&password=wrong") {
            addHeader("X-nid", "123")
        }.apply {
            Assert.assertEquals("Response is Forbidden", HttpStatusCode.Forbidden, response.status())
        }
    }

    @Test
    fun givenParentalConsentIsRequiredButNotGranted_whenLoginCalled_thenShouldRespondFailedDepdency() = withTestRegister {
        val di: DI by closestDI { application }
        val repo: UmAppDatabase by di.on(Endpoint("localhost")).instance(tag= DoorTag.TAG_REPO)
        val pbkdf2Params: Pbkdf2Params by di.instance()

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

        handleRequest(HttpMethod.Post, "/auth/login?username=mary&password=secret23") {
            addHeader("X-nid", "123")
        }.apply {
            Assert.assertEquals("Response is Precondition Failed",
                HttpStatusCode.FailedDependency, response.status())
        }
    }

}