package com.ustadmobile.core.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.door.paging.DataSourceFactory
import dev.icerock.moko.resources.StringResource

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
enum class SelectionOption(val stringResource: StringResource, val commandId: Int) {
    EDIT(MR.strings.edit, 1),
    DELETE(MR.strings.delete, 2),
    MOVE(MR.strings.move,3),
    HIDE(MR.strings.hide, 4),
    UNHIDE(MR.strings.unhide, 5)
}

/**
 *
 */
interface UstadListView<RT, DT: Any>: UstadView {

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