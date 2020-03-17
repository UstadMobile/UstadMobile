package com.ustadmobile.core.view

interface AddQuestionSetDialogView : UstadView, DismissableDialog {
    fun finish()

    companion object {
        val VIEW_NAME = "AddQuestionSetDialogView"
    }
}
