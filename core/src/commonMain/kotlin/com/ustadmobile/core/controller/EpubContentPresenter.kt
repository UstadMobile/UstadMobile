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

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.storeProgressStatement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.impl.dumpException
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.util.UMUtil
import com.ustadmobile.lib.util.getSystemTimeInMillis
import io.ktor.client.request.get
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.io.ByteArrayInputStream
import org.kmp.io.KMPXmlParser
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.js.JsName
import kotlin.math.max

/**
 * Shows an EPUB with a table of contents, and page by page swipe navigation
 *
 * @author mike
 */
class EpubContentPresenter(context: Any,
                           args: Map<String, String>,
                           private val epubContentView: EpubContentView, di: DI)
    : UstadBaseController<EpubContentView>(context, args, epubContentView, di) {

    private var ocf: OcfDocument? = null

    private var mountedPath: String = ""

    private var mountedEndpoint: String = ""

    private var opfBaseUrl: String? = null

    private var linearSpineUrls: Array<String> = arrayOf()

    private val accountManager: UstadAccountManager by instance()

    private val mountHandler: ContainerMounter by instance()

    //The time that the
    private var startTime: Long = 0L

    private val xapiStatementEndpoint: XapiStatementEndpoint by on(accountManager.activeAccount).instance()

    var contentEntryUid: Long = 0L

    var maxPageReached: Int = 0

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val containerUid = arguments[UstadView.ARG_CONTAINER_UID]?.toLong() ?: 100
        contentEntryUid = arguments[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0
        view.progressValue = -1
        view.progressVisible = true
        mountedEndpoint = accountManager.activeAccount.endpointUrl
        GlobalScope.launch {
            mountedPath = mountHandler.mountContainer(accountManager.activeAccount.endpointUrl,
                    containerUid)
            handleMountedContainer()
        }
    }

    override fun onStart() {
        super.onStart()
        startTime = getSystemTimeInMillis()
    }

    override fun onStop() {
        super.onStop()
        val duration = getSystemTimeInMillis() - startTime
        if(accountManager.activeAccount.personUid == 0L)
            return //no one is really logged in

        GlobalScope.launch {
            val db: UmAppDatabase = on(accountManager.activeAccount).direct.instance(tag = TAG_DB)
            val contentEntry =  db.contentEntryDao.findByUid(contentEntryUid) ?: return@launch
            val progress = ((maxPageReached + 1) * 100) / max(linearSpineUrls.size, 1)
            xapiStatementEndpoint.storeProgressStatement(accountManager.activeAccount,
                    contentEntry, progress, duration)
        }

    }

    private suspend fun handleMountedContainer(){
        try {
            val client = defaultHttpClient()
            val ocfContent = client.get<String>(UMFileUtil.joinPaths(mountedPath, OCF_CONTAINER_PATH))

            ocf = OcfDocument()
            val ocfParser = KMPXmlParser()

            ocfParser.setInput(ByteArrayInputStream(ocfContent.toByteArray()), "UTF-8")
            ocf?.loadFromParser(ocfParser)

            //get and parse the first publication
            val opfUrl = ocf?.rootFiles?.get(0)?.fullPath?.let {
                UMFileUtil.joinPaths(mountedPath, it)
            }
            val opfContent = client.get<String>(opfUrl.toString())

            val opf = OpfDocument()
            val opfParser = KMPXmlParser()
            opfParser.setInput(ByteArrayInputStream(opfContent.toByteArray()), "UTF-8")
            opf.loadFromOPF(opfParser)
            val linearSpineHrefsRelative = opf.linearSpineHREFs

            opfBaseUrl = ocf?.rootFiles?.get(0)?.fullPath?.let {
                UMFileUtil.joinPaths(mountedPath, it) }?.let {
                UMFileUtil.getParentFilename(it) }

            linearSpineUrls = Array(linearSpineHrefsRelative.size) { "" }

            for (i in linearSpineHrefsRelative.indices) {
                linearSpineUrls[i] = opfBaseUrl?.let { UMFileUtil.joinPaths(it,
                        linearSpineHrefsRelative[i]) }.toString()
            }

            val opfCoverImageItem = opf.getCoverImage("")
            val authorNames = if (opf.numCreators > 0)
                opf.creators?.let { UMUtil.joinStrings(it, ", ") }
            else
                null

            val position : Int = if(arguments.containsKey(EpubContentView.ARG_INITIAL_PAGE_HREF))
                opf.getLinearSpinePositionByHREF(arguments.getValue(EpubContentView.ARG_INITIAL_PAGE_HREF)) else 0

            epubContentView.runOnUiThread(Runnable {
                epubContentView.containerTitle = opf.title
                epubContentView.spineUrls = linearSpineUrls.toList()
                if (opfCoverImageItem != null) {
                    epubContentView.coverImageUrl = opfCoverImageItem.href?.let {
                        opfBaseUrl?.let { url -> UMFileUtil.resolveLink(url, it) }
                    }
                }

                if (authorNames != null) {
                    epubContentView.authorName = authorNames
                }

            })

            val navXhtmlUrl = opf.navItem?.href?.let {
                ocf?.rootFiles?.get(0)?.fullPath?.let { path -> UMFileUtil.joinPaths(mountedPath, path)
                }?.let { url -> UMFileUtil.resolveLink(url, it) }
            }

            if(navXhtmlUrl != null) {
                val navContent = client.get<String>(navXhtmlUrl.toString())

                val navDocument = EpubNavDocument()
                val navParser = KMPXmlParser()
                navParser.setInput(ByteArrayInputStream(navContent.toByteArray()), "UTF-8")
                navDocument.load(navParser)
                epubContentView.runOnUiThread(Runnable {
                    epubContentView.tableOfContents = navDocument.toc
                })
            }

            view.runOnUiThread(Runnable {
                view.progressVisible = false
            })
        } catch (e: Exception) {
            dumpException(e)
        }
    }

    @JsName("handleClickNavItem")
    fun handleClickNavItem(navItem: EpubNavItem) {
        val opfUrl = opfBaseUrl
        if (opfUrl != null && linearSpineUrls.isNotEmpty()) {
            val navItemUrl = navItem.href?.let { UMFileUtil.resolveLink(opfUrl, it) }
            val hrefIndex = listOf(*linearSpineUrls).indexOf(navItemUrl)
            if (hrefIndex != -1) {
                epubContentView.spinePosition = hrefIndex
            }
        }
    }

    fun handlePageChanged(index: Int) {
        maxPageReached = max(index, maxPageReached)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mountedPath.isNotEmpty()) suspend {
            mountHandler.unMountContainer(mountedEndpoint, mountedPath)
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
