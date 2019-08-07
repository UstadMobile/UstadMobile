package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.db.dao.CustomFieldDao
import com.ustadmobile.core.db.dao.CustomFieldValueDao
import com.ustadmobile.core.db.dao.CustomFieldValueOptionDao
import com.ustadmobile.core.db.dao.ScheduleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddScheduleDialogView
import com.ustadmobile.core.view.ClazzEditView
import com.ustadmobile.core.view.SelectClazzFeaturesView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.CustomFieldValue
import com.ustadmobile.lib.db.entities.CustomFieldValueOption
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.UMCalendar

import com.ustadmobile.core.view.ClazzEditView.Companion.ARG_SCHEDULE_UID
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_ACTIVITY_ENABLED
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_ATTENDANCE_ENABLED
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_CLAZZUID
import com.ustadmobile.core.view.SelectClazzFeaturesView.Companion.CLAZZ_FEATURE_SEL_ENABLED


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

    private var clazzScheduleLiveData: UmProvider<Schedule>? = null
    private var holidaysLiveData: UmLiveData<List<UMCalendar>>? = null
    private var locationsLiveData: UmLiveData<List<Location>>? = null

    private var loggedInPersonUid = 0L

    private val viewIdToCustomFieldUid: HashMap<Int, Long>

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    private val clazzDao = repository.clazzDao
    private val customFieldDao: CustomFieldDao
    private val customFieldValueDao: CustomFieldValueDao
    private val customFieldValueOptionDao: CustomFieldValueOptionDao

    internal var holidayCalendarUidToPosition: HashMap<Long, Int>
    internal var positionToHolidayCalendarUid: HashMap<Int, Long>? = null

    internal var locationUidToPosition: HashMap<Long, Int>
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
        view.runOnUiThread({ view.clearAllCustomFields() })

        //1. Get all custom fields
        customFieldDao.findAllCustomFieldsProviderForEntityAsync(Clazz.TABLE_ID,
                object : UmCallback<List<CustomField>> {
                    override fun onSuccess(result: List<CustomField>?) {
                        for (c in result!!) {

                            //Get value as well
                            customFieldValueDao.findValueByCustomFieldUidAndEntityUid(
                                    c.customFieldUid, mUpdatedClazz!!.clazzUid,
                                    object : UmCallback<CustomFieldValue> {
                                        override fun onSuccess(result: CustomFieldValue?) {
                                            var valueString: String? = ""
                                            var valueSelection = 0



                                            if (c.customFieldType == CustomField.FIELD_TYPE_TEXT) {

                                                if (result != null) {
                                                    valueString = result.customFieldValueValue
                                                }
                                                val finalValueString = valueString
                                                view.runOnUiThread({ view.addCustomFieldText(c, finalValueString!!) })

                                            } else if (c.customFieldType == CustomField.FIELD_TYPE_DROPDOWN) {
                                                if (result != null) {
                                                    valueSelection = Integer.valueOf(result.customFieldValueValue!!)
                                                }
                                                val finalValueSelection = valueSelection
                                                customFieldValueOptionDao.findAllOptionsForFieldAsync(c.customFieldUid,
                                                        object : UmCallback<List<CustomFieldValueOption>> {
                                                            override fun onSuccess(result: List<CustomFieldValueOption>?) {
                                                                val options = ArrayList<String>()

                                                                for (o in result!!) {
                                                                    options.add(o.customFieldValueOptionName)
                                                                }

                                                                view.runOnUiThread({
                                                                    view.addCustomFieldDropdown(c,
                                                                            options.toTypedArray<String>(),
                                                                            finalValueSelection)
                                                                })
                                                            }

                                                            override fun onFailure(exception: Throwable?) {
                                                                print(exception!!.message)
                                                            }
                                                        })
                                            }
                                        }

                                        override fun onFailure(exception: Throwable?) {
                                            print(exception!!.message)
                                        }
                                    })
                        }
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
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
    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid


        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)
            initFromClazz(currentClazzUid)
        } else if (arguments!!.containsKey(ClazzEditView.ARG_NEW)) {
            repository.locationDao.insertAsync(Location("Temp Location",
                    "Temp location",
                    TimeZone.getDefault().getID()), object : UmCallback<Long> {
                override fun onSuccess(newLocationUid: Long?) {

                    tempClazzLocationUid = newLocationUid!!

                    clazzDao.insertAsync(Clazz("", newLocationUid),
                            object : UmCallback<Long> {
                                override fun onSuccess(result: Long?) {
                                    initFromClazz(result!!)
                                }

                                override fun onFailure(exception: Throwable?) {
                                    print(exception!!.message)
                                }
                            })
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                }
            })
        }
    }

    private fun initFromClazz(clazzUid: Long) {

        this.currentClazzUid = clazzUid
        //Handle Clazz info changed:
        //Get person live data and observe
        val clazzLiveData = clazzDao.findByUidLive(currentClazzUid)
        //Observe the live data
        clazzLiveData.observe(this@ClazzEditPresenter,
                UmObserver<Clazz> { this@ClazzEditPresenter.handleClazzValueChanged(it) })

        clazzDao.findByUidAsync(currentClazzUid, object : UmCallback<Clazz> {
            override fun onSuccess(result: Clazz?) {
                mUpdatedClazz = result
                currentClazzUid = mUpdatedClazz!!.clazzUid
                view.runOnUiThread({ view.updateClazzEditView(result!!) })

                //Holidays
                holidaysLiveData = repository.getUMCalendarDao().findAllHolidaysLiveData()
                holidaysLiveData!!.observe(this@ClazzEditPresenter,
                        UmObserver<List<UMCalendar>> { this@ClazzEditPresenter.handleAllHolidaysChanged(it) })

                //Locations
                locationsLiveData = repository.locationDao.findAllActiveLocationsLive()
                locationsLiveData!!.observe(this@ClazzEditPresenter,
                        UmObserver<List<Location>> { this@ClazzEditPresenter.handleAllLocationsChanged(it) })

                getAllClazzCustomFields()

            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

        //Set Schedule live data:
        clazzScheduleLiveData = repository.scheduleDao
                .findAllSchedulesByClazzUid(currentClazzUid)
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
    private fun handleAllHolidaysChanged(umCalendar: List<UMCalendar>) {
        var selectedPosition = 0

        holidayCalendarUidToPosition = HashMap()
        positionToHolidayCalendarUid = HashMap()

        val holidayList = ArrayList<String>()
        var pos = 0
        for (ec in umCalendar) {
            holidayList.add(ec.umCalendarName)
            holidayCalendarUidToPosition[ec.umCalendarUid] = pos
            positionToHolidayCalendarUid!![pos] = ec.umCalendarUid
            pos++
        }
        var holidayPreset = arrayOfNulls<String>(holidayList.size)
        holidayPreset = holidayList.toTypedArray<String>()

        if (mOriginalClazz == null) {
            mOriginalClazz = Clazz()
        }

        if (mOriginalClazz!!.clazzHolidayUMCalendarUid != 0L) {
            if (holidayCalendarUidToPosition.containsKey(
                            mOriginalClazz!!.clazzHolidayUMCalendarUid))
                selectedPosition = holidayCalendarUidToPosition[mOriginalClazz!!.clazzHolidayUMCalendarUid]!!
        }

        view.setHolidayPresets(holidayPreset, selectedPosition)
    }

    /**
     *
     *
     * @param locations The list of Locations available.
     */
    private fun handleAllLocationsChanged(locations: List<Location>) {
        var selectedPosition = 0

        locationUidToPosition = HashMap()
        positionToLocationUid = HashMap()

        val locationList = ArrayList<String>()
        var pos = 0
        for (el in locations) {
            locationList.add(el.title)
            locationUidToPosition[el.locationUid] = pos
            positionToLocationUid!![pos] = el.locationUid
            pos++
        }
        var locationPreset = arrayOfNulls<String>(locationList.size)
        locationPreset = locationList.toTypedArray<String>()

        if (mOriginalClazz == null) {
            mOriginalClazz = Clazz()
        }

        if (mOriginalClazz!!.clazzLocationUid != 0L) {
            if (locationUidToPosition.containsKey(mOriginalClazz!!.clazzLocationUid))
                selectedPosition = locationUidToPosition[mOriginalClazz!!.clazzLocationUid]!!
        }

        view.setLocationPresets(locationPreset, selectedPosition)
    }

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
        args.put(ARG_CLAZZ_UID, currentClazzUid)
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
                customFieldValueDao.findValueByCustomFieldUidAndEntityUid(customFieldUid,
                        currentClazzUid, object : UmCallback<CustomFieldValue> {
                    override fun onSuccess(result: CustomFieldValue?) {
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

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
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
        repository.locationDao.findByUidAsync(mUpdatedClazz!!.clazzLocationUid,
                object : UmCallback<Location> {
                    override fun onSuccess(result: Location?) {
                        if (result!!.locationUid == tempClazzLocationUid) {
                            result.title = mUpdatedClazz!!.clazzName!! + "'s default location"
                            result.setLocationActive(true)
                            repository.locationDao.update(result)
                        }
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })


        clazzDao.updateClazzAsync(mUpdatedClazz!!, loggedInPersonUid, object : UmCallback<Int> {
            override fun onSuccess(result: Int?) {
                //Close the activity.
                view.finish()
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
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
        args.put(ARG_CLAZZ_UID, currentClazzUid)
        args.put(ARG_SCHEDULE_UID, arg)
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
        scheduleDao.disableSchedule(arg as Long)
    }

    fun handleClickFeaturesSelection() {
        val args = HashMap<String, String>()
        args.put(CLAZZ_FEATURE_CLAZZUID, currentClazzUid)
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
