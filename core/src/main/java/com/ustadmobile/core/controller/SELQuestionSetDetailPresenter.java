package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SelQuestionDao;
import com.ustadmobile.core.db.dao.SelQuestionSetDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SELQuestionDetail2View;
import com.ustadmobile.core.view.SELQuestionSetDetailView;
import com.ustadmobile.lib.db.entities.SelQuestion;

import java.util.Hashtable;

import static com.ustadmobile.core.view.SELQuestionSetDetailView.ARG_SEL_QUESTION_SET_NAME;
import static com.ustadmobile.core.view.SELQuestionSetDetailView.ARG_SEL_QUESTION_SET_UID;

public class SELQuestionSetDetailPresenter extends
        UstadBaseController<SELQuestionSetDetailView> {

    private UmProvider<SelQuestion> questionUmProvider;
    UmAppDatabase repository;
    private SelQuestionDao selQuestionDao;
    private SelQuestionSetDao selQuestionSetDao;
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

    public void handleQuestionEdit(SelQuestion question){

        goToQuestionDetail(question);
    }

    public void handleQuestionDelete(long selQuestionUid){
        selQuestionDao.findByUidAsync(selQuestionUid, new UmCallback<SelQuestion>() {
            @Override
            public void onSuccess(SelQuestion selQuestionObj) {
                if(selQuestionObj != null){
                    selQuestionObj.setQuestionActive(false);
                    selQuestionDao.updateAsync(selQuestionObj, new UmCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            //ola
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    public void goToQuestionDetail(SelQuestion question){
        long questionUid = question.getSelQuestionUid();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(SELQuestionDetail2View.ARG_QUESTION_UID_QUESTION_DETAIL, questionUid);
        args.put(ARG_SEL_QUESTION_SET_UID, question.getSelQuestionSelQuestionSetUid());
        impl.go(SELQuestionDetail2View.VIEW_NAME, args, context);
    }

    public void handleClickPrimaryActionButton(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_SEL_QUESTION_SET_UID, questionSetUid);
        impl.go(SELQuestionDetail2View.VIEW_NAME, args, context);
    }

    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        questionUmProvider = selQuestionDao.findAllActivrQuestionsInSet(questionSetUid);
        view.setListProvider(questionUmProvider);
        view.updateToolbarTitle(questionSetName);
    }
}
