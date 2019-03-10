package com.ustadmobile.core.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzActivityChangeDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.AddActivityChangeDialogView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;


/**
 * The AddActivityChangeDialog Presenter.
 */
public class AddActivityChangeDialogPresenter
        extends UstadBaseController<AddActivityChangeDialogView> {

    ClazzActivityChange currentChange;
    HashMap<Integer, Integer> measurementToUOM;
    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);


    public AddActivityChangeDialogPresenter(Object context, Hashtable arguments,
                                            AddActivityChangeDialogView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        //eg: if(arguments.containsKey(ARG_CLAZZ_UID)){
        //    currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        //}

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        if(currentChange == null){
            currentChange = new ClazzActivityChange();
        }

        ArrayList<String> dayAL = new ArrayList<>();
        measurementToUOM = new HashMap<>();
        dayAL.add(impl.getString(MessageID.frequency, context));
        measurementToUOM.put(0, ClazzActivityChange.UOM_FREQUENCY);
        dayAL.add(impl.getString(MessageID.duration, context));
        measurementToUOM.put(1, ClazzActivityChange.UOM_DURATION);
        dayAL.add(impl.getString(MessageID.yes_no, context));
        measurementToUOM.put(2, ClazzActivityChange.UOM_BINARY);

        String[] d = new String[dayAL.size()];
        d = dayAL.toArray(d);


        view.setMeasurementDropdownPresets(d);
    }

    public void handleClickPrimaryActionButton(long selectedObjectUid) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Create arguments
        Hashtable args = new Hashtable();
        //eg: args.put(ARG_CLAZZ_UID, selectedObjectUid);

        //Go to view
        //eg: impl.go(SELEditView.VIEW_NAME, args, view.getContext());
    }

    public void handleAddActivityChange(){

        ClazzActivityChangeDao clazzActivityChangeDao = repository.getClazzActivityChangeDao();
        currentChange.setClazzActivityChangeActive(true);
        clazzActivityChangeDao.insertAsync(currentChange, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }

    public void handleCancelActivityChange(){
        currentChange = null;
    }

    public void handleMeasurementSelected(int posiiton, long id){

        currentChange.setClazzActivityUnitOfMeasure(measurementToUOM.get(posiiton));
    }

    public void handleTitleChanged(String title){
        currentChange.setClazzActivityChangeTitle(title);
    }
}
