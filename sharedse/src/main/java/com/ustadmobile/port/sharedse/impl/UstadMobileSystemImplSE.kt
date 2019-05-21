/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.sharedse.impl

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.HttpCache
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileConstants
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.UstadMobileSystemImplFs
import com.ustadmobile.core.impl.http.UmHttpCall
import com.ustadmobile.core.impl.http.UmHttpRequest
import com.ustadmobile.core.impl.http.UmHttpResponse
import com.ustadmobile.core.impl.http.UmHttpResponseCallback
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.sharedse.impl.http.UmHttpCallSe
import com.ustadmobile.port.sharedse.impl.http.UmHttpResponseSe
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer

import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.util.ArrayList
import java.util.Locale
import java.util.Properties

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 *
 * @author mike
 */
abstract class UstadMobileSystemImplSE : UstadMobileSystemImpl(), UstadMobileSystemImplFs {

    private var xmlPullParserFactory: XmlPullParserFactory? = null

    private var httpCache: HttpCache? = null

    private val client = OkHttpClient()

    private var appConfig: Properties? = null

    /**
     * Get NetworkManagerBle instance
     * @return Instance of NetworkManagerBle
     */
    abstract val networkManagerBle: NetworkManagerBle

    override fun init(context: Any) {
        super.init(context)

        if (httpCache == null)
            httpCache = HttpCache(getCacheDir(UstadMobileSystemCommon.SHARED_RESOURCE, context))
    }

    /**
     * Open the given connection and return the HttpURLConnection object using a proxy if required
     *
     * @param url
     *
     * @return
     */
    @Throws(IOException::class)
    abstract fun openConnection(url: URL): URLConnection

    /**
     * Returns the system base directory to work from
     *
     * @return
     */
    protected abstract override fun getSystemBaseDir(context: Any): String


    fun getCacheDir(mode: Int, context: Any?): String {
        val systemBaseDir = getSystemBaseDir(context!!)
        return UMFileUtil.joinPaths(systemBaseDir, UstadMobileConstants.CACHEDIR)
    }


    fun getStorageDirs(mode: Int, context: Any): Array<UMStorageDir> {
        val dirList = ArrayList<Any>()
        val systemBaseDir = getSystemBaseDir(context)
        val impl = UstadMobileSystemImpl.Companion.instance
        val contentDirName = getContentDirName(context)

        if (mode and UstadMobileSystemCommon.SHARED_RESOURCE == UstadMobileSystemCommon.SHARED_RESOURCE) {
            dirList.add(UMStorageDir(systemBaseDir, getString(MessageID.device, context),
                    false, true, false))

            //Find external directories
            val externalDirs = findRemovableStorage()
            for (extDir in externalDirs) {
                dirList.add(UMStorageDir(UMFileUtil.joinPaths(extDir, contentDirName),
                        getString(MessageID.memory_card, context),
                        true, true, false, false))
            }
        }

        val account = UmAccountManager.getActiveAccount(context)
        if (account != null && mode and UstadMobileSystemCommon.USER_RESOURCE == UstadMobileSystemCommon.USER_RESOURCE) {
            val userBase = UMFileUtil.joinPaths(systemBaseDir, "user-", account.username)
            dirList.add(UMStorageDir(userBase, getString(MessageID.device, context),
                    false, true, true))
        }


        val retVal = arrayOfNulls<UMStorageDir>(dirList.size())
        dirList.toArray(retVal)
        return retVal
    }

    fun getStorageDirs(context: Any, callback: UmResultCallback<List<UMStorageDir>>) {

        val dirList = ArrayList<Any>()
        val systemBaseDir = getSystemBaseDir(context)
        val contentDirName = getContentDirName(context)

        dirList.add(UMStorageDir(systemBaseDir, getString(MessageID.device, context),
                false, true, false))

        //Find external directories
        val externalDirs = findRemovableStorage()
        for (extDir in externalDirs) {
            dirList.add(UMStorageDir(UMFileUtil.joinPaths(extDir, contentDirName),
                    getString(MessageID.memory_card, context),
                    true, true, false, false))
        }

        callback.onDone(dirList)

    }

