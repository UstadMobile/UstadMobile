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

    SelQuestionOption currentOption;
    UmAppDatabase repository;
    private long currentQuestionUid;
    private long currnetQuestonOptionUid;
    SelQuestionOptionDao questionOptionDao;

    public AddQuestionOptionDialogPresenter(Object context, Hashtable arguments, AddQuestionOptionDialogView view) {
        super(context, arguments, view);
        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        
        if(arguments.containsKey(ARG_QUESTION_UID_QUESTION_DETAIL)){
            currentQuestionUid = (long) arguments.get(ARG_QUESTION_UID_QUESTION_DETAIL);
        }
        if(arguments.containsKey(ARG_QUESTION_OPTION_UID)){
            currnetQuestonOptionUid = (long) arguments.get(ARG_QUESTION_OPTION_UID);
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        questionOptionDao = repository.getSELQuestionOptionDao();
        questionOptionDao.findByUidAsync(currnetQuestonOptionUid, 
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
            public void onFailure(Throwable exception) {

            }
        });
        
    }

    public void handleCancelQuestionOption(){
        currentOption = null;
    }
    
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
                        public void onFailure(Throwable exception) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }
}
