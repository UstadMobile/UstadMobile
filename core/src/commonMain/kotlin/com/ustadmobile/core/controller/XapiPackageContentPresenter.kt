package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.tincan.UmAccountActor
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.core.util.UMUUID
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.util.ext.toXapiActorJsonObject
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.XapiPackageContentView
import io.ktor.client.request.get
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.io.ByteArrayInputStream
import kotlinx.serialization.json.Json
import org.kmp.io.KMPXmlParser
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * Created by mike on 9/13/17.
 *
 * Displays an XAPI Zip Package.
 *
 * Pass EpubContentPresenter.ARG_CONTAINERURI when creating to provide the location of the xAPI
 * zip to open
 *
 * Uses the Rustici launch method to find the URL to launch:
 * https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md
 *
 */
class XapiPackageContentPresenter(context: Any, args: Map<String, String>, view: XapiPackageContentView,
                                  di: DI)
    : UstadBaseController<XapiPackageContentView>(context, args, view, di) {

    private var tinCanXml: TinCanXML? = null

    private var registrationUUID: String? = null

    private var mountedPath: String = ""

    private val mounter: ContainerMounter by instance()

    private val accountManager: UstadAccountManager by instance()

    private lateinit var mountedEndpoint: String

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        registrationUUID = UMUUID.randomUUID().toString()
        val containerUid = arguments[UstadView.ARG_CONTAINER_UID]?.toLongOrNull() ?: 0L
        val contentEntryUid = arguments[UstadView.ARG_CONTENT_ENTRY_UID]?.toLongOrNull() ?: 0L
        val activeEndpoint = accountManager.activeAccount.endpointUrl.also {
            mountedEndpoint = it
        } ?: return

        GlobalScope.launch {
            mountedPath = mounter.mountContainer(activeEndpoint, containerUid)
            val client = defaultHttpClient()
            val tincanContent = client.get<String>(UMFileUtil.joinPaths(mountedPath, "tincan.xml"))

            val xpp = KMPXmlParser()
            xpp.setInput(ByteArrayInputStream(tincanContent.toByteArray()), "UTF-8")
            tinCanXml = TinCanXML.loadFromXML(xpp)
            val launchHref = tinCanXml?.launchActivity?.launchUrl
            val actorJsonStr = Json.stringify(UmAccountActor.serializer(),
                    accountManager.activeAccount.toXapiActorJsonObject(context))
            val launchMethodParams = mapOf(
                    "actor" to actorJsonStr,
                    "endpoint" to UMFileUtil.resolveLink(mountedPath,
                            "/${UMURLEncoder.encodeUTF8(activeEndpoint)}/xapi/$contentEntryUid/"),
                    "auth" to "OjFjMGY4NTYxNzUwOGI4YWY0NjFkNzU5MWUxMzE1ZGQ1",
                    "registration" to registrationUUID,
                    "activity_id" to (tinCanXml?.launchActivity?.id ?: "xapi_id"))
            if(launchHref != null) {
                val launchUrl = UMFileUtil.joinPaths(mountedPath, launchHref) + "?"  +
                        (launchMethodParams as Map<String, String>).toQueryString()
                view.runOnUiThread(Runnable {
                    view.contentTitle = tinCanXml?.launchActivity?.name ?: ""
                    view.url = launchUrl
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalScope.launch (Dispatchers.Main){
            if(mountedPath.isNotEmpty()){
                mounter.unMountContainer(mountedEndpoint, mountedPath)
            }
        }
    }



}
