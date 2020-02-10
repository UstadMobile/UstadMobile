package com.ustadmobile.core.view

interface PersonPictureDialogView : UstadView, DismissableDialog {
    fun finish()
    fun setPictureOnView(imagePath: String)

    companion object {
        val VIEW_NAME = "PersonPictureDialogView"
        val ARG_PERSON_IMAGE_PATH = "PersonImagePath"
    }

}
