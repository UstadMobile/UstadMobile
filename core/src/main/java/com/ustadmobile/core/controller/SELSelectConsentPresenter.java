package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.view.SELSelectConsentView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;


/**
 * The SELSelectConsent Presenter.
 */
public class SELSelectConsentPresenter
        extends UstadBaseController<SELSelectConsentView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private long currentPersonUid = -1;

    //Provider 
    UmProvider<Person> providerList;

    public SELSelectConsentPresenter(Object context, Hashtable arguments, SELSelectConsentView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
        if(arguments.containsKey(ARG_PERSON_UID)){
            currentPersonUid = (long) arguments.get(ARG_PERSON_UID);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate the provider
        providerList = UmAppDatabase.getInstance(context).getClazzMemberDao()
                .findAllPeopleInClassUid(currentClazzUid);

        //Not valid for this screen.
        //set Provider.
        //view.setListProvider(providerList);

    }

    /**
     *
     * @param selectedObject the consent checkbox
     */
    public void handleClickPrimaryActionButton(long selectedObject) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //TODO: Check selectedObject for consent given.

        //Create arguments
        Hashtable args = new Hashtable();
        //eg: args.put(ARG_CLAZZ_UID, selectedObjectUid);

        //Go to view
        //eg: impl.go(SELEditView.VIEW_NAME, args, view.getContext());
    }

    @Override
    public void setUIStrings() {

    }

}
