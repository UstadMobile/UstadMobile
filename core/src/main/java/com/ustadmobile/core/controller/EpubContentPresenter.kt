/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2019 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.http.UmHttpCall
import com.ustadmobile.core.impl.http.UmHttpRequest
import com.ustadmobile.core.impl.http.UmHttpResponse
import com.ustadmobile.core.impl.http.UmHttpResponseCallback
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.lib.util.UMUtil
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*

/**
 * Shows an EPUB with a table of contents, and page by page swipe navigation
 *
 * @author mike
 */
class EpubContentPresenter(context: Any, args: Map<String, String>?, private val epubContentView: EpubContentView?)
    : UstadBaseController<EpubContentView>(context, args!!, epubContentView!!) {

    private var ocf: OcfDocument? = null

    private var mountedUrl: String? = null

    private var opfBaseUrl: String? = null

    private var linearSpineUrls: Array<String>? = null

    /**
     * First HTTP callback: run this once the container has been mounted to an http directory
     *
     */
    private val mountedCallbackHandler : UmCallback<String> ? = object : UmCallback<String> {

        override fun onSuccess(result: String?) {
            mountedUrl = result
            val containerUri = UMFileUtil.joinPaths(mountedUrl!!, OCF_CONTAINER_PATH)
            UstadMobileSystemImpl.instance.makeRequestAsync(
                    UmHttpRequest(context, containerUri), containerHttpCallbackHandler)
        }

        override fun onFailure(exception: Throwable) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 500, "Exception mounting container")
            exception.printStackTrace()
        }
    }

    /**
     * Second HTTP callback: parses the container.xml file and finds the OPF
     */
    private val containerHttpCallbackHandler = object : UmHttpResponseCallback {
        override fun onComplete(call: UmHttpCall, response: UmHttpResponse) {
            if (response.isSuccessful) {
                ocf = OcfDocument()

                try {
                    val xpp = UstadMobileSystemImpl.instance.newPullParser(
                            ByteArrayInputStream(response.responseBody))
                    ocf!!.loadFromParser(xpp)

                    //get and parse the first publication
                    val opfUrl = UMFileUtil.joinPaths(mountedUrl!!,
                            ocf!!.rootFiles[0].fullPath!!)
                    UstadMobileSystemImpl.instance.makeRequestAsync(
                            UmHttpRequest(context, opfUrl), opfHttpCallbackHandler)

                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (x: XmlPullParserException) {
                    x.printStackTrace()
                }

            }
        }

        override fun onFailure(call: UmHttpCall, exception: IOException) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 500, "Exception loading container")
        }
    }

    /**
     * Third HTTP callback: parses the OPF and sets up the view
     */
    private val opfHttpCallbackHandler = object : UmHttpResponseCallback {
        override fun onComplete(call: UmHttpCall, response: UmHttpResponse) {
            val opf = OpfDocument()
            try {
                val xpp = UstadMobileSystemImpl.instance.newPullParser(
                        ByteArrayInputStream(response.responseBody))
                opf.loadFromOPF(xpp)
                val linearSpineHrefsRelative = opf.linearSpineHREFs

                opfBaseUrl = UMFileUtil.getParentFilename(UMFileUtil.joinPaths(
                        mountedUrl!!, ocf!!.rootFiles[0].fullPath!!))

                linearSpineUrls = Array(linearSpineHrefsRelative.size){""}

                for (i in linearSpineHrefsRelative.indices) {
                    linearSpineUrls!![i] = UMFileUtil.joinPaths(opfBaseUrl!!,
                            linearSpineHrefsRelative[i])
                }

                val opfCoverImageItem = opf.getCoverImage("")
                val authorNames = if (opf.numCreators > 0)
                    UMUtil.joinStrings(opf.creators!!, ", ")
                else
                    null

                epubContentView!!.runOnUiThread(Runnable  {
                    epubContentView.setContainerTitle(opf.title!!)
                    epubContentView.setSpineUrls(linearSpineUrls!!)
                    if (opfCoverImageItem != null) {
                        epubContentView.setCoverImage(UMFileUtil.resolveLink(opfBaseUrl!!,
                                opfCoverImageItem.href!!))
                    }

                    if (authorNames != null) {
                        epubContentView.setAuthorName(authorNames)
                    }
                })

                if (opf.navItem == null)
                    return

                val navXhtmlUrl = UMFileUtil.resolveLink(UMFileUtil.joinPaths(
                        mountedUrl!!, ocf!!.rootFiles[0].fullPath!!), opf.navItem!!.href!!)

                UstadMobileSystemImpl.instance.makeRequestAsync(UmHttpRequest(
                        context, navXhtmlUrl), navCallbackHandler)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (x: XmlPullParserException) {
                x.printStackTrace()
            }

        }

        override fun onFailure(call: UmHttpCall, exception: IOException) {
            exception.printStackTrace()
        }
    }

    private val navCallbackHandler = object : UmHttpResponseCallback {
        override fun onComplete(call: UmHttpCall, response: UmHttpResponse) {
            val navDocument = EpubNavDocument()
            try {
                val xpp = UstadMobileSystemImpl.instance.newPullParser(
                        ByteArrayInputStream(response.responseBody), "UTF-8")
                xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
                navDocument.load(xpp)
                epubContentView!!.runOnUiThread(Runnable  { epubContentView.setTableOfContents(navDocument.toc!!) })
                view.runOnUiThread(Runnable  { view.setProgressBarVisible(false) })
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (x: XmlPullParserException) {
                x.printStackTrace()
            }

        }

        override fun onFailure(call: UmHttpCall, exception: IOException) {
            exception.printStackTrace()
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val containerUid = java.lang.Long.parseLong(arguments.get(EpubContentView.ARG_CONTAINER_UID))
        view.setProgressBarProgress(-1)
        view.setProgressBarVisible(true)
        view.mountContainer(containerUid, mountedCallbackHandler!!)
    }

    fun handleClickNavItem(navItem: EpubNavItem) {
        if (opfBaseUrl != null && linearSpineUrls != null) {
            val navItemUrl = UMFileUtil.resolveLink(opfBaseUrl!!, navItem.href!!)
            val hrefIndex = Arrays.asList(*linearSpineUrls!!).indexOf(navItemUrl)
            if (hrefIndex != -1) {
                epubContentView!!.goToLinearSpinePosition(hrefIndex)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mountedUrl != null) {
            view.unmountContainer(mountedUrl!!)
        }
    }

    companion object {

        /**
         * Hardcoded fixed path to the container.xml file as per the open container
         * format spec : META-INF/container.xml
         */
        val OCF_CONTAINER_PATH = "META-INF/container.xml"
    }
}
