package com.ustadmobile.core.controller;

import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetResponseDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SELEditView;
import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.core.view.SELSelectConsentView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSetResponse;

import static com.ustadmobile.core.controller.ClazzListPresenter.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_CLAZZMEMBER_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_INDEX_ID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_SET_RESPONSE_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_SET_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_UID;


/**
 * The SELSelectConsent Presenter.
 */
public class SELSelectConsentPresenter
        extends UstadBaseController<SELSelectConsentView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private long currentPersonUid = -1;
    private long currentClazzMemberUid = -1;
    private int MIN_RECOGNITION_SUCCESSES = -1;

    public SELSelectConsentPresenter(Object context, Hashtable arguments, SELSelectConsentView view) {
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

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //No provider for this activity.
    }

    /**
     * Handles click "START SELECTION"
     * */
    public void handleClickPrimaryActionButton(boolean consentGiven) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        SocialNominationQuestionSetResponseDao socialNominationQuestionSetResponseDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionSetResponseDao();
        ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(context).getClazzMemberDao();
        SocialNominationQuestionSetDao questionSetDao = UmAppDatabase.getInstance(context)
                .getSocialNominationQuestionSetDao();
        SocialNominationQuestionDao questionDao = UmAppDatabase.getInstance(context)
                .getSocialNominationQuestionDao();


        //Check selectedObject for consent given.
        if(consentGiven){
            //Create arguments
            Hashtable args = new Hashtable();
            args.put(ARG_CLAZZ_UID, currentClazzUid);
            args.put(ARG_PERSON_UID, currentPersonUid);


            //TODO: Check: Decide when to show recognition and when to show the SEL questions themselves.
            socialNominationQuestionSetResponseDao.findAllPassedRecognitionByPersonUid(
                    currentClazzMemberUid,
                    new UmCallback<List<SocialNominationQuestionSetResponse>>() {
                @Override
                public void onSuccess(List<SocialNominationQuestionSetResponse> listPassed) {

                    if(listPassed.size() > MIN_RECOGNITION_SUCCESSES){
                        //Go straight to the Questions

                        //Loop through questions.
                        // 1. Loop through Question set
                        questionSetDao.findAllQuestionsAsync(new UmCallback<List<SocialNominationQuestionSet>>() {
                            @Override
                            public void onSuccess(List<SocialNominationQuestionSet> questionSets) {

                                //TODO: Change this when we add more Question Sets to findNextQuestionSet like we did for findNextQuestion
                                for(SocialNominationQuestionSet questionSet : questionSets){

                                    questionDao.findNextQuestionByQuestionSetUidAsync(questionSet.getSocialNominationQuestionSetUid(),
                                        0, new UmCallback<SocialNominationQuestion>() {
                                        @Override
                                        public void onSuccess(SocialNominationQuestion nextQuestion) {
                                            if(nextQuestion != null) {

                                                SocialNominationQuestionSetResponse newResponse = new SocialNominationQuestionSetResponse();
                                                newResponse.setSocialNominationQuestionSetResponseStartTime(System.currentTimeMillis());
                                                newResponse.setSocialNominationQuestionSetResponseSocialNominationQuestionSetUid(questionSet.getSocialNominationQuestionSetUid());
                                                newResponse.setSocialNominationQuestionSetResponseClazzMemberUid(currentClazzMemberUid);

                                                socialNominationQuestionSetResponseDao.insertAsync(newResponse, new UmCallback<Long>() {
                                                    @Override
                                                    public void onSuccess(Long questionSetResponseUid) {

                                                        view.finish();

                                                        args.put(ARG_QUESTION_SET_UID, questionSet.getSocialNominationQuestionSetUid());
                                                        args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid);
                                                        args.put(ARG_QUESTION_UID, nextQuestion.getSocialNominationQuestionUid());
                                                        args.put(ARG_QUESTION_INDEX_ID, nextQuestion.getQuestionIndex());
                                                        args.put(ARG_QUESTION_SET_RESPONSE_UID, questionSetResponseUid);


                                                        //TODO: Change to go to SELQuestion instead.
                                                        impl.go(SELEditView.VIEW_NAME, args, view.getContext());

                                                    }

                                                    @Override
                                                    public void onFailure(Throwable exception) {
                                                        System.out.println("fail3");
                                                    }
                                                });


                                            }else{
                                                //TODO. end the SEL activitieS properly.
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



                        //impl.go(SELQuestionView.VIEW_NAME, args, view.getContext());
                    }else{
                        //Go re-do / do the recognition activity.
                        SocialNominationQuestionSetResponse newResponse =
                                new SocialNominationQuestionSetResponse();
                        newResponse.setSocialNominationQuestionSetResponseStartTime(System.currentTimeMillis());
                        newResponse.setSocialNominationQuestionSetResponseClazzMemberUid(currentClazzMemberUid);
                        socialNominationQuestionSetResponseDao.insert(newResponse);
                        //impl.go(SELQuestionView.VIEW_NAME, args, view.getContext());
                        //TODO: at the end of the recognition, end update this object with
                        //      end time as well as calculate the percentage.
                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    System.out.println("fail4");
                }
            });
        }else {
            //TODO: Handle and think about what happens if the consent is NOT given.
            System.out.println("SELSelectConsentPresenter - No Consent - " +
                    "What to do ? Not doing anything.");
        }

    }

    @Override
    public void setUIStrings() {

    }

}
