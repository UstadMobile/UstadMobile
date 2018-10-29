package com.ustadmobile.core.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzActivityChangeDao;
import com.ustadmobile.core.db.dao.ClazzActivityDao;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.ClazzActivityEditView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClazzActivityEditView.ARG_CLAZZACTIVITY_UID;


/**
 * The ClazzActivityEdit Presenter.
 */
public class ClazzActivityEditPresenter
        extends UstadBaseController<ClazzActivityEditView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1L;
    private long currentLogDate = -1L;
    private long currentClazzActivityUid = -1L;

    private ClazzActivity currentClazzActivity;
    public Clazz currentClazz;

    HashMap<Integer, Long> changeToIdMap;

    ClazzActivityDao clazzActivityDao =
            UmAppDatabase.getInstance(context).getClazzActivityDao();
    ClazzActivityChangeDao activityChangeDao =
            UmAppDatabase.getInstance(context).getClazzActivityChangeDao();

    public ClazzActivityEditPresenter(Object context, Hashtable arguments,
                                      ClazzActivityEditView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = Long.parseLong(arguments.get(ARG_CLAZZ_UID).toString());
        }
        if(arguments.containsKey(ClazzListView.ARG_LOGDATE)){
            String thisLogDate = arguments.get(ClazzListView.ARG_LOGDATE).toString();
            currentLogDate = Long.parseLong(thisLogDate);
        }
        if(arguments.containsKey(ARG_CLAZZACTIVITY_UID)){
            currentClazzActivityUid = Long.parseLong(arguments.get(ARG_CLAZZACTIVITY_UID).toString());
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


        //Check for ClassActivity
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        ClazzDao clazzDao = UmAppDatabase.getInstance(getContext()).getClazzDao();

        //Find by Clazz and LogDate .
        clazzActivityDao.findByClazzAndDateAsync(currentClazzUid, currentLogDate,
                new UmCallback<ClazzActivity>() {
                    @Override
                    public void onSuccess(ClazzActivity result) {
                        //Update any toolbar title
                        currentClazz = clazzDao.findByUid(currentClazzUid);

                        view.updateToolbarTitle(currentClazz.getClazzName() + " "
                                + impl.getString(MessageID.activity, context));

                        if(result == null){
                            //Create one anyway if not set
                            clazzActivityDao.createClazzActivityForDate(currentClazzUid, currentLogDate,
                                    new UmCallback<Long>() {
                                        @Override
                                        public void onSuccess(Long result) {
                                            currentClazzActivityUid = result;
                                            currentClazzActivity = clazzActivityDao.findByUid(result);

                                            updateChangeOptions(currentClazzActivity);
                                        }
                                        @Override
                                        public void onFailure(Throwable exception) {

                                        }
                                    });
                        }else{
                            currentClazzActivity = result;

                            updateChangeOptions(currentClazzActivity);
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });


    }

    public void handleChangeNotes(String newNote){
        currentClazzActivity.setClazzActivityNotes(newNote);
    }
    public void handleChangeActivityChange(long choosenId){
        Long newChangeUid = changeToIdMap.get(choosenId);
        currentClazzActivity.setClazzActivityClazzActivityChangeUid(newChangeUid);
    }

    public void handleChangeFeedback(boolean didItGoWell){
        currentClazzActivity.setClazzActivityGoodFeedback(didItGoWell);
    }

    public void updateChangeOptions(ClazzActivity clazzActivity){

        activityChangeDao.findAllClazzActivityChangesAsync(new UmCallback<List<ClazzActivityChange>>() {
            @Override
            public void onSuccess(List<ClazzActivityChange> result) {
                changeToIdMap = new HashMap<>();
                ArrayList<String> presetAL = new ArrayList<String>();
                int i=0;
                for(ClazzActivityChange everyChange: result){
                    i++;
                    presetAL.add(everyChange.getClazzActivityChangeTitle());
                    //TODO: any way to send id as well ?
                    changeToIdMap.put(i, everyChange.getClazzActivityChangeUid());
                }
                view.setClazzActivityChangesDropdownPresets((String[]) presetAL.toArray());
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }


    public void handleClickPrimaryActionButton(long selectedObjectUid) {

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
