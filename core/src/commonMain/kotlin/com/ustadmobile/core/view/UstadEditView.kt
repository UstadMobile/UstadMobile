package com.ustadmobile.core.view

interface UstadEditView<RT>: UstadSingleEntityView<RT> {

    var fieldsEnabled: Boolean

    fun finishWithResult(result: RT)

}