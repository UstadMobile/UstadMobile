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
 * The AddActivityChangeDialog Presenter. Usually triggered when editing a single Clazz Activity
 * This presenter is responsible for persisting an edited/new ClazzActivity.
 */
public class AddActivityChangeDialogPresenter
        extends UstadBaseController<AddActivityChangeDialogView> {

    //The current Clazz Activity Change that this Clazz Activity will be assigned to.
    private ClazzActivityChange currentChange;
    //Map of all the measurement type options's uid AND its position. Useful when we know what
    //position from the view('s spinner) was selected so we can find the corresponding measurement
    // type.
    private HashMap<Integer, Integer> measurementToUOM;

    //The Database repo
    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);


    public AddActivityChangeDialogPresenter(Object context, Hashtable arguments,
                                            AddActivityChangeDialogView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Create a new activity change
        if(currentChange == null){
            currentChange = new ClazzActivityChange();
        }

        //We know all the measurement type. So we just populate them here..
        ArrayList<String> measurementTypes = new ArrayList<>();
        measurementToUOM = new HashMap<>();
        measurementTypes.add(impl.getString(MessageID.frequency, context));
        measurementToUOM.put(0, ClazzActivityChange.UOM_FREQUENCY);
        measurementTypes.add(impl.getString(MessageID.duration, context));
        measurementToUOM.put(1, ClazzActivityChange.UOM_DURATION);
        measurementTypes.add(impl.getString(MessageID.yes_no, context));
        measurementToUOM.put(2, ClazzActivityChange.UOM_BINARY);

        String[] measurementTypesArray = new String[measurementTypes.size()];
        measurementTypesArray = measurementTypes.toArray(measurementTypesArray);

        //.. and set them on the view.
        view.setMeasurementDropdownPresets(measurementTypesArray);
    }

    /**
     * Method that gets called when user clicks "Add" on the dialog (primary).
     * This will persist the information added about this new Activity
     */
    public void handleAddActivityChange(){

        ClazzActivityChangeDao clazzActivityChangeDao = repository.getClazzActivityChangeDao();
        currentChange.setClazzActivityChangeActive(true); //set active
        clazzActivityChangeDao.insertAsync(currentChange, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });

    }

    /**
     * Method that gets called when user clicks "Cancel" on the dialog (dismiss)
     */
    public void handleCancelActivityChange(){
        currentChange = null;
    }

    /**
     * Updates the unit of measurement selected for the clazz activity.
     * @param position  The position of item selected from the drop down.
     */
    public void handleMeasurementSelected(int position){
        currentChange.setClazzActivityUnitOfMeasure(measurementToUOM.get(position));
    }

    /**
     * Updates the title of the clazz activity.
     * @param title The activity title
     */
    public void handleTitleChanged(String title){
        currentChange.setClazzActivityChangeTitle(title);
    }
}
