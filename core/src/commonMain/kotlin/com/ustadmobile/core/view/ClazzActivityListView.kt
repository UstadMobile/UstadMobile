package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzActivityWithChangeTitle

/**
 * ClazzActivityList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ClazzActivityListView : UstadView {

    /**
     * Sets Current provider
     *
     *
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param listProvider The provider data
     */
    fun setListProvider(listProvider: DataSource.Factory<Int, ClazzActivityWithChangeTitle>)

    /**
     * For Android: closes the activity.
     */
    fun finish()

    fun updateActivityBarChart(dataMap: LinkedHashMap<Float, Float>)

    fun setClazzActivityChangesDropdownPresets(presets: Array<String?>)

    /**
     * Resets the report buttons
     */
    fun resetReportButtons()


    fun setFABVisibility(visible: Boolean)

    companion object {

        val VIEW_NAME = "ClazzActivityList"

        val ACTIVITY_BAR_LABEL_DESC = "Activity Bar chart"
        val ACTIVITY_BAR_CHART_HEIGHT = 100
        val ACTIVITY_BAR_CHART_AXIS_MINIMUM = 0

        val CHART_DURATION_LAST_WEEK = 1
        val CHART_DURATION_LAST_MONTH = 2
        val CHART_DURATION_LAST_YEAR = 3
    }
}
