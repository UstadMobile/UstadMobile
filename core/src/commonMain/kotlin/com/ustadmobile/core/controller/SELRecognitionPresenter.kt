package com.ustadmobile.core.controller



import androidx.paging.DataSource
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_CLAZZMEMBER_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_INDEX_ID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_RESPONSE_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_SET_RESPONSE_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_SET_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_QUESTION_UID
import com.ustadmobile.core.view.SELQuestionView
import com.ustadmobile.core.view.SELQuestionView.Companion.ARG_QUESTION_INDEX
import com.ustadmobile.core.view.SELQuestionView.Companion.ARG_QUESTION_TEXT
import com.ustadmobile.core.view.SELQuestionView.Companion.ARG_QUESTION_TOTAL
import com.ustadmobile.core.view.SELRecognitionView
import com.ustadmobile.core.view.SELRecognitionView.Companion.ARG_RECOGNITION_UID
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_DONE_CLAZZMEMBER_UIDS
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_SELECTED_QUESTION_SET_UID
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture
import com.ustadmobile.lib.db.entities.SelQuestionResponse
import com.ustadmobile.lib.db.entities.SelQuestionSetResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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
    private var providerList: DataSource.Factory<Int, PersonWithPersonPicture>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        //Get class uid arguments and set them.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }
        //Person uid argument gotten and set to Presenter
        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            currentPersonUid = arguments!!.get(ARG_PERSON_UID)!!.toLong()
        }
        //Clazz Member doing the SEL task.
        if (arguments!!.containsKey(ARG_CLAZZMEMBER_UID)) {
            currentClazzMemberUid = arguments!!.get(ARG_CLAZZMEMBER_UID)!!.toLong()
        }
        //Recognition Uid.
        if (arguments!!.containsKey(ARG_RECOGNITION_UID)) {
            currentRecognitionQuestionNominationResponse = arguments!!.get(ARG_RECOGNITION_UID)!!.toLong()
        }

        //Add on any SEL things done
        if (arguments!!.containsKey(ARG_DONE_CLAZZMEMBER_UIDS)) {
            doneClazzMemberUids = arguments!!.get(ARG_DONE_CLAZZMEMBER_UIDS)!!.toString()
        }

        if (arguments!!.containsKey(ARG_SELECTED_QUESTION_SET_UID)) {
            currentQuestionSetUid = arguments!!.get(ARG_SELECTED_QUESTION_SET_UID)!!.toLong()
        }

    }

    /**
     * In Order:
     * 1. Gets all Clazz Member as UmProvider from the database of type Person and sets it
     * to the view.
     *
     * @param savedState    The saved state
     */
    override fun onCreate(savedState: Map<String, String?>?) {
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

        val selQuestionSetResponseDao = repository.selQuestionSetResponseDao
        val questionSetDao = repository.selQuestionSetDao
        val questionDao = repository.selQuestionDao

        //Loop through questions.
        GlobalScope.launch {
            val questionSet = questionSetDao.findByUidAsync(currentQuestionSetUid)
            //Find total number of questions as well.
            val totalSELQuestions =
                    questionDao.findTotalNumberOfActiveQuestionsInAQuestionSet(
                            questionSet!!.selQuestionSetUid)

            val nextQuestion = questionDao.findNextQuestionByQuestionSetUidAsync(
                    questionSet.selQuestionSetUid,0)

            if (nextQuestion != null) {

                val newResponse = SelQuestionSetResponse()
                newResponse.selQuestionSetResponseStartTime =
                        UMCalendarUtil.getDateInMilliPlusDays(0)
                newResponse.selQuestionSetResponseSelQuestionSetUid = questionSet.selQuestionSetUid
                newResponse.selQuestionSetResponseClazzMemberUid = currentClazzMemberUid

                val questionSetResponseUid = selQuestionSetResponseDao.insertAsync(newResponse)
                view.finish()

                //Make a question response for the next Question for
                // this Response-Set instance.
                val questionResponseDao = repository.selQuestionResponseDao
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
                args.put(ARG_QUESTION_RESPONSE_UID,
                        questionResponse.selQuestionResponseUid.toString())
                args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids)

                impl.go(SELQuestionView.VIEW_NAME, args, view.viewContext)

            } else {
                //End the SEL activities properly.
                view.finish()
            }
        }
    }

    /**
     * Primary action button handler - To go next to the first SEL question - Checks if recognition
     * is pressed ok - then calls goToNextQuestion() - that checks the next question, etc.
     *
     * @param recognitionDone   true if recognition check box ticked.
     */
    fun handleClickPrimaryActionButton(recognitionDone: Boolean) {
        val questionResponseNominationDao = repository.selQuestionSetResponseDao

        if (recognitionDone) {

            GlobalScope.launch {
                val responseNomination =
                        questionResponseNominationDao.findByUidAsync(
                                currentRecognitionQuestionNominationResponse)
                responseNomination!!.selQuestionSetResponseFinishTime =
                        UMCalendarUtil.getDateInMilliPlusDays(0)
                questionResponseNominationDao.updateAsync(responseNomination)
                goToNextQuestion()
            }
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
    override fun handleCommonPressed(arg: Any, arg2:Any) {
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
