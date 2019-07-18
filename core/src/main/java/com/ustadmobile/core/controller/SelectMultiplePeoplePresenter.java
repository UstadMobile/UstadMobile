package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.view.SelectMultiplePeopleView;
import com.ustadmobile.lib.db.entities.Person;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.controller.ReportOptionsDetailPresenter.convertCSVStringToLongList;
import static com.ustadmobile.core.controller.SelectMultipleProductTypeTreeDialogPresenter.convertLongArray;
import static com.ustadmobile.core.view.SelectMultiplePeopleView.ARG_SELECTED_PEOPLE;


/**
 * The SelectClazzesDialog Presenter.
 */
public class SelectMultiplePeoplePresenter
        extends UstadBaseController<SelectMultiplePeopleView> {

    //Any arguments stored as variables here
    private UmProvider<Person> personWithEnrollmentUmProvider;
    private List<Long> selectedPeopleList;

    private HashMap<String, Long> people;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SelectMultiplePeoplePresenter(Object context, Hashtable arguments,
                                         SelectMultiplePeopleView view) {
        super(context, arguments, view);
        if(arguments.containsKey(ARG_SELECTED_PEOPLE)){
            String selectedPeopleCSString = arguments.get(ARG_SELECTED_PEOPLE).toString();

            selectedPeopleList = convertCSVStringToLongList(selectedPeopleCSString);

        }

    }

    public void addToPeople(Person person){
        String personName = person.getFirstNames() + " " +
                person.getLastName();
        if(!people.containsKey(personName)){
            people.put(personName, person.getPersonUid());
        }
    }

    public void removePeople(Person person){
        String personName = person.getFirstNames() + " " +
                person.getLastName();
        if(people.containsKey(personName)){
            people.remove(personName);
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        people = new HashMap<>();

        //Find the provider
        personWithEnrollmentUmProvider = repository.getPersonDao().findAllPeopleProvider();
        view.setListProvider(personWithEnrollmentUmProvider);

    }

    public void handleSelection(long personUid, boolean enrolled){
        //TODO
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
