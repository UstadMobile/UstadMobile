package com.ustadmobile.lib.rest

import com.google.gson.Gson
import com.ustadmobile.core.db.UmAppDatabase
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import org.junit.Test
import org.kodein.di.ktor.DIFeature
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.Pbkdf2Params
import com.ustadmobile.core.account.RegisterRequest
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.di.commonJvmDiModule
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.encryptWithPbkdf2
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.view.ParentalConsentManagementView
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.ext.DoorTag
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import org.kodein.di.*
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonWithAccount
import kotlinx.serialization.json.Json
import org.mockito.kotlin.*
import org.xmlpull.v1.XmlPullParserFactory
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.clearAllTablesAndResetSync
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.rest.ext.insertDefaultSite
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.util.*
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
                    NodeIdAndAuth(Random.nextInt(0, Int.MAX_VALUE), randomUuid().toString())
                }

                bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                    val nodeIdAndAuth : NodeIdAndAuth = instance()
                    UmAppDatabase.getInstance(Any(), nodeIdAndAuth, primary = true).also {
                        it.clearAllTablesAndResetSync(nodeIdAndAuth.nodeId, isPrimary = true)
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

                registerContextTranslator { _: ApplicationCall ->
                    Endpoint("localhost")
                }
            }

            routing {
                PersonAuthRegisterRoute()
            }
        }) {
            val di: DI by closestDI { this.application }
            val repo: UmAppDatabase by di.on(Endpoint("localhost")).instance(tag = DoorTag.TAG_REPO)
            repo.insertDefaultSite()

            testFn()
        }
    }

    @Test
    fun givenRegisterRequestFromMinor_whenGetCalled_thenShouldSendEmailAndReply() = withTestRegister {
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
                    it.contains("https://org.ustadmobile.app/umapp/#${ParentalConsentManagementView.VIEW_NAME}")
                })
            }
        }
    }

    @Test
    fun givenRegisterPersonWithAuth_whenGetCalled_thenShouldGenerateAuth() = withTestRegister {
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
            val createdAccount : UmAccount = Json.decodeFromString(UmAccount.serializer(), response)
            val di: DI by closestDI()

            val pbkdf2Params: Pbkdf2Params = di.direct.instance()

            runBlocking {
                val db: UmAppDatabase = di.direct.on(Endpoint("localhost")).instance(tag = DoorTag.TAG_DB)
                val personAuth2 = db.personAuth2Dao.findByPersonUid(createdAccount.personUid)
                val salt = db.siteDao.getSite()?.authSalt ?: throw IllegalStateException("No auth salt!")
                Assert.assertEquals("PersonAuth2 created with valid hashed password",
                    "secret23".encryptWithPbkdf2(salt, pbkdf2Params).encryptWithPbkdf2(salt, pbkdf2Params),
                    personAuth2?.pauthAuth)
            }
        }
    }

}