package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.LINK_INTENT_FILTER
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.UstadView.Companion.ARG_INTENT
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_WEB_PLATFORM
import com.ustadmobile.core.view.UstadView.Companion.MASTER_SERVER_ROOT_ENTRY_UID
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
        val isWebPlatform = arguments[ARG_WEB_PLATFORM].toBoolean()

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
                if (canSelectServer && !isWebPlatform)
                    SiteEnterLinkView.VIEW_NAME
                else if(canSelectServer && isWebPlatform)
                    ContentEntryListTabsView.VIEW_NAME
                else
                    Login2View.VIEW_NAME
            } else {
                ContentEntryListTabsView.VIEW_NAME
            }
            args.remove(ARG_WEB_PLATFORM)
            args.putAll(handleExtraArgsForWeb(destination,isWebPlatform, args))
            view.showNextScreen(destination, args)
        }
    }

    private fun handleExtraArgsForWeb(viewName: String, isWeb:Boolean,
                                      args: MutableMap<String, String>): MutableMap<String,String>{
        if((viewName == ContentEntryListTabsView.VIEW_NAME ||
                    viewName == Login2View.VIEW_NAME) && isWeb){
            if(!args.containsKey(ARG_PARENT_ENTRY_UID)){
                args[ARG_PARENT_ENTRY_UID] = MASTER_SERVER_ROOT_ENTRY_UID.toString()
            }else if(!args.containsKey(ARG_CONTENT_FILTER)){
                args[ARG_CONTENT_FILTER] = ARG_LIBRARIES_CONTENT
            }else if(!args.containsKey(ARG_NEXT) && viewName == Login2View.VIEW_NAME)
                args[ARG_NEXT] = ContentEntryListTabsView.VIEW_NAME
            return args
        }
        return args
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
