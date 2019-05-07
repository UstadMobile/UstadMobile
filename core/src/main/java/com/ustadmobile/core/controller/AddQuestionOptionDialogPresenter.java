package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.SelQuestionOptionDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.AddQuestionOptionDialogView;
import com.ustadmobile.lib.db.entities.SelQuestionOption;

import java.util.Hashtable;

import static com.ustadmobile.core.view.SELQuestionDetail2View.ARG_QUESTION_OPTION_UID;
import static com.ustadmobile.core.view.SELQuestionDetail2View.ARG_QUESTION_UID_QUESTION_DETAIL;

public class AddQuestionOptionDialogPresenter extends UstadBaseController<AddQuestionOptionDialogView> {

    private SelQuestionOption currentOption;
    UmAppDatabase repository;
    private long currentQuestionUid;
    private long currentQuestionOptionUid;
    private SelQuestionOptionDao questionOptionDao;

    /**
     * Gets arguments and initialises app database and repositories
     * @param context   context
     * @param arguments arguments
     * @param view  view
     */
    public AddQuestionOptionDialogPresenter(Object context, Hashtable arguments,
                                            AddQuestionOptionDialogView view) {
        super(context, arguments, view);
        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get question uid
        if(arguments.containsKey(ARG_QUESTION_UID_QUESTION_DETAIL)){
            currentQuestionUid = (long) arguments.get(ARG_QUESTION_UID_QUESTION_DETAIL);
        }
        //Get Question option uid
        if(arguments.containsKey(ARG_QUESTION_OPTION_UID)){
            currentQuestionOptionUid = (long) arguments.get(ARG_QUESTION_OPTION_UID);
        }

        questionOptionDao = repository.getSELQuestionOptionDao();
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Get the question from database or create a new one and set it to the view
        questionOptionDao.findByUidAsync(currentQuestionOptionUid,
                new UmCallback<SelQuestionOption>() {
            @Override
            public void onSuccess(SelQuestionOption result) {
                if(result == null){
                    currentOption = new SelQuestionOption();
                    currentOption.setSelQuestionOptionQuestionUid(currentQuestionUid);
                    currentOption.setOptionText("");
                }else{
                    currentOption = result;
                }

                view.setOptionText(currentOption.getOptionText());
            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });
        
    }

    /**
     * Nulls current option (effectively dismissing the progress done in this presenter)
     */
    public void handleCancelQuestionOption(){
        currentOption = null;
    }

    /**
     * Persists the question with the new title to the database
     * @param newTitle  The new question title
     */
    public void handleAddQuestionOption(String newTitle){
        currentOption.setOptionText(newTitle);
        currentOption.setOptionActive(true);
        questionOptionDao.findByUidAsync(currentOption.getSelQuestionOptionUid(),
                new UmCallback<SelQuestionOption>() {
            @Override
            public void onSuccess(SelQuestionOption result) {
                if(result != null){
                    //exists. update
                    questionOptionDao.updateAsync(currentOption, new UmCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            view.finish();
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });
                }else{
                    //new. insert
                    questionOptionDao.insertAsync(currentOption, new UmCallback<Long>() {
                        @Override
                        public void onSuccess(Long result) {
                            view.finish();
                        }

                        @Override
                        public void onFailure(Throwable exception) {exception.printStackTrace();}
                    });
                }
            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });
    }
}
