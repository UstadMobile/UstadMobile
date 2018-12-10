package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.core.view.ClazzEditView;
import com.ustadmobile.lib.db.entities.Clazz;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;


/**
 * The ClazzDetail Presenter - responsible for displaying the details of the Clazz who's detail we
 * want to see.
 * This is usually called first when we click on a Class from a list of Classes to get into it.
 */
public class ClazzDetailPresenter
        extends UstadBaseController<ClassDetailView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);
    private ClazzDao clazzDao = repository.getClazzDao();


    public ClazzDetailPresenter(Object context, Hashtable arguments, ClassDetailView view) {
        super(context, arguments, view);

        //Get Clazz Uid and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

    }

    /**
     * In Order:
     *      1. Just set the title of the toolbar.
     *
     * @param savedState    The savedState
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        updateToolbarTitle();

        view.setActivityVisibility(true);
        view.setAttendanceVisibility(true);
        view.setSELVisibility(true);


    }

    /**
     * Updates the title of the Clazz after finding it from the database.
     */
    public void updateToolbarTitle(){

        clazzDao.findByUidAsync(currentClazzUid, new UmCallback<Clazz>() {
            @Override
            public void onSuccess(Clazz result) {
                view.setToolbarTitle(result.getClazzName());
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Handles what happens when Class Edit is clicked. This takes the class to the edit page.
     */
    public void handleClickClazzEdit(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzUid);
        impl.go(ClazzEditView.VIEW_NAME, args, view.getContext());
    }

    @Override
    public void setUIStrings() {

    }

}
