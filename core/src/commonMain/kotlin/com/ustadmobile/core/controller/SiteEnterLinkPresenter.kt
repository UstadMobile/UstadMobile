package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.putIfNotAlreadySet
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_SERVER_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_SITE
import com.ustadmobile.core.view.SiteEnterLinkView
import com.ustadmobile.core.view.UstadView.Companion.ARG_POPUPTO_ON_FINISH
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Site
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.get
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

class SiteEnterLinkPresenter(context: Any, arguments: Map<String, String>, view: SiteEnterLinkView,
                             di: DI) :
        UstadBaseController<SiteEnterLinkView>(context, arguments, view, di) {

    private var site: Site? = null

    private var checkTextLinkJob: Deferred<Unit>? = null

    private val impl: UstadMobileSystemImpl by instance()

    private val httpClient: HttpClient by instance()

    fun handleClickNext(){
        val mSite = site
        if(mSite != null){
            val args = arguments.toMutableMap().also {
                val siteLink = view.siteLink
                if(siteLink != null)
                    it[ARG_SERVER_URL] = siteLink

                it[ARG_SITE] = Json.encodeToString(Site.serializer(), mSite)
            }

            impl.go(Login2View.VIEW_NAME, args, context)
        }
    }

    fun handleCheckLinkText(href: String){

        if(checkTextLinkJob != null){
            checkTextLinkJob?.cancel()
            checkTextLinkJob = null
        }

        checkTextLinkJob = GlobalScope.async(doorMainDispatcher()) {
            try {
                var formattedHref = if(href.startsWith("http")) href else "https://$href"
                formattedHref = UMFileUtil.joinPaths(formattedHref, "Site","verify")
                site = httpClient.get<Site>(formattedHref) {
                    timeout {
                        requestTimeoutMillis = LINK_REQUEST_TIMEOUT
                    }
                }
                view.validLink = site != null
            }catch (e: Exception) {
                view.validLink = false
            }

            view.progressVisible = false
            checkTextLinkJob = null

            return@async
        }
    }

    fun handleClickUsePublicLibrary() {
        val args = arguments.toMutableMap().also {
            it[ARG_SERVER_URL] = "https://library.ustadmobile.app/"
            it[ARG_SITE] = safeStringify(di, Site.serializer(), Site().apply {
                registrationAllowed = true
                guestLogin = true
            })
        }
        args.putIfNotAlreadySet(ARG_POPUPTO_ON_FINISH, SiteEnterLinkView.VIEW_NAME)

        impl.go(Login2View.VIEW_NAME, args, context)
    }

    fun handleClickCreateNewSite() {
        impl.openLinkInBrowser("https://www.ustadmobile.com/hosting/", context)
    }


    override fun onDestroy() {
        super.onDestroy()
        checkTextLinkJob = null
        site = null
    }

    companion object {

        const val LINK_REQUEST_TIMEOUT: Long = 10000

    }

}
