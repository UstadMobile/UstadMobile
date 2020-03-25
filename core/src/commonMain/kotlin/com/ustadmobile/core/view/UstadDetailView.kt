package com.ustadmobile.core.view

enum class EditButtonVisibility {
    GONE, FAB
}

interface UstadDetailView<RT>: UstadSingleEntityView<RT> {

    var editButtonVisibility: EditButtonVisibility

}