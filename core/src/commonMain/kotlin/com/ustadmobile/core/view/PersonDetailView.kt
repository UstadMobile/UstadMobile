package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails

interface PersonDetailView: UstadDetailView<PersonWithDisplayDetails> {

    var presenterFields: DoorLiveData<PersonDetailViewField>?

    companion object {

        const val VIEW_NAME = "PersonDetailView"

    }

}