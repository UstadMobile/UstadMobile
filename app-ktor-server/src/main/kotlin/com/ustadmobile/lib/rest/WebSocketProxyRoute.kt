package com.ustadmobile.lib.rest

import com.ustadmobile.lib.rest.ext.resolveProxyToUrl
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

/**
 * This is a basic websocket proxy to handle webpack's /websocket endpoint
 */
fun Route.webSocketProxyRoute(proxyToBaseUrl: String) {
    val di: DI by closestDI()
    val httpClient: HttpClient by di.instance()

    webSocket("/websocket") {
        val serverSession = this

        //create a websocket connection to the proxy dest
        try {
            httpClient.webSocket(request = {
                //Add all headers
                call.request.headers.forEach { headerName, headerValues ->
                    headerValues.forEach { headerValue ->
                        this.header(headerName, headerValue)
                    }
                }
                this.method = call.request.httpMethod
                url(call.resolveProxyToUrl(proxyToBaseUrl))
            }) {
                val clientSession = this
                //Read from server and send to client
                launch {
                    while(isActive) {
                        val frame = serverSession.incoming.receive()
                        clientSession.outgoing.send(frame)
                    }
                }

                //Read from client and send to server
                launch {
                    while(isActive) {
                        val frame = clientSession.incoming.receive()
                        serverSession.outgoing.send(frame)
                    }
                }
            }
        }finally {

        }

    }
}