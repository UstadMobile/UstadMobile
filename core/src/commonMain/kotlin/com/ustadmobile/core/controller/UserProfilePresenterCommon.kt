package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.UserProfileView
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.js.JsName

abstract class UserProfilePresenterCommon (context: Any, arguments: Map<String, String?>, view: UserProfileView,
                                  private val personDao: PersonDao, val impl: UstadMobileSystemImpl)
    : UstadBaseController<UserProfileView>(context, arguments, view){

    private val languageOptions = impl.getAllUiLanguage(context)

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val account = UmAccountManager.getActiveAccount(context)
        view.setCurrentLanguage(languageOptions[impl.getDisplayedLocale(context)!!])

        if(account != null){
            GlobalScope.launch {
                val person = personDao.findByUid(account.personUid)
                if(person != null){
                    view.setLoggedPerson(person)
                    view.loadProfileIcon(if(account == null) "" else "")
                }
            }
        }
    }

    abstract fun handleNavigation()

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