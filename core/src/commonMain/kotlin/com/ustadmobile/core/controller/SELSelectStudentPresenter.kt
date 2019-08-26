package com.ustadmobile.core.controller


import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.SELEditView.Companion.ARG_CLAZZMEMBER_UID
import com.ustadmobile.core.view.SELSelectConsentView
import com.ustadmobile.core.view.SELSelectStudentView
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_DONE_CLAZZMEMBER_UIDS
import com.ustadmobile.core.view.SELSelectStudentView.Companion.ARG_SELECTED_QUESTION_SET_UID
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SelQuestionSet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * SELSelectStudent's Presenter - Responsible for showing every Clazz Member that will participate
 * in the SEL questions run task before consent, recognition and aswers.
 *
 */
class SELSelectStudentPresenter(context: Any, arguments: Map<String, String>?,
                                view: SELSelectStudentView,
                                val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        CommonHandlerPresenter<SELSelectStudentView>(context, arguments!!, view) {

    private var currentClazzUid: Long = 0

    private var doneClazzMemberUids = ""

    private var currentQuestionSetUid: Long = 0

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    private var questionSetUmProvider: DoorLiveData<List<SelQuestionSet>>? = null

    private var idToQuestionSetMap: HashMap<Long, Long>? = null
    private var questionSetToIdMap: HashMap<Long, Long>? = null

    init {

        //Get Clazz Uid for the current Clazz.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }

        if (arguments!!.containsKey(ARG_DONE_CLAZZMEMBER_UIDS)) {
            doneClazzMemberUids = arguments!!.get(ARG_DONE_CLAZZMEMBER_UIDS)!!.toString()
        }
    }

    /**
     * In Order:
     * 1. Get all Clazz Members and set it to the View.
     *
     * @param savedState    The saved state.
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Convert doneClazzMemberUids csv to List<Integer>
        val donClazzMemberUidsList = ArrayList<Long>()
        for (s in doneClazzMemberUids.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (s.length > 0) {
                donClazzMemberUidsList.add((s).toLong())
            }
        }

        val selStudentsProvider = repository.clazzMemberDao
                .findAllPeopleInClassUidExcept(currentClazzUid, donClazzMemberUidsList)

        view.setSELAnswerListProvider(selStudentsProvider)

        updateSELQuestionSetOptions()
    }

    fun updateSELQuestionSetOptions() {
        //Get sel question set change list live data
        questionSetUmProvider = repository.selQuestionSetDao.findAllQuestionSetsLiveData()

        questionSetUmProvider!!.observe(this, this::updateSELQuestionSetOnView)
    }

    private fun updateSELQuestionSetOnView(questionSets: List<SelQuestionSet>?) {
        idToQuestionSetMap = HashMap()
        questionSetToIdMap = HashMap()
        val questions = ArrayList<String>()
        var i: Long = 0
        for (questionSet in questionSets!!) {
            questions.add(questionSet.title!!)
            idToQuestionSetMap!![i] = questionSet.selQuestionSetUid
            questionSetToIdMap!![questionSet.selQuestionSetUid] = i
            i++
        }

        val questionPresets = questions.toTypedArray()

        view.setQuestionSetDropdownPresets(questionPresets)
    }


    /**
     * Handles primary press on the student list - Selects that student that runs the SEL. Passes
     * its Clazz Member Ui (gets it first) and begins asking for consent.
     *
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    override fun handleCommonPressed(arg: Any) {
        val clazzMemberDao = repository.clazzMemberDao
        val currentPersonUid = arg as Long
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
        args.put(ARG_PERSON_UID, currentPersonUid.toString())

        GlobalScope.launch {
            val clazzMember = clazzMemberDao.findByPersonUidAndClazzUidAsync(currentPersonUid,
                    currentClazzUid)
            args.put(ARG_CLAZZMEMBER_UID, clazzMember!!.clazzMemberUid.toString())
            args.put(ARG_DONE_CLAZZMEMBER_UIDS, doneClazzMemberUids)
            args.put(ARG_SELECTED_QUESTION_SET_UID, currentQuestionSetUid.toString())
            impl.go(SELSelectConsentView.VIEW_NAME, args, view.viewContext)
            view.finish()

        }
    }

    /**
     * Handle what happens when a different Question Set is selected.
     * @param questionChangeUid The question set selected
     */
    fun handleChangeQuestionSetSelected(questionChangeUid: Long) {
        if (idToQuestionSetMap != null && idToQuestionSetMap!!.containsKey(questionChangeUid)) {
            currentQuestionSetUid = idToQuestionSetMap!![questionChangeUid]!!
        } else {
            currentQuestionSetUid = 0
        }


    }

    /**
     * Handles secondary press on the students - Nothing for this screen. Does nothing.
     *
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    override fun handleSecondaryPressed(arg: Any) {
        // No secondary button for this here.
    }

    companion object {

        private val FIRST_SELECTION_ID_FROM: Long = 0
    }

}
