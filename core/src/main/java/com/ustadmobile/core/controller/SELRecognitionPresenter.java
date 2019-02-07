package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.db.dao.SelQuestionDao;
import com.ustadmobile.core.db.dao.SelQuestionResponseDao;
import com.ustadmobile.core.db.dao.SelQuestionSetDao;
import com.ustadmobile.core.db.dao.SelQuestionSetResponseDao;
import com.ustadmobile.core.db.dao.SelQuestionSetResponseDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.core.view.SELRecognitionView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture;
import com.ustadmobile.lib.db.entities.SelQuestion;
import com.ustadmobile.lib.db.entities.SelQuestionResponse;
import com.ustadmobile.lib.db.entities.SelQuestionSet;
import com.ustadmobile.lib.db.entities.SelQuestionSetResponse;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_CLAZZMEMBER_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_INDEX_ID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_RESPONSE_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_SET_RESPONSE_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_SET_UID;
import static com.ustadmobile.core.view.SELEditView.ARG_QUESTION_UID;
import static com.ustadmobile.core.view.SELQuestionView.ARG_QUESTION_INDEX;
import static com.ustadmobile.core.view.SELQuestionView.ARG_QUESTION_TEXT;
import static com.ustadmobile.core.view.SELQuestionView.ARG_QUESTION_TOTAL;
import static com.ustadmobile.core.view.SELRecognitionView.ARG_RECOGNITION_UID;
import static com.ustadmobile.core.view.SELSelectStudentView.ARG_DONE_CLAZZMEMBER_UIDS;
import static com.ustadmobile.core.view.SELSelectStudentView.ARG_SELECTED_QUESTION_SET_UID;


/**
 * The SELRecognition Presenter - responsible for loading students with toggle names and allowing
 * for the SEL task to continue only when recognition check is enabled.
 */