    /**
     * Provides a list of paths to removable stoage (e.g. sd card) directories
     *
     * @return
     */
    fun findRemovableStorage(): Array<String> {
        return arrayOfNulls(0)
    }

    /**
     * Will return language_COUNTRY e.g. en_US
     *
     * @return
     */
    override fun getSystemLocale(context: Any): String {
        return Locale.getDefault().toString()
    }


    @Throws(XmlPullParserException::class)
    override fun newPullParser(): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        return factory.newPullParser()
    }

    override fun newXMLSerializer(): XmlSerializer {
        var serializer: XmlSerializer? = null
        try {
            if (xmlPullParserFactory == null) {
                xmlPullParserFactory = XmlPullParserFactory.newInstance()
            }

            serializer = xmlPullParserFactory!!.newSerializer()
        } catch (e: XmlPullParserException) {
            UstadMobileSystemImpl.Companion.l(UMLog.ERROR, 92, null, e)
        }

        return serializer
    }

    override fun makeRequestAsync(request: UmHttpRequest, callback: UmHttpResponseCallback): UmHttpCall {
        val httpRequest = Request.Builder().url(request.url!!)
        if (request.headers != null) {
            val allHeaders = request.headers!!.keys.iterator()
            var header: String
            while (allHeaders.hasNext()) {
                header = allHeaders.next()
                httpRequest.addHeader(header, request.headers!![header])
            }
        }

        val call = client.newCall(httpRequest.build())
        val umCall = UmHttpCallSe(call)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(umCall, e)
            }

            override fun onResponse(call: Call, response: Response) {
                callback.onComplete(umCall, UmHttpResponseSe(response))
            }
        })

        return umCall
    }

    override fun sendRequestAsync(request: UmHttpRequest, responseListener: UmHttpResponseCallback): UmHttpCall {
        return makeRequestAsync(request, responseListener)
    }

    @Throws(IOException::class)
    override fun sendRequestSync(request: UmHttpRequest): UmHttpResponse {
        val httpRequest = Request.Builder().url(request.url!!)
        val call = client.newCall(httpRequest.build())
        return UmHttpResponseSe(call.execute())
    }

    @Throws(IOException::class)
    override fun makeRequestSync(request: UmHttpRequest): UmHttpResponse {
        return getHttpCache(request.context).getSync(request)
    }

    fun getHttpCache(context: Any?): HttpCache {
        if (httpCache == null)
            httpCache = HttpCache(getCacheDir(UstadMobileSystemCommon.SHARED_RESOURCE, context))

        return httpCache
    }


    @Throws(IOException::class)
    abstract override fun getAssetSync(context: Any, path: String): InputStream

    override fun getAppConfigString(key: String, defaultVal: String?, context: Any): String? {
        if (appConfig == null) {
            val appPrefResource = getManifestPreference("com.ustadmobile.core.appconfig",
                    "/com/ustadmobile/core/appconfig.properties", context)
            appConfig = Properties()
            var prefIn: InputStream? = null

            try {
                prefIn = getAssetSync(context, appPrefResource)
                appConfig!!.load(prefIn)
            } catch (e: IOException) {
                UstadMobileSystemImpl.Companion.l(UMLog.ERROR, 685, appPrefResource, e)
            } finally {
                UMIOUtils.closeInputStream(prefIn)
            }
        }

        return appConfig!!.getProperty(key, defaultVal)
    }

    companion object {

        /**
         * Convenience method to return a casted instance of UstadMobileSystemImplSharedSE
         *
         * @return Casted UstadMobileSystemImplSharedSE
         */
        val instanceSE: UstadMobileSystemImplSE
            get() = UstadMobileSystemImpl.Companion.instance as UstadMobileSystemImplSE
    }

}
