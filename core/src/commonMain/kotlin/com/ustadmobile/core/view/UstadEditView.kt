package com.ustadmobile.core.view

interface UstadEditView<RT>: UstadView {

    var entity: RT?

    var loading: Boolean

    var fieldsEnabled: Boolean

    fun finishWithResult(result: RT)

    fun finish()

}