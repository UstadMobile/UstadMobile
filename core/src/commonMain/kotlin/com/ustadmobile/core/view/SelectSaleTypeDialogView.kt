package com.ustadmobile.core.view


/**
 * SelectSaleTypeDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SelectSaleTypeDialogView : UstadView {


    /**
     * For Android: closes the activity.
     */
    fun finish()

    companion object {

        val VIEW_NAME = "SelectSaleTypeDialog"
    }

}
