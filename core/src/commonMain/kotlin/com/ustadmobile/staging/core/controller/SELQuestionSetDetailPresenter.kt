package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.db.dao.SelQuestionSetDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SELQuestionDetail2View
import com.ustadmobile.core.view.SELQuestionSetDetailView
import com.ustadmobile.core.view.SELQuestionSetDetailView.Companion.ARG_SEL_QUESTION_SET_NAME
import com.ustadmobile.core.view.SELQuestionSetDetailView.Companion.ARG_SEL_QUESTION_SET_UID
import com.ustadmobile.lib.db.entities.SelQuestion
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SELQuestionSetDetailPresenter(context: Any, arguments: Map<String, String>?, view: 
SELQuestionSetDetailView, val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<SELQuestionSetDetailView>(context, arguments!!,
        view) {

    private var questionUmProvider: DataSource.Factory<Int, SelQuestion>? = null
    internal var repository: UmAppDatabase
    private val selQuestionDao: SelQuestionDao
    private val selQuestionSetDao: SelQuestionSetDao? = null
    private var questionSetUid = 0L
    private var questionSetName = ""

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        selQuestionDao = repository.selQuestionDao

        if (arguments!!.containsKey(ARG_SEL_QUESTION_SET_UID)) {
            questionSetUid = arguments!!.get(ARG_SEL_QUESTION_SET_UID)!!.toLong()
        }

        if (arguments!!.containsKey(ARG_SEL_QUESTION_SET_NAME)) {
            questionSetName = arguments!!.get(ARG_SEL_QUESTION_SET_NAME)!!.toString()
        }
    }

    fun handleQuestionEdit(question: SelQuestion) {

        goToQuestionDetail(question)
    }

    fun handleQuestionDelete(selQuestionUid: Long) {
        GlobalScope.launch {
            val selQuestionObj = selQuestionDao.findByUidAsync(selQuestionUid)
            if (selQuestionObj != null) {
                selQuestionObj.questionActive = false
                selQuestionDao.updateAsync(selQuestionObj)
            }
        }
    }

    fun goToQuestionDetail(question: SelQuestion) {
        val questionUid = question.selQuestionUid
        val args = HashMap<String, String>()
        args.put(SELQuestionDetail2View.ARG_QUESTION_UID_QUESTION_DETAIL, questionUid.toString())
        args.put(ARG_SEL_QUESTION_SET_UID, question.selQuestionSelQuestionSetUid.toString())
        impl.go(SELQuestionDetail2View.VIEW_NAME, args, context)
    }

    fun handleClickPrimaryActionButton() {
        val args = HashMap<String, String>()
        args.put(ARG_SEL_QUESTION_SET_UID, questionSetUid.toString())
        impl.go(SELQuestionDetail2View.VIEW_NAME, args, context)
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        GlobalScope.launch {
            questionUmProvider = selQuestionDao.findAllActivrQuestionsInSet(questionSetUid)
            view.setListProvider(questionUmProvider!!)
            view.updateToolbarTitle(questionSetName)
        }
    }
}
