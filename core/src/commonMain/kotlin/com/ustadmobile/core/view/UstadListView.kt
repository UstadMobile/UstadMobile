package com.ustadmobile.core.view

import com.ustadmobile.core.MR
import dev.icerock.moko.resources.StringResource

enum class ListViewMode(val mode: String) {
    BROWSER("BROWSER"),
    PICKER("PICKER")
}

enum class ListViewAddMode {
    FIRST_ITEM,
    FAB,
    NONE
}

/**
 * Options that can be shown when the user makes selections in a list.
 */
enum class SelectionOption(val stringResource: StringResource, val commandId: Int) {
    EDIT(MR.strings.edit, 1),
    DELETE(MR.strings.delete, 2),
    MOVE(MR.strings.move,3),
    HIDE(MR.strings.hide, 4),
    UNHIDE(MR.strings.unhide, 5)
}

