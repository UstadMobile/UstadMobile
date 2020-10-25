package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ClazzWorkQuestionAndOptionsEditPresenter
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptions
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionOption


interface ClazzWorkQuestionAndOptionsEditView: UstadEditView<ClazzWorkQuestionAndOptions> {

    var clazzWorkQuestionOptionList : DoorMutableLiveData<List<ClazzWorkQuestionOption>>?
    var clazzWorkQuestionOptionDeactivateList: DoorMutableLiveData<List<ClazzWorkQuestionOption>>?
    var typeOptions: List<ClazzWorkQuestionAndOptionsEditPresenter.ClazzWorkQuestionOptionTypeMessageIdOption>?
    var errorMessage: String?

    companion object {

        const val VIEW_NAME = "ClazzWorkQuestionAndOptionsEditEditView"

    }

}