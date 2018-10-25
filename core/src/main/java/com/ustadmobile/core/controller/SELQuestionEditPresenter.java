package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SELQuestionEditView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;


/**
 * The SELQuestionEdit Presenter.
 */
public class SELQuestionEditPresenter
        extends UstadBaseController<SELQuestionEditView> {

    private long DEFAULT_QUESTION_SET_UID = 1;

    //Any arguments stored as variables here
    //eg: private long clazzUid = -1;
    SocialNominationQuestionDao socialNominationQuestionDao;


    public SELQuestionEditPresenter(Object context, Hashtable arguments, SELQuestionEditView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        //eg: if(arguments.containsKey(ARG_CLAZZ_UID)){
        //    currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        //}
        socialNominationQuestionDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }

    public void handleClickDone(String newQuestion, boolean allClasses, boolean multiNominations){

        socialNominationQuestionDao.getMaxIndexAsync(new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                SocialNominationQuestion socialNominationQuestion = new SocialNominationQuestion();
                socialNominationQuestion.setQuestionText(newQuestion);
                socialNominationQuestion.setQuestionIndex(result);
                socialNominationQuestion.setAssignToAllClasses(allClasses);
                socialNominationQuestion.setMultiNominations(multiNominations);
                socialNominationQuestion.setSocialNominationQuestionSocialNominationQuestionSetUid(
                        DEFAULT_QUESTION_SET_UID);

                socialNominationQuestionDao.insertAsync(socialNominationQuestion,
                        new UmCallback<Long>() {
                    @Override
                    public void onSuccess(Long result) {
                        view.finish();
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

    }

    @Override
    public void setUIStrings() {

    }

}
