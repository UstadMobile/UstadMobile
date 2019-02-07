package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.SelQuestionDao;
import com.ustadmobile.core.db.dao.SelQuestionResponseDao;
import com.ustadmobile.core.db.dao.SelQuestionResponseNominationDao;
import com.ustadmobile.core.db.dao.SelQuestionSetResponseDao;
import com.ustadmobile.core.db.dao.SelQuestionSetResponseDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SELEditView;
import com.ustadmobile.core.view.SELQuestionView;
import com.ustadmobile.core.view.SELSelectStudentView;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture;
import com.ustadmobile.lib.db.entities.SelQuestion;
import com.ustadmobile.lib.db.entities.SelQuestionResponse;
import com.ustadmobile.lib.db.entities.SelQuestionResponseNomination;
import com.ustadmobile.lib.db.entities.SelQuestionSetResponse;

import java.util.Hashtable;
import java.util.List;

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
import static com.ustadmobile.core.view.SELSelectStudentView.ARG_DONE_CLAZZMEMBER_UIDS;
import static com.ustadmobile.core.view.SELSelectStudentView.ARG_STUDENT_DONE;


/**
 * The SELEdit's Presenter - responsible for the logic behind editing every SEL Question attempt.
 * This involves setting up the Students as blobs with images on them and implement check for
 * the next SEL Question in a repetition until the end of all applicable SEL questions.
 */
