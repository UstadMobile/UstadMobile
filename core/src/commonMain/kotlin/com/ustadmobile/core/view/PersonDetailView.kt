package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Person

interface PersonDetailView : UstadView {

    fun finish()

    fun updateToolbarTitle(personName: String)

    fun updateImageOnView(imagePath: String)

    fun updatePersonOnView(person: Person)

    companion object{
        val VIEW_NAME = "PersonDetailView"
        val ARG_PERSON_UID = "ArgPersonUid"
    }
}