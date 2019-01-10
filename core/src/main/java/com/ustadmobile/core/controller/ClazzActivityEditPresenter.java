package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.ClazzActivityChangeDao;
import com.ustadmobile.core.db.dao.ClazzActivityDao;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.LocaleUtil;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.AddActivityChangeDialogView;
import com.ustadmobile.core.view.ClazzActivityEditView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Role;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ustadmobile.core.view.ClazzActivityEditView.THUMB_BAD;
import static com.ustadmobile.core.view.ClazzActivityEditView.THUMB_GOOD;
import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClazzActivityEditView.ARG_CLAZZACTIVITY_UID;


/**
 * The ClazzActivityEdit Presenter - Responsible for the logic behind the view that edits a
 * Clazz Activity or creates one new if doesn't exist.
 *
 * Called when editing a Clazz Activity or adding a new Clazz Activity.
 */
public class ClazzActivityEditPresenter
        extends UstadBaseController<ClazzActivityEditView> {

    //Any arguments stored as variables here
    private long currentClazzUid = 0L;
    private long currentLogDate = 0L;
    private long currentClazzActivityUid = 0L;

    private static final long CHANGEUIDMAPSTART = 2;
    private static final long ADD_NEW_ACTIVITY_DROPDOWN_ID = 1;
    private static final long SELECT_ONE_ID = 0;
    public static final int TRUE_ID = 0;
    public static final int FALSE_ID = 1;

    private boolean changeSelected = false;
    private boolean measurementEntered = false;

    private long currentClazzActivityChangeUid = 0L;
    private boolean activityEditable = false;
    private long loggedInPersonUid = 0L;

    //The current clazz activity being edited.
    private ClazzActivity currentClazzActivity;

    //The mapping of activity change uid to drop - down id on the view.
    private HashMap<Long, Long> changeToIdMap;
    private HashMap<Long, Long> idToChangeMap;

    private UmLiveData<List<ClazzActivityChange>> activityChangeLiveData;

    //Daos needed - ClazzActivtyDao and ClazzActivityChangeDao
    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    private ClazzActivityDao clazzActivityDao  = repository.getClazzActivityDao();
    private ClazzActivityChangeDao activityChangeDao = repository.getClazzActivityChangeDao();
    private ClazzDao clazzdao = repository.getClazzDao();

    /**
     * Constructor that gets Clazz Uid, Log Date and Activity Uid (to be edited)\
     *
     * @param context   context of the application
     * @param arguments Arguments of the view
     * @param view      The View itself.
     */
    public ClazzActivityEditPresenter(Object context, Hashtable arguments,
                                      ClazzActivityEditView view) {
        super(context, arguments, view);

        //Get Clazz Uid
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = Long.parseLong(arguments.get(ARG_CLAZZ_UID).toString());
        }
        //Get Log Date
        if(arguments.containsKey(ClazzListView.ARG_LOGDATE)){
            String thisLogDate = arguments.get(ClazzListView.ARG_LOGDATE).toString();
            currentLogDate = Long.parseLong(thisLogDate);
        }
        //Get Activity Uid (if editing)
        if(arguments.containsKey(ARG_CLAZZACTIVITY_UID)){
            currentClazzActivityUid =
                    Long.parseLong(arguments.get(ARG_CLAZZACTIVITY_UID).toString());
        }

        loggedInPersonUid = UmAccountManager.getActiveAccount(context).getPersonUid();

    }

    /**
     * Order:
     *      1. Gets the Clazz Activity first by clazz activity id provided (editing). If invalid,
     *          find Clazz Activity associated with the Clazz id and log date.
     *      2. Updates the toolbar of the view.
     *      3. Creates a new Clazz Activity if it doesn't exist (usually: clicked Record Activity)
     *      4. Updates all available Clazz Activity Changes for this clazz (usually all) to
 *              the view.
     *
     * @param savedState    Saved state
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        fillClazzActivity();

        //Check permissions
        checkPermissions();
    }

    public void checkPermissions(){
        clazzdao.personHasPermission(loggedInPersonUid, currentClazzUid,
                Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT, new UmCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                setActivityEditable(result);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }

    /**
     * Starts the logic for filling up activity page.
     */
    private void fillClazzActivity(){
        //Find Activity first by activity id. If it is not valid, find by clazz uid and log date.

        //If activity uid given , find the ClazzActivity:
        if(currentClazzActivityUid != 0)
            clazzActivityDao.findByUidAsync(currentClazzActivityUid,
                    new UmCallback<ClazzActivity>() {
                        //success doesn't mean it exists
                        @Override
                        public void onSuccess(ClazzActivity result) {
                            currentClazzUid = result.getClazzActivityClazzUid();
                            currentLogDate = result.getClazzActivityLogDate();

                            //Check if ClazzActivity exists. If it doesn't, create it (It ought to exist)
                            checkActivityCreateIfNotExist(result);
                        }

                        @Override
                        public void onFailure(Throwable exception) { exception.printStackTrace(); }
                    });
            //Else find by Clazz uid and Log date given:
        else {
            clazzActivityDao.findByClazzAndDateAsync(currentClazzUid, currentLogDate,
                    new UmCallback<ClazzActivity>() {
                        //success doesn't mean it exists.
                        @Override
                        public void onSuccess(ClazzActivity result) {
                            //Check if activity given exists or not - create if it doesn't.
                            checkActivityCreateIfNotExist(result);
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });
        }
    }

    /**
     * Common method to check if given ClazzActivity exists. If it doesn't create it. Afterwards
     * update the ClazzActivityChange options on the view
     *
     * @param result The ClazzActivity object to check.
     */
    private void checkActivityCreateIfNotExist(ClazzActivity result){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        ClazzDao clazzDao = repository.getClazzDao();


        //Update Activity Change options
        updateChangeOptions();

        //Update any toolbar title
        Clazz currentClazz = clazzDao.findByUid(currentClazzUid);
        view.updateToolbarTitle(currentClazz.getClazzName() + " "
                + impl.getString(MessageID.activity, context));

        //Create one anyway given ClazzActivity doesn't exist (is null)
        if(result == null)
            clazzActivityDao.createClazzActivityForDate(currentClazzUid, currentLogDate,
                    new UmCallback<Long>() {
                        @Override
                        public void onSuccess(Long result) {
                            currentClazzActivityUid = result;
                            currentClazzActivity = clazzActivityDao.findByUid(result);
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });

        //Set up presenter and start filling the UI with its elements
        else{

            currentClazzActivity = result;

            //set current clazz activity change
            currentClazzActivityChangeUid = currentClazzActivity.getClazzActivityClazzActivityChangeUid();
            currentLogDate = currentClazzActivity.getClazzActivityLogDate();
            handleChangeFeedback(currentClazzActivity.isClazzActivityGoodFeedback());
            view.setNotes(currentClazzActivity.getClazzActivityNotes());
            view.setUOMText(String.valueOf(currentClazzActivity.getClazzActivityQuantity()));
        }



        //Update date
        updateViewDateHeading();
    }

    /**
     * Handles notes added/edited to this Clazz Activity.
     *
     * @param newNote   The new note string
     */
    public void handleChangeNotes(String newNote){
        currentClazzActivity.setClazzActivityNotes(newNote);
    }

    /**
     * Opens new Add ActivityChange Dialog
     */
    private void handleClickAddNewActivityChange(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        impl.go(AddActivityChangeDialogView.VIEW_NAME, args, getContext());
    }



    /**
     * Handles Activity Change selected for this Clazz Activity. The given selection id is the
     * position that will be looked up to get the uid of the Activity Change selected.
     *
     * @param chosenId The id of the selected preset from the drop-down (spinner).
     */
    public void handleChangeActivityChange(long chosenId){
        measurementEntered = false;
        Long newChangeUid = changeToIdMap.get(chosenId);

        if(chosenId == SELECT_ONE_ID){
            changeSelected = false;
            view.setMeasureBitVisibility(false);
            view.setTrueFalseVisibility(false);
        }
        //If add new activity selected
        else if(chosenId == ADD_NEW_ACTIVITY_DROPDOWN_ID ) {
            changeSelected = false;
            handleClickAddNewActivityChange();
            view.setMeasureBitVisibility(false);
            view.setTrueFalseVisibility(false);

        //Get the ClazzActivityChange object, set the unit of measure as well.
        }else if(chosenId >= CHANGEUIDMAPSTART){
            view.setMeasureBitVisibility(true);
            currentClazzActivity.setClazzActivityClazzActivityChangeUid(newChangeUid);

            ClazzActivityChangeDao clazzActivityChangeDao =
                    repository.getClazzActivityChangeDao();
            clazzActivityChangeDao.findByUidAsync(newChangeUid,
                                                    new UmCallback<ClazzActivityChange>() {
                @Override
                public void onSuccess(ClazzActivityChange result) {
                    if(result != null) {
                        //Set the unit of measure for this ClazzActivityChange
                        view.setUnitOfMeasureType(result.getClazzActivityUnitOfMeasure());
                        changeSelected = true;
                    }
                }

                @Override
                public void onFailure(Throwable exception) {}
            });
        }

    }

    /**
     * Adds choosen true/false as per given id.
     *
     * @param choosenId The id choosen.
     */
    public void handleChangeTrueFalseMeasurement(int choosenId){
        measurementEntered = true;
        switch (choosenId){
            case TRUE_ID:
                currentClazzActivity.setClazzActivityQuantity(1);
                break;
            case FALSE_ID:
                currentClazzActivity.setClazzActivityQuantity(0);
                break;
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
    public void handleChangeUnitOfMeasure(long choosenUnitId){
        measurementEntered = true;
        currentClazzActivity.setClazzActivityQuantity(choosenUnitId);
    }

    /**
     * Handles feedback (good or bad) selected on the Activity
     *
     * @param didItGoWell true for good, false for not good.
     */
    public void handleChangeFeedback(boolean didItGoWell){
        currentClazzActivity.setClazzActivityGoodFeedback(didItGoWell);
        if(didItGoWell) {
            view.setThumbs(THUMB_GOOD);
        }else{
            view.setThumbs(THUMB_BAD);
        }
    }

    /**
     * Updates the activity changes given to it to the view.
     */
    private void updateActivityChangesOnView(List<ClazzActivityChange> result){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        changeToIdMap = new HashMap<>();
        idToChangeMap = new HashMap<>();
        ArrayList<String> presetAL = new ArrayList<>();

        //Add Select one option
        presetAL.add("Select one");
        changeToIdMap.put(SELECT_ONE_ID, SELECT_ONE_ID);
        idToChangeMap.put(SELECT_ONE_ID, SELECT_ONE_ID);


        //Add "Add new activity" option
        presetAL.add(impl.getString(MessageID.add_activity, context));
        changeToIdMap.put(ADD_NEW_ACTIVITY_DROPDOWN_ID, ADD_NEW_ACTIVITY_DROPDOWN_ID);
        idToChangeMap.put(ADD_NEW_ACTIVITY_DROPDOWN_ID, ADD_NEW_ACTIVITY_DROPDOWN_ID);

        //Add all other options starting from CHANGEUIDMAPSTART
        long i = CHANGEUIDMAPSTART;
        for(ClazzActivityChange everyChange: result){

            presetAL.add(everyChange.getClazzActivityChangeTitle());

            //Add the preset to a mapping where position is paired with the Activity
            // Change's uid so that we can handle which Activity Change got selected.
            changeToIdMap.put(i, everyChange.getClazzActivityChangeUid());
            idToChangeMap.put(everyChange.getClazzActivityChangeUid(), i);
            i++;
        }

        //set the presets to the view's activity change drop down (spinner)
        view.setClazzActivityChangesDropdownPresets(presetAL.toArray(new String[presetAL.size()]));

        if(currentClazzActivityChangeUid != 0){
            view.setActivityChangeOption(idToChangeMap.get(currentClazzActivityChangeUid));
        }

    }

    /**
     * Finds all Activity Changes available for a clazz and gives it to the view to render.
     * Also saves the mapping to the Presenter such that we can handle what Activity change was
     * selected.
     *
     */
    private void updateChangeOptions(){

        //Get activity change list live data
        activityChangeLiveData = activityChangeDao.findAllClazzActivityChangesAsyncLive();
        //Observing it
        activityChangeLiveData.observe(ClazzActivityEditPresenter.this,
                ClazzActivityEditPresenter.this::updateActivityChangesOnView);
    }

    /**
     * Handles primary action button in the Clazz Activity Edit View. Here it is the Done button.
     * Clicking this will persist the currently editing Clazz Activity to the database. If this
     * button is not clicked the changes to this Activity change (which is in a new ClazzActivity
     * object) will be discarded.
     *
     */
    public void handleClickPrimaryActionButton() {

        if(changeSelected && measurementEntered) {
            currentClazzActivity.setClazzActivityDone(true);
            clazzActivityDao.updateAsync(currentClazzActivity, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    view.finish();
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });
        }
        //else maybe alert/nodd the user that you need to select and fill everything.
    }

    public void handleClickGoBackDate(){
        long newDate = UMCalendarUtil.getDateInMilliPlusDaysRelativeTo(currentLogDate, -1);
        System.out.println("Go back: " + newDate);
        reloadLogDetailForDate(newDate);
    }


    public void handleClickGoForwardDate(){
        Date currentLogDateDate = new Date(currentLogDate);

        if(!UMCalendarUtil.isToday(currentLogDateDate)){
            if(currentLogDate < System.currentTimeMillis()){
                //Go to next day's
                long newDate = UMCalendarUtil.getDateInMilliPlusDaysRelativeTo(currentLogDate, 1);
                reloadLogDetailForDate(newDate);

            }
        }
    }

    public void updateViewDateHeading(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Date currentLogDateDate = new Date(currentLogDate);
        String prettyDate="";
        if(UMCalendarUtil.isToday(currentLogDateDate)){
            prettyDate = impl.getString(MessageID.today, context);
        }

        Locale currentLocale = Locale.getDefault();

        prettyDate += " (" + UMCalendarUtil.getPrettyDateFromLong(currentLogDate, currentLocale) + ")";

        view.updateDateHeading(prettyDate);
    }

    /**
     * Re loads the attendance log detail view
     *
     * @param newDate The new date set
     */
    public void reloadLogDetailForDate(long newDate){
        System.out.println("Reload for date: " + newDate);

        //1. Set currentLogDate to newDate
        currentLogDate = newDate;

        //2. Reset the current ClazzActivity uid
        currentClazzActivityUid = 0L;

        //3. Re load view and recycler
        fillClazzActivity();

        //4. Update date heading
        updateViewDateHeading();


    }

    public boolean isActivityEditable() {
        return activityEditable;
    }

    public void setActivityEditable(boolean activityEditable) {
        this.activityEditable = activityEditable;
    }

    @Override
    public void setUIStrings() {

    }

}
