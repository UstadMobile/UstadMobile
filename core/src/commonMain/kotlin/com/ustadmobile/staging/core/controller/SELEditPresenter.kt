package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.SELEditView
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
import com.ustadmobile.core.view.SELSelectStudentView
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_DONE_CLAZZMEMBER_UIDS
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_STUDENT_DONE
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture
import com.ustadmobile.lib.db.entities.SelQuestionResponse
import com.ustadmobile.lib.db.entities.SelQuestionResponseNomination
import com.ustadmobile.lib.db.entities.SelQuestionSetResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * The SELEdit's Presenter - responsible for the logic behind editing every SEL Question attempt.
 * This involves setting up the Students as blobs with images on them and implement check for
 * the next SEL Question in a repetition until the end of all applicable SEL questions.
 */
class SELEditPresenter (context: Any, arguments: Map<String, String>?, view: SELEditView,
                        val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        CommonHandlerPresenter<SELEditView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var currentClazzUid: Long = 0
    private var currentPersonUid: Long = 0
    private var currentQuestionSetUid: Long = 0
    private var currentClazzMemberUid: Long = 0
    private var currentQuestionIndexId = 0
    private var currentQuestionSetResponseUid: Long = 0
    private var currentQuestionResponseUid: Long = 0
    private var doneClazzMemberUids: String? = ""

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    //Provider
    private var providerList: DataSource.Factory<Int, PersonWithPersonPicture>? = null

    init {

        //Get current class and store it.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }
        //Get current person and store it.
        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            currentPersonUid = arguments!!.get(ARG_PERSON_UID)!!.toLong()
        }
        //Get current clazz member  and store it.
        if (arguments!!.containsKey(ARG_CLAZZMEMBER_UID)) {
            currentClazzMemberUid = arguments!!.get(ARG_CLAZZMEMBER_UID)!!.toLong()
        }
        //Get current question set and store it.
        if (arguments!!.containsKey(ARG_QUESTION_SET_UID)) {
            currentQuestionSetUid = arguments!!.get(ARG_QUESTION_SET_UID)!!.toLong()
        }
        //Get current question index and store it.
        if (arguments!!.containsKey(ARG_QUESTION_INDEX_ID)) {
            currentQuestionIndexId = arguments!!.get(ARG_QUESTION_INDEX_ID)!!.toInt()
        }
        //Get current question set response uid and store it.
        if (arguments!!.containsKey(ARG_QUESTION_SET_RESPONSE_UID)) {
            currentQuestionSetResponseUid = arguments!!.get(ARG_QUESTION_SET_RESPONSE_UID)!!.toLong()
        }
        //Get current question response uid and store it.
        if (arguments!!.containsKey(ARG_QUESTION_RESPONSE_UID)) {
            currentQuestionResponseUid = arguments!!.get(ARG_QUESTION_RESPONSE_UID)!!.toLong()
        }
        //Get current question text and update the heading.
        if (arguments!!.containsKey(ARG_QUESTION_TEXT)) {
            view.updateHeading(arguments!!.get(ARG_QUESTION_TEXT)!!.toString())
        }

        //Check if question index exists. If it does, update the heading accordingly.
        if (arguments!!.containsKey(ARG_QUESTION_INDEX)) {
            if (arguments!!.containsKey(ARG_QUESTION_TOTAL)) {
                view.updateHeading(arguments!!.get(ARG_QUESTION_INDEX)!!.toString(),
                        arguments!!.get(ARG_QUESTION_TOTAL)!!.toString())
            }
        }

        //Add on any SEL things done
        if (arguments!!.containsKey(ARG_DONE_CLAZZMEMBER_UIDS)) {
            doneClazzMemberUids = arguments!!.get(ARG_DONE_CLAZZMEMBER_UIDS)
        }

    }

    /**
     * In Order:
     * 1. Find all clazz members to be part of this clazz to populate the Students.
     * 2. Set the Clazz Member people list provider to the view.
     *
     * @param savedState    The saved state
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Populate the provider
        providerList = repository.clazzMemberDao
                .findAllPeopleWithPersonPictureInClassUid2(currentClazzUid, currentClazzMemberUid)

        //set Provider.
        setPeopleProviderToView()

    }

    /**
     * Sets the currently set UMProvider of People type to the View
     */
    private fun setPeopleProviderToView() {
        view.setListProvider(providerList!!)
    }

    /**
     * Handle the primary button after/while editing the SEL task. This means we either
     * end the SEL task or progress it further to the next SEL questions. this method checks for
     * those and also persists the SEL nominations accordingly.
     *
     */
    fun handleClickPrimaryActionButton() {
        val questionDao = repository.selQuestionDao
        val questionSetResponseDao = repository.selQuestionSetResponseDao
        val questionResponseDao = repository.selQuestionResponseDao


        //Before we go to the next one. We need to end the current one.
        GlobalScope.launch {
            val currentQuestionSetResponse =
                    questionSetResponseDao.findByUidAsync(currentQuestionSetResponseUid)
            currentQuestionSetResponse!!.selQuestionSetResponseFinishTime =
                    UMCalendarUtil.getDateInMilliPlusDays(0)

            val questionSetResponseUpdatedResult = questionSetResponseDao.updateAsync(currentQuestionSetResponse)

            //Find total number of questions as well.
            val totalSELQuestions = questionDao.findTotalNumberOfActiveQuestionsInAQuestionSet(
                    currentQuestionSetResponse.selQuestionSetResponseSelQuestionSetUid
            )


            val nextQuestion = questionDao.findNextQuestionByQuestionSetUidAsync(currentQuestionSetUid,
                    currentQuestionIndexId)

            if (nextQuestion != null) {

                val newResponse = SelQuestionSetResponse()
                newResponse.selQuestionSetResponseStartTime = UMCalendarUtil.getDateInMilliPlusDays(0)
                newResponse.selQuestionSetResponseSelQuestionSetUid = currentQuestionSetUid
                newResponse.selQuestionSetResponseClazzMemberUid = currentClazzMemberUid

                val result = questionSetResponseDao.insertAsync(newResponse)

                view.finish()

                //Make a question response for the next Question for
                // this Response-Set instance.
                val questionResponse = SelQuestionResponse()
                questionResponse
                        .selQuestionResponseSelQuestionSetResponseUid = currentQuestionSetResponseUid
                questionResponse
                        .selQuestionResponseSelQuestionUid = nextQuestion.selQuestionUid
                questionResponse
                        .selQuestionResponseUid = questionResponseDao.insert(questionResponse)

                //Create arguments
                val args = HashMap<String, String>()
                args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
                args.put(ARG_PERSON_UID, currentPersonUid.toString())
                args.put(ARG_QUESTION_SET_UID, currentQuestionSetUid.toString())
                args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid.toString())
                args.put(ARG_QUESTION_UID, nextQuestion.selQuestionUid.toString())
                args.put(ARG_QUESTION_SET_RESPONSE_UID, currentQuestionSetResponseUid.toString())
                args.put(ARG_QUESTION_INDEX_ID, nextQuestion.questionIndex.toString())
                args.put(ARG_QUESTION_TEXT, nextQuestion.questionText.toString())
                args.put(ARG_QUESTION_INDEX, nextQuestion.questionIndex.toString())
                args.put(ARG_QUESTION_TOTAL, totalSELQuestions.toString())
                args.put(ARG_QUESTION_RESPONSE_UID,
                        questionResponse.selQuestionResponseUid.toString())
                args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids.toString())

                impl.go(SELQuestionView.VIEW_NAME, args, view.viewContext)


            } else {
                //All questions gone through OK.
                val args = HashMap<String, String>()
                args.put(ARG_STUDENT_DONE, currentPersonUid.toString())
                args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
                if (doneClazzMemberUids != null) {
                    if (doneClazzMemberUids == "") {
                        doneClazzMemberUids += currentClazzMemberUid
                    } else {
                        doneClazzMemberUids += ",$currentClazzMemberUid"
                    }
                }
                args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids.toString())
                impl.go(SELSelectStudentView.VIEW_NAME, args, context)
                view.finish()
            }

        }

    }


    /**
     * Handles what happens when every Clazz Member Person Blob 's primary button in the SEL
     * activity is pressed. Here we save every Nomination to the database as it happens.
     * We also highlight the selected.
     *
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    override fun handleCommonPressed(arg: Any) {
        //Record nomination and highlight selected.
        val clazzMemberDao = repository.clazzMemberDao
        val questionResponseNominationDao = repository.selQuestionResponseNominationDao

        GlobalScope.launch {
            val result = clazzMemberDao.findByPersonUidAndClazzUidAsync(arg as Long,
                    currentClazzUid)

            val existingNominations = questionResponseNominationDao.findExistingNomination(
                    result!!.clazzMemberUid,currentQuestionResponseUid)
            if (existingNominations != null && !existingNominations.isEmpty()) {
                if (existingNominations.size == 1) {
                    val thisNomination = existingNominations[0]

                    thisNomination.nominationActive = !thisNomination.nominationActive
                    questionResponseNominationDao.update(thisNomination)
                }
            } else {
                //Create a new one.
                val responseNomination = SelQuestionResponseNomination()
                responseNomination
                        .selqrnSelQuestionResponseUId = currentQuestionResponseUid
                responseNomination
                        .selqrnClazzMemberUid = result.clazzMemberUid
                responseNomination.nominationActive = true

                questionResponseNominationDao.insert(responseNomination)
            }

        }

    }

    /**
     * Handles what happens when every Clazz Member Person Blob 's secondary button in the SEL
     * activity is pressed.
     * Here there is no secondary button or task for every item. Does nothing here.
     *
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    override fun handleSecondaryPressed(arg: Any) {
        //No secondary option here.
    }


}
