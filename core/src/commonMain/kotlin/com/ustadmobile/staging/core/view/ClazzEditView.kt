package com.ustadmobile.core.view


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.Schedule

/**
 * ClazzEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface ClazzEditView : UstadView {

    /**
     * For Android: closes the activity.
     */
    fun finish()

    fun updateToolbarTitle(titleName: String)

    fun setInProgress(inProgress: Boolean)

    /**
     * Provider for schedule of this class.
     *
     * @param clazzScheduleProvider The Provider of Schedule type
     */
    fun setClazzScheduleProvider(clazzScheduleProvider: DataSource.Factory<Int, Schedule>)

    fun updateClazzEditView(updatedClazz: Clazz)

    fun setHolidayPresets(presets: Array<String>, position: Int)
    fun setLocationPresets(presets: Array<String>, position: Int)
    fun setTimezonePresets(presets: Array<String?>, position: Int)
    fun setTimeZonePosition(position: Int)
    /**
     * Handles holiday selected
     * @param position    The id/position of the DateRange selected from the drop-down.
     */
    fun setHolidaySelected(position: Int)

    fun setLocationSelected(position: Int)

    fun setTimezoneSelected(position: Int)

    fun addCustomFieldText(label: CustomField, value: String)
    fun addCustomFieldDropdown(label: CustomField, options: Array<String>, selected: Int)
    fun clearAllCustomFields()

    fun updateLocationDataAdapter(locations: List<Location>)

    fun updateLocationSetName(locationName : String)

    companion object {

        val VIEW_NAME = "ClazzEdit"

        val ARG_NEW = "ArgNew"

        val ARG_SCHEDULE_UID = "argScheduleUid"
    }

}
