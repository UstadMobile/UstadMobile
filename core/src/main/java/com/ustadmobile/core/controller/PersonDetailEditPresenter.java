package com.ustadmobile.core.controller;

import com.ustadmobile.core.view.PersonDetailEditView;
import com.ustadmobile.core.view.PersonDetailView;

import java.util.Hashtable;

public class PersonDetailEditPresenter extends UstadBaseController<PersonDetailEditView> {

    //The person we are editing
    private long personUid;

    //The clazz uid which we are looking/the clazz uid to assign this personUid.
    private long addToClazzUid;

    /**
     * Presenter's constructor where we are getting arguments and setting the personUid and the
     * clazz uid to add this person to (if applicable)
     *
     * @param context Android context
     * @param arguments Arguments to the Activity passed here.
     * @param view  The view that called this Presenter (PersonDetailActivity)
     */
    public PersonDetailEditPresenter(Object context, Hashtable arguments, PersonDetailEditView view) {
        super(context, arguments, view);

        personUid = Long.parseLong(arguments.get(PersonDetailView.ARG_PERSON_UID).toString());
        addToClazzUid = Long.parseLong(arguments.get(PersonDetailEditView.ARG_ADD_TO_CLASS).toString());



    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }

    @Override
    public void setUIStrings() {

    }
}
