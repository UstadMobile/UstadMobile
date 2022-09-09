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
package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.storeProgressStatement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.core.view.ContainerMounter.Companion.FILTER_MODE_EPUB
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import com.ustadmobile.xmlpullparserkmp.setInputString
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.js.JsName
import kotlin.jvm.Volatile
import kotlin.math.max

/**
 * Shows an EPUB with a table of contents, and page by page swipe navigation
 *
 * @author mike
 */
class EpubContentPresenter(context: Any,
                           args: Map<String, String>,
                           private val epubContentView: EpubContentView, di: DI)
    : UstadBaseController<EpubContentView>(context, args, epubContentView, di, activeSessionRequired = false) {

    private var clazzUid: Long =0L
    private var ocf: OcfDocument? = null

    private var mountedPath: String = ""

    private var mountedEndpoint: String = ""

    private var opfBaseUrl: String? = null

    private var linearSpineUrls: Array<String> = arrayOf()

    private val accountManager: UstadAccountManager by instance()

    private val mountHandler: ContainerMounter by instance()

    private val systemImpl: UstadMobileSystemImpl by instance()

    private var onCreateException: Exception? = null

    private var isStarted: Boolean = false


    //The time that the
    private var startTime: Long = 0L

    private val xapiStatementEndpoint: XapiStatementEndpoint by on(accountManager.activeAccount).instance()

    var contentEntryUid: Long = 0L

    var maxPageReached: Int = 0

    var mCurrentPage: Int = 0

    lateinit var contextRegistration: String

    private val pageTitles = mutableMapOf<Int, String?>()

    @Volatile
    private var mNavDocument: EpubNavDocument? = null

    private val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_DB)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val containerUid = arguments[UstadView.ARG_CONTAINER_UID]?.toLong() ?: 100
        contentEntryUid = arguments[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0
        clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0
        contextRegistration = randomUuid().toString()
        view.progressValue = -1
        view.progressVisible = true
        mountedEndpoint = accountManager.activeAccount.endpointUrl
        presenterScope.launch {
            withContext(Dispatchers.Default) {
                mountedPath = mountHandler.mountContainer(accountManager.activeAccount.endpointUrl,
                    containerUid, FILTER_MODE_EPUB)
                handleMountedContainer()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startTime = getSystemTimeInMillis()
        isStarted = true
        onCreateException?.also {
            navigateToErrorScreen(it)
        }
        onCreateException = null
    }

    override fun onStop() {
        super.onStop()
        val duration = getSystemTimeInMillis() - startTime
        if(accountManager.activeAccount.personUid == 0L)
            return //no one is really logged in

        presenterScope.launch {
            withContext(Dispatchers.Default) {
                val contentEntry =  db.contentEntryDao.findByUid(contentEntryUid) ?: return@withContext
                val progress = ((maxPageReached + 1) * 100) / max(linearSpineUrls.size, 1)
                xapiStatementEndpoint.storeProgressStatement(
                    accountManager.activeAccount,
                    contentEntry, progress, duration, contextRegistration, clazzUid)
            }
        }
    }

    private suspend fun handleMountedContainer(){
        try {
            val client : HttpClient = di.direct.instance()
            val ocfContent: String = client.get(UMFileUtil.joinPaths(mountedPath, OCF_CONTAINER_PATH)).body()
            val xppFactoryNsAware: XmlPullParserFactory = di.direct.instance(tag = DiTag.XPP_FACTORY_NSAWARE)

            ocf = OcfDocument()
            val ocfParser = xppFactoryNsAware.newPullParser()
            ocfParser.setInputString(ocfContent)
            ocf?.loadFromParser(ocfParser)

            //get and parse the first publication
            val opfUrl = ocf?.rootFiles?.get(0)?.fullPath?.let {
                UMFileUtil.joinPaths(mountedPath, it)
            }
            val opfContent: String = client.get(opfUrl.toString()).body()

            val opf = OpfDocument()
            val xppFactoryNsUnaware: XmlPullParserFactory = di.direct.instance(tag = DiTag.XPP_FACTORY_NSUNAWARE)
            val opfParser = xppFactoryNsUnaware.newPullParser()
            opfParser.setInputString(opfContent)
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
                opf.creators?.joinToString(separator = ",")
            else
                null

            val position : Int = if(arguments.containsKey(EpubContentView.ARG_INITIAL_PAGE_HREF))
                opf.getLinearSpinePositionByHREF(arguments.getValue(EpubContentView.ARG_INITIAL_PAGE_HREF)) else 0

            val containerTitle = if(!opf.title.isNullOrBlank()) {
                opf.title
            } else {
                db.contentEntryDao.findTitleByUidAsync(contentEntryUid)
            }

            epubContentView.runOnUiThread(Runnable {
                epubContentView.containerTitle = containerTitle
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

            val ncxUrl = opf.ncxItem?.href?.let {
                ocf?.rootFiles?.get(0)?.fullPath?.let { path -> UMFileUtil.joinPaths(mountedPath, path)
                }?.let { url -> UMFileUtil.resolveLink(url, it) }
            }

            val navUrlToLoad = navXhtmlUrl ?: ncxUrl

            if(navUrlToLoad != null) {
                val navContent: String = client.get(navUrlToLoad).body()

                val navDocument = EpubNavDocument().also {
                    mNavDocument = it
                }

                val navParser = xppFactoryNsAware.newPullParser()
                navParser.setInputString(navContent)
                navDocument.load(navParser)
                epubContentView.runOnUiThread(Runnable {
                    epubContentView.tableOfContents = navDocument.toc ?: navDocument.ncxNavMap
                })
            }

            view.runOnUiThread(Runnable {
                view.progressVisible = false
            })
        } catch (e: Exception) {
            if(e !is CancellationException) {
                if(isStarted){
                    navigateToErrorScreen(e)
                }else{
                    onCreateException = e
                }
            }
        }
    }



    @JsName("handleClickNavItem")
    fun handleClickNavItem(navItem: EpubNavItem) {
        val opfUrl = opfBaseUrl
        if (opfUrl != null && linearSpineUrls.isNotEmpty()) {
            val navItemUrl = navItem.href?.let { UMFileUtil.resolveLink(opfUrl, it.substringBeforeLast("#")) }
            val hrefIndex = listOf(*linearSpineUrls).indexOf(navItemUrl)
            if (hrefIndex != -1) {
                epubContentView.scrollToSpinePosition(hrefIndex,
                        navItem.href?.substringAfterLast("#", ""))
            }else {
                epubContentView.showSnackBar(systemImpl.getString(MessageID.error_message_load_page,
                    context))
            }
        }
    }

    fun handlePageChanged(index: Int) {
        mCurrentPage = index
        maxPageReached = max(index, maxPageReached)
        updateWindowTitle()
    }

    fun handlePageTitleChanged(index: Int, title: String?) {
        pageTitles[index] = title
        updateWindowTitle()
    }

    private fun updateWindowTitle() {
        val relativeHref = opfBaseUrl?.let { linearSpineUrls[mCurrentPage].substringAfter(it, "") } ?: ""
        if(mCurrentPage == mCurrentPage)
            view.windowTitle =  mNavDocument?.getNavByHref(relativeHref)?.title ?: pageTitles[mCurrentPage]
                    ?: view.containerTitle ?: ""
    }


    override fun onDestroy() {
        if (mountedPath.isNotEmpty()) suspend {
            mountHandler.unMountContainer(mountedEndpoint, mountedPath)
        }

        super.onDestroy()
    }

    companion object {

        /**
         * Hardcoded fixed path to the container.xml file as per the open container
         * format spec : META-INF/container.xml
         */
        const val OCF_CONTAINER_PATH = "META-INF/container.xml"
    }
}
