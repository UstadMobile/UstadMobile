package com.ustadmobile.core.view

interface UstadSingleEntityView<RT>: UstadView {

    var entity: RT?

    var loading: Boolean

}