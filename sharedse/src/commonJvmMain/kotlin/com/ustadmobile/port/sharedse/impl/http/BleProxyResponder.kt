package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.core.util.UMFileUtil
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.io.InputStream
import java.nio.charset.Charset
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.sharedse.network.BleMessage
import com.ustadmobile.sharedse.network.BleMessageResponseListener
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.toUtf8Bytes
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import com.ustadmobile.core.impl.UMLog

@Serializable
data class BleHttpRequest(var method: String, var reqUri: String, var reqHeaders: Map<String, String>,
                          var reqParameters: MutableMap<String, MutableList<String>>,
                          var postBody: String?) : NanoHTTPD.IHTTPSession{

    override fun getRemoteIpAddress() = "localhost"

    override fun getQueryParameterString(): String {
        return reqUri.substringAfter('?')
    }

    override fun getCookies(): NanoHTTPD.CookieHandler {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMethod() = NanoHTTPD.Method.valueOf(method)

    override fun getUri() = reqUri

    override fun getParms() = reqParameters.map { it.key to it.value[0]}.toMap().toMutableMap()

    override fun getRemoteHostName()= "localhost"

    override fun execute() {
        //do nothing
    }

    override fun getHeaders(): MutableMap<String, String> = reqHeaders.toMutableMap()

    override fun getParameters(): MutableMap<String, MutableList<String>> =  reqParameters

    override fun parseBody(files: MutableMap<String, String>) {
        val postBodyVal = postBody
        if(postBodyVal != null)
            files["postData"] = postBodyVal
    }

    override fun getInputStream(): InputStream {
        throw IllegalAccessException("Not implemented here")
    }
}

@Serializable
data class BleHttpResponse(var statusCode: Int, var mimeType: String,
                           var headers: Map<String, String>, var body: String)

fun NanoHTTPD.IHTTPSession.asBleHttpRequest(): BleHttpRequest {
    val postBody = if(this.method == NanoHTTPD.Method.POST) {
        mutableMapOf<String, String?>().also { parseBody(it) }.get("postBody")
    }else {
        null
    }

    return BleHttpRequest(this.method.name, this.uri, this.headers, this.parameters,
            postBody)
}

fun NanoHTTPD.Response.asBleHttpResponse(): BleHttpResponse {
    val respClass = NanoHTTPD.Response::class.java
    val headersField = respClass.getDeclaredField("header")
    headersField.isAccessible = true
    val headers = headersField.get(this) as MutableMap<String, String>
    return BleHttpResponse(this.status.requestStatus, this.mimeType, headers,
            this.data.readBytes().toString(Charset.defaultCharset()))
}

fun BleHttpResponse.asNanoHttpdResponse(): NanoHTTPD.Response {
    val responseStatus = NanoHTTPD.Response.Status.values().first { it.getRequestStatus() == this.statusCode }
    val response = NanoHTTPD.newFixedLengthResponse(responseStatus, this.mimeType, this.body)
    this.headers.forEach {
        response.addHeader(it.key, it.value)
    }

    return response
}

/**
 * This is a NanoHTTPD UriResponder that proxies the request over BLE. The incoming
 */
class BleProxyResponder: RouterNanoHTTPD.UriResponder {
    override fun put(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val networkManager = uriResource.initParameter(0, NetworkManagerBleCommon::class.java)
        val destDeviceAddr  = urlParams?.get("bleaddr")!!.substringBefore('/')
        return runBlocking {
            val bleRequest = session.asBleHttpRequest()
            bleRequest.reqUri = bleRequest.reqUri.substringAfter("/$destDeviceAddr")
            val bleRequestSerialized = Json.stringify(BleHttpRequest.serializer(), bleRequest)
            val messageId = BleMessage.getNextMessageIdForReceiver(destDeviceAddr)
            val bleMessage = BleMessage(BleMessage.MESSAGE_TYPE_HTTP, messageId,
                    bleRequestSerialized.toUtf8Bytes())
            val destDevice = NetworkNode().also { it.bluetoothMacAddress = destDeviceAddr }
            UMLog.l(UMLog.DEBUG, 691,
                    "BLEProxyResponder: Request ID# ${bleMessage.messageId} " +
                            "TO ${destDevice.bluetoothMacAddress} - ${bleRequest.reqUri} ")

            try {
                val messageReceived = withTimeout(20000) {
                    networkManager.sendBleMessage(networkManager.context, bleMessage,
                            destDevice.bluetoothMacAddress!!)
                }
                val payload = messageReceived?.payload
                val payloadStr = if(payload != null) String(payload) else null
                if(payload != null && payloadStr != null) {
                    try {
                        val bleHttpResponse = Json.parse(BleHttpResponse.serializer(), payloadStr)
                        UMLog.l(UMLog.DEBUG, 691,
                                "BLEProxyResponder: Request ID# ${bleMessage.messageId} " +
                                        " RECEIVE Status ${bleHttpResponse.statusCode} - " +
                                        "${bleRequest.reqUri} \n==Content==\n" +
                                        "${bleHttpResponse.body}\n\n")
                        //Thread.sleep(1000)
                        return@runBlocking bleHttpResponse.asNanoHttpdResponse()
                    }catch(e: Exception) {
                        UMLog.l(UMLog.ERROR, 691,
                                "BLEProxyResponder: Exception parsing ID# ${bleMessage.messageId} \n" +
                                        "==Payload received ${payload.size} bytes==\n$payloadStr\n\n")
                    }

                }
            }catch(e: Exception) {
                UMLog.l(UMLog.ERROR, 691,
                        "BLEProxyResponder: Request ID# ${bleMessage.messageId} " +
                                " ERROR $e")
                e.printStackTrace()
            }

            //Thread.sleep(1000)
            UMLog.l(UMLog.ERROR, 691, "BLEProxyResponder: Request ID# ${bleMessage.messageId}  ERROR Sending error response")
            NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR,
                    "text/plain", "failed")

        }
    }

    override fun other(method: String?, uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun post(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {

        val BLE_LOCK = ReentrantLock()

    }
}