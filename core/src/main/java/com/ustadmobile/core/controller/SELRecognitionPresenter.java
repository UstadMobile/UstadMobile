package com.ustadmobile.core.controller;

import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionResponseNominationDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetResponseDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.core.view.SELRecognitionView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionResponseNomination;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSetResponse;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_CLAZZMEMBER_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_INDEX_ID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_SET_RESPONSE_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_SET_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_UID;
import static com.ustadmobile.core.view.SELQuestionView.ARG_QUESTION_INDEX;
import static com.ustadmobile.core.view.SELQuestionView.ARG_QUESTION_TEXT;
import static com.ustadmobile.core.view.SELQuestionView.ARG_QUESTION_TOTAL;
import static com.ustadmobile.core.view.SELRecognitionView.ARG_RECOGNITION_UID;


/**
 * The SELRecognition Presenter.
 */
public class SELRecognitionPresenter
        extends CommonHandlerPresenter<SELRecognitionView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private long currentPersonUid = -1;
    private long currentClazzMemberUid = -1;
    private long currnetRecognitionQuestionNominationResponse = -1;

    //Provider 
    UmProvider<Person> providerList;

    public SELRecognitionPresenter(Object context, Hashtable arguments, SELRecognitionView view) {
        super(context, arguments, view);

        //Get arguments and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
        if(arguments.containsKey(ARG_PERSON_UID)){
            currentPersonUid = (long) arguments.get(ARG_PERSON_UID);
        }if(arguments.containsKey(ARG_CLAZZMEMBER_UID)){
            currentClazzMemberUid = (long) arguments.get(ARG_CLAZZMEMBER_UID);
        }
        if(arguments.containsKey(ARG_RECOGNITION_UID)){
            currnetRecognitionQuestionNominationResponse = (long) arguments.get(ARG_RECOGNITION_UID);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate the provider
        providerList = UmAppDatabase.getInstance(context).getClazzMemberDao()
                .findAllPeopleInClassUid(currentClazzUid);

        //set Provider.
        view.setListProvider(providerList);

    }

    public void goToNextQuestion(){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        SocialNominationQuestionSetResponseDao socialNominationQuestionSetResponseDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionSetResponseDao();
        SocialNominationQuestionSetDao questionSetDao = UmAppDatabase.getInstance(context)
                .getSocialNominationQuestionSetDao();
        SocialNominationQuestionDao questionDao = UmAppDatabase.getInstance(context)
                .getSocialNominationQuestionDao();

        //Loop through questions.
        questionSetDao.findAllQuestionsAsync(new UmCallback<List<SocialNominationQuestionSet>>() {
            @Override
            public void onSuccess(List<SocialNominationQuestionSet> questionSets) {

                //TODO: Change this when we add more Question Sets to findNextQuestionSet like we did for findNextQuestion
                for(SocialNominationQuestionSet questionSet : questionSets){

                    //Find total number of questions as well.
                    int totalSELQuestions = questionDao.findTotalNumberOfQuestions();

                    questionDao.findNextQuestionByQuestionSetUidAsync(questionSet.getSocialNominationQuestionSetUid(),
                            0, new UmCallback<SocialNominationQuestion>() {
                                @Override
                                public void onSuccess(SocialNominationQuestion nextQuestion) {
                                    if(nextQuestion != null) {

                                        SocialNominationQuestionSetResponse newResponse = new SocialNominationQuestionSetResponse();
                                        newResponse.setSocialNominationQuestionSetResponseStartTime(System.currentTimeMillis());
                                        newResponse.setSocialNominationQuestionSetResponseSocialNominationQuestionSetUid(
                                                questionSet.getSocialNominationQuestionSetUid());
                                        newResponse.setSocialNominationQuestionSetResponseClazzMemberUid(currentClazzMemberUid);

                                        socialNominationQuestionSetResponseDao.insertAsync(newResponse, new UmCallback<Long>() {
                                            @Override
                                            public void onSuccess(Long questionSetResponseUid) {

                                                view.finish();

                                                //Create arguments
                                                Hashtable args = new Hashtable();
                                                args.put(ARG_CLAZZ_UID, currentClazzUid);
                                                args.put(ARG_PERSON_UID, currentPersonUid);
                                                args.put(ARG_QUESTION_SET_UID, questionSet.getSocialNominationQuestionSetUid());
                                                args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid);
                                                args.put(ARG_QUESTION_UID, nextQuestion.getSocialNominationQuestionUid());
                                                args.put(ARG_QUESTION_INDEX_ID, nextQuestion.getQuestionIndex());
                                                args.put(ARG_QUESTION_SET_RESPONSE_UID, questionSetResponseUid);
                                                args.put(ARG_QUESTION_TEXT, nextQuestion.getQuestionText());
                                                args.put(ARG_QUESTION_INDEX, nextQuestion.getQuestionIndex());
                                                args.put(ARG_QUESTION_TOTAL, totalSELQuestions);

                                                impl.go(SELQuestionView.VIEW_NAME, args, view.getContext());

                                            }

                                            @Override
                                            public void onFailure(Throwable exception) {
                                                System.out.println("fail3");
                                            }
                                        });

                                    }else{
                                        //End the SEL activities properly.
                                        view.finish();
                                    }
                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    System.out.println("fail2");
                                }
                            });
                }

            }

            @Override
            public void onFailure(Throwable exception) {
                System.out.println("fail1");
            }
        });


    }

    public void handleClickPrimaryActionButton(boolean recognitionDone) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        SocialNominationQuestionSetResponseDao questionResponseNominationDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionSetResponseDao();

        if(recognitionDone){

            questionResponseNominationDao.findByUidAsync(currnetRecognitionQuestionNominationResponse,
                    new UmCallback<SocialNominationQuestionSetResponse>() {
                        @Override
                        public void onSuccess(SocialNominationQuestionSetResponse responseNomination) {

                            responseNomination.setSocialNominationQuestionSetResponseFinishTime(System.currentTimeMillis());
                            questionResponseNominationDao.updateAsync(responseNomination, new UmCallback<Integer>() {
                                @Override
                                public void onSuccess(Integer result) {
                                    goToNextQuestion();
                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    System.out.println("SELRecognitionPresenter - handleClickPrimaryActionButton - Failed");
                                }
                            });
                        }

                        @Override
                        public void onFailure(Throwable exception) {

                        }
                    });



        }else{
            //TODO: Handle if not recognised
            System.out.println("SELRecognitionPresenter - Student recognized not checked.");
        }

    }

    @Override
    public void setUIStrings() {

    }

    @Override
    public void handleCommonPressed(Object arg) {
        System.out.println("Toggle student name for SEL to see if the student got it correct.");
        //Doesn't do more than this. If you want it to do something, you would put it over here.
    }

    @Override
    public void handleSecondaryPressed(Object arg) {
        //No secondary option here.
    }
}
