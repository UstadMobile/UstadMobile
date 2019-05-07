package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.PersonDetailEnrollClazzView;
import com.ustadmobile.lib.db.entities.ClazzMember;
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

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

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
        ClazzDao clazzDao = repository.getClazzDao();
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
     * Toggles the person being enrolled in the clazz.
     *
     * @param clazzUid      The clazz Uid
     * @param personUid     The person Uid
     */
    public void handleToggleClazzChecked(long clazzUid, long personUid, boolean checked){
        ClazzMemberDao clazzMemberDao = repository.getClazzMemberDao();
        clazzMemberDao.findByPersonUidAndClazzUidAsync(personUid, clazzUid, new UmCallback<ClazzMember>() {
            @Override
            public void onSuccess(ClazzMember result) {
                if(result != null){
                    result.setClazzMemberActive(checked);
                    clazzMemberDao.update(result);

                }else{
                    if(checked){
                        //Create new
                        ClazzMember newClazzMember  = new ClazzMember();
                        newClazzMember.setClazzMemberClazzUid(clazzUid);
                        newClazzMember.setClazzMemberPersonUid(personUid);
                        newClazzMember.setClazzMemberActive(true);
                        clazzMemberDao.insert(newClazzMember);
                    }
                    //else Don't create. false anyway

                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

}
