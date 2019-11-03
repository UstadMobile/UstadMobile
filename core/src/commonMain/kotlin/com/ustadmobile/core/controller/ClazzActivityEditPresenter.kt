package com.ustadmobile.core.controller


import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.AddActivityChangeDialogView
import com.ustadmobile.core.view.ClazzActivityEditView
import com.ustadmobile.core.view.ClazzActivityEditView.Companion.ARG_CLAZZACTIVITY_LOGDATE
import com.ustadmobile.core.view.ClazzActivityEditView.Companion.ARG_CLAZZACTIVITY_UID
import com.ustadmobile.core.view.ClazzActivityEditView.Companion.THUMB_BAD
import com.ustadmobile.core.view.ClazzActivityEditView.Companion.THUMB_GOOD
import com.ustadmobile.core.view.ClazzActivityEditView.Companion.THUMB_OFF
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.lib.db.entities.ClazzActivity
import com.ustadmobile.lib.db.entities.ClazzActivityChange
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * The ClazzActivityEdit Presenter - Responsible for the logic behind the view that edits a
 * Clazz Activity or creates one new if doesn't exist.
 *
 * Called when editing a Clazz Activity or adding a new Clazz Activity.
 */
class ClazzActivityEditPresenter (context: Any, arguments: Map<String, String>?,
                                  view: ClazzActivityEditView,
                                  val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<ClazzActivityEditView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var currentClazzUid = 0L
    private var currentLogDate = 0L
    private var currentClazzActivityUid = 0L

    private var changeSelected = false
    private var measurementEntered = false

    private var currentClazzActivityChangeUid = 0L
    private val loggedInPersonUid: Long

    //The current clazz activity being edited.
    private var currentClazzActivity: ClazzActivity? = null

    //The mapping of activity change uid to drop - down id on the view.
    private var changeToIdMap: HashMap<Long, Long>? = null

    //Daos needed - ClazzActivtyDao and ClazzActivityChangeDao
    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    private val clazzActivityDao = repository.clazzActivityDao
    private val activityChangeDao = repository.clazzActivityChangeDao
    private val clazzdao = repository.clazzDao

    init {

        //Get Clazz Uid
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = (arguments!!.get(ARG_CLAZZ_UID)!!.toString()).toLong()
        }
        //Get Log Date
        if (arguments!!.containsKey(ARG_CLAZZACTIVITY_LOGDATE)) {
            val thisLogDate = arguments!!.get(ARG_CLAZZACTIVITY_LOGDATE)!!.toString().toLong()
            currentLogDate = (thisLogDate)
        }
        //Get Activity Uid (if editing)
        if (arguments!!.containsKey(ARG_CLAZZACTIVITY_UID)) {
            currentClazzActivityUid = (arguments!!.get(ARG_CLAZZACTIVITY_UID)!!.toString()).toLong()
        }

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

    }

    /**
     * Order:
     * 1. Gets the Clazz Activity first by clazz activity id provided (editing). If invalid,
     * find Clazz Activity associated with the Clazz id and log date.
     * 2. Updates the toolbar of the view.
     * 3. Creates a new Clazz Activity if it doesn't exist (usually: clicked Record Activity)
     * 4. Updates all available Clazz Activity Changes for this clazz (usually all) to
     * the view.
     *
     * @param savedState    Saved state
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Check permissions
        checkPermissions()
    }

    /**
     * Checks permission and updates view accordingly
     */
    fun checkPermissions() {
        GlobalScope.launch {
            val result = clazzdao.personHasPermission(loggedInPersonUid, currentClazzUid,
                    Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT)
            setActivityEditable(result!!)
        }

    }

    /**
     * Starts the logic for filling up activity page.
     */
    private fun fillClazzActivity() {
        //Find Activity first by activity id. If it is not valid, find by clazz uid and log date.

        var result : ClazzActivity? = null

        GlobalScope.launch {
            //If activity uid given , find the ClazzActivity:
            if (currentClazzActivityUid != 0L) {

                result = clazzActivityDao.findByUidAsync(currentClazzActivityUid)
                if (result != null) {
                    currentClazzUid = result!!.clazzActivityClazzUid
                    currentLogDate = result!!.clazzActivityLogDate
                }
            } else {
                result = clazzActivityDao.findByClazzAndDateAsync(currentClazzUid,
                        currentLogDate)
            }//Else find by Clazz uid and Log date given:

            //Check if ClazzActivity exists. If it doesn't, create it
            checkActivityCreateIfNotExist(result)
        }
    }

    /**
     * Common method to check if given ClazzActivity exists. If it doesn't create it. Afterwards
     * update the ClazzActivityChange options on the view
     *
     * @param result The ClazzActivity object to check.
     */
    private fun checkActivityCreateIfNotExist(result: ClazzActivity?) {

        val clazzDao = repository.clazzDao


        //Update Activity Change options
        updateChangeOptions()

        //Update any toolbar title
        val currentClazz = clazzDao.findByUid(currentClazzUid)
        view.updateToolbarTitle(currentClazz!!.clazzName + " "
                + impl.getString(MessageID.activity, context))

        //Create one anyway given ClazzActivity doesn't exist (is null)
        if (result == null) {
            GlobalScope.launch {
                val result = clazzActivityDao.createClazzActivityForDate(currentClazzUid, currentLogDate)
                currentClazzActivityUid = result!!
                currentClazzActivity = clazzActivityDao.findByUid(result)
                view.setThumbs(THUMB_OFF)
            }
        }else {

            currentClazzActivity = result

            //set current clazz activity change
            currentClazzActivityChangeUid = currentClazzActivity!!.clazzActivityClazzActivityChangeUid
            currentLogDate = currentClazzActivity!!.clazzActivityLogDate
            handleChangeFeedback(currentClazzActivity!!.isClazzActivityGoodFeedback)
            view.setNotes(currentClazzActivity!!.clazzActivityNotes!!)
            view.setUOMText(currentClazzActivity!!.clazzActivityQuantity.toString())
        }//Set up presenter and start filling the UI with its elements


        //Update date
        updateViewDateHeading()
    }

    /**
     * Handles notes added/edited to this Clazz Activity.
     *
     * @param newNote   The new note string
     */
    fun handleChangeNotes(newNote: String) {
        currentClazzActivity!!.clazzActivityNotes = newNote
    }

    /**
     * Opens new Add ActivityChange Dialog
     */
    private fun handleClickAddNewActivityChange() {
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
        impl.go(AddActivityChangeDialogView.VIEW_NAME, args, context)
    }


    /**
     * Handles Activity Change selected for this Clazz Activity. The given selection id is the
     * position that will be looked up to get the uid of the Activity Change selected.
     *
     * @param chosenId The id of the selected preset from the drop-down (spinner).
     */
    fun handleChangeActivityChange(chosenId: Long) {
        measurementEntered = false
        val newChangeUid = changeToIdMap!![chosenId]

        if (chosenId == SELECT_ONE_ID) {
            changeSelected = false
            view.setMeasureBitVisibility(false)
            view.setTrueFalseVisibility(false)
        } else if (chosenId == ADD_NEW_ACTIVITY_DROPDOWN_ID) {
            changeSelected = false
            handleClickAddNewActivityChange()
            view.setMeasureBitVisibility(false)
            view.setTrueFalseVisibility(false)

            //Get the ClazzActivityChange object, set the unit of measure as well.
        } else if (chosenId >= CHANGEUIDMAPSTART) {
            view.setMeasureBitVisibility(true)
            currentClazzActivity!!.clazzActivityClazzActivityChangeUid = newChangeUid!!

            val clazzActivityChangeDao = repository.clazzActivityChangeDao

            GlobalScope.launch {
                val result = clazzActivityChangeDao.findByUidAsync(newChangeUid)
                if (result != null) {
                    //Set the unit of measure for this ClazzActivityChange
                    view.setUnitOfMeasureType(result.clazzActivityUnitOfMeasure.toLong())
                    changeSelected = true
                }
            }
        }//If add new activity selected

    }

    /**
     * Adds choosen true/false as per given id.
     *
     * @param choosenId The id choosen.
     */
    fun handleChangeTrueFalseMeasurement(choosenId: Int) {
        measurementEntered = true
        when (choosenId) {
            TRUE_ID -> {
                if(currentClazzActivity!=null) {
                    currentClazzActivity!!.clazzActivityQuantity = 1
                }

            }
            FALSE_ID -> {
                if(currentClazzActivity!=null) {
                    currentClazzActivity!!.clazzActivityQuantity = 0
                }
            }
        }
    }

    /**
     * Handles Unit of measure selection for this Clazz Activity Change selected. The given
     * selection is the position taht will be looked up to get the uid of the corresponding
     * Unit of measurement selected.
     * This will update the unit of measure on the view as well.
     *
     * @param choosenUnitId     The id of the selected preset form the drop down / spinner
     */
    fun handleChangeUnitOfMeasure(choosenUnitId: Long) {
        measurementEntered = true
        currentClazzActivity!!.clazzActivityQuantity = choosenUnitId
    }

    /**
     * Handles feedback (good or bad) selected on the Activity
     *
     * @param didItGoWell true for good, false for not good.
     */
    fun handleChangeFeedback(didItGoWell: Boolean) {
        currentClazzActivity!!.isClazzActivityGoodFeedback = didItGoWell
        if (didItGoWell) {
            view.setThumbs(THUMB_GOOD)
        } else {
            view.setThumbs(THUMB_BAD)
        }
    }

    /**
     * Finds all Activity Changes available for a clazz and gives it to the view to render.
     * Also saves the mapping to the Presenter such that we can handle what Activity change was
     * selected.
     *
     */
    private fun updateChangeOptions() {

        //Get activity change list live data
        val activityChangeLiveData = activityChangeDao.findAllClazzActivityChangesAsyncLive()
        var thisP = this
        //Observing it
        GlobalScope.launch(Dispatchers.Main) {
            activityChangeLiveData.observe(thisP, thisP::updateActivityChangesOnView)
        }

    }
    /**
     * Updates the activity changes given to it to the view.
     */
    private fun updateActivityChangesOnView(result: List<ClazzActivityChange>?) {
        changeToIdMap = HashMap()
        val idToChangeMap = HashMap<Long, Long>()
        val presetAL = ArrayList<String>()

        //Add Select one option
        presetAL.add("Select one")
        changeToIdMap!![SELECT_ONE_ID] = SELECT_ONE_ID
        idToChangeMap[SELECT_ONE_ID] = SELECT_ONE_ID


        //Add "Add new activity" option
        presetAL.add(impl.getString(MessageID.add_activity, context))
        changeToIdMap!![ADD_NEW_ACTIVITY_DROPDOWN_ID] = ADD_NEW_ACTIVITY_DROPDOWN_ID
        idToChangeMap[ADD_NEW_ACTIVITY_DROPDOWN_ID] = ADD_NEW_ACTIVITY_DROPDOWN_ID

        //Add all other options starting from CHANGEUIDMAPSTART
        var i = CHANGEUIDMAPSTART
        for (everyChange in result!!) {

            presetAL.add(everyChange.clazzActivityChangeTitle!!)

            //Add the preset to a mapping where position is paired with the Activity
            // Change's uid so that we can handle which Activity Change got selected.
            changeToIdMap!![i] = everyChange.clazzActivityChangeUid
            idToChangeMap[everyChange.clazzActivityChangeUid] = i
            i++
        }

        //set the presets to the view's activity change drop down (spinner)
        val presetsArray = presetAL.toTypedArray<String>()
        view.setClazzActivityChangesDropdownPresets(presetsArray)

        if (currentClazzActivityChangeUid != 0L) {
            view.setActivityChangeOption(idToChangeMap[currentClazzActivityChangeUid]!!)
        }

    }


    /**
     * Handles primary action button in the Clazz Activity Edit View. Here it is the Done button.
     * Clicking this will persist the currently editing Clazz Activity to the database. If this
     * button is not clicked the changes to this Activity change (which is in a new ClazzActivity
     * object) will be discarded.
     *
     */
    fun handleClickPrimaryActionButton() {

        if (changeSelected && measurementEntered) {
            currentClazzActivity!!.clazzActivityDone = true
            GlobalScope.launch {
                val result = clazzActivityDao.updateAsync(currentClazzActivity!!)
                view.finish()
            }
        }
        //else maybe alert/nodd the user that you need to select and fill everything.
    }

    /**
     * Goes back in date and updates the view.
     */
    fun handleClickGoBackDate() {
        val newDate = UMCalendarUtil.getDateInMilliPlusDaysRelativeTo(currentLogDate, -1)
        println("Go back: $newDate")
        reloadLogDetailForDate(newDate)
    }


    /**
     * Goes forward in date and updates the view.
     */
    fun handleClickGoForwardDate() {

        if (!UMCalendarUtil.isToday(currentLogDate)) {
            val currentTime = UMCalendarUtil.getDateInMilliPlusDays(0)
            if (currentLogDate < currentTime) {
                //Go to next day's
                val newDate = UMCalendarUtil.getDateInMilliPlusDaysRelativeTo(currentLogDate, 1)
                reloadLogDetailForDate(newDate)

            }
        }
    }

    private fun updateViewDateHeading() {
        var prettyDate = ""
        if (UMCalendarUtil.isToday(currentLogDate)) {
            prettyDate = impl.getString(MessageID.today, context)
        }

        //TODO: Locale substitute for KMP
        //val currentLocale = Locale.getDefault()
        prettyDate += " (" + UMCalendarUtil.getPrettyDateFromLong(currentLogDate, null) + ")"

        view.updateDateHeading(prettyDate)
    }

    /**
     * Re loads the attendance log detail view
     *
     * @param newDate The new date set
     */
    private fun reloadLogDetailForDate(newDate: Long) {
        println("Reload for date: $newDate")

        //1. Set currentLogDate to newDate
        currentLogDate = newDate

        //2. Reset the current ClazzActivity uid
        currentClazzActivityUid = 0L

        //3. Re load view and recycler
        fillClazzActivity()

        //4. Update date heading
        updateViewDateHeading()

    }

    private fun setActivityEditable(activityEditable: Boolean) {
        if (activityEditable) {
            fillClazzActivity()
        }

    }

    companion object {

        private val CHANGEUIDMAPSTART: Long = 2
        private val ADD_NEW_ACTIVITY_DROPDOWN_ID: Long = 1
        private val SELECT_ONE_ID: Long = 0
        val TRUE_ID = 0
        val FALSE_ID = 1
    }

}
