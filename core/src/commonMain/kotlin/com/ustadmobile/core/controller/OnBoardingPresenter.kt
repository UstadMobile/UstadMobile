package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.LOCALE_USE_SYSTEM
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.OnBoardingView
import org.kodein.di.DI
import org.kodein.di.instance

class OnBoardingPresenter(context: Any, arguments: Map<String, String>, view: OnBoardingView, di: DI) :
        UstadBaseController<OnBoardingView>(context, arguments, view, di, activeSessionRequired = false) {

    private val impl: UstadMobileSystemImpl by instance()

    private val languageOptions = impl.getAllUiLanguagesList(context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val selectedLocaleIndex = languageOptions.indexOfFirst { it.langCode == impl.getLocale(context) }
        view.setLanguageOptions(languageOptions.map { it.langDisplay }, languageOptions[selectedLocaleIndex].langDisplay)
    }

    fun handleLanguageSelected(position: Int){
        val newLocaleCode = languageOptions[position].langCode
        val newLocaleToDisplay = if(newLocaleCode == LOCALE_USE_SYSTEM) {
            impl.getSystemLocale(context).substring(0, 2)
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
