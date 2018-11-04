package com.ustadmobile.core.controller;

import java.util.Hashtable;
import java.util.List;

import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetResponseDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.core.view.SELRecognitionView;
import com.ustadmobile.core.view.SELSelectConsentView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSetResponse;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
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
 * The SELSelectConsent Presenter - responsible for the logic in displaying seeking consent from
 * the student/sel officer on behalf of the student - a reminder that we are taking this information.
 *
 */
public class SELSelectConsentPresenter
        extends UstadBaseController<SELSelectConsentView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private long currentPersonUid = -1;
    private long currentClazzMemberUid = -1;
    private int MIN_RECOGNITION_SUCCESSES = 0;

    public SELSelectConsentPresenter(Object context, Hashtable arguments, SELSelectConsentView view) {
        super(context, arguments, view);

        //Get clazz uid and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
        //Get person uid
        if(arguments.containsKey(ARG_PERSON_UID)){
            currentPersonUid = (long) arguments.get(ARG_PERSON_UID);
        }
        //Get clazz member doing the sel
        if(arguments.containsKey(ARG_CLAZZMEMBER_UID)){
            currentClazzMemberUid = (long) arguments.get(ARG_CLAZZMEMBER_UID);
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }

    /**
     * Handles click "START SELECTION". Checks for consent and Gets to display the first question
     * after creating a new SEL run on the database and response.
     *
     * @param consentGiven true if consent given. False if not.
     */
    public void handleClickPrimaryActionButton(boolean consentGiven) {
        SocialNominationQuestionSetResponseDao socialNominationQuestionSetResponseDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionSetResponseDao();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Check selectedObject for consent given.
        if(consentGiven){

            //TODO: Check: Decide when to show recognition and when to show the SEL questions themselves.
            socialNominationQuestionSetResponseDao.findAllPassedRecognitionByPersonUid(
                    currentClazzMemberUid,
                    new UmCallback<List<SocialNominationQuestionSetResponse>>() {
                @Override
                public void onSuccess(List<SocialNominationQuestionSetResponse> listPassed) {

                    if(listPassed.size() > MIN_RECOGNITION_SUCCESSES){
                        //Go straight to the Questions
                        goToNextQuestion();

                    }else{
                        //Go re-do/do the recognition activity.
                        SocialNominationQuestionSetResponse newResponse =
                                new SocialNominationQuestionSetResponse();
                        newResponse.setSocialNominationQuestionSetResponseStartTime(System.currentTimeMillis());
                        newResponse.setSocialNominationQuestionSetResponseClazzMemberUid(currentClazzMemberUid);
                        newResponse.setSocialNominationQuestionSetResposeUid(
                                socialNominationQuestionSetResponseDao.insert(newResponse));

                        Hashtable<String, Object> args = new Hashtable<>();
                        args.put(ARG_RECOGNITION_UID, newResponse.getSocialNominationQuestionSetResposeUid());
                        args.put(ARG_CLAZZ_UID, currentClazzUid);
                        args.put(ARG_PERSON_UID, currentPersonUid);
                        args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid);

                        view.finish();

                        impl.go(SELRecognitionView.VIEW_NAME, args, view.getContext());

                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }else {
            //TODO: Handle and think about what happens if the consent is NOT given.
            System.out.println("SELSelectConsentPresenter - No Consent - " +
                    "What to do ? Not doing anything.");
            //UI: Maybe some toast?
        }

    }

    /**
     * Method that checks where the current SEL task is in and goes to the next question.
     *
     */
    private void goToNextQuestion(){

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
                                                Hashtable<String, Object> args = new Hashtable<>();
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
                                                exception.printStackTrace();
                                            }
                                        });

                                    }else{
                                        //End the SEL activities properly.
                                        view.finish();
                                    }
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
                exception.printStackTrace();
            }
        });
    }

    /**
     * Overridden. Does nothing.
     */
    @Override
    public void setUIStrings() {

    }

}
