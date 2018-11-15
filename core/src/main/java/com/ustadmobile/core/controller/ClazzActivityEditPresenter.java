package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzActivityChangeDao;
import com.ustadmobile.core.db.dao.ClazzActivityDao;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClazzActivityEditView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

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
    private long currentClazzUid = -1L;
    private long currentLogDate = -1L;
    private long currentClazzActivityUid = -1L;

    private ClazzActivity currentClazzActivity;

    private HashMap<Long, Long> changeToIdMap;

    private ClazzActivityDao clazzActivityDao =
            UmAppDatabase.getInstance(context).getClazzActivityDao();
    private ClazzActivityChangeDao activityChangeDao =
            UmAppDatabase.getInstance(context).getClazzActivityChangeDao();

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

        //Find Activity first by activity id. If it is not valid, find by clazz uid and log date.

        //Find if Activity Uid :
        if(currentClazzActivityUid > 0)
            clazzActivityDao.findByUidAsync(currentClazzActivityUid, new UmCallback<ClazzActivity>() {
                @Override
                public void onSuccess(ClazzActivity result) {
                    checkActivityCreateIfNotExist(result);
                }

                @Override
                public void onFailure(Throwable exception) { exception.printStackTrace(); }
            });
        else {
            //Find by Clazz and LogDate .
            clazzActivityDao.findByClazzAndDateAsync(currentClazzUid, currentLogDate,
                    new UmCallback<ClazzActivity>() {
                        @Override
                        public void onSuccess(ClazzActivity result) {
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
     * update the ClazzActivityChange options.
     *
     * @param result The ClazzActivity object to check.
     */
    private void checkActivityCreateIfNotExist(ClazzActivity result){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        ClazzDao clazzDao = UmAppDatabase.getInstance(getContext()).getClazzDao();
        //Update any toolbar title
        Clazz currentClazz = clazzDao.findByUid(currentClazzUid);

        view.updateToolbarTitle(currentClazz.getClazzName() + " "
                + impl.getString(MessageID.activity, context));

        //Create one anyway if not set
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
        else{
            currentClazzActivity = result;
        }

        //Update Activity Change options
        updateChangeOptions();
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
     * Handles Activity Change selected for this Clazz Activity. The given selection id is the
     * position that will be looked up to get the uid of the Activity Change selected.
     *
     * @param chosenId The id of the selected preset from the drop-down (spinner).
     */
    public void handleChangeActivityChange(long chosenId){
        Long newChangeUid = changeToIdMap.get(chosenId);
        currentClazzActivity.setClazzActivityClazzActivityChangeUid(newChangeUid);
    }

    /**
     * Handles feedback (good or bad) selected on the Activity
     *
     * @param didItGoWell true for good, false for not good.
     */
    public void handleChangeFeedback(boolean didItGoWell){
        currentClazzActivity.setClazzActivityGoodFeedback(didItGoWell);
    }

    /**
     * Finds all Activity Changes available for a clazz and gives it to the view to render.
     * Also saves the mapping to the Presenter such that we can handle what Activity change was
     * selected.
     *
     */
    private void updateChangeOptions(){

        activityChangeDao.findAllClazzActivityChangesAsync(new UmCallback<List<ClazzActivityChange>>() {
            @Override
            public void onSuccess(List<ClazzActivityChange> result) {
                changeToIdMap = new HashMap<>();
                ArrayList<String> presetAL = new ArrayList<>();
                int i=0;
                for(ClazzActivityChange everyChange: result){
                    i++;
                    presetAL.add(everyChange.getClazzActivityChangeTitle());

                    //Add the preset to a mapping where position is paired with the Activity
                    // Change's uid so that we can handle which Activity Change got selected.
                    changeToIdMap.put((long) i, everyChange.getClazzActivityChangeUid());
                }

                //set the presets to the view's activity change drop down (spinner)
                view.setClazzActivityChangesDropdownPresets(presetAL.toArray(new String[presetAL.size()]));
                //view.setClazzActivityChangesDropdownPresets((String[]) presetAL.toArray());
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Handles primary action button in the Clazz Activity Edit View. Here it is the Done button.
     * Clicking this will persist the currently editing Clazz Activity to the database. If this
     * button is not clicked the changes to this Activity change (which is in a new ClazzActivity
     * object) will be discarded.
     *
     */
    public void handleClickPrimaryActionButton() {

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

    @Override
    public void setUIStrings() {

    }

}
