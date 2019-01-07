package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SELQuestionSetDetailView;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;

import java.util.Hashtable;

import static com.ustadmobile.core.view.SELQuestionSetDetailView.ARG_SEL_QUESTION_SET_NAME;
import static com.ustadmobile.core.view.SELQuestionSetDetailView.ARG_SEL_QUESTION_SET_UID;

public class SELQuestionSetDetailPresenter extends
        UstadBaseController<SELQuestionSetDetailView> {

    private UmProvider<SocialNominationQuestion> questionUmProvider;
    UmAppDatabase repository;
    private SocialNominationQuestionDao selQuestionDao;
    private SocialNominationQuestionSetDao selQuestionSetDao;
    private long questionSetUid = 0L;
    private String questionSetName = "";

    public SELQuestionSetDetailPresenter(Object context, Hashtable arguments, SELQuestionSetDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        selQuestionDao = repository.getSocialNominationQuestionDao();

        if(arguments.containsKey(ARG_SEL_QUESTION_SET_UID)){
            questionSetUid = (long) arguments.get(ARG_SEL_QUESTION_SET_UID);
        }

        if (arguments.containsKey(ARG_SEL_QUESTION_SET_NAME)) {
            questionSetName = (String) arguments.get(ARG_SEL_QUESTION_SET_NAME);
        }
    }

    public void handleQuestionEdit(long selQuestionUid){

        //TODO
    }

    public void handleQuestionDelete(long selQuestionUid){

        //TODO
    }

    public void handleClickPrimaryActionButton(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        //TODO:
        //impl.go(SELQuestionDetail2View.VIEW_NAMe, args, context);
    }

    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        questionUmProvider = selQuestionDao.findAllQuestionsInSet(questionSetUid);
        view.setListProvider(questionUmProvider);
        view.updateToolbarTitle(questionSetName);
    }

    @Override
    public void setUIStrings() {

    }
}
