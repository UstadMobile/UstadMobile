package com.ustadmobile.core.view

interface UstadEditView<RT : Any>: UstadSingleEntityView<RT> {

    var fieldsEnabled: Boolean

    /**
     * Show a dialog to prompt the user to choose explicitly between discarding or saving changes
     * in case they use back navigation before selecting to save.
     */
    fun showSaveOrDiscardChangesDialog()

    companion object {

        const val ARG_ENTITY_JSON = "entity"

    }

}