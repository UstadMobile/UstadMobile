package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SELAnswerListView;
import com.ustadmobile.core.view.SELSelectStudentView;
import com.ustadmobile.lib.db.entities.Person;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;

/**
 * SELAnswerList's presenter - responsible for the logic of all SEL Answer list from the database
 * and handling starting a new SEL run.
 *
 */
public class SELAnswerListPresenter extends
        CommonHandlerPresenter<SELAnswerListView>{

    private long currentClazzUid = -1L;

    private UmProvider<Person> selAnswersProvider;

    public SELAnswerListPresenter(Object context, Hashtable arguments, SELAnswerListView view) {
        super(context, arguments, view);

        //Get the clazz uid and set it to the presenter.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }

    }

    /**
     * The Presenter here's onCreate.
     * In Order:
     *      1. This populates the provider and sets it to the View.
     *
     * This will be called when the implementation View is ready.
     * (ie: on Android, this is called in the Fragment's onCreateView() )
     *
     * @param savedState    The saved state
     */
    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        selAnswersProvider = UmAppDatabase.getInstance(context).getSocialNominationQuestionSetResponseDao()
                .findAllDoneSN();
        setSELAnswerProviderToView();

    }

    /**
     * Sets the SEL Answer list provider of Person type that is set on this Presenter to the View.
     */
    private void setSELAnswerProviderToView(){
        view.setSELAnswerListProvider(selAnswersProvider);
    }

    /**
     * Handles when Record SEL FAB button is pressed.
     * It should open a new Record SEL activity.
     */
    public void handleClickRecordSEL() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ARG_CLAZZ_UID, currentClazzUid);

        impl.go(SELSelectStudentView.VIEW_NAME, args, view.getContext());

    }


    /**
     * UstadBaseController's setUiString().
     *
     * Right not it doesn't do anything.
     */
    @Override
    public void setUIStrings() {

    }

    /**
     * Handles what happens when the primary button of every item on the SEL Answer list recycler
     * adapter is clicked - It should go to the SEL Answers. TODO: Finish this.
     *
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    @Override
    public void handleCommonPressed(Object arg) {
//        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
//        Hashtable<String, Object> args = new Hashtable<>();
//        args.put(ARG_CLAZZ_UID, currentClazzUid);
//        args.put(ARG_PERSON_UID, arg);
//        //Go somewhere ? To SELEdit maybe?
//        //TODO: this

    }

    /**
     * Handles what happens when the secondary button of every item on the SEL Answer List Recycler
     * adapter is clicked - It does nothing here.
     *
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    @Override
    public void handleSecondaryPressed(Object arg) {
        // No secondary option here.
    }
}