public class SELRecognitionPresenter
        extends CommonHandlerPresenter<SELRecognitionView> {

    //Any arguments stored as variables here
    private long currentClazzUid = -1;
    private long currentPersonUid = -1;
    private long currentClazzMemberUid = -1;
    private long currentRecognitionQuestionNominationResponse = -1;
    private String doneClazzMemberUids ="";
    private long currentQuestionSetUid = 0;

    //Provider 
    private UmProvider<PersonWithPersonPicture> providerList;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SELRecognitionPresenter(Object context, Hashtable arguments, SELRecognitionView view) {
        super(context, arguments, view);

        //Get class uid arguments and set them.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
        //Person uid argument gotten and set to Presenter
        if(arguments.containsKey(ARG_PERSON_UID)){
            currentPersonUid = (long) arguments.get(ARG_PERSON_UID);
        }
        //Clazz Member doing the SEL task.
        if(arguments.containsKey(ARG_CLAZZMEMBER_UID)){
            currentClazzMemberUid = (long) arguments.get(ARG_CLAZZMEMBER_UID);
        }
        //Recognition Uid.
        if(arguments.containsKey(ARG_RECOGNITION_UID)){
            currentRecognitionQuestionNominationResponse = (long) arguments.get(ARG_RECOGNITION_UID);
        }

        //Add on any SEL things done
        if(arguments.containsKey(ARG_DONE_CLAZZMEMBER_UIDS)){
            doneClazzMemberUids = (String) arguments.get(ARG_DONE_CLAZZMEMBER_UIDS);
        }

        if(arguments.containsKey(ARG_SELECTED_QUESTION_SET_UID)){
            currentQuestionSetUid = (long) arguments.get(ARG_SELECTED_QUESTION_SET_UID);
        }

    }

    /**
     * In Order:
     *          1. Gets all Clazz Member as UmProvider from the database of type Person and sets it
     *          to the view.
     *
     * @param savedState    The saved state
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate the provider
        providerList = repository.getClazzMemberDao()
                .findAllPeopleWithPersonPictureInClassUid(currentClazzUid);
        setListProviderToView();

    }

    /**
     * Sets the Clazz Member people provider set in the Presenter to the View.
     */
    private void setListProviderToView(){
        view.setListProvider(providerList);
    }

    /**
     * Goes to next question after recognition was checked ok.
     * This method goes through all questions set for SEL in order and loads the SELQuestionDetail
     * screen first, and so on.
     *
     */
    private void goToNextQuestion(){

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        SelQuestionSetResponseDao selQuestionSetResponseDao =
                repository.getSocialNominationQuestionSetResponseDao();
        SelQuestionSetDao questionSetDao = repository
                .getSocialNominationQuestionSetDao();
        SelQuestionDao questionDao = repository
                .getSocialNominationQuestionDao();

        //Loop through questions.
        questionSetDao.findByUidAsync(currentQuestionSetUid, new UmCallback<SelQuestionSet>() {
            @Override
            public void onSuccess(SelQuestionSet questionSet) {
                //Find total number of questions as well.
                int totalSELQuestions =
                        questionDao.findTotalNumberOfActiveQuestionsInAQuestionSet
                                (questionSet.getSelQuestionSetUid());

                questionDao.findNextQuestionByQuestionSetUidAsync(questionSet.getSelQuestionSetUid(),
                        0, new UmCallback<SelQuestion>() {
                            @Override
                            public void onSuccess(SelQuestion nextQuestion) {
                                if(nextQuestion != null) {

                                    SelQuestionSetResponse newResponse = new SelQuestionSetResponse();
                                    newResponse.setSelQuestionSetResponseStartTime(System.currentTimeMillis());
                                    newResponse.setSelQuestionSetResponseSelQuestionSetUid(
                                            questionSet.getSelQuestionSetUid());
                                    newResponse.setSelQuestionSetResponseClazzMemberUid(currentClazzMemberUid);

                                    selQuestionSetResponseDao.insertAsync(newResponse, new UmCallback<Long>() {
                                        @Override
                                        public void onSuccess(Long questionSetResponseUid) {

                                            view.finish();

                                            //Make a question response for the next Question for
                                            // this Response-Set instance.
                                            SelQuestionResponseDao questionResponseDao =
                                                    repository.getSocialNominationQuestionResponseDao();
                                            SelQuestionResponse questionResponse =
                                                    new SelQuestionResponse();
                                            questionResponse
                                                    .setSelQuestionResponseSelQuestionSetResponseUid(
                                                            questionSetResponseUid);
                                                            //newResponse.getSocialNominationQuestionSetResposeUid());
                                            questionResponse
                                                    .setSelQuestionResponseSelQuestionUid(
                                                            nextQuestion.getSelQuestionUid());
                                            questionResponse
                                                    .setSelQuestionResponseUid(
                                                            questionResponseDao.insert(questionResponse));

                                            //Create arguments
                                            Hashtable<String, Object> args = new Hashtable<>();
                                            args.put(ARG_CLAZZ_UID, currentClazzUid);
                                            args.put(ARG_PERSON_UID, currentPersonUid);
                                            args.put(ARG_QUESTION_SET_UID, questionSet.getSelQuestionSetUid());
                                            args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid);
                                            args.put(ARG_QUESTION_UID, nextQuestion.getSelQuestionUid());
                                            args.put(ARG_QUESTION_INDEX_ID, nextQuestion.getQuestionIndex());
                                            args.put(ARG_QUESTION_SET_RESPONSE_UID, questionSetResponseUid);
                                            args.put(ARG_QUESTION_TEXT, nextQuestion.getQuestionText());
                                            args.put(ARG_QUESTION_INDEX, nextQuestion.getQuestionIndex());
                                            args.put(ARG_QUESTION_TOTAL, totalSELQuestions);
                                            args.put(ARG_QUESTION_RESPONSE_UID,
                                                    questionResponse.getSelQuestionResponseUid());

                                            args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids);

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

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });



//        questionSetDao.findAllQuestionsAsync(new UmCallback<List<SelQuestionSet>>() {
//            @Override
//            public void onSuccess(List<SelQuestionSet> questionSets) {
//
//                //Update: Question Set selection moved to SELSelectStudentView screen in Sprint 5
//                //TODOne: Change this when we add more Question Sets to findNextQuestionSet
//                // like we did for findNextQuestion
//                for(SelQuestionSet questionSet : questionSets){
//
//                    //Find total number of questions as well.
//                    int totalSELQuestions =
//                            questionDao.findTotalNumberOfActiveQuestionsInAQuestionSet
//                                    (questionSet.getSocialNominationQuestionSetUid());
//
//                    questionDao.findNextQuestionByQuestionSetUidAsync(questionSet.getSocialNominationQuestionSetUid(),
//                        0, new UmCallback<SelQuestion>() {
//                        @Override
//                        public void onSuccess(SelQuestion nextQuestion) {
//                            if(nextQuestion != null) {
//
//                                SelQuestionSetResponse newResponse = new SelQuestionSetResponse();
//                                newResponse.setSocialNominationQuestionSetResponseStartTime(System.currentTimeMillis());
//                                newResponse.setSocialNominationQuestionSetResponseSocialNominationQuestionSetUid(
//                                        questionSet.getSocialNominationQuestionSetUid());
//                                newResponse.setSocialNominationQuestionSetResponseClazzMemberUid(currentClazzMemberUid);
//
//                                socialNominationQuestionSetResponseDao.insertAsync(newResponse, new UmCallback<Long>() {
//                                    @Override
//                                    public void onSuccess(Long questionSetResponseUid) {
//
//                                        view.finish();
//
//                                        //Make a question response for the next Question for
//                                        // this Response-Set instance.
//                                        SelQuestionResponseDao questionResponseDao =
//                                                repository.getSocialNominationQuestionResponseDao();
//                                        SelQuestionResponse questionResponse =
//                                                new SelQuestionResponse();
//                                        questionResponse
//                                                .setSocialNominationQuestionResponseSocialNominationQuestionSetResponseUid(
//                                                        newResponse.getSocialNominationQuestionSetResposeUid());
//                                        questionResponse
//                                                .setSocialNominationQuestionResponseSocialNominationQuestionUid(
//                                                        nextQuestion.getSocialNominationQuestionUid());
//                                        questionResponse
//                                                .setSocialNominationQuestionResponseUid(
//                                                        questionResponseDao.insert(questionResponse));
//
//                                        //Create arguments
//                                        Hashtable<String, Object> args = new Hashtable<>();
//                                        args.put(ARG_CLAZZ_UID, currentClazzUid);
//                                        args.put(ARG_PERSON_UID, currentPersonUid);
//                                        args.put(ARG_QUESTION_SET_UID, questionSet.getSocialNominationQuestionSetUid());
//                                        args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid);
//                                        args.put(ARG_QUESTION_UID, nextQuestion.getSocialNominationQuestionUid());
//                                        args.put(ARG_QUESTION_INDEX_ID, nextQuestion.getQuestionIndex());
//                                        args.put(ARG_QUESTION_SET_RESPONSE_UID, questionSetResponseUid);
//                                        args.put(ARG_QUESTION_TEXT, nextQuestion.getQuestionText());
//                                        args.put(ARG_QUESTION_INDEX, nextQuestion.getQuestionIndex());
//                                        args.put(ARG_QUESTION_TOTAL, totalSELQuestions);
//                                        args.put(ARG_QUESTION_RESPONSE_UID, questionResponse.getSocialNominationQuestionResponseUid());
//
//                                        args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids);
//
//                                        impl.go(SELQuestionView.VIEW_NAME, args, view.getContext());
//
//                                    }
//
//                                    @Override
//                                    public void onFailure(Throwable exception) {
//                                        exception.printStackTrace();
//                                    }
//                                });
//
//                            }else{
//                                //End the SEL activities properly.
//                                view.finish();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Throwable exception) {
//                            exception.printStackTrace();
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable exception) {
//                exception.printStackTrace();
//            }
//        });


    }

    /**
     * Primary action button handler - To go next to the first SEL question - Checks if recognition
     * is pressed ok - then calls goToNextQuestion() - that checks the next question, etc.
     *
     * @param recognitionDone   true if recognition check box ticked.
     */
    public void handleClickPrimaryActionButton(boolean recognitionDone) {
        SelQuestionSetResponseDao questionResponseNominationDao =
                repository.getSocialNominationQuestionSetResponseDao();

        if(recognitionDone){

            questionResponseNominationDao.findByUidAsync(currentRecognitionQuestionNominationResponse,
                new UmCallback<SelQuestionSetResponse>() {
                    @Override
                    public void onSuccess(SelQuestionSetResponse responseNomination) {

                        responseNomination.setSelQuestionSetResponseFinishTime(
                                System.currentTimeMillis());
                        questionResponseNominationDao.updateAsync(responseNomination,
                                new UmCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer result) {
                                goToNextQuestion();
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

        }else{
            // UI : Maybe a toast ?
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            view.showMessage(impl.getString(MessageID.recognition_not_selected, context));

        }

    }



    /**
     * Handler for toggling student name. Does nothing on the presenter side.
     *
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    @Override
    public void handleCommonPressed(Object arg) {
        System.out.println("Toggle student name for SEL to see if the student got it correct.");
        //Doesn't do more than this. If you want it to do something, you would put it over here.
    }

    /**
     * Handler for secondary press on every student. Does not exist. Does nothing.
     *
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    @Override
    public void handleSecondaryPressed(Object arg) {
        //No secondary option here.
    }
}
