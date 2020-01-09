package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Person
import kotlin.js.JsName

interface UserProfileView : LanguageOptionView {

    @JsName("loadProfileIcon")
    fun loadProfileIcon(profile: String)

    @JsName("setLoggedPerson")
    fun setLoggedPerson(person: Person)


    companion object {

        const val VIEW_NAME = "UserProfile"
    }
}
