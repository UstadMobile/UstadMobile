package com.ustadmobile.core.view

import com.ustadmobile.core.controller.SelQuestionAndOptionsEditPresenter
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.SelQuestionAndOptions
import com.ustadmobile.lib.db.entities.SelQuestionOption


interface SelQuestionAndOptionsEditView: UstadEditView<SelQuestionAndOptions> {

    var selQuestionOptionList: DoorMutableLiveData<List<SelQuestionOption>>?
    var selQuestionOptionDeactivateList: DoorMutableLiveData<List<SelQuestionOption>>?
    var typeOptions: List<SelQuestionAndOptionsEditPresenter.OptionTypeMessageIdOption>?

    companion object {

        const val VIEW_NAME = "SelQuestionAndOptionsEditView"

    }

}