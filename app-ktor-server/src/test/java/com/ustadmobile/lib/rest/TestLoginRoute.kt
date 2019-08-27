package com.ustadmobile.lib.rest

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth
import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.takeFrom
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class TestLoginRoute {
    lateinit var server: ApplicationEngine

    lateinit var db: UmAppDatabase

    lateinit var httpClient: HttpClient

    @Before
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(Any() ,UmAppDatabase::class, "UmAppDatabase").build()
        db.clearAllTables()
        server = embeddedServer(Netty, port = 8097) {
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }

            install(Routing) {
                LoginRoute(db)
            }
        }.start(wait = false)

        httpClient = HttpClient(){
            install(JsonFeature)
        }
    }

    @After
    fun tearDown() {
        server.stop(0, 5, TimeUnit.SECONDS)
        httpClient.close()
    }

    @Test
    fun givenValidUsernameAndPassword_whenLoginCalled_thenShouldReturnAccountObject() {
        val person = Person("bobjones", "Bob", "Jones")
        person.personUid = db.personDao.insert(person)
        val personAuth = PersonAuth(person.personUid, PersonAuthDao.PLAIN_PASS_PREFIX+"secret")
        db.personAuthDao.insert(personAuth)

        runBlocking {
            val authResponse = httpClient.get<HttpResponse> {
                url{
                    takeFrom("http://localhost:8097")
                    path("Login", "login")
                    parameter("username", "bobjones")
                    parameter("password", "secret")
                }
            }

            val accountObj = authResponse.receive<UmAccount>()

            Assert.assertEquals("With valid login, response code is 200",
                    HttpStatusCode.OK, authResponse.status)
            Assert.assertEquals(accountObj.personUid, person.personUid)
        }
    }

    @Test
    fun givenInvalidUsernameAndPassword_whenLoginCalled_thenShouldReturn403Forbidden() {
        val person = Person("bobjones", "Bob", "Jones")
        person.personUid = db.personDao.insert(person)
        val personAuth = PersonAuth(person.personUid, PersonAuthDao.PLAIN_PASS_PREFIX+"secret")
        db.personAuthDao.insert(personAuth)

        runBlocking {
            val authResponse = httpClient.get<HttpResponse> {
                url {
                    takeFrom("http://localhost:8097")
                    path("Login", "login")
                    parameter("username", "bobjones")
                    parameter("password", "wrongsecret")
                }
            }

            Assert.assertEquals("With invalid login, response code is 403",
                    HttpStatusCode.Forbidden, authResponse.status)
        }
    }

    @Test
    fun givenRequestWithNoUsernameOrPassword_whenLoginCalled_thenShouldReturn400BadRequest() {
        runBlocking {
            val authResponse = httpClient.get<HttpResponse> {
                url {
                    takeFrom("http://localhost:8097")
                    path("Login", "login")
                }
            }

            Assert.assertEquals("With invalid login, response code is 403",
                    HttpStatusCode.BadRequest, authResponse.status)
        }
    }
}