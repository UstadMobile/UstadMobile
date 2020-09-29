package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption

enum class ListViewMode(val mode: String) {
    BROWSER("browser"),
    PICKER("picker")
}

enum class ListViewAddMode {
    FIRST_ITEM,
    FAB,
    NONE
}

/**
 * Options that can be shown when the user makes selections in a list.
 */
enum class SelectionOption(val messageId: Int, val commandId: Int) {
    EDIT(MessageID.edit, 1),
    DELETE(MessageID.delete, 2)
}

/**
 *
 */
interface UstadListView<RT, DT>: UstadView {

    var addMode: ListViewAddMode

    var list: DataSource.Factory<Int, DT>?

    @Deprecated("impl sortOptions from UstadListPresenter")
    var sortOptions: List<MessageIdOption>?

    var selectionOptions: List<SelectionOption>?

    fun finishWithResult(result: List<RT>)

}