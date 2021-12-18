package com.ustadmobile.test.http

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.rest.umRestApplication
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException
import java.net.ServerSocket
import java.util.*
import javax.naming.InitialContext
import com.ustadmobile.util.test.ReverseProxyDispatcher
import io.ktor.server.engine.ApplicationEngineEnvironment
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.net.InetAddress
import kotlin.random.Random.Default.nextInt
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.core.db.ext.addSyncCallback
import kotlin.random.Random.Default.nextLong

val umRestServerInstances = mutableMapOf<Int, TestApplicationHolder>()

fun findFreePort(hostName: String, minPortNum: Int, maxPortNum: Int): Int {
    for(portNum in minPortNum until maxPortNum) {
        if(umRestServerInstances.containsKey(portNum))
            continue

        try {
            ServerSocket(portNum, 50, InetAddress.getByName(hostName)).also {
                it.close()
            }

            return portNum
        }catch(ioe: IOException) {
            //not available...
        }
    }

    return -1
}

data class TestApplicationHolder(var proxyPort: Int, var appPort: Int, var token: String,
                                 var db: UmAppDatabase,
                                 var application: ApplicationEngine,
                                 var mockWebServer: MockWebServer,
                                 var reverseProxy: ReverseProxyDispatcher)

fun Application.testServerManager() {
    var proxyHost = "localhost"
    val env = this.environment
    if(env is ApplicationEngineEnvironment) {
        proxyHost = env.connectors[0].host
    }

    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Options)
        header(HttpHeaders.ContentType)
        anyHost()
    }

    install(CallLogging)

    install(ContentNegotiation) {
        gson {
            register(ContentType.Application.Json, GsonConverter())
            register(ContentType.Any, GsonConverter())
        }
    }

    install(Routing) {
        get("/servers/newServer") {
            val appPort = findFreePort(proxyHost,8000, 9000)
            val proxyPort = findFreePort(proxyHost,appPort + 1, 9000)

            val initialContext = InitialContext()

            val dbName = "testserver-$appPort"
            val nodeIdAndAuth = NodeIdAndAuth(nextLong(), randomUuid().toString())
            initialContext.bindNewSqliteDataSourceIfNotExisting(dbName, isPrimary = true)

            val umDb = DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, "UmAppDatabase")
                .addSyncCallback(nodeIdAndAuth)
                .build()
                .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)

            val umRestServer = embeddedServer(Netty, port = appPort, host= proxyHost, module = {
                umRestApplication(devMode = true, singletonDbName = dbName)
            })
            umRestServer.start()
            val token = UUID.randomUUID().toString()

            val mockDispatcher = ReverseProxyDispatcher("http://$proxyHost:$appPort/".toHttpUrl())
            val mockWebServer = MockWebServer()
            mockWebServer.dispatcher = mockDispatcher
            mockWebServer.start(InetAddress.getByName(proxyHost), proxyPort)


            umRestServerInstances[proxyPort] = TestApplicationHolder(proxyPort, appPort, token, umDb,
                    umRestServer, mockWebServer, mockDispatcher)

            call.respond(HttpStatusCode.OK, Pair(proxyPort, token))
        }

        get("/servers/throttle/{serverPort}") {
            val portNum = call.parameters["serverPort"]?.toInt() ?: -1
            val testApplicationHolder = umRestServerInstances[portNum]
            if(testApplicationHolder != null) {
                val bytesPerPeriod = call.request.queryParameters["bytesPerPeriod"]?.toLong() ?: 0L
                val periodDuration = call.request.queryParameters["periodDuration"]?.toLong() ?: 0L
                testApplicationHolder.reverseProxy.throttleBytesPerPeriod = bytesPerPeriod
                testApplicationHolder.reverseProxy.throttlePeriod = periodDuration
                call.respond("OK - Throttle = $bytesPerPeriod bytes each $periodDuration ms")
            }else {
                call.respond(HttpStatusCode.NotFound, "No such server")
            }
        }

        get("/servers/setNumDisconnects/{serverPort}") {
            val portNum = call.parameters["serverPort"]?.toInt() ?: -1
            val testApplicationHolder = umRestServerInstances[portNum]
            if(testApplicationHolder != null) {
                val numDisconnects = call.request.queryParameters["numDisconnects"]?.toInt() ?: 0
                testApplicationHolder.reverseProxy.numTimesToFail.set(numDisconnects)
                call.respond("OK - will disconnect $numDisconnects times")
            }else {
                call.respond(HttpStatusCode.NotFound, "No such server")
            }
        }

        get("/servers/reset/{serverPort}") {
            val portNum = call.parameters["serverPort"]?.toInt() ?: -1
            val testApplicationHolder = umRestServerInstances[portNum]
            if(testApplicationHolder != null) {
                testApplicationHolder.reverseProxy.apply {
                    numTimesToFail.set(0)
                    throttleBytesPerPeriod = 0L
                    throttlePeriod = 1000L
                }
                testApplicationHolder.db.clearAllTables()
                call.respond(HttpStatusCode.OK, "OK - reset")
            }else {
                call.respond(HttpStatusCode.NotFound, "No such server")
            }
        }


        get("/servers/close/{serverPort}") {
            val portNum = call.parameters["serverPort"]?.toInt() ?: -1
            val testApplicationHolder = umRestServerInstances[portNum]
            if(testApplicationHolder != null) {
                //shut it down
                testApplicationHolder.mockWebServer.close()
                testApplicationHolder.application.stop(0, 2000)
                umRestServerInstances.remove(portNum)
                call.respond(HttpStatusCode.OK, "OK - closed")
            }else {
                call.respond(HttpStatusCode.NotFound, "No such server")
            }
        }
    }
}
