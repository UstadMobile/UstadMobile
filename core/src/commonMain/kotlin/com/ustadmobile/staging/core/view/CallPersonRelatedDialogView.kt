package com.ustadmobile.core.view

import com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter


interface CallPersonRelatedDialogView : UstadView {
    fun finish()
    fun setOnDisplay(numbers: LinkedHashMap<Int, CallPersonRelatedDialogPresenter.NameWithNumber>)
    fun handleClickCall(number: String)
    fun showRetention(show: Boolean)

    companion object {
        val VIEW_NAME = "CallPersonRelatedDialogView"
    }
}
