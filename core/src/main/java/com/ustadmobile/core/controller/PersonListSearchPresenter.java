package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.view.PersonListSearchView;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.Hashtable;

public class PersonListSearchPresenter extends CommonHandlerPresenter<PersonListSearchView> {

    //Provider
    private UmProvider<PersonWithEnrollment> personWithEnrollmentUmProvider;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public PersonListSearchPresenter(Object context, Hashtable arguments, PersonListSearchView view) {
        super(context, arguments, view);

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

        personWithEnrollmentUmProvider = repository.getPersonDao()
                .findAllPeopleWithEnrollment();
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
