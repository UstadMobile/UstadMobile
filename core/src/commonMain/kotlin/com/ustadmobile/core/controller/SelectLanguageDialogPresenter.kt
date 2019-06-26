package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.SelectLanguageDialogView


/**
 * Presenter for SelectLanguageDialog view
 */
class SelectLanguageDialogPresenter(context: Any,
                                    arguments: Map<String, String?>,
                                    view: SelectLanguageDialogView)
    : UstadBaseController<SelectLanguageDialogView>(context, arguments, view) {

    internal var repository: UmAppDatabase

    init {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
    }


}
