package com.ustadmobile.core.view

enum class EditButtonMode {
    GONE, FAB
}

interface UstadDetailView<RT : Any>: UstadSingleEntityView<RT> {

    var editButtonMode: EditButtonMode

}