package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_FROM
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
        val cameFromGetStarted = arguments[ARG_FROM] == OnBoardingView.VIEW_NAME
        val nextViewArg = arguments[ARG_NEXT]
        val intentArg = arguments[ARG_INTENT]

        if(intentArg?.isNotEmpty() == true){
            loadFromUriString(intentArg)
        }else {
            val canSelectServer = systemImpl.getAppConfigBoolean(AppConfig.KEY_ALLOW_SERVER_SELECTION,
                    context)

            val args = mutableMapOf<String, String>()
            val destination = if (nextViewArg != null) {
                args.putAll(UMFileUtil.parseURLQueryString(nextViewArg))
                nextViewArg.substringBefore('?')
            } else if (cameFromGetStarted) {
                if (canSelectServer) GetStartedView.VIEW_NAME else Login2View.VIEW_NAME
            } else {
                ContentEntryListTabsView.VIEW_NAME
            }

            view.showNextScreen(destination, args)
        }
    }

    private fun loadFromUriString(uri: String?){
        val destinationIndex : Int? = uri?.indexOf("/umclient")?.plus(10)

        val apiUrl = uri?.substring(0, uri?.indexOf("/umclient")?:0) + '/'

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

        println("REDIRECT42: RedirectPresenter")
        view.showNextScreen(destinationOnly, args)

    }
}
