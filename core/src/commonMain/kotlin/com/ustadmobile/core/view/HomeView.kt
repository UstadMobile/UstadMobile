package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Person
import kotlin.js.JsName

interface HomeView : LanguageOptionView {

    @JsName("showDownloadAllButton")
    fun showDownloadAllButton(show:Boolean)

    @JsName("loadProfileIcon")
    fun loadProfileIcon(profileUrl: String)

    @JsName("setLoggedPerson")
    fun setLoggedPerson(person: Person)

    @JsName("showReportMenu")
    fun showReportMenu(show: Boolean)


    companion object {

        const val VIEW_NAME = "Home"
    }
}
