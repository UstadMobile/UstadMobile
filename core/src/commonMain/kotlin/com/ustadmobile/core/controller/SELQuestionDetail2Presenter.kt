package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.db.dao.SelQuestionOptionDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddQuestionOptionDialogView
import com.ustadmobile.core.view.SELQuestionDetail2View
import com.ustadmobile.core.view.SELQuestionDetail2View.Companion.ARG_QUESTION_OPTION_UID
import com.ustadmobile.core.view.SELQuestionDetail2View.Companion.ARG_QUESTION_UID_QUESTION_DETAIL
import com.ustadmobile.core.view.SELQuestionSetDetailView.Companion.ARG_SEL_QUESTION_SET_UID
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.lib.db.entities.SelQuestionOption
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SELQuestionDetail2Presenter(context: Any, arguments: Map<String, String>?,
                                  view: SELQuestionDetail2View,
                                  val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<SELQuestionDetail2View>(context, arguments!!, view) {


    //Provider
    private var providerList: DataSource.Factory<Int, SelQuestionOption>? = null
    internal lateinit var repository: UmAppDatabase
    private var currentQuestionUid: Long = 0
    private var currentQuestionSetUid: Long = 0
    internal lateinit var questionUmLiveData: DoorLiveData<SelQuestion?>

    private var mOriginalQuestion: SelQuestion? = null
    private var mUpdatedQuestion: SelQuestion? = null
    internal lateinit var questionDao: SelQuestionDao
    internal lateinit var questionOptionDao: SelQuestionOptionDao

    private var questionTypePresets: Array<String>? = null

    init {


        if (arguments!!.containsKey(ARG_SEL_QUESTION_SET_UID)) {
            currentQuestionSetUid = arguments!!.get(ARG_SEL_QUESTION_SET_UID)!!.toLong()

        }
        if (arguments!!.containsKey(ARG_QUESTION_UID_QUESTION_DETAIL)) {
            currentQuestionUid = arguments!!.get(ARG_QUESTION_UID_QUESTION_DETAIL)!!.toLong()
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        questionDao = repository.selQuestionDao
        questionOptionDao = repository.selQuestionOptionDao

        providerList = questionOptionDao.findAllActiveOptionsByQuestionUidProvider(currentQuestionUid)


        //Set questionType preset
        questionTypePresets =
                arrayOf(impl.getString(MessageID.sel_question_type_nomination, context),
                impl.getString(MessageID.sel_question_type_multiple_choise, context),
                impl.getString(MessageID.sel_question_type_free_text, context))

        //Set to view
        view.setQuestionTypePresets(questionTypePresets!!)

        //Create / Get question
        questionUmLiveData = questionDao.findByUidLive(currentQuestionUid)

        //Observe the live data :
        questionUmLiveData.observe(this, this::handleSELQuestionValueChanged)

        GlobalScope.launch {
            val selQuestion = questionDao.findByUidAsync(currentQuestionUid)
            if (selQuestion != null) {
                mUpdatedQuestion = selQuestion
                view.setQuestionOnView(mUpdatedQuestion!!)
            } else {

                //Set index
                val result = questionDao.getMaxIndexByQuestionSetAsync(currentQuestionSetUid)

                //Create a new one
                val newSELQuestion = SelQuestion()
                newSELQuestion.selQuestionSelQuestionSetUid = currentQuestionSetUid
                newSELQuestion.questionIndex = result!! + 1
                mUpdatedQuestion = newSELQuestion

                if (mOriginalQuestion == null) {
                    mOriginalQuestion = mUpdatedQuestion
                }

                view.setQuestionOnView(mUpdatedQuestion!!)

            }
        }

        //Set provider
        view.setQuestionOptionsProvider(providerList!!)

    }

    fun handleQuestionTypeChange(type: Int) {

        when (type) {
            SelQuestionDao.SEL_QUESTION_TYPE_NOMINATION -> view.showQuestionOptions(false)
            SelQuestionDao.SEL_QUESTION_TYPE_MULTI_CHOICE -> view.showQuestionOptions(true)
            SelQuestionDao.SEL_QUESTION_TYPE_FREE_TEXT -> view.showQuestionOptions(false)
            else -> {
            }
        }
        if (mUpdatedQuestion != null)
            mUpdatedQuestion!!.questionType = type
    }

    fun updateQuestionTitle(title: String) {
        mUpdatedQuestion!!.questionText = title
    }

    fun handleSELQuestionValueChanged(question: SelQuestion?) {
        //set the og person value
        if (mOriginalQuestion == null)
            mOriginalQuestion = question

        if (mUpdatedQuestion == null || mUpdatedQuestion != question) {

            if (question != null) {
                //Update the currently editing class object
                mUpdatedQuestion = question

                view.setQuestionOnView(question)
            }
        }
    }

    fun handleClickAddOption() {
        val args = HashMap<String, String>()
        args.put(ARG_QUESTION_UID_QUESTION_DETAIL, currentQuestionUid.toString())

        impl.go(AddQuestionOptionDialogView.VIEW_NAME, args, context)
    }

    fun handleClickDone() {

        mUpdatedQuestion!!.questionActive = true
        mUpdatedQuestion!!.selQuestionSelQuestionSetUid = currentQuestionSetUid
        GlobalScope.launch {
            val questionInDB = questionDao.findByUidAsync(mUpdatedQuestion!!.selQuestionUid)
            if (questionInDB != null) {
                questionDao.updateAsync(mUpdatedQuestion!!)
                view.finish()

            } else {
                questionDao.insertAsync(mUpdatedQuestion!!)
                view.finish()
            }
        }

    }

    fun handleQuestionOptionEdit(questionOptionUid: Long) {
        GlobalScope.launch {
            val result = questionOptionDao.findByUidAsync(questionOptionUid)
            if (result != null) {
                val args = HashMap<String, String>()
                args.put(ARG_QUESTION_UID_QUESTION_DETAIL, result.selQuestionOptionQuestionUid.toString())
                args.put(ARG_QUESTION_OPTION_UID, result.selQuestionOptionUid.toString())
                impl.go(AddQuestionOptionDialogView.VIEW_NAME, args, context)

            }
        }
    }

    fun handleQuestionOptionDelete(questionOptionUid: Long) {
        GlobalScope.launch {
            val result = questionOptionDao.findByUidAsync(questionOptionUid)
            if (result != null) {
                result.optionActive = false
                questionOptionDao.updateAsync(result)
            }
        }
    }

}
