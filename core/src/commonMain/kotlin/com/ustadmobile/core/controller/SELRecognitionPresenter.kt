package com.ustadmobile.core.controller



import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.db.dao.SelQuestionResponseDao
import com.ustadmobile.core.db.dao.SelQuestionSetDao
import com.ustadmobile.core.db.dao.SelQuestionSetResponseDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.SELQuestionView
import com.ustadmobile.core.view.SELRecognitionView
import com.ustadmobile.core.impl.UstadMobileSystemImpl

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.lib.db.entities.SelQuestionResponse
import com.ustadmobile.lib.db.entities.SelQuestionSet
import com.ustadmobile.lib.db.entities.SelQuestionSetResponse

import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_CLAZZMEMBER_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_INDEX_ID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_RESPONSE_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_SET_RESPONSE_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_SET_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_UID
import com.ustadmobile.core.view.SELQuestionView.Companion.ARG_QUESTION_INDEX
import com.ustadmobile.core.view.SELQuestionView.Companion.ARG_QUESTION_TEXT
import com.ustadmobile.core.view.SELQuestionView.Companion.ARG_QUESTION_TOTAL
import com.ustadmobile.core.view.SELRecognitionView.Companion.ARG_RECOGNITION_UID
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_DONE_CLAZZMEMBER_UIDS
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_SELECTED_QUESTION_SET_UID


/**
 * The SELRecognition Presenter - responsible for loading students with toggle names and allowing
 * for the SEL task to continue only when recognition check is enabled.
 */
