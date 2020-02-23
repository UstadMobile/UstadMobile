package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.CustomFieldDao
import com.ustadmobile.core.db.dao.CustomFieldValueDao
import com.ustadmobile.core.db.dao.CustomFieldValueOptionDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.staging.core.util.TimeZoneUtil
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

    private var timezoneSelected: String ?= ""

    private var tempClazzLocationUid: Long = 0

    private var clazzScheduleLiveData: DataSource.Factory<Int, Schedule>? = null
    private var holidaysLiveData: DoorLiveData<List<UMCalendar>>? = null
    private var locationsLiveData: DoorLiveData<List<Location>>? = null

    private var loggedInPersonUid = 0L

    private val viewIdToCustomFieldUid: HashMap<Int, Long>

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    internal var database = UmAccountManager.getActiveDatabase(context)
    private val clazzDao = repository.clazzDao
    private val clazzDaoDB = database.clazzDao
    private val locationDao = repository.locationDao
    private val locationDaoDB = database.locationDao
    private val customFieldDao: CustomFieldDao
    private val customFieldDaoDB = database.customFieldDao
    private val customFieldValueDao: CustomFieldValueDao
    private val customFieldValueOptionDao: CustomFieldValueOptionDao

    internal var holidayCalendarUidToPosition: HashMap<Long, Int>?= null
    internal var positionToHolidayCalendarUid: HashMap<Int, Long>? = null

    internal var locationUidToPosition: HashMap<Long, Int>? = null
    internal var positionToLocationUid: HashMap<Int, Long>? = null
    internal var positionToTimeZoneUid: HashMap<Long, String>? = null
    internal var timeZoneToPositionId: HashMap<String, Int>? = null

    init {
        customFieldDao = repository.customFieldDao
        customFieldValueDao = repository.customFieldValueDao
        customFieldValueOptionDao = repository.customFieldValueOptionDao

        viewIdToCustomFieldUid = HashMap()
    }

    fun addToMap(viewId: Int, fieldId: Long) {
        viewIdToCustomFieldUid[viewId] = fieldId
    }

    fun handleLocationTyped(locationName: String){

        val name = "%$locationName%"

        GlobalScope.launch {
            val locations = locationDaoDB.findByTitleLikeAsync(name)
            view.runOnUiThread(Runnable {
                view.updateLocationDataAdapter(locations)
            })
        }
    }

    private fun getAllClazzCustomFields() {
        //0. Clear all added custom fields on view.
        view.runOnUiThread(Runnable{ view.clearAllCustomFields() })

        //1. Get all custom fields
        GlobalScope.launch {
            val result = customFieldDao.findAllCustomFieldsProviderForEntityAsync(Clazz.TABLE_ID)
            for (c in result) {

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
                //TODOne: KMP: TimeZone alternative
                timeZoneString = ""

                val tz = TimeZoneUtil()
                var deviceTZString = tz.getDeviceTimezone()

                // The issue this is fixing is, the result "GMT+04:00" is converted to "GMT+4:00"
                if(
                        (deviceTZString.startsWith("GMT+0") && !deviceTZString.startsWith("GMT+0:"))
                        ||
                        (deviceTZString.startsWith("GMT-0") && !deviceTZString.startsWith("GMT-0:")))

                {
                    deviceTZString = "GMT+" + deviceTZString.substring(5)
                }

                var tempLocation = Location("",
                        "Temp location", timeZoneString)
                tempLocation.locationActive = false
                tempLocation.timeZone = deviceTZString

                val newLocationUid = locationDaoDB.insertAsync(tempLocation)

                tempClazzLocationUid = newLocationUid!!


                val clazzUid = clazzDaoDB.insertAsync(Clazz("", newLocationUid))
                initFromClazz(clazzUid!!)

            }
        }
    }

    private fun initFromClazz(clazzUid: Long) {
        var thisP = this
        this.currentClazzUid = clazzUid

        GlobalScope.launch {
            val clazzData = clazzDaoDB.findByUidAsync(currentClazzUid)
            handleClazzValueChanged(clazzData)

            mUpdatedClazz = clazzData
            currentClazzUid = mUpdatedClazz!!.clazzUid

            //Holidays
            holidaysLiveData = repository.umCalendarDao.findAllHolidaysLiveData()
            view.runOnUiThread(Runnable {
                holidaysLiveData!!.observeWithPresenter(thisP, thisP::handleAllHolidaysChanged)
            })

            //Timezones
            updateTimezonePreset()

            //Get location if set
            if(clazzData!!.clazzLocationUid != 0L){
                val location = locationDaoDB.findByUidAsync(clazzData!!.clazzLocationUid)
                view.runOnUiThread(Runnable {
                    var locationTimeZone = location!!.timeZone
                    if(locationTimeZone != null && !locationTimeZone.isEmpty()){
                        if(!locationTimeZone.startsWith("GMT-") && !locationTimeZone.startsWith("GMT+")){
                            locationTimeZone = "GMT+" + locationTimeZone.substring(3)
                        }
                        if(timeZoneToPositionId!!.containsKey(locationTimeZone)) {
                            view.setTimeZonePosition(timeZoneToPositionId!!.get(locationTimeZone)!! - 1)
                        }
                    }
                    view.updateLocationSetName(location!!.title!!)
                })
            }

            getAllClazzCustomFields()

            //Set Schedule live data:
            clazzScheduleLiveData = repository.scheduleDao.findAllSchedulesByClazzUid(currentClazzUid)
            updateViewWithProvider()

        }
    }

    /**
     * Common method to convert Array List to String Array
     *
     * @param presetAL The array list of string type
     * @return  String array
     */
    private fun arrayListToStringArray(presetAL: ArrayList<String>): Array<String?> {
        val objectArr = presetAL.toTypedArray()
        val strArr = arrayOfNulls<String>(objectArr.size)
        for (j in objectArr.indices) {
            strArr[j] = objectArr[j]
        }
        return strArr
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private fun updateTimezonePreset() {
        val presetAL = ArrayList<String>()

        positionToTimeZoneUid = HashMap<Long, String>()
        timeZoneToPositionId = HashMap()

        presetAL.add("GMT-12:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-11:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-10:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-9:30")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-9:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-8:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-7:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-6:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-5:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-4:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-3:30")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-3:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-2:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT-1:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT0:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+1:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))

        presetAL.add("GMT+2:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+3:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+3:30")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+4:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+4:30")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+5:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+5:30")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+5.45")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+6:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+6:30")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+7:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+8:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+8.45")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+9:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+9:30")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+10:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+10:30")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+11:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+12:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+12.45")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+13:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)
        presetAL.add("GMT+14:00")
        positionToTimeZoneUid!!.put(presetAL.size.toLong(), presetAL.get(presetAL.size-1))
        timeZoneToPositionId!!.put(presetAL.get(presetAL.size-1), presetAL.size)


        val sortPresets = arrayListToStringArray(presetAL)

        view.runOnUiThread(Runnable {
            view.setTimezonePresets(sortPresets,0)
        })
    }

    /**
     * Common method to update the provider st on this Presenter to the view.
     */
    private fun updateViewWithProvider() {
        view.runOnUiThread(Runnable {
            view.setClazzScheduleProvider(clazzScheduleLiveData!!)
        })
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
            mOriginalClazz!!.isClazzActive = false
        }

        if (mOriginalClazz!!.clazzHolidayUMCalendarUid != 0L) {
            if (holidayCalendarUidToPosition!!.containsKey(
                            mOriginalClazz!!.clazzHolidayUMCalendarUid))
                selectedPosition = holidayCalendarUidToPosition!![mOriginalClazz!!.clazzHolidayUMCalendarUid]!!
        }

        view.setHolidayPresets(holidayPreset, selectedPosition)
    }

    /**
     * EVent method for when Features are updated.
     */
    fun updateFeatures(clazz: Clazz) {
        mUpdatedClazz!!.clazzFeatures = clazz.clazzFeatures

        GlobalScope.launch {
            clazzDao.updateAsync(mUpdatedClazz!!)
        }
    }

    /**
     * Updates the class name of the currently editing class. Does NOT persist the data.
     *
     * @param newName The class name
     */
    fun updateName(newName: String) {
        if(mUpdatedClazz != null && newName!=null && !newName.isEmpty()) {
            mUpdatedClazz!!.clazzName = newName
        }
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

    fun updateTimezone(position: Int) {
        if (positionToTimeZoneUid != null && positionToTimeZoneUid!!.containsKey(position.toLong())) {
            timezoneSelected = positionToTimeZoneUid!![position.toLong() + 1]!!

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
                //Update the currently editing class object
                mUpdatedClazz = clazz

                //update class edit views
                view.runOnUiThread(Runnable {
                    view.updateClazzEditView(mUpdatedClazz!!)
                })

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

    var selectedLocation : Location ? = null

    /**
     * Handles when the Class Edit screen's done/tick button is pressed. This intent denotes
     * confirmation of all changes done in the screen. Hence, the method will persist the updated
     * Class object, set it to active, and finish(close) the screen.
     *
     */
    fun handleClickDone(newLocation: String) {
        mUpdatedClazz!!.isClazzActive = true

        GlobalScope.launch {

            if(selectedLocation != null ){
                //Set it
                mUpdatedClazz!!.clazzLocationUid = selectedLocation!!.locationUid

            }else {
                val clazzLocation = locationDao.findByUidAsync(mUpdatedClazz!!.clazzLocationUid)
                if (clazzLocation!!.locationUid == tempClazzLocationUid) {
                    if(newLocation != null && !newLocation.isEmpty()){
                        clazzLocation.title = newLocation
                    }else {
                        clazzLocation.title = mUpdatedClazz!!.clazzName!! + "'s default location"
                    }
                    clazzLocation.locationActive = (true)
                    locationDao.update(clazzLocation)
                    selectedLocation = clazzLocation
                }else{
                    selectedLocation = clazzLocation
                }
            }

            if(selectedLocation != null && timezoneSelected != null && !timezoneSelected!!.isEmpty()){
                selectedLocation!!.timeZone = timezoneSelected
                locationDao.update(selectedLocation!!)
            }

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
        args[CLAZZ_FEATURE_CLAZZUID] = currentClazzUid.toString()
        args[CLAZZ_FEATURE_ATTENDANCE_ENABLED] = if (mUpdatedClazz!!.isAttendanceFeature()) "yes" else "no"
        args[CLAZZ_FEATURE_ACTIVITY_ENABLED] = if (mUpdatedClazz!!.isActivityFeature()) "yes" else "no"
        args[CLAZZ_FEATURE_SEL_ENABLED] = if (mUpdatedClazz!!.isSelFeature()) "yes" else "no"
        impl.go(SelectClazzFeaturesView.VIEW_NAME, args, context)
    }

    fun handleUpdateStartTime(startTime: Long) {
        mUpdatedClazz!!.clazzStartTime = startTime
    }

    fun handleUpdateEndTime(endTime: Long) {
        mUpdatedClazz!!.clazzEndTime = endTime
    }
}
