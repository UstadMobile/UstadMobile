package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.lib.db.entities.DistinctCategorySchema
import com.ustadmobile.lib.db.entities.LangUidAndName
import com.ustadmobile.lib.db.entities.Language
import kotlin.js.JsName

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

    companion object {

        const val ARG_FILTER_BUTTONS = "filterbuttons"

        const val ARG_DOWNLOADED_CONTENT = "downloaded"

        const val ARG_RECYCLED_CONTENT = "recycled"

        const val ARG_LIBRARIES_CONTENT = "libraries"

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

        const val VIEW_NAME = "ContentEntryList"

        const val CONTENT_CREATE_FOLDER = 1

        const val CONTENT_IMPORT_FILE = 2

        const val CONTENT_CREATE_CONTENT = 3

        const val CONTENT_IMPORT_LINK = 4
    }
}
