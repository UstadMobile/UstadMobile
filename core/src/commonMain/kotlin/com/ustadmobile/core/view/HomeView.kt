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

    /**
     * @param options - a list of labels (given as the messageid) and the and the viewname / args.
     *
     * On Android: displayed as bottomnavigation
     * On Angular/web - a navigation shelf / menu
     *
     * e.g. MessageID.content, ContentEntryList?parentUid=...
     */
    @JsName("setMenu")
    fun setOptions(options: List<Pair<Int, String>>)

    fun showShareAppDialog()

    companion object {

        const val VIEW_NAME = "Home"
    }
}
