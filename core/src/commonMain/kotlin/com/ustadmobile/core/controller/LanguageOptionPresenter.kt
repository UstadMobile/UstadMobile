package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.LanguageOptionView
import com.ustadmobile.lib.db.entities.UmAccount
import kotlin.js.JsName

abstract class LanguageOptionPresenter(context: Any, arguments: Map<String, String?>, view: LanguageOptionView,
                              private val impl: UstadMobileSystemImpl):
        UstadBaseController<LanguageOptionView>(context, arguments, view){

    private val languageOptions = impl.getAllUiLanguage(context)

    abstract fun handleNavigation()

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        view.setCurrentLanguage(languageOptions[impl.getDisplayedLocale(context)!!])
    }

    @JsName("handleUserLogout")
    fun handleUserLogout(){
        UmAccountManager.setActiveAccount(UmAccount(0,
                null, null, null), context)
        handleNavigation()
    }

    @JsName("handleShowLanguageOptions")
    fun handleShowLanguageOptions(){
        view.setLanguageOption(languageOptions.values.sorted().toMutableList())
    }

    @JsName("handleLanguageSelected")
    fun handleLanguageSelected(position: Int){
        val languageName = languageOptions.values.sorted().toMutableList()[position]
        val localeCode = getLocaleCode(languageName)
        if(impl.getDisplayedLocale(context) != localeCode){
            impl.setLocale(localeCode, context)
            view.restartUI()
        }
    }

    private fun getLocaleCode(name: String): String{
        for(pair in languageOptions){
            if(name == pair.value) return pair.key
        }
        return "en"
    }

}