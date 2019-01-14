package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionOptionDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AddQuestionOptionDialogView;
import com.ustadmobile.core.view.SELQuestionDetail2View;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionOption;

import java.util.Hashtable;

import static com.ustadmobile.core.view.SELQuestionDetail2View.ARG_QUESTION_OPTION_UID;
import static com.ustadmobile.core.view.SELQuestionDetail2View.ARG_QUESTION_UID_QUESTION_DETAIL;
import static com.ustadmobile.core.view.SELQuestionSetDetailView.ARG_SEL_QUESTION_SET_UID;

public class SELQuestionDetail2Presenter extends
        UstadBaseController<SELQuestionDetail2View> {


    //Provider
    private UmProvider<SocialNominationQuestionOption> providerList;
    UmAppDatabase repository;
    private long currentQuestionUid;
    private long currentQuestionSetUid;
    UmLiveData<SocialNominationQuestion> questionUmLiveData;

    private SocialNominationQuestion mOriginalQuestion;
    private SocialNominationQuestion mUpdatedQuestion;
    SocialNominationQuestionDao questionDao;
    SocialNominationQuestionOptionDao questionOptionDao;

    private String[] questionTypePresets;

    public SELQuestionDetail2Presenter(Object context, Hashtable arguments,
                                       SELQuestionDetail2View view) {
        super(context, arguments, view);


        if (arguments.containsKey(ARG_SEL_QUESTION_SET_UID)) {
            currentQuestionSetUid = (long) arguments.get(ARG_SEL_QUESTION_SET_UID);

        }
        if(arguments.containsKey(ARG_QUESTION_UID_QUESTION_DETAIL)){
            currentQuestionUid = (long) arguments.get(ARG_QUESTION_UID_QUESTION_DETAIL);
        }
    }

    @Override
    public void onCreate(Hashtable savedState){
        super.onCreate(savedState);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        questionDao = repository.getSocialNominationQuestionDao();
        questionOptionDao = repository.getSELQuestionOptionDao();

        providerList = repository.getSELQuestionOptionDao()
                .findAllOptionsByQuestionUidProvider(currentQuestionUid);



        //Set questionType preset
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        questionTypePresets = new String[]{
                impl.getString(MessageID.sel_question_type_nomination, context),
                impl.getString(MessageID.sel_question_type_multiple_choise, context),
                impl.getString(MessageID.sel_question_type_free_text, context)};

        //Set to view
        view.setQuestionTypePresets(questionTypePresets);

        //Create / Get question
        questionUmLiveData =
                repository.getSocialNominationQuestionDao().findByUidLive(currentQuestionUid);

        //Observe the live data :
        questionUmLiveData.observe(SELQuestionDetail2Presenter.this,
                SELQuestionDetail2Presenter.this::handleSELQuestionValueChanged);

        repository.getSocialNominationQuestionDao().findByUidAsync(currentQuestionUid,
                new UmCallback<SocialNominationQuestion>() {
            @Override
            public void onSuccess(SocialNominationQuestion selQuestion) {
                if(selQuestion != null){
                    mUpdatedQuestion = selQuestion;

                }else{

                    //Create a new one
                    selQuestion = new SocialNominationQuestion();
                    selQuestion.setSocialNominationQuestionSocialNominationQuestionSetUid(
                            currentQuestionSetUid);
                    mUpdatedQuestion = selQuestion;
                    if(mOriginalQuestion == null){
                        mOriginalQuestion = mUpdatedQuestion;
                    }
                }

                view.setQuestionOnView(mUpdatedQuestion);


            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        //Set provider
        view.setQuestionOptionsProvider(providerList);

    }

    public void handleQuestionTypeChange(int type){

        switch (type){
            case SocialNominationQuestionDao.SEL_QUESTION_TYPE_NOMINATION:
                view.showQuestionOptions(false);
                break;
            case SocialNominationQuestionDao.SEL_QUESTION_TYPE_MULTI_CHOICE:
                view.showQuestionOptions(true);
                break;
            case SocialNominationQuestionDao.SEL_QUESTION_TYPE_FREE_TEXT:
                view.showQuestionOptions(false);
                break;
            default:
                break;
        }
        if(mUpdatedQuestion!=null)
            mUpdatedQuestion.setQuestionType(type);
    }

    public void handleSELQuestionValueChanged(SocialNominationQuestion question){
        //set the og person value
        if(mOriginalQuestion == null)
            mOriginalQuestion = question;

        if(mUpdatedQuestion == null || !mUpdatedQuestion.equals(question)) {

            if(question != null) {
                //Update the currently editing class object
                mUpdatedQuestion = question;

                view.setQuestionOnView(question);
            }
        }
    }

    public void handleClickAddOption(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ARG_QUESTION_UID_QUESTION_DETAIL, currentQuestionUid);

        impl.go(AddQuestionOptionDialogView.VIEW_NAME, args, getContext());
    }

    public void handleClickDone(){

        mUpdatedQuestion.setQuestionActive(true);
        questionDao.updateAsync(mUpdatedQuestion, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                //Close the activity
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public void handleQuestionOptionEdit(long questionOptionUid){
        questionOptionDao.findByUidAsync(questionOptionUid, new UmCallback<SocialNominationQuestionOption>() {
            @Override
            public void onSuccess(SocialNominationQuestionOption result) {
                if(result != null){
                    //TODO: Go to dialog
                    UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                    Hashtable args = new Hashtable();
                    args.put(ARG_QUESTION_UID_QUESTION_DETAIL, currentQuestionUid);
                    args.put(ARG_QUESTION_OPTION_UID, result.getOptionText());
                    //impl.go(AddQuestionOptionDialogView.VIEW_NAME, args, getContext());

                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public void handleQuestionOptionDelete(long questionOptionUid){
        questionOptionDao.findByUidAsync(questionOptionUid, new UmCallback<SocialNominationQuestionOption>() {
            @Override
            public void onSuccess(SocialNominationQuestionOption result) {
                if(result != null){
                    result.setOptionActive(false);
                    questionOptionDao.updateAsync(result, new UmCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            view.finish();
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

    @Override
    public void setUIStrings() {

    }
}
