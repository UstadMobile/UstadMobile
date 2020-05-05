package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.lib.db.entities.PresenterFieldRow

interface PersonDetailView: UstadDetailView<PersonWithDisplayDetails> {

    var presenterFieldRows: DoorMutableLiveData<List<PresenterFieldRow>>?

    companion object {

        const val VIEW_NAME = "PersonDetailView"

    }

}