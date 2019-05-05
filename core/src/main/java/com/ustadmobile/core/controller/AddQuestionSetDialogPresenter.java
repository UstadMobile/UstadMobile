package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.SelQuestionSetDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.AddQuestionSetDialogView;
import com.ustadmobile.lib.db.entities.SelQuestionSet;

import java.util.Hashtable;

public class AddQuestionSetDialogPresenter extends UstadBaseController<AddQuestionSetDialogView> {

    private SelQuestionSet questionSet;
    private SelQuestionSetDao selQuestionSetDao;

    public AddQuestionSetDialogPresenter(Object context, Hashtable arguments,
                                         AddQuestionSetDialogView view) {
        super(context, arguments, view);

        UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);
        selQuestionSetDao = repository.getSocialNominationQuestionSetDao();
    }

    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);
    }

    /**
     * Creates a new question Set with the title given here
     * @param title The title of the question
     */
    public void handleAddQuestionSet(String title){
        questionSet = new SelQuestionSet();
        questionSet.setTitle(title);
        selQuestionSetDao.insertAsync(questionSet, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                //Do nothing
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * Nulls current question (effectively dismissing the progress done in this presenter)
     */
    public void handleCancelSchedule(){
        //Do nothing
        questionSet = null;
    }

}
