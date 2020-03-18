package com.ustadmobile.core.view

import androidx.paging.DataSource
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
 *
 */
interface UstadListView<RT, DT>: UstadView {

    var addMode: ListViewAddMode

    var list: DataSource.Factory<Int, DT>?

    var sortOptions: List<MessageIdOption>?

    fun finishWithResult(result: List<RT>)

}