package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Person
import kotlin.js.JsName

interface UserProfileView : LanguageOptionView  {

    fun finish()

    fun updateToolbarTitle(personName: String)

    fun setUsername(username: String)

    fun updateImageOnView(imagePath: String, skipCached: Boolean)

    fun addImageFromCamera()

    fun addImageFromGallery()

    fun sendMessage(messageId: Int)

    fun callFinishAffinity()

    @JsName("loadProfileIcon")
    fun loadProfileIcon(profile: String)

    @JsName("setLoggedPerson")
    fun setLoggedPerson(person: Person)

    companion object {

        const val VIEW_NAME = "UserProfile"

        const val PERSON_UID = "person_uid"
    }
}
