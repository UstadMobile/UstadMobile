package com.ustadmobile.core.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.door.paging.DataSourceFactory

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
    DELETE(MessageID.delete, 2),
    MOVE(MessageID.move,3),
    HIDE(MessageID.hide, 4),
    UNHIDE(MessageID.unhide, 5)
}

/**
 *
 */
interface UstadListView<RT, DT>: UstadView {

    var addMode: ListViewAddMode

    var list: DataSourceFactory<Int, DT>?

    @Deprecated("impl sortOptions from UstadListPresenter")
    var sortOptions: List<MessageIdOption>?

    var selectionOptions: List<SelectionOption>?

    /**
     * This is an optional list of chips that could be used to filter the list. E.g. for classes,
     * we may offer 'Active classes', 'My classes', and 'All'.
     *
     * This is optional. It can be null or an empty list when there are no filter options on this
     * view.
     */
    var listFilterOptionChips: List<ListFilterIdOption>?

    /**
     * The currently selected filter chip. The ListView must call the ListPresenter's
     * onListFilterOptionSelected when this is changed.
     */
    var checkedFilterOptionChip: ListFilterIdOption?

}