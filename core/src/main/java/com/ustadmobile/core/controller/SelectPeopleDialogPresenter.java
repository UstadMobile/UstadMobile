package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.view.SelectClazzesDialogView;
import com.ustadmobile.core.view.SelectPeopleDialogView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.view.ReportEditView.ARG_CLASSES_SET;
import static com.ustadmobile.core.view.ReportEditView.ARG_LOCATIONS_SET;
import static com.ustadmobile.core.view.SelectPeopleDialogView.ARG_SELECTED_PEOPLE;


/**
 * The SelectClazzesDialog Presenter.
 */
public class SelectPeopleDialogPresenter
        extends UstadBaseController<SelectPeopleDialogView> {

    //Any arguments stored as variables here
    private UmProvider<PersonWithEnrollment> personWithEnrollmentUmProvider;
    private List<Long> selectedPeopleList;

    private HashMap<String, Long> people;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SelectPeopleDialogPresenter(Object context, Hashtable arguments,
                                       SelectPeopleDialogView view) {
        super(context, arguments, view);
        if(arguments.containsKey(ARG_SELECTED_PEOPLE)){
            long[] clazzesSelected = (long[]) arguments.get(ARG_SELECTED_PEOPLE);
            selectedPeopleList =
                    ReportOverallAttendancePresenter.convertLongArray(clazzesSelected);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        people = new HashMap<>();

        //Find the provider
        personWithEnrollmentUmProvider = repository.getPersonDao().findAllPeopleWithEnrollment();
        view.setPeopleProvider(personWithEnrollmentUmProvider);

    }

    public void handleCommonPressed(Object arg) {
        // The finish() should call the onResult method in parent activity, etc.
        // Make sure you send the list
        view.finish();
    }

    public HashMap<String, Long> getPeople() {
        return people;
    }

    public void setPeople(HashMap<String, Long> people) {
        this.people = people;
    }


    public List<Long> getSelectedPeopleList() {
        return selectedPeopleList;
    }

    public void setSelectedPeopleList(List<Long> selectedPeopleList) {
        this.selectedPeopleList = selectedPeopleList;
    }
}
