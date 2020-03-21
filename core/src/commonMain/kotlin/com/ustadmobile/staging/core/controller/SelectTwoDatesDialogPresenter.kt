package com.ustadmobile.core.controller



import com.ustadmobile.core.view.SelectTwoDatesDialogView


/**
 * The SelectTwoDatesDialog Presenter.
 */
class SelectTwoDatesDialogPresenter(context: Any, arguments: Map<String, String>?, view:
SelectTwoDatesDialogView) : UstadBaseController<SelectTwoDatesDialogView>(context, arguments!!,
        view) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    fun handleClickPrimaryActionButton() {
        view.finish()
    }

}
