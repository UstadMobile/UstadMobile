package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELQuestionDetailView;
import com.ustadmobile.core.view.SELQuestionEditView;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;


/**
 * The SELQuestionDetail Presenter - responsible for the logic of showing all SEL Questions
 * and the ability to add new Questions for the SEL task. This is a Class Management feature.
 *
 */
public class SELQuestionDetailPresenter
        extends UstadBaseController<SELQuestionDetailView> {

    //Provider 
    private UmProvider<SocialNominationQuestion> providerList;

    public SELQuestionDetailPresenter(Object context, Hashtable arguments, SELQuestionDetailView view) {
        super(context, arguments, view);
    }

    /**
     * In Order:
 *          1. Gets all Social Nomination Questions and sets the SocialNominationQuestion Provider
     *          to the view.
     * @param savedState    The saved state
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Populate the provider
        providerList = repository.getSocialNominationQuestionDao()
                .findAllQuestions();
        setQuestionListProviderToView();

    }

    /**
     * Sets already set SocialNominationQuestion UmProvider to the View.
     */
    private void setQuestionListProviderToView(){
        //set Provider.
        view.setListProvider(providerList);
    }

    /**
     * Handler for when Add Question is pressed - it takes the application to the SELQuestionEdit
     * screen - where new Question can be added.
     *
     */
    public void handleClickAddQuestion(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(SELQuestionEditView.VIEW_NAME, view.getContext());
    }

    /**
     * Clicking done will close the activity. Since everything is already persisted OK.
     */
    public void handleClickDone(){
        //Exit the activity
        view.finish();
    }

//    public void handleClickSortOrder(int sortOrder){
//        //TODO: Sort order
//
//    }

    /**
     * Overridden. Does nothing here.
     */
    @Override
    public void setUIStrings() {

    }

}
