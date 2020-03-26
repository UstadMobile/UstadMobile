package com.ustadmobile.core.view

interface UstadEditView<RT>: UstadSingleEntityView<RT> {

    var fieldsEnabled: Boolean

    fun finishWithResult(result: RT)

    companion object {

        const val ARG_ENTITY_JSON = "entity"

    }

}