package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.CustomFieldDao
import com.ustadmobile.core.db.dao.CustomFieldValueDao
import com.ustadmobile.core.db.dao.CustomFieldValueOptionDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddScheduleDialogView
import com.ustadmobile.core.view.ClazzEditView
import com.ustadmobile.core.view.ClazzEditView.Companion.ARG_SCHEDULE_UID
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.SelectClazzFeaturesView
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_ACTIVITY_ENABLED
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_ATTENDANCE_ENABLED
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_CLAZZUID
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_SEL_ENABLED
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * The ClazzEdit Presenter - responsible for The logic behind editing a Clazz.
 * Usually called when adding a new class or editing a current one from the Class list and Class
 * Detail screens.
 */
class ClazzEditPresenter(context: Any, arguments: Map<String, String>?, view: ClazzEditView,
                         val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : CommonHandlerPresenter<ClazzEditView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var currentClazzUid: Long = -1
    private var mOriginalClazz: Clazz? = null
    private var mUpdatedClazz: Clazz? = null

    private var tempClazzLocationUid: Long = 0

    private var clazzScheduleLiveData: DataSource.Factory<Int, Schedule>? = null
    private var holidaysLiveData: DoorLiveData<List<UMCalendar>>? = null
    private var locationsLiveData: DoorLiveData<List<Location>>? = null

    private var loggedInPersonUid = 0L

    private val viewIdToCustomFieldUid: HashMap<Int, Long>

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    private val clazzDao = repository.clazzDao
    private val customFieldDao: CustomFieldDao
    private val customFieldValueDao: CustomFieldValueDao
    private val customFieldValueOptionDao: CustomFieldValueOptionDao

    internal var holidayCalendarUidToPosition: HashMap<Long, Int>?= null
    internal var positionToHolidayCalendarUid: HashMap<Int, Long>? = null

    internal var locationUidToPosition: HashMap<Long, Int>? = null
    internal var positionToLocationUid: HashMap<Int, Long>? = null

    init {
        customFieldDao = repository.customFieldDao
        customFieldValueDao = repository.customFieldValueDao
        customFieldValueOptionDao = repository.customFieldValueOptionDao

        viewIdToCustomFieldUid = HashMap()
    }

    fun addToMap(viewId: Int, fieldId: Long) {
        viewIdToCustomFieldUid[viewId] = fieldId
    }

    private fun getAllClazzCustomFields() {
        //0. Clear all added custom fields on view.
        view.runOnUiThread(Runnable{ view.clearAllCustomFields() })

        //1. Get all custom fields
        GlobalScope.launch {
            val result = customFieldDao.findAllCustomFieldsProviderForEntityAsync(Clazz.TABLE_ID)
            for (c in result!!) {

                //Get value as well
                val result2 = customFieldValueDao.findValueByCustomFieldUidAndEntityUid(
                        c.customFieldUid, mUpdatedClazz!!.clazzUid)
                var valueString: String? = ""
                var valueSelection = 0

                if (c.customFieldType == CustomField.FIELD_TYPE_TEXT) {

                    if (result2 != null) {
                        valueString = result2.customFieldValueValue
                    }
                    val finalValueString = valueString
                    view.runOnUiThread(Runnable{ view.addCustomFieldText(c,
                            finalValueString!!) })

                } else if (c.customFieldType == CustomField.FIELD_TYPE_DROPDOWN) {
                    if (result2 != null) {
                        valueSelection = result2.customFieldValueValue!!.toInt()
                    }
                    val finalValueSelection = valueSelection
                    val result3 = customFieldValueOptionDao.findAllOptionsForFieldAsync(
                            c.customFieldUid)
                    val options = ArrayList<String>()

                    for (o in result3!!) {
                        options.add(o.customFieldValueOptionName!!)
                    }

                    view.runOnUiThread(Runnable{
                        view.addCustomFieldDropdown(c,
                                options.toTypedArray<String>(),
                                finalValueSelection)
                    })

                }

            }
        }
    }

    /**
     * In Order:
     * 1. Gets the clazz object
     * 2. Observes changes in the Class object and attaches to handleClazzValueChanged()
     * 3. Gets the Schedule live data for this class.
     * 4. Updates the view with the schedule provider.
     *
     * @param savedState The saved state
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        if (arguments.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments.get(ARG_CLAZZ_UID)!!.toLong()
            initFromClazz(currentClazzUid)
        } else if (arguments!!.containsKey(ClazzEditView.ARG_NEW)) {
            GlobalScope.launch {
                var timeZoneString : String = ""
                //TODO: KMP: TimeZone alternative
//                timeZoneString = TimeZone.getDefault().getID()
                timeZoneString = ""

                val newLocationUid = repository.locationDao.insertAsync(Location("Temp Location",
                        "Temp location", timeZoneString))

                tempClazzLocationUid = newLocationUid!!

                val clazzUid = clazzDao.insertAsync(Clazz("", newLocationUid))
                initFromClazz(clazzUid!!)

            }
        }
    }

    private fun initFromClazz(clazzUid: Long) {
        var thisP = this
        this.currentClazzUid = clazzUid

        //Get person live data and observe
        val clazzLiveData = clazzDao.findByUidLive(currentClazzUid)
        //Observe the live data
        view.runOnUiThread(Runnable {
            clazzLiveData.observe(thisP, thisP::handleClazzValueChanged)
        })



        GlobalScope.launch {


            val result = clazzDao.findByUidAsync(currentClazzUid)

            mUpdatedClazz = result
            currentClazzUid = mUpdatedClazz!!.clazzUid
            view.runOnUiThread(Runnable{ view.updateClazzEditView(result!!) })

            //Holidays
            holidaysLiveData = repository.umCalendarDao.findAllHolidaysLiveData()
            view.runOnUiThread(Runnable {
                holidaysLiveData!!.observe(thisP, thisP::handleAllHolidaysChanged)
            })


            //Locations
            locationsLiveData = repository.locationDao.findAllActiveLocationsLive()
            view.runOnUiThread(Runnable {
                locationsLiveData!!.observe(thisP, thisP::handleAllLocationsChanged)
            })

            getAllClazzCustomFields()

        }

        //Set Schedule live data:
        clazzScheduleLiveData = repository.scheduleDao.findAllSchedulesByClazzUid(currentClazzUid)
        updateViewWithProvider()
    }


    /**
     * Common method to update the provider st on this Presenter to the view.
     */
    private fun updateViewWithProvider() {
        view.setClazzScheduleProvider(clazzScheduleLiveData!!)
    }

    /**
     * Handles the change in holidays called (mostly called from UMCalendar live data observing.
     * Upon this method call (ie: when calendar updates, it will set the DateRange presets on
     * the view.
     *
     * @param umCalendar The list of UMCalendar holidays
     */
    private fun handleAllHolidaysChanged(umCalendar: List<UMCalendar>?) {
        var selectedPosition = 0

        holidayCalendarUidToPosition = HashMap()
        positionToHolidayCalendarUid = HashMap()

        val holidayList = ArrayList<String>()
        var pos = 0
        for (ec in umCalendar!!) {
            holidayList.add(ec.umCalendarName!!)
            holidayCalendarUidToPosition!![ec.umCalendarUid] = pos
            positionToHolidayCalendarUid!![pos] = ec.umCalendarUid
            pos++
        }
        val holidayPreset = holidayList.toTypedArray<String>()

        if (mOriginalClazz == null) {
            mOriginalClazz = Clazz()
        }

        if (mOriginalClazz!!.clazzHolidayUMCalendarUid != 0L) {
            if (holidayCalendarUidToPosition!!.containsKey(
                            mOriginalClazz!!.clazzHolidayUMCalendarUid))
                selectedPosition = holidayCalendarUidToPosition!![mOriginalClazz!!.clazzHolidayUMCalendarUid]!!
        }

        view.setHolidayPresets(holidayPreset, selectedPosition)
    }

    /**
     *
     *
     * @param locations The list of Locations available.
     */
    private fun handleAllLocationsChanged(locations: List<Location>?) {
        var selectedPosition = 0

        locationUidToPosition = HashMap()
        positionToLocationUid = HashMap()

        val locationList = ArrayList<String>()
        var pos = 0
        for (el in locations!!) {
            locationList.add(el.title!!)
            locationUidToPosition!![el.locationUid] = pos
            positionToLocationUid!![pos] = el.locationUid
            pos++
        }
        val locationPreset = locationList.toTypedArray<String>()

        if (mOriginalClazz == null) {
            mOriginalClazz = Clazz()
        }

        if (mOriginalClazz!!.clazzLocationUid != 0L) {
            if (locationUidToPosition!!.containsKey(mOriginalClazz!!.clazzLocationUid))
                selectedPosition = locationUidToPosition!![mOriginalClazz!!.clazzLocationUid]!!
        }

        view.setLocationPresets(locationPreset, selectedPosition)
    }

    /**
     * EVent method for when Features are updated.
     */
    fun updateFeatures(clazz: Clazz) {
        mUpdatedClazz!!.isAttendanceFeature = clazz.isAttendanceFeature
        mUpdatedClazz!!.isActivityFeature = clazz.isActivityFeature
        mUpdatedClazz!!.isSelFeature = clazz.isSelFeature
    }

    /**
     * Updates the class name of the currently editing class. Does NOT persist the data.
     *
     * @param newName The class name
     */
    fun updateName(newName: String) {
        mUpdatedClazz!!.clazzName = newName
    }

    /**
     * Updates the class description of the currently editing class. Does NOT persist the data.
     * @param newDesc The new class description.
     */
    fun updateDesc(newDesc: String) {
        mUpdatedClazz!!.clazzDesc = newDesc
    }

    /**
     * Updates the class holiday calendar set to the currently editing class. Does NOT persist the
     * data to the database.
     *
     * @param position The position of the DateRange Calendars from the DateRange drop down preset.
     */
    fun updateHoliday(position: Int) {
        if (positionToHolidayCalendarUid != null && positionToHolidayCalendarUid!!.containsKey(position)) {
            val holidayCalendarUid = positionToHolidayCalendarUid!![position]!!
            mUpdatedClazz!!.clazzHolidayUMCalendarUid = holidayCalendarUid
        }
    }

    fun updateLocation(position: Int) {
        if (positionToLocationUid != null && positionToLocationUid!!.containsKey(position)) {
            val locationUid = positionToLocationUid!![position]!!
            mUpdatedClazz!!.clazzLocationUid = locationUid
        }
    }

    /**
     * Method that handles every Class change (usually from observing an entry from the DB) that
     * gets set on this Presenter's onCreate(). Its job here is to update the current working
     * class and the view as well.
     *
     * This will get called every time we stat editing a class and again if the Class gets updated
     * while we are editing it (ie: on the screen and this presenter). While rare, we are handling
     * it.
     *
     * @param clazz The Class object that got updated.
     */
    private fun handleClazzValueChanged(clazz: Clazz?) {
        //set the og person value
        if (mOriginalClazz == null)
            mOriginalClazz = clazz

        if (mUpdatedClazz == null || mUpdatedClazz != clazz) {
            if (clazz != null) {
                //update class edit views
                view.updateClazzEditView(mUpdatedClazz!!)
                //Update the currently editing class object
                mUpdatedClazz = clazz
            }
        }
    }

    /**
     * Handles what happens when Add Schedule button clicked - to add a new schedule to the Class.
     * Over here, we open the AddScheduleDialogView Dialog for that Class.
     */
    fun handleClickAddSchedule() {
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
        impl.go(AddScheduleDialogView.VIEW_NAME, args, context)
    }

    fun handleSaveCustomFieldValues(viewId: Int, type: Int, value: Any) {

        //Lookup viewId
        if (viewIdToCustomFieldUid.containsKey(viewId)) {
            val customFieldUid = viewIdToCustomFieldUid[viewId]!!

            var valueString: String? = null
            if (type == CustomField.FIELD_TYPE_TEXT) {
                valueString = value.toString()

            } else if (type == CustomField.FIELD_TYPE_DROPDOWN) {
                val spinnerSelection = value as Int
                valueString = spinnerSelection.toString()
            }
            if (valueString != null && !valueString.isEmpty()) {
                val finalValueString = valueString
                GlobalScope.launch {
                    val result = customFieldValueDao.findValueByCustomFieldUidAndEntityUid(
                            customFieldUid, currentClazzUid)
                    val customFieldValue: CustomFieldValue?
                    if (result == null) {
                        customFieldValue = CustomFieldValue()
                        customFieldValue.customFieldValueEntityUid = mUpdatedClazz!!.clazzUid
                        customFieldValue.customFieldValueFieldUid = customFieldUid
                        customFieldValue.customFieldValueValue = finalValueString
                        customFieldValueDao.insert(customFieldValue)
                    } else {
                        customFieldValue = result
                        customFieldValue.customFieldValueValue = finalValueString
                        customFieldValueDao.update(customFieldValue)
                    }
                }
            }
        }
    }

    /**
     * Handles when the Class Edit screen's done/tick button is pressed. This intent denotes
     * confirmation of all changes done in the screen. Hence, the method will persist the updated
     * Class object, set it to active, and finish(close) the screen.
     *
     */
    fun handleClickDone() {
        mUpdatedClazz!!.isClazzActive = true
        GlobalScope.launch {
            val result = repository.locationDao.findByUidAsync(mUpdatedClazz!!.clazzLocationUid)
            if (result!!.locationUid == tempClazzLocationUid) {
                result.title = mUpdatedClazz!!.clazzName!! + "'s default location"
                result.locationActive = (true)
                repository.locationDao.update(result)

                //TODO: Set Temp location to false
            }
        }

        GlobalScope.launch {
            clazzDao.updateClazzAsync(mUpdatedClazz!!, loggedInPersonUid)
            //Close the activity.
            view.finish()
        }
    }


    /**
     * Handles the primary button pressed on the recycler adapter on the Class Edit page. This is
     * triggered from the options menu of each schedule in the Class edit screen. The primary task
     * here is to edit this Schedule assigned to this Clazz.
     *
     * @param arg Any argument needed - Not used here.
     */
    override fun handleCommonPressed(arg: Any) {
        // To edit the schedule assigned to clazz
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
        args.put(ARG_SCHEDULE_UID, arg.toString())
        impl.go(AddScheduleDialogView.VIEW_NAME, args, context)
    }

    /**
     * Handles the secondary button pressed on the recycler adapter on the Class Edit page. This is
     * triggered from the options menu of each schedule in the Class edit screen. The secondary task
     * here is to remove this Schedule assigned to this Clazz.
     *
     * @param arg Any argument needed - The Schedule to be deleted's UID
     */
    override fun handleSecondaryPressed(arg: Any) {
        //To delete schedule assigned to clazz
        val scheduleDao = repository.scheduleDao
        GlobalScope.launch {
            scheduleDao.disableSchedule(arg as Long)
        }
    }

    fun handleClickFeaturesSelection() {
        val args = HashMap<String, String>()
        args.put(CLAZZ_FEATURE_CLAZZUID, currentClazzUid.toString())
        args.put(CLAZZ_FEATURE_ATTENDANCE_ENABLED, if (mUpdatedClazz!!.isAttendanceFeature) "yes" else "no")
        args.put(CLAZZ_FEATURE_ACTIVITY_ENABLED, if (mUpdatedClazz!!.isActivityFeature) "yes" else "no")
        args.put(CLAZZ_FEATURE_SEL_ENABLED, if (mUpdatedClazz!!.isSelFeature) "yes" else "no")
        impl.go(SelectClazzFeaturesView.VIEW_NAME, args, context)
    }

    fun handleUpdateStartTime(startTime: Long) {
        mUpdatedClazz!!.clazzStartTime = startTime
    }

    fun handleUpdateEndTime(endTime: Long) {
        mUpdatedClazz!!.clazzEndTime = endTime
    }
}
