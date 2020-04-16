package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Person
import kotlin.js.JsName

interface UserProfileView : LanguageOptionView, UstadViewWithSnackBar  {

    fun finish()

    var person: Person

    fun updateToolbarTitle(personName: String)

    fun updateImageOnView(imagePath: String, skipCached: Boolean)

    fun callFinishAffinity()

    fun updateLastSyncedText(lastSynced: String)

    companion object {

        const val VIEW_NAME = "UserProfile"

        const val PERSON_UID = "person_uid"
    }
}
