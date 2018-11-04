package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.view.PersonDetailEnrollClazzView;
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment;

import java.util.Hashtable;

import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;

/**
 * PersonDetailsEnrollClazz's Presenter - responsible for the logic of showing all classes a person
 * given is enrolled to and handle when any of those classes' enrollment changes (added or removed)
 */
public class PersonDetailEnrollClazzPresenter extends UstadBaseController<PersonDetailEnrollClazzView> {

    private long currentPersonUid = -1L;

    private UmProvider<ClazzWithEnrollment> clazzWithEnrollmentUmProvider;

    public PersonDetailEnrollClazzPresenter(Object context,
                                            Hashtable arguments, PersonDetailEnrollClazzView view) {
        super(context, arguments, view);

        //Get the person and set it to this Presenter
        if(arguments.containsKey(ARG_PERSON_UID)){
            currentPersonUid = Long.parseLong(arguments.get(ARG_PERSON_UID).toString());
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate classes
        ClazzDao clazzDao = UmAppDatabase.getInstance(context).getClazzDao();
        clazzWithEnrollmentUmProvider =
                clazzDao.findAllClazzesWithEnrollmentByPersonUid(currentPersonUid);

        setClazzProviderToView();

    }

    /**
     * Sets the class list with enrollment provider set to this presenter on the View
     */
    private void setClazzProviderToView(){
        view.setClazzListProvider(clazzWithEnrollmentUmProvider);
    }

    /**
     * Handle done will close the view open (ie: go back to the edit page). All changes were in
     * real time and already persisted to the database.
     */
    public void handleClickDone(){
        view.finish();
    }

    /**
     * Blank method does nothing
     */
    public void handleClickClazz(){
        //does nothing
    }

    /**
     * Overriding. Does nothing
     */
    @Override
    public void setUIStrings() {

    }
}
