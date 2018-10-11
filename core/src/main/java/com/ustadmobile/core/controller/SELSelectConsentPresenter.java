package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.view.SELEditView;
import com.ustadmobile.core.view.SELQuestionView;
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

        //No provider for this activity.
    }

    /**
     * Handles click "START SELECTION"
     * */
    public void handleClickPrimaryActionButton(boolean consentGiven) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Check selectedObject for consent given.
        if(consentGiven){
            //Create arguments
            Hashtable args = new Hashtable();
            args.put(ARG_CLAZZ_UID, currentClazzUid);
            args.put(ARG_PERSON_UID, currentPersonUid);

            //TODO: Decide when to show recognition and when to show the SEL questions themselves.

            //Go to view
            impl.go(SELQuestionView.VIEW_NAME, args, view.getContext());
        }
        //TODO: Handle and think about what happens if the consent is NOT given.

    }

    @Override
    public void setUIStrings() {

    }

}
