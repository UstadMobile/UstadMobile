package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.SelQuestionSet
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions

interface SelQuestionSetListView: UstadListView<SelQuestionSet, SELQuestionSetWithNumQuestions> {

    companion object {
        const val VIEW_NAME = "SelQuestionSetListView"
    }

}