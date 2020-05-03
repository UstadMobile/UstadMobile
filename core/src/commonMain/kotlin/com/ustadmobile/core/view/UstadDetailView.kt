package com.ustadmobile.core.view

enum class EditButtonMode {
    GONE, FAB
}

interface UstadDetailView<RT>: UstadSingleEntityView<RT> {

    var editButtonMode: EditButtonMode

}