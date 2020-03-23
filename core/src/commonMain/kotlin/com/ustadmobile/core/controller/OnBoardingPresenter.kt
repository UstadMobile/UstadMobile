package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.LOCALE_USE_SYSTEM
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.OnBoardingView
import com.ustadmobile.core.view.OnBoardingView.Companion.PREF_TAG
import com.ustadmobile.core.view.UstadView

class OnBoardingPresenter(context: Any, arguments: Map<String, String>, view: OnBoardingView, val impl: UstadMobileSystemImpl) :
        UstadBaseController<OnBoardingView>(context, arguments, view) {

    private val languageOptions = impl.getAllUiLanguagesList(context)

    override fun onCreate(savedState: Map<String, String?> ?) {
        super.onCreate(savedState)

        view.setLanguageOptions(languageOptions.map { it.second })
    }

    }

    fun handleGetStarted() {
        val args: MutableMap<String,String?> = arguments.toMutableMap()
       // args[UstadView.ARG_CONTENT_ENTRY_UID] = 1777919737.toString()
        impl.setAppPref(PREF_TAG, true.toString(), view.viewContext)
       // impl.go(ContentEntryDetailView.VIEW_NAME, args, context)
        impl.go(HomeView.VIEW_NAME, args, context)
    }

    fun handleLanguageSelected(position: Int){
        val newLocaleCode = languageOptions[position].first
        val newLocaleToDisplay = if(newLocaleCode == LOCALE_USE_SYSTEM) {
            impl.getDisplayedLocale(context).substring(0, 2)
        }else {
            newLocaleCode
        }

        val needsRestart = impl.getDisplayedLocale(context) != newLocaleToDisplay

        if(newLocaleCode != impl.getLocale(context)) {
            impl.setLocale(newLocaleCode, context)
        }

        view.takeIf { needsRestart }?.restartUI()
    }

}
