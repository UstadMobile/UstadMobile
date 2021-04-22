package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.LINK_INTENT_FILTER
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_INTENT
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import org.kodein.di.DI
import org.kodein.di.instance

class RedirectPresenter(context: Any, arguments: Map<String, String>, view: RedirectView,
                        di: DI) :
        UstadBaseController<RedirectView>(context, arguments, view, di) {

    val systemImpl: UstadMobileSystemImpl by di.instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val nextViewArg = arguments[ARG_NEXT]
        val intentArg = arguments[ARG_INTENT]

        if(intentArg?.isNotEmpty() == true){
            loadFromUriString(intentArg)
        }else {
            val canSelectServer = systemImpl.getAppConfigBoolean(AppConfig.KEY_ALLOW_SERVER_SELECTION,
                    context)
            val userHasLoggedInOrSelectedGuest = systemImpl.getAppPref(
                    Login2Presenter.PREFKEY_USER_LOGGED_IN, "false", context).toBoolean()

            val args = mutableMapOf<String, String>()
            val destination = if (nextViewArg != null) {
                args.putAll(UMFileUtil.parseURLQueryString(nextViewArg))
                nextViewArg.substringBefore('?')
            } else if (!userHasLoggedInOrSelectedGuest) {
                if (canSelectServer)
                    SiteEnterLinkView.VIEW_NAME
                else
                    Login2View.VIEW_NAME
            } else {
                FeedEntryListView.VIEW_NAME
            }

            view.showNextScreen(destination, args)
        }
    }

    private fun loadFromUriString(uri: String?){
        val destinationIndex : Int? = uri?.indexOf("/${LINK_INTENT_FILTER}")?.plus(10)

        val apiUrl = uri?.substring(0, uri.indexOf("/${LINK_INTENT_FILTER}")) + '/'

        var charToAdd = "?"
        val sansApi = uri?.substring(destinationIndex?:0+1?:0)?:""
        if(sansApi.contains('?') || sansApi.contains('&')){
            charToAdd = "&"
        }
        val destination = uri?.substring(destinationIndex?:0) +
                "${charToAdd}${UstadView.ARG_SERVER_URL}=$apiUrl"

        val destinationQueryPos = destination.indexOf('?')
        val destinationOnly = destination.substring(0, destinationQueryPos)

        val args = UMFileUtil.parseURLQueryString(destination)

        view.showNextScreen(destinationOnly, args)

    }
}
