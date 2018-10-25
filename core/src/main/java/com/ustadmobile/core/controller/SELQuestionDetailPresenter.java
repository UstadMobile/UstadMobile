package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SELQuestionDetailView;
import com.ustadmobile.core.view.SELQuestionEditView;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;


/**
 * The SELQuestionDetail Presenter.
 */
public class SELQuestionDetailPresenter
        extends UstadBaseController<SELQuestionDetailView> {

    //Any arguments stored as variables here
    //eg: private long clazzUid = -1;

    //Provider 
    UmProvider<SocialNominationQuestion> providerList;

    public SELQuestionDetailPresenter(Object context, Hashtable arguments, SELQuestionDetailView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        //eg: if(arguments.containsKey(ARG_CLAZZ_UID)){
        //    currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        //}

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate the provider
        providerList = UmAppDatabase.getInstance(context).getSocialNominationQuestionDao()
                .findAllQuestions();

        //set Provider.
        view.setListProvider(providerList);

    }

    public void handleClickAddQuestion(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(SELQuestionEditView.VIEW_NAME, view.getContext());
    }

    public void handleClickDone(){
        //Exit the activity
        view.finish();
    }

    public void handleClickSortOrder(int sortOrder){
        //TODO: Sort order

    }

    @Override
    public void setUIStrings() {

    }

}
