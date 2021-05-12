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
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.di.commonJvmDiModule
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.appendQueryArgs
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

class PersonAuthRegisterRouteTest {

    lateinit var mockNotificationSender: NotificationSender

    fun <R> withTestRegister(testFn: TestApplicationEngine.() -> R) {
        val endpointScope = EndpointScope()
        mockNotificationSender = mock { }
        withTestApplication({
            install(DIFeature) {
                import(commonJvmDiModule)

                bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                    UmAppDatabase.getInstance(Any()).also {
                        it.clearAllTables()
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
                    UstadMobileSystemImpl(instance(tag = DiTag.XPP_FACTORY_NSAWARE))
                }

                bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
                    val db = instance<UmAppDatabase>(tag = DoorTag.TAG_DB)
                    db.asRepository(
                        RepositoryConfig.repositoryConfig(
                            Any(), "http://localhost/",
                            instance(), instance()))
                }

                registerContextTranslator { call: ApplicationCall ->
                    Endpoint("localhost")
                }
            }

            routing {
                PersonAuthRegisterRoute()
            }
        }, testFn)
    }

    @Test
    fun givenRegisterRequestFromMinor_whenGetCalled_thenShouldSendEmailAndReply() = withTestRegister{
        val registerPerson = PersonWithAccount().apply {
            this.newPassword = "test"
            firstNames = "Bob"
            lastName = "Jones"
            dateOfBirth = systemTimeInMillis() - 1000
        }
        val registerParent = PersonParentJoin().apply {
            this.ppjEmail = "parent@email.com"
        }

        val registerArgs = mapOf(
            "person" to Json.encodeToString(PersonWithAccount.serializer(), registerPerson),
            "parent" to Json.encodeToString(PersonParentJoin.serializer(), registerParent),
            "endpoint" to "https://org.ustadmobile.app/")

        handleRequest(HttpMethod.Post, "/auth/register".appendQueryArgs(registerArgs.toQueryString())) {

        }.apply {
            //val response = response.content!!
            verifyBlocking(mockNotificationSender) {
                sendEmail(eq("parent@email.com"), any(), argWhere {
                    it.contains("https://org.ustadmobile.app/umapp/#${ParentalConsentManagementView.VIEW_NAME}")
                })
            }
        }
    }
}