package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.PersonWithInventoryCount

interface PersonWithInventoryCountListener {

    fun onClickPerson(person: PersonWithInventoryCount)

}