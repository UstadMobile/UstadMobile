package com.ustadmobile.core.controller;

import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.PeopleListView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClazzDetailEnrollStudentView.ARG_NEW_PERSON;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.CUSTOM_FIELD_MIN_UID;


/**
 * The PeopleList Presenter.
 */
public class PeopleListPresenter
        extends CommonHandlerPresenter<PeopleListView> {

    //Any arguments stored as variables here

    //Provider 
    UmProvider<PersonWithEnrollment> personWithEnrollmentUmProvider;

    public PeopleListPresenter(Object context, Hashtable arguments, PeopleListView view) {
        super(context, arguments, view);

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        personWithEnrollmentUmProvider = UmAppDatabase.getInstance(context).getPersonDao()
                .findAllPeopleWithEnrollment();
        view.setPeopleListProvider(personWithEnrollmentUmProvider);

    }

    public void handleClickPrimaryActionButton() {
        //Goes to PersonEditActivity with currentClazzUid passed as argument
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Person newPerson = new Person();
        PersonDao personDao = UmAppDatabase.getInstance(context).getPersonDao();
        PersonCustomFieldDao personFieldDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldDao();
        PersonCustomFieldValueDao customFieldValueDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldValueDao();

        personDao.insertAsync(newPerson, new UmCallback<Long>() {

            @Override
            public void onSuccess(Long result) {

                //Also create null Custom Field values so it shows up in the Edit screen.
                personFieldDao.findAllCustomFields(CUSTOM_FIELD_MIN_UID,
                        new UmCallback<List<PersonField>>() {
                    @Override
                    public void onSuccess(List<PersonField> allCustomFields) {

                        for (PersonField everyCustomField:allCustomFields){
                            PersonCustomFieldValue cfv = new PersonCustomFieldValue();
                            cfv.setPersonCustomFieldValuePersonCustomFieldUid(
                                    everyCustomField.getPersonCustomFieldUid());
                            cfv.setPersonCustomFieldValuePersonUid(result);
                            cfv.setPersonCustomFieldValueUid(customFieldValueDao.insert(cfv));
                        }

                        Hashtable args = new Hashtable();
                        args.put(ARG_PERSON_UID, result);
                        args.put(ARG_NEW_PERSON, "true");
                        impl.go(PersonEditView.VIEW_NAME, args, view.getContext());
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });


            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }

    @Override
    public void setUIStrings() {

    }

    @Override
    public void handleCommonPressed(Object arg) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_PERSON_UID, arg);
        impl.go(PersonDetailView.VIEW_NAME, args, view.getContext());
    }

    @Override
    public void handleSecondaryPressed(Object arg) {
        //No secondary action for every item here.
    }
}
