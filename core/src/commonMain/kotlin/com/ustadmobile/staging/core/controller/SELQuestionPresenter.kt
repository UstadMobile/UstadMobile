package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
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
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_DONE_CLAZZMEMBER_UIDS


/**
 * The SELQuestion Presenter - responsible for the logic of loading and displaying the current
 * Question of the SEL task run. This should navigate to the SELEdit Screen with this question upon
 * click Next and pass along every argument needed to run the SEL run for that question and overall
 * SEL run.
 *
 */
class SELQuestionPresenter(context: Any, arguments: Map<String, String>?,
                           view: SELQuestionView,
                           val impl : UstadMobileSystemImpl =
                                   UstadMobileSystemImpl.instance)
    : UstadBaseController<SELQuestionView>(context, arguments!!, view) {

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
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }
        //Get person uid and set it to the Presenter
        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            currentPersonUid = arguments!!.get(ARG_PERSON_UID)!!.toLong()
        }
        //Get question set uid and set it to the Presenter
        if (arguments!!.containsKey(ARG_QUESTION_SET_UID)) {
            currentQuestionSetUid = arguments!!.get(ARG_QUESTION_SET_UID)!!.toLong()
        }
        //Get clazz member uid and set it to the Presenter
        if (arguments!!.containsKey(ARG_CLAZZMEMBER_UID)) {
            currentClazzMemberUid = arguments!!.get(ARG_CLAZZMEMBER_UID)!!.toLong()
        }
        //Get question uid and set it to the Presenter
        if (arguments!!.containsKey(ARG_QUESTION_UID)) {
            currentQuestionUid = arguments!!.get(ARG_QUESTION_UID)!!.toLong()
        }
        //Get question index set it to the Presenter
        if (arguments!!.containsKey(ARG_QUESTION_INDEX_ID)) {
            currentQuestionIndexId = arguments!!.get(ARG_QUESTION_INDEX_ID)!!.toInt()
        }
        //Get question set response uid and set it to the Presenter
        if (arguments!!.containsKey(ARG_QUESTION_SET_RESPONSE_UID)) {
            currentQuestionSetResponseUid = arguments!!.get(ARG_QUESTION_SET_RESPONSE_UID)!!.toLong()
        }
        //Get question uid and set it to the Presenter
        if (arguments!!.containsKey(ARG_QUESTION_RESPONSE_UID)) {
            currentQuestionResponseUid = arguments!!.get(ARG_QUESTION_RESPONSE_UID)!!.toLong()
        }
        //Get the question text and set it to the View
        if (arguments!!.containsKey(ARG_QUESTION_TEXT)) {
            questionText = arguments!!.get(ARG_QUESTION_TEXT)!!.toString()
            view.updateQuestion(questionText)
        }

        //Get question index and total and set it to the View.
        if (arguments!!.containsKey(ARG_QUESTION_INDEX)) {
            if (arguments!!.containsKey(ARG_QUESTION_TOTAL)) {
                view.updateQuestionNumber(arguments!!.get(ARG_QUESTION_INDEX)!!.toString(),
                        arguments!!.get(ARG_QUESTION_TOTAL)!!.toString())
            }

        }

        //Add on any SEL things done
        if (arguments!!.containsKey(ARG_DONE_CLAZZMEMBER_UIDS)) {
            doneClazzMemberUids = arguments!!.get(ARG_DONE_CLAZZMEMBER_UIDS)!!.toString()
        }

    }

    override fun onCreate(savedState: Map<String, String>?) {
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
        args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
        args.put(ARG_PERSON_UID, currentPersonUid.toString())
        args.put(ARG_QUESTION_SET_UID, currentQuestionSetUid.toString())
        args.put(ARG_CLAZZMEMBER_UID, currentClazzMemberUid.toString())
        args.put(ARG_QUESTION_UID, currentQuestionUid.toString())
        args.put(ARG_QUESTION_INDEX_ID, currentQuestionIndexId.toString())
        args.put(ARG_QUESTION_SET_RESPONSE_UID, currentQuestionSetResponseUid.toString())
        args.put(ARG_QUESTION_RESPONSE_UID, currentQuestionResponseUid.toString())
        args.put(ARG_QUESTION_TEXT, questionText)
        args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids)

        view.finish()

        //Go to view
        impl.go(SELEditView.VIEW_NAME, args, view.viewContext)

    }


}
