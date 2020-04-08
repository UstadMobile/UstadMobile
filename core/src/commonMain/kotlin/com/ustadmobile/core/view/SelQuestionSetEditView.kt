package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.lib.db.entities.SelQuestionSet


interface SelQuestionSetEditView: UstadEditView<SelQuestionSet> {

    abstract var selQuestionList: DoorLiveData<List<SelQuestion>>?

    companion object {

        const val VIEW_NAME = "SelQuestionSetEditView"

    }

}