package com.ustadmobile.core.controller



import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.staging.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_CLAZZMEMBER_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_INDEX_ID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_SET_RESPONSE_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_SET_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_UID
import com.ustadmobile.core.view.SELQuestionView
import com.ustadmobile.core.view.SELQuestionView.Companion.ARG_QUESTION_INDEX
import com.ustadmobile.core.view.SELQuestionView.Companion.ARG_QUESTION_TEXT
import com.ustadmobile.core.view.SELQuestionView.Companion.ARG_QUESTION_TOTAL
import com.ustadmobile.core.view.SELRecognitionView
import com.ustadmobile.core.view.SELRecognitionView.Companion.ARG_RECOGNITION_UID
import com.ustadmobile.core.view.SELSelectConsentView
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_DONE_CLAZZMEMBER_UIDS
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_SELECTED_QUESTION_SET_UID
import com.ustadmobile.lib.db.entities.SelQuestionSetResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }
        //Get person uid
        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            currentPersonUid = arguments!!.get(ARG_PERSON_UID)!!.toLong()
        }
        //Get clazz member doing the sel
        if (arguments!!.containsKey(ARG_CLAZZMEMBER_UID)) {
            currentClazzMemberUid = arguments!!.get(ARG_CLAZZMEMBER_UID)!!.toLong()
        }
        if (arguments!!.containsKey(ARG_DONE_CLAZZMEMBER_UIDS)) {
            doneClazzMemberUids = arguments!!.get(ARG_DONE_CLAZZMEMBER_UIDS)!!.toString()
        }
        if (arguments!!.containsKey(ARG_SELECTED_QUESTION_SET_UID)) {
            currentQuestionSetUid = arguments!!.get(ARG_SELECTED_QUESTION_SET_UID)!!.toLong()
        }

    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    /**
     * Handles click "START SELECTION". Checks for consent and Gets to display the first question
     * after creating a new SEL run on the database and response.
     *
     * @param consentGiven true if consent given. False if not.
     */
    fun handleClickPrimaryActionButton(consentGiven: Boolean) {
        val selQuestionSetResponseDao = repository.selQuestionSetResponseDao

        //Check selectedObject for consent given.
        if (consentGiven) {
            GlobalScope.launch {
                val listPassed = selQuestionSetResponseDao.findAllPassedRecognitionByPersonUid(
                        currentClazzMemberUid)

                if (listPassed.size > MIN_RECOGNITION_SUCCESSES) {
                    //Go straight to the Questions
                    goToNextQuestion()

                } else {
                    //Go re-do/do the recognition activity.
                    val newResponse = SelQuestionSetResponse()
                    newResponse.selQuestionSetResponseStartTime = UMCalendarUtil
                            .getDateInMilliPlusDays(0)
                    newResponse.selQuestionSetResponseClazzMemberUid = currentClazzMemberUid
                    newResponse.selQuestionSetResposeUid = selQuestionSetResponseDao.insert(newResponse)

                    val args = HashMap<String, String>()
                    args.put(ARG_RECOGNITION_UID, newResponse.selQuestionSetResposeUid.toString())
                    args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
                    args.put(ARG_PERSON_UID, currentPersonUid.toString())
                    args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid.toString())
                    args.put(ARG_SELECTED_QUESTION_SET_UID, currentQuestionSetUid.toString())
                    doneClazzMemberUids += ",$currentClazzMemberUid"
                    args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids)

                    view.finish()

                    impl.go(SELRecognitionView.VIEW_NAME, args, view.viewContext)

                }
            }
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

        val selQuestionSetResponseDao = repository.selQuestionSetResponseDao
        val questionSetDao = repository.selQuestionSetDao
        val questionDao = repository.selQuestionDao

        GlobalScope.launch {
            //Loop through questions.
            val questionSets = questionSetDao.findAllQuestionsAsync()

            //Update: Sprint 5: Question Set will be selectable at the
            // SELSelectStudentView screen.
            //TODOne: Change this when we add more Question Sets to
            // findNextQuestionSet like we did for findNextQuestion
            for (questionSet in questionSets!!) {

                //Find total number of questions as well.
                val totalSELQuestions = questionDao.findTotalNumberOfActiveQuestionsInAQuestionSet(
                        questionSet.selQuestionSetUid)

                val nextQuestion = questionDao.findNextQuestionByQuestionSetUidAsync(
                        questionSet.selQuestionSetUid,BASE_INDEX_SEL_QUESTION)
                if (nextQuestion != null) {

                    val newResponse = SelQuestionSetResponse()
                    newResponse.selQuestionSetResponseStartTime = UMCalendarUtil
                            .getDateInMilliPlusDays(0)
                    newResponse.selQuestionSetResponseSelQuestionSetUid = questionSet.selQuestionSetUid
                    newResponse.selQuestionSetResponseClazzMemberUid = currentClazzMemberUid

                    val questionSetResponseUid = selQuestionSetResponseDao.insertAsync(newResponse)
                    view.finish()

                    //Create arguments
                    val args = HashMap<String, String>()
                    args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
                    args.put(ARG_PERSON_UID, currentPersonUid.toString())
                    args.put(ARG_QUESTION_SET_UID, questionSet.selQuestionSetUid.toString())
                    args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid.toString())
                    args.put(ARG_QUESTION_UID, nextQuestion.selQuestionUid.toString())
                    args.put(ARG_QUESTION_INDEX_ID, nextQuestion.questionIndex.toString())
                    args.put(ARG_QUESTION_SET_RESPONSE_UID, questionSetResponseUid.toString())
                    args.put(ARG_QUESTION_TEXT, nextQuestion.questionText.toString())
                    args.put(ARG_QUESTION_INDEX, nextQuestion.questionIndex.toString())
                    args.put(ARG_QUESTION_TOTAL, totalSELQuestions.toString())

                    impl.go(SELQuestionView.VIEW_NAME, args, view.viewContext)


                } else {
                    //End the SEL activities properly.
                    view.finish()
                }
            }
        }
    }

    companion object {
        val BASE_INDEX_SEL_QUESTION = 0
    }


}
