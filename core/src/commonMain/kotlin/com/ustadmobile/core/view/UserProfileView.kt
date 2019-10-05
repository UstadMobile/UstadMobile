package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Person
import kotlin.js.JsName

interface UserProfileView : UstadView {

    @JsName("setCurrentLanguage")
    fun setCurrentLanguage(language: String?)

    @JsName("setLanguageOption")
    fun setLanguageOption(languages: MutableList<String>)

    @JsName("loadProfileIcon")
    fun loadProfileIcon(profile: String)

    @JsName("setLoggedPerson")
    fun setLoggedPerson(person: Person)

    @JsName("restartUI")
    fun restartUI()

    @JsName("showLanguageOptions")
    fun showLanguageOptions()

    companion object {

        const val VIEW_NAME = "UserProfile"
    }
}
