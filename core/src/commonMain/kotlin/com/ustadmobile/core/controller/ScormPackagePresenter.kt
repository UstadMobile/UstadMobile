package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.scorm.ScormManifest
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.dumpException
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ScormPackageView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadViewWithNotifications
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.io.StringReader
import org.kmp.io.KMPPullParserException
import org.kmp.io.KMPXmlParser

/**
 *
 * Present a SCORM package. Scorm tech specs as per :
 * https://scorm.com/scorm-explained/technical-scorm/
 *
 * Created by mike on 1/6/18.
 */
class ScormPackagePresenter(context: Any, arguments: Map<String, String?>, view: ScormPackageView)
    : UstadBaseController<ScormPackageView>(context, arguments, view) {
/*

    private var scormManifest: ScormManifest? = null

    private var mountedPath: String? = null

    private val zipMountedCallback = object : UmCallback<String> {
        override fun onSuccess(result: String?) {
            mountedPath = result

            GlobalScope.launch {
                try {
                    val client = HttpClient()
                    val manifestContent = client.get<String>(UMFileUtil.joinPaths(mountedPath!!, "imsmanifest.xml"))

                    val xpp = KMPXmlParser()
                    xpp.setInput(StringReader(manifestContent))

                    scormManifest = ScormManifest()
                    scormManifest!!.loadFromXpp(xpp)
                    val defaultOrg = scormManifest!!.defaultOrganization
                    val startRes = scormManifest!!.getResourceByIdentifier(
                            defaultOrg.items[0].identifierRef!!)
                    view.runOnUiThread(Runnable {
                        view.setTitle(scormManifest!!.defaultOrganization.title!!)
                        view.loadUrl(UMFileUtil.joinPaths(mountedPath!!,
                                startRes.href!!))
                    })
                } catch (e: IOException) {
                    dumpException(e)
                } catch (x: KMPPullParserException) {
                    dumpException(x)
                }

            }
        }

        override fun onFailure(exception: Throwable?) {
            view.showNotification("ERROR: failed to open package file",
                    UstadViewWithNotifications.LENGTH_LONG)
        }
    }

    fun onCreate(args: HashMap<String, String>) {
        view.mountZip(args[UstadView.ARG_CONTAINER_UID]!!,
                zipMountedCallback)
    }
*/

}
