package com.ustadmobile.core.controller



import com.ustadmobile.core.view.SELEditView
import com.ustadmobile.core.view.SELQuestionView
import com.ustadmobile.core.impl.UstadMobileSystemImpl

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
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_DONE_CLAZZMEMBER_UIDS


/**
 * The SELQuestion Presenter - responsible for the logic of loading and displaying the current
 * Question of the SEL task run. This should navigate to the SELEdit Screen with this question upon
 * click Next and pass along every argument needed to run the SEL run for that question and overall
 * SEL run.
 *
 */
class SELQuestionPresenter(context: Any, private val gottenArguments: Map<String, String>?, view:
SELQuestionView, val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) : UstadBaseController<SELQuestionView>(context, gottenArguments!!, view) {

    //Any arguments stored as variables here
    private var doneClazzMemberUids = ""
    private var currentClazzUid: Long = 0
    private var currentPersonUid: Long = 0
    private var currentQuestionSetUid: Long = 0
    private var currentClazzMemberUid: Long = 0
    private var currentQuestionUid: Long = 0
    private var currentQuestionIndexId = 0
    private var currentQuestionSetResponseUid: Long = 0
    private var currentQuestionResponseUid: Long = 0
    private var questionText = ""


    init {

        //Get class uid and set it to the Presenter
        if (gottenarguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = gottenarguments!!.get(ARG_CLAZZ_UID)
        }
        //Get person uid and set it to the Presenter
        if (gottenarguments!!.containsKey(ARG_PERSON_UID)) {
            currentPersonUid = gottenarguments!!.get(ARG_PERSON_UID)
        }
        //Get question set uid and set it to the Presenter
        if (gottenarguments!!.containsKey(ARG_QUESTION_SET_UID)) {
            currentQuestionSetUid = gottenarguments!!.get(ARG_QUESTION_SET_UID)
        }
        //Get clazz member uid and set it to the Presenter
        if (gottenarguments!!.containsKey(ARG_CLAZZMEMBER_UID)) {
            currentClazzMemberUid = gottenarguments!!.get(ARG_CLAZZMEMBER_UID)
        }
        //Get question uid and set it to the Presenter
        if (gottenarguments!!.containsKey(ARG_QUESTION_UID)) {
            currentQuestionUid = gottenarguments!!.get(ARG_QUESTION_UID)
        }
        //Get question index set it to the Presenter
        if (gottenarguments!!.containsKey(ARG_QUESTION_INDEX_ID)) {
            currentQuestionIndexId = gottenarguments!!.get(ARG_QUESTION_INDEX_ID)
        }
        //Get question set response uid and set it to the Presenter
        if (gottenarguments!!.containsKey(ARG_QUESTION_SET_RESPONSE_UID)) {
            currentQuestionSetResponseUid = gottenarguments!!.get(ARG_QUESTION_SET_RESPONSE_UID)
        }
        //Get question uid and set it to the Presenter
        if (gottenarguments!!.containsKey(ARG_QUESTION_RESPONSE_UID)) {
            currentQuestionResponseUid = gottenarguments!!.get(ARG_QUESTION_RESPONSE_UID)
        }
        //Get the question text and set it to the View
        if (gottenarguments!!.containsKey(ARG_QUESTION_TEXT)) {
            questionText = gottenarguments!!.get(ARG_QUESTION_TEXT)!!.toString()
            view.updateQuestion(questionText)
        }

        //Get question index and total and set it to the View.
        if (gottenarguments!!.containsKey(ARG_QUESTION_INDEX)) {
            if (gottenarguments!!.containsKey(ARG_QUESTION_TOTAL)) {
                view.updateQuestionNumber(gottenarguments!!.get(ARG_QUESTION_INDEX)!!.toString(),
                        gottenarguments!!.get(ARG_QUESTION_TOTAL)!!.toString())
            }

        }

        //Add on any SEL things done
        if (gottenarguments!!.containsKey(ARG_DONE_CLAZZMEMBER_UIDS)) {
            doneClazzMemberUids = gottenarguments!!.get(ARG_DONE_CLAZZMEMBER_UIDS)
        }

    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    /**
     * The Next button handler goes to the SELEdit View passing along every info needed for the
     * current SEL run as well as the question seen in this presenter.
     *
     */
    fun handleClickPrimaryActionButton() {

        //Create arguments  - OR- just sent arguments ?
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzUid)
        args.put(ARG_PERSON_UID, currentPersonUid)
        args.put(ARG_QUESTION_SET_UID, currentQuestionSetUid)
        args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid)
        args.put(ARG_QUESTION_UID, currentQuestionUid)
        args.put(ARG_QUESTION_INDEX_ID, currentQuestionIndexId)
        args.put(ARG_QUESTION_SET_RESPONSE_UID, currentQuestionSetResponseUid)
        args.put(ARG_QUESTION_RESPONSE_UID, currentQuestionResponseUid)
        args.put(ARG_QUESTION_TEXT, questionText)
        args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids)

        view.finish()

        //Go to view
        impl.go(SELEditView.VIEW_NAME, args, view.getContext())

    }


}
