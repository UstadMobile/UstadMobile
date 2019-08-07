package com.ustadmobile.core.controller



import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.db.dao.SelQuestionSetDao
import com.ustadmobile.core.db.dao.SelQuestionSetResponseDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.SELQuestionView
import com.ustadmobile.core.view.SELRecognitionView
import com.ustadmobile.core.view.SELSelectConsentView
import com.ustadmobile.core.impl.UstadMobileSystemImpl

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.lib.db.entities.SelQuestionSet
import com.ustadmobile.lib.db.entities.SelQuestionSetResponse

import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_CLAZZMEMBER_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_INDEX_ID
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
 * The SELSelectConsent Presenter - responsible for the logic in displaying seeking consent from
 * the student/sel officer on behalf of the student - a reminder that we are taking this information.
 *
 */
class SELSelectConsentPresenter(context: Any, arguments: Map<String, String>?, view:
SELSelectConsentView, val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<SELSelectConsentView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var currentClazzUid: Long = 0
    private var currentPersonUid: Long = 0
    private var currentClazzMemberUid: Long = 0
    private val MIN_RECOGNITION_SUCCESSES = 0
    private var doneClazzMemberUids = ""
    private var currentQuestionSetUid: Long = 0

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        //Get clazz uid and set them.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)
        }
        //Get person uid
        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            currentPersonUid = arguments!!.get(ARG_PERSON_UID)
        }
        //Get clazz member doing the sel
        if (arguments!!.containsKey(ARG_CLAZZMEMBER_UID)) {
            currentClazzMemberUid = arguments!!.get(ARG_CLAZZMEMBER_UID)
        }
        if (arguments!!.containsKey(ARG_DONE_CLAZZMEMBER_UIDS)) {
            doneClazzMemberUids = arguments!!.get(ARG_DONE_CLAZZMEMBER_UIDS)
        }
        if (arguments!!.containsKey(ARG_SELECTED_QUESTION_SET_UID)) {
            currentQuestionSetUid = arguments!!.get(ARG_SELECTED_QUESTION_SET_UID)
        }

    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    /**
     * Handles click "START SELECTION". Checks for consent and Gets to display the first question
     * after creating a new SEL run on the database and response.
     *
     * @param consentGiven true if consent given. False if not.
     */
    fun handleClickPrimaryActionButton(consentGiven: Boolean) {
        val selQuestionSetResponseDao = repository.getSocialNominationQuestionSetResponseDao()

        //Check selectedObject for consent given.
        if (consentGiven) {

            selQuestionSetResponseDao.findAllPassedRecognitionByPersonUid(
                    currentClazzMemberUid,
                    object : UmCallback<List<SelQuestionSetResponse>> {
                        override fun onSuccess(listPassed: List<SelQuestionSetResponse>?) {

                            if (listPassed!!.size > MIN_RECOGNITION_SUCCESSES) {
                                //Go straight to the Questions
                                goToNextQuestion()

                            } else {
                                //Go re-do/do the recognition activity.
                                val newResponse = SelQuestionSetResponse()
                                newResponse.selQuestionSetResponseStartTime = System.currentTimeMillis()
                                newResponse.selQuestionSetResponseClazzMemberUid = currentClazzMemberUid
                                newResponse.selQuestionSetResposeUid = selQuestionSetResponseDao.insert(newResponse)

                                val args = HashMap<String, String>()
                                args.put(ARG_RECOGNITION_UID, newResponse.selQuestionSetResposeUid)
                                args.put(ARG_CLAZZ_UID, currentClazzUid)
                                args.put(ARG_PERSON_UID, currentPersonUid)
                                args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid)
                                args.put(ARG_SELECTED_QUESTION_SET_UID, currentQuestionSetUid)
                                doneClazzMemberUids += ",$currentClazzMemberUid"
                                args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids)

                                view.finish()

                                impl.go(SELRecognitionView.VIEW_NAME, args, view.getContext())

                            }
                        }

                        override fun onFailure(exception: Throwable?) {
                            print(exception!!.message)
                        }
                    })
        } else {
            //TODOne: Handle and think about what happens if the consent is NOT given.
            //UI: Maybe some toast?
            view.toastMessage(impl.getString(MessageID.consent_not_selected, context))
        }
    }

    /**
     * Method that checks where the current SEL task is in and goes to the next question.
     *
     */
    private fun goToNextQuestion() {

        val selQuestionSetResponseDao = repository.getSocialNominationQuestionSetResponseDao()
        val questionSetDao = repository
                .getSocialNominationQuestionSetDao()
        val questionDao = repository
                .getSocialNominationQuestionDao()

        //Loop through questions.
        questionSetDao.findAllQuestionsAsync(object : UmCallback<List<SelQuestionSet>> {
            override fun onSuccess(questionSets: List<SelQuestionSet>?) {

                //Update: Sprint 5: Question Set will be selectable at the
                // SELSelectStudentView screen.
                //TODOne: Change this when we add more Question Sets to
                // findNextQuestionSet like we did for findNextQuestion
                for (questionSet in questionSets!!) {

                    //Find total number of questions as well.
                    val totalSELQuestions = questionDao.findTotalNumberOfActiveQuestionsInAQuestionSet(
                            questionSet.selQuestionSetUid
                    )

                    questionDao.findNextQuestionByQuestionSetUidAsync(questionSet.selQuestionSetUid,
                            BASE_INDEX_SEL_QUESTION, object : UmCallback<SelQuestion> {
                        override fun onSuccess(nextQuestion: SelQuestion?) {
                            if (nextQuestion != null) {

                                val newResponse = SelQuestionSetResponse()
                                newResponse.selQuestionSetResponseStartTime = System.currentTimeMillis()
                                newResponse.selQuestionSetResponseSelQuestionSetUid = questionSet.selQuestionSetUid
                                newResponse.selQuestionSetResponseClazzMemberUid = currentClazzMemberUid

                                selQuestionSetResponseDao.insertAsync(newResponse, object : UmCallback<Long> {
                                    override fun onSuccess(questionSetResponseUid: Long?) {

                                        view.finish()

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

            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

    companion object {
        val BASE_INDEX_SEL_QUESTION = 0
    }


}
