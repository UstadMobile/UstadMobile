package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.SelQuestionAndOptions
import com.ustadmobile.lib.db.entities.SelQuestionOption


interface SelQuestionAndOptionsEditView: UstadEditView<SelQuestionAndOptions> {

    var selQuestionOptionList: DoorLiveData<List<SelQuestionOption>>?

    companion object {

        const val VIEW_NAME = "SelQuestionAndOptionsEditView"

    }

}