public class SELEditPresenter
        extends CommonHandlerPresenter<SELEditView> {

    //Any arguments stored as variables here
    private long currentClazzUid = 0;
    private long currentPersonUid = 0;
    private long currentQuestionSetUid = 0;
    private long currentClazzMemberUid = 0;
    private int currentQuestionIndexId = 0;
    private long currentQuestionSetResponseUid = 0;
    private long currentQuestionResponseUid = 0;
    private String doneClazzMemberUids = "";

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    //Provider 
    private UmProvider<PersonWithPersonPicture> providerList;

    /**
     * Gets arguments needed to conduct SEL activity and progression and update heading accordingly.
     *
     * @param context       The application context
     * @param arguments     The arguments to the presenter and view
     * @param view          The view
     */
    public SELEditPresenter(Object context, Hashtable arguments, SELEditView view) {
        super(context, arguments, view);

        //Get current class and store it.
        if(arguments.containsKey(ARG_CLAZZ_UID)){
            currentClazzUid = (long) arguments.get(ARG_CLAZZ_UID);
        }
        //Get current person and store it.
        if(arguments.containsKey(ARG_PERSON_UID)){
            currentPersonUid = (long) arguments.get(ARG_PERSON_UID);
        }
        //Get current clazz member  and store it.
        if(arguments.containsKey(ARG_CLAZZMEMBER_UID)){
            currentClazzMemberUid = (long) arguments.get(ARG_CLAZZMEMBER_UID);
        }
        //Get current question set and store it.
        if(arguments.containsKey(ARG_QUESTION_SET_UID)){
            currentQuestionSetUid = (long) arguments.get(ARG_QUESTION_SET_UID);
        }
        //Get current question index and store it.
        if(arguments.containsKey(ARG_QUESTION_INDEX_ID)){
            currentQuestionIndexId = (int) arguments.get(ARG_QUESTION_INDEX_ID);
        }
        //Get current question set response uid and store it.
        if(arguments.containsKey(ARG_QUESTION_SET_RESPONSE_UID)){
            currentQuestionSetResponseUid = (long) arguments.get(ARG_QUESTION_SET_RESPONSE_UID);
        }
        //Get current question response uid and store it.
        if(arguments.containsKey(ARG_QUESTION_RESPONSE_UID)){
            currentQuestionResponseUid = (long) arguments.get(ARG_QUESTION_RESPONSE_UID);
        }
        //Get current question text and update the heading.
        if(arguments.containsKey(ARG_QUESTION_TEXT)){
            view.updateHeading(arguments.get(ARG_QUESTION_TEXT).toString());
        }

        //Check if question index exists. If it does, update the heading accordingly.
        if(arguments.containsKey(ARG_QUESTION_INDEX)){
            if(arguments.containsKey(ARG_QUESTION_TOTAL)){
                view.updateHeading(arguments.get(ARG_QUESTION_INDEX).toString(),
                        arguments.get(ARG_QUESTION_TOTAL).toString());
            }
        }

        //Add on any SEL things done
        if(arguments.containsKey(ARG_DONE_CLAZZMEMBER_UIDS)){
            doneClazzMemberUids = (String) arguments.get(ARG_DONE_CLAZZMEMBER_UIDS);
        }

    }

    /**
     * In Order:
     *      1. Find all clazz members to be part of this clazz to populate the Students.
     *      2. Set the Clazz Member people list provider to the view.
     *
     * @param savedState    The saved state
     */
    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Populate the provider
        providerList = repository.getClazzMemberDao()
                .findAllPeopleWithPersonPictureInClassUid(currentClazzUid, currentClazzMemberUid);

        //set Provider.
        setPeopleProviderToView();

    }

    /**
     * Sets the currently set UMProvider of People type to the View
     */
    private void setPeopleProviderToView(){
        view.setListProvider(providerList);
    }

    /**
     * Handle the primary button after/while editing the SEL task. This means we either
     * end the SEL task or progress it further to the next SEL questions. this method checks for
     * those and also persists the SEL nominations accordingly.
     *
     */
    public void handleClickPrimaryActionButton() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        SelQuestionDao questionDao =
                repository.getSocialNominationQuestionDao();
        SelQuestionSetResponseDao questionSetResponseDao =
                repository.getSocialNominationQuestionSetResponseDao();
        SelQuestionResponseDao questionResponseDao =
                repository.getSocialNominationQuestionResponseDao();


        //Before we go to the next one. We need to end the current one.
        questionSetResponseDao.findByUidAsync(currentQuestionSetResponseUid,
                new UmCallback<SelQuestionSetResponse>() {
            @Override
            public void onSuccess(SelQuestionSetResponse currentQuestionSetResponse) {
                currentQuestionSetResponse.setSelQuestionSetResponseFinishTime(
                        System.currentTimeMillis());

                questionSetResponseDao.updateAsync(currentQuestionSetResponse, new UmCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer questionSetResponseUpdatedResult) {

                        //Find total number of questions as well.
                        int totalSELQuestions =
                                questionDao.findTotalNumberOfActiveQuestionsInAQuestionSet(
                                        currentQuestionSetResponse.getSelQuestionSetResponseSelQuestionSetUid()
                                );


                        questionDao.findNextQuestionByQuestionSetUidAsync(currentQuestionSetUid,
                            currentQuestionIndexId, new UmCallback<SelQuestion>() {
                                @Override
                                public void onSuccess(SelQuestion nextQuestion) {

                                    if(nextQuestion != null) {

                                        SelQuestionSetResponse newResponse = new SelQuestionSetResponse();
                                        newResponse.setSelQuestionSetResponseStartTime(System.currentTimeMillis());
                                        newResponse.setSelQuestionSetResponseSelQuestionSetUid(currentQuestionSetUid);
                                        newResponse.setSelQuestionSetResponseClazzMemberUid(currentClazzMemberUid);

                                        questionSetResponseDao.insertAsync(newResponse, new UmCallback<Long>() {
                                            @Override
                                            public void onSuccess(Long result) {

                                                view.finish();

                                                //Make a question response for the next Question for
                                                // this Response-Set instance.
                                                SelQuestionResponse questionResponse =
                                                        new SelQuestionResponse();
                                                questionResponse
                                                        .setSelQuestionResponseSelQuestionSetResponseUid(
                                                                currentQuestionSetResponseUid);
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
                                                args.put(ARG_QUESTION_SET_UID, currentQuestionSetUid);
                                                args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid);
                                                args.put(ARG_QUESTION_UID, nextQuestion.getSelQuestionUid());
                                                args.put(ARG_QUESTION_SET_RESPONSE_UID, currentQuestionSetResponseUid);
                                                args.put(ARG_QUESTION_INDEX_ID, nextQuestion.getQuestionIndex());
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
                                        //All questions gone through OK.
                                        Hashtable<String, Object> args = new Hashtable<>();
                                        args.put(ARG_STUDENT_DONE, currentPersonUid);
                                        args.put(ARG_CLAZZ_UID, currentClazzUid);
                                        if(doneClazzMemberUids != null){
                                            if(doneClazzMemberUids.equals("")){
                                                doneClazzMemberUids += currentClazzMemberUid ;
                                            }else{
                                                doneClazzMemberUids += "," + currentClazzMemberUid ;
                                            }
                                        }
                                        args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids);
                                        impl.go(SELSelectStudentView.VIEW_NAME, args, getContext());
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
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }


    /**
     * Handles what happens when every Clazz Member Person Blob 's primary button in the SEL
     * activity is pressed. Here we save every Nomination to the database as it happens.
     * We also highlight the selected.
     *
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    @Override
    public void handleCommonPressed(Object arg) {
        //Record nomination and highlight selected.
        ClazzMemberDao clazzMemberDao = repository.getClazzMemberDao();
        SelQuestionResponseNominationDao questionResponseNominationDao =
                repository.getSocialNominationQuestionResponseNominationDao();

        clazzMemberDao.findByPersonUidAndClazzUidAsync((Long) arg, currentClazzUid,
                new UmCallback<ClazzMember>() {
            @Override
            public void onSuccess(ClazzMember result) {

                questionResponseNominationDao.findExistingNomination(result.getClazzMemberUid(),
                    currentQuestionResponseUid, new UmCallback<List<SelQuestionResponseNomination>>() {
                        @Override
                        public void onSuccess(List<SelQuestionResponseNomination> existingNominations) {
                            if(existingNominations != null && !existingNominations.isEmpty()){
                                if(existingNominations.size() == 1){
                                    SelQuestionResponseNomination
                                            thisNomination = existingNominations.get(0);

                                    thisNomination.setNominationActive(!thisNomination.isNominationActive());
                                    questionResponseNominationDao.update(thisNomination);
                                }
                            }else{
                                //Create a new one.
                                SelQuestionResponseNomination responseNomination =
                                        new SelQuestionResponseNomination();
                                responseNomination
                                        .setSelQuestionResponseNominationSelQuestionResponseUId(
                                                currentQuestionResponseUid);
                                responseNomination
                                        .setSelQuestionResponseNominationClazzMemberUid(
                                                result.getClazzMemberUid());
                                responseNomination.setNominationActive(true);

                                questionResponseNominationDao.insert(responseNomination);
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

    }

    /**
     * Handles what happens when every Clazz Member Person Blob 's secondary button in the SEL
     * activity is pressed.
     * Here there is no secondary button or task for every item. Does nothing here.
     *
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    @Override
    public void handleSecondaryPressed(Object arg) {
        //No secondary option here.
    }


}
