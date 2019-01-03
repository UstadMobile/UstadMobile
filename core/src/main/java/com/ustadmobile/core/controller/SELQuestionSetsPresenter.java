package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AddQuestionSetDialogView;
import com.ustadmobile.core.view.SELQuestionSetsView;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;

import java.util.Hashtable;

public class SELQuestionSetsPresenter extends UstadBaseController<SELQuestionSetsView> {

    private UmProvider<SocialNominationQuestionSet> questionSetUmProvider;
    UmAppDatabase repository;
    private SocialNominationQuestionSetDao socialNominationQuestionSetDao;


    public SELQuestionSetsPresenter(Object context, Hashtable arguments, SELQuestionSetsView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        socialNominationQuestionSetDao = repository.getSocialNominationQuestionSetDao();
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        questionSetUmProvider = socialNominationQuestionSetDao.findAllQuestions();

    }

    public void handleClickPrimaryActionButton(){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        impl.go(AddQuestionSetDialogView.VIEW_NAME, args, context);

    }

    @Override
    public void setUIStrings() {

    }
}
