package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.requireHttpPrefix
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.verifySite
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.Site
import io.ktor.client.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class SiteEnterLinkViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<Person>(di, savedStateHandle) {

    private var site: Site? = null

    private var checkTextLinkJob: Deferred<Unit>? = null

    private val impl: UstadMobileSystemImpl by instance()

    private val httpClient: HttpClient by instance()

    private var validatedLink: String? = null

    init {

    }

    fun handleClickNext(){
        val mSite = site
        val validatedLinkVal = validatedLink
        if(mSite != null && validatedLinkVal != null){
            val args = arguments.toMutableMap().also {
                val siteLink = view.siteLink
                if(siteLink != null)
                    it[UstadView.ARG_SERVER_URL] = validatedLinkVal

                it[UstadView.ARG_SITE] = Json.encodeToString(Site.serializer(), mSite)
            }

            impl.go(Login2View.VIEW_NAME, args, context)
        }
    }

    fun handleCheckLinkText(href: String){

        if(checkTextLinkJob != null){
            checkTextLinkJob?.cancel()
            checkTextLinkJob = null
        }

        checkTextLinkJob = presenterScope.async {
            try {
                val endpointUrl = href.requireHttpPrefix().requirePostfix("/")
                site = httpClient.verifySite(endpointUrl)
                view.validLink = site != null
                validatedLink = endpointUrl
            }catch (e: Exception) {
                view.validLink = false
            }

            view.progressVisible = false
            checkTextLinkJob = null

            return@async
        }
    }

    fun handleClickCreateNewSite() {
        impl.openLinkInBrowser("https://www.ustadmobile.com/")
    }
}