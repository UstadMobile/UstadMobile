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
import com.ustadmobile.core.impl.dumpException
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.lib.util.UMUtil
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.io.StringReader
import org.kmp.io.KMPXmlParser

/**
 * Shows an EPUB with a table of contents, and page by page swipe navigation
 *
 * @author mike
 */
class EpubContentPresenter(context: Any,
                           args: Map<String, String>?,
                           private val epubContentView: EpubContentView)
    : UstadBaseController<EpubContentView>(context, args!!, epubContentView!!) {

    private var ocf: OcfDocument? = null

    private var mountedUrl: String? = null

    private var opfBaseUrl: String? = null

    private var linearSpineUrls: Array<String>? = null

    /**
     * First HTTP callback: run this once the container has been mounted to an http directory
     *
     */
    private val mountedCallbackHandler: UmCallback<String> = object : UmCallback<String> {

        override fun onSuccess(result: String?) {
            mountedUrl = result
            val containerUri = UMFileUtil.joinPaths(mountedUrl!!, OCF_CONTAINER_PATH)
            GlobalScope.launch {
                try {
                    val client = defaultHttpClient()
                    val ocfContent = client.get<String>(containerUri)

                    ocf = OcfDocument()
                    val ocfParser = KMPXmlParser()
                    ocfParser.setInput(StringReader(ocfContent))
                    ocf!!.loadFromParser(ocfParser)

                    //get and parse the first publication
                    val opfUrl = UMFileUtil.joinPaths(mountedUrl!!,
                            ocf!!.rootFiles[0].fullPath!!)
                    val opfContent = client.get<String>(opfUrl)

                    val opf = OpfDocument()
                    val opfParser = KMPXmlParser()
                    opfParser.setInput(StringReader(opfContent))
                    opf.loadFromOPF(opfParser)
                    val linearSpineHrefsRelative = opf.linearSpineHREFs

                    opfBaseUrl = UMFileUtil.getParentFilename(UMFileUtil.joinPaths(
                            mountedUrl!!, ocf!!.rootFiles[0].fullPath!!))

                    linearSpineUrls = Array(linearSpineHrefsRelative.size) { "" }

                    for (i in linearSpineHrefsRelative.indices) {
                        linearSpineUrls!![i] = UMFileUtil.joinPaths(opfBaseUrl!!,
                                linearSpineHrefsRelative[i])
                    }

                    val opfCoverImageItem = opf.getCoverImage("")
                    val authorNames = if (opf.numCreators > 0)
                        UMUtil.joinStrings(opf.creators!!, ", ")
                    else
                        null

                    epubContentView.runOnUiThread(Runnable {
                        val position : Int = if(arguments.containsKey(EpubContentView.ARG_INITIAL_PAGE_HREF)) opf.getLinearSpinePositionByHREF(
                                arguments.getValue(EpubContentView.ARG_INITIAL_PAGE_HREF)!!) else 0

                        epubContentView.setContainerTitle(opf.title!!)
                        epubContentView.setSpineUrls(linearSpineUrls!!, if(position >= 0) position else 0)
                        if (opfCoverImageItem != null) {
                            epubContentView.setCoverImage(UMFileUtil.resolveLink(opfBaseUrl!!,
                                    opfCoverImageItem.href!!))
                        }

                        if (authorNames != null) {
                            epubContentView.setAuthorName(authorNames)
                        }
                    })

                    if (opf.navItem == null)
                        throw IllegalArgumentException()

                    val navXhtmlUrl = UMFileUtil.resolveLink(UMFileUtil.joinPaths(
                            mountedUrl!!, ocf!!.rootFiles[0].fullPath!!), opf.navItem!!.href!!)

                    val navContent = client.get<String>(navXhtmlUrl)

                    val navDocument = EpubNavDocument()
                    val navParser = KMPXmlParser()
                    navParser.setInput(StringReader(navContent))
                    navDocument.load(navParser)
                    epubContentView.runOnUiThread(Runnable { epubContentView.setTableOfContents(navDocument.toc!!) })
                    view.runOnUiThread(Runnable { view.setProgressBarVisible(false) })
                } catch (e: Exception) {
                    dumpException(e)
                }

            }
        }

        override fun onFailure(exception: Throwable?) {
            if (exception != null) {
                UMLog.l(UMLog.ERROR, 500, "Exception mounting container")
                dumpException(exception)
            }
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val containerUid = (arguments[EpubContentView.ARG_CONTAINER_UID]?.toLong() ?: 0)
        view.setProgressBarProgress(-1)
        view.setProgressBarVisible(true)
        view.mountContainer(containerUid, mountedCallbackHandler)
    }

    fun handleClickNavItem(navItem: EpubNavItem) {
        if (opfBaseUrl != null && linearSpineUrls != null) {
            val navItemUrl = UMFileUtil.resolveLink(opfBaseUrl!!, navItem.href!!)
            val hrefIndex = listOf(*linearSpineUrls!!).indexOf(navItemUrl)
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
        const val OCF_CONTAINER_PATH = "META-INF/container.xml"
    }
}