class SELRecognitionPresenter(context: Any, arguments: Map<String, String>?, view:
SELRecognitionView, val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : CommonHandlerPresenter<SELRecognitionView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var currentClazzUid: Long = -1
    private var currentPersonUid: Long = -1
    private var currentClazzMemberUid: Long = -1
    private var currentRecognitionQuestionNominationResponse: Long = -1
    private var doneClazzMemberUids = ""
    private var currentQuestionSetUid: Long = 0

    //Provider
    private var providerList: UmProvider<PersonWithPersonPicture>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        //Get class uid arguments and set them.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)
        }
        //Person uid argument gotten and set to Presenter
        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            currentPersonUid = arguments!!.get(ARG_PERSON_UID)
        }
        //Clazz Member doing the SEL task.
        if (arguments!!.containsKey(ARG_CLAZZMEMBER_UID)) {
            currentClazzMemberUid = arguments!!.get(ARG_CLAZZMEMBER_UID)
        }
        //Recognition Uid.
        if (arguments!!.containsKey(ARG_RECOGNITION_UID)) {
            currentRecognitionQuestionNominationResponse = arguments!!.get(ARG_RECOGNITION_UID)
        }

        //Add on any SEL things done
        if (arguments!!.containsKey(ARG_DONE_CLAZZMEMBER_UIDS)) {
            doneClazzMemberUids = arguments!!.get(ARG_DONE_CLAZZMEMBER_UIDS)
        }

        if (arguments!!.containsKey(ARG_SELECTED_QUESTION_SET_UID)) {
            currentQuestionSetUid = arguments!!.get(ARG_SELECTED_QUESTION_SET_UID)
        }

    }

    /**
     * In Order:
     * 1. Gets all Clazz Member as UmProvider from the database of type Person and sets it
     * to the view.
     *
     * @param savedState    The saved state
     */
    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Populate the provider
        providerList = repository.clazzMemberDao
                .findAllPeopleWithPersonPictureInClassUid(currentClazzUid)
        setListProviderToView()

    }

    /**
     * Sets the Clazz Member people provider set in the Presenter to the View.
     */
    private fun setListProviderToView() {
        view.setListProvider(providerList!!)
    }

    /**
     * Goes to next question after recognition was checked ok.
     * This method goes through all questions set for SEL in order and loads the SELQuestionDetail
     * screen first, and so on.
     *
     */
    private fun goToNextQuestion() {

        val selQuestionSetResponseDao = repository.getSocialNominationQuestionSetResponseDao()
        val questionSetDao = repository
                .getSocialNominationQuestionSetDao()
        val questionDao = repository
                .getSocialNominationQuestionDao()

        //Loop through questions.
        questionSetDao.findByUidAsync(currentQuestionSetUid, object : UmCallback<SelQuestionSet> {
            override fun onSuccess(questionSet: SelQuestionSet?) {
                //Find total number of questions as well.
                val totalSELQuestions = questionDao.findTotalNumberOfActiveQuestionsInAQuestionSet(questionSet!!.selQuestionSetUid)

                questionDao.findNextQuestionByQuestionSetUidAsync(questionSet.selQuestionSetUid,
                        0, object : UmCallback<SelQuestion> {
                    override fun onSuccess(nextQuestion: SelQuestion?) {
                        if (nextQuestion != null) {

                            val newResponse = SelQuestionSetResponse()
                            newResponse.selQuestionSetResponseStartTime = System.currentTimeMillis()
                            newResponse.selQuestionSetResponseSelQuestionSetUid = questionSet.selQuestionSetUid
                            newResponse.selQuestionSetResponseClazzMemberUid = currentClazzMemberUid

                            selQuestionSetResponseDao.insertAsync(newResponse, object : UmCallback<Long> {
                                override fun onSuccess(questionSetResponseUid: Long?) {

                                    view.finish()

                                    //Make a question response for the next Question for
                                    // this Response-Set instance.
                                    val questionResponseDao = repository.getSocialNominationQuestionResponseDao()
                                    val questionResponse = SelQuestionResponse()
                                    questionResponse
                                            .selQuestionResponseSelQuestionSetResponseUid = questionSetResponseUid
                                    //newResponse.getSocialNominationQuestionSetResposeUid());
                                    questionResponse
                                            .selQuestionResponseSelQuestionUid = nextQuestion.selQuestionUid
                                    questionResponse
                                            .selQuestionResponseUid = questionResponseDao.insert(questionResponse)

                                    //Create arguments
                                    val args = HashMap<String, String>()
                                    args.put(ARG_CLAZZ_UID, currentClazzUid)
                                    args.put(ARG_PERSON_UID, currentPersonUid)
                                    args.put(ARG_QUESTION_SET_UID, questionSet.selQuestionSetUid)
                                    args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid)
                                    args.put(ARG_QUESTION_UID, nextQuestion.selQuestionUid)
                                    args.put(ARG_QUESTION_INDEX_ID, nextQuestion.questionIndex)
                                    args.put(ARG_QUESTION_SET_RESPONSE_UID, questionSetResponseUid)
                                    args.put(ARG_QUESTION_TEXT, nextQuestion.questionText)
                                    args.put(ARG_QUESTION_INDEX, nextQuestion.questionIndex)
                                    args.put(ARG_QUESTION_TOTAL, totalSELQuestions)
                                    args.put(ARG_QUESTION_RESPONSE_UID,
                                            questionResponse.selQuestionResponseUid)

                                    args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids)

                                    impl.go(SELQuestionView.VIEW_NAME, args, view.getContext())

                                }

                                override fun onFailure(exception: Throwable?) {
                                    print(exception!!.message)
                                }
                            })

                        } else {
                            //End the SEL activities properly.
                            view.finish()
                        }
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })


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
    fun handleClickPrimaryActionButton(recognitionDone: Boolean) {
        val questionResponseNominationDao = repository.getSocialNominationQuestionSetResponseDao()

        if (recognitionDone) {

            questionResponseNominationDao.findByUidAsync(currentRecognitionQuestionNominationResponse,
                    object : UmCallback<SelQuestionSetResponse> {
                        override fun onSuccess(responseNomination: SelQuestionSetResponse?) {

                            responseNomination!!.selQuestionSetResponseFinishTime = System.currentTimeMillis()
                            questionResponseNominationDao.updateAsync(responseNomination,
                                    object : UmCallback<Int> {
                                        override fun onSuccess(result: Int?) {
                                            goToNextQuestion()
                                        }

                                        override fun onFailure(exception: Throwable?) {
                                            print(exception!!.message)
                                        }
                                    })
                        }

                        override fun onFailure(exception: Throwable?) {
                            print(exception!!.message)
                        }
                    })

        } else {
            // UI : Maybe a toast ?
            view.showMessage(impl.getString(MessageID.recognition_not_selected, context))

        }

    }


    /**
     * Handler for toggling student name. Does nothing on the presenter side.
     *
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    override fun handleCommonPressed(arg: Any) {
        println("Toggle student name for SEL to see if the student got it correct.")
        //Doesn't do more than this. If you want it to do something, you would put it over here.
    }

    /**
     * Handler for secondary press on every student. Does not exist. Does nothing.
     *
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    override fun handleSecondaryPressed(arg: Any) {
        //No secondary option here.
    }
}
