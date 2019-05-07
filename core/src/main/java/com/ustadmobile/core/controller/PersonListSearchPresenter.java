package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.view.PersonListSearchView;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.Hashtable;

import static com.ustadmobile.core.view.PersonListSearchView.ARGUMENT_CURRNET_CLAZZ_UID;

public class PersonListSearchPresenter extends CommonHandlerPresenter<PersonListSearchView> {

    //Provider
    private UmProvider<PersonWithEnrollment> personWithEnrollmentUmProvider;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    private long currentClazzUid = 0;

    public PersonListSearchPresenter(Object context, Hashtable arguments, PersonListSearchView view) {
        super(context, arguments, view);

        if(arguments.containsKey(ARGUMENT_CURRNET_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARGUMENT_CURRNET_CLAZZ_UID);
        }

    }

    /**
     * In order:
     *      1. Gets all people via the database as UmProvider and sets it to the view.
     *
     * @param savedState The saved state.
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        personWithEnrollmentUmProvider = repository.getClazzMemberDao()
                .findAllPersonWithEnrollmentInClazzByClazzUidWithSearchFilter(currentClazzUid,
                        0,1, "%");
        setPeopleProviderToView();

    }

    public void updateFilter(float apl, float aph, String value){
        String stringQuery = "%" + value + "%";
        personWithEnrollmentUmProvider = repository.getClazzMemberDao()
                .findAllPersonWithEnrollmentInClazzByClazzUidWithSearchFilter(currentClazzUid,
                        apl,aph, stringQuery);
        setPeopleProviderToView();
    }

    /**
     * Sets the people list provider set in the Presenter to the View.
     */
    private void setPeopleProviderToView(){
        view.setPeopleListProvider(personWithEnrollmentUmProvider);
    }

    @Override
    public void handleCommonPressed(Object arg) {

    }

    @Override
    public void handleSecondaryPressed(Object arg) {

    }
}
