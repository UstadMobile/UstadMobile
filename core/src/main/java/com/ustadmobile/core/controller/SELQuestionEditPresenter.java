package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SELQuestionEditView;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;

import java.util.Hashtable;


/**
 * The SELQuestionEdit's Presenter - Responsible for the logic to add a new SocialNomination
 * Question. This is part of Class Management.
 *
 */
public class SELQuestionEditPresenter
        extends UstadBaseController<SELQuestionEditView> {

    private long DEFAULT_QUESTION_SET_UID = 1;

    private SocialNominationQuestionDao socialNominationQuestionDao;


    public SELQuestionEditPresenter(Object context, Hashtable arguments, SELQuestionEditView view) {
        super(context, arguments, view);

        socialNominationQuestionDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionDao();

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }

    /**
     * Done click handler when a new Question has been filled in -  This will add the new Question
     * in to the database where SEL task will pick it up for future SEL runs.
     *
     * @param newQuestion   The string of the new Question
     * @param allClasses    The checkbox value if this question is for every class (default true)
     * @param multiNominations  The checkbox value to check if mulitple nominations for this
     *                          Question is allowed (default true)
     */
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
                        exception.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }

    /**
     * Overridden method. Does nothing.
     */
    @Override
    public void setUIStrings() {

    }

}
