package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SelQuestionAndOptions
import com.ustadmobile.lib.db.entities.SelQuestionSet

interface SelQuestionSetEditView: UstadEditView<SelQuestionSet> {

    var selQuestionList: DoorLiveData<List<SelQuestionAndOptions>>?

    companion object {

        const val VIEW_NAME = "SelQuestionSetEditView"

    }

}