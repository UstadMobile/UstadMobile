package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

enum class ContentEntryListViewMode(val viewMode: Int) {
    NORMAL(1),
    PICKER(2)

}

interface ContentEntryListView : UstadView {

    @JsName("setContentEntryProvider")
    fun setContentEntryProvider(entryProvider: DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>)

    @JsName("setToolbarTitle")
    fun setToolbarTitle(title: String)

    @JsName("showError")
    fun showError()

    @JsName("setCategorySchemaSpinner")
    fun setCategorySchemaSpinner(spinnerData: Map<Long, List<DistinctCategorySchema>>)

    @JsName("setLanguageOptions")
    fun setLanguageOptions(result: List<LangUidAndName>)

    fun setEmptyView(selectedFilter: String)

    /**
     * Set filter buttons if applicable (e.g. on Android to show all libraries / downloaded items)
     *
     * Filter buttons MUST be set before setting the contentEntryProvider
     */
    @JsName("setFilterButtons")
    fun setFilterButtons(buttonLabels: List<String>, activeIndex: Int)

    /**
     * Sets whether or not buttons that allow the user to edit content are visibile
     *
     * @param buttonVisibilityFlags - flags for buttons to be shown
     *
     */
    fun setEditButtonsVisibility(buttonVisibilityFlags: Int)

    fun finishWithPickResult(contentEntry: ContentEntry)

    companion object {

        const val ARG_FILTER_BUTTONS = "filterbuttons"

        const val ARG_DOWNLOADED_CONTENT = "downloaded"

        const val ARG_RECYCLED_CONTENT = "recycled"

        const val ARG_LIBRARIES_CONTENT = "libraries"

        /**
         * When used in picker mode, the result will container this extra which will contain a
         * JSON string of the ContentEntry selected by the user
         */
        const val EXTRA_RESULT_CONTENTENTRY = "contentEntry"

        /**
         * show controls to allow adding content
         */
        const val EDIT_BUTTONS_ADD_CONTENT = 1

        /**
         * show an option for creating a new folder
         */
        const val EDIT_BUTTONS_NEWFOLDER = 2

        /**
         * show an option to allow the user to edit this contententry (e.g. the parent of the children in this list)
         */
        const val EDIT_BUTTONS_EDITOPTION = 4

        const val ARG_EDIT_BUTTONS_CONTROL_FLAG = "EditControlFlag"

        const val ARG_VIEWMODE = "viewMode"

        const val VIEW_NAME = "ContentEntryList"

        const val CONTENT_CREATE_FOLDER = 1

        const val CONTENT_IMPORT_FILE = 2

        const val CONTENT_CREATE_CONTENT = 3

        const val CONTENT_IMPORT_LINK = 4


    }
}
