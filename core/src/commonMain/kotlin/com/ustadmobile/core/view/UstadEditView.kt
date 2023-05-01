package com.ustadmobile.core.view

interface UstadEditView<RT : Any>: UstadSingleEntityView<RT> {

    var fieldsEnabled: Boolean

    companion object {

        const val ARG_ENTITY_JSON = "entity"

        const val DEFAULT_COMMIT_DELAY = 200L

    }

}