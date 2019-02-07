package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SelQuestionSetDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AddQuestionSetDialogView;
import com.ustadmobile.core.view.SELQuestionSetDetailView;
import com.ustadmobile.core.view.SELQuestionSetsView;
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions;

import java.util.Hashtable;

public class SELQuestionSetsPresenter extends UstadBaseController<SELQuestionSetsView> {

    private UmProvider<SELQuestionSetWithNumQuestions> questionSetWithNumQuestionsUmProvider;
    UmAppDatabase repository;
    private SelQuestionSetDao selQuestionSetDao;


    public SELQuestionSetsPresenter(Object context, Hashtable arguments, SELQuestionSetsView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        selQuestionSetDao = repository.getSocialNominationQuestionSetDao();
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        questionSetWithNumQuestionsUmProvider = selQuestionSetDao
                .findAllQuestionSetsWithNumQuestions();
        view.setListProvider(questionSetWithNumQuestionsUmProvider);

    }

    public void handleGoToQuestionSet(long questionSetUid, String questionSetName){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(SELQuestionSetDetailView.ARG_SEL_QUESTION_SET_UID, questionSetUid);
        args.put(SELQuestionSetDetailView.ARG_SEL_QUESTION_SET_NAME, questionSetName);
        impl.go(SELQuestionSetDetailView.VIEW_NAME, args, context);
    }

    public void handleClickPrimaryActionButton(){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(AddQuestionSetDialogView.VIEW_NAME, args, context);

    }

}
