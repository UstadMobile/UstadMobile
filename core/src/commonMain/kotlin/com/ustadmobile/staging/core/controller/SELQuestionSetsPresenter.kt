package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SelQuestionSetDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddQuestionSetDialogView
import com.ustadmobile.core.view.SELQuestionSetDetailView
import com.ustadmobile.core.view.SELQuestionSetsView
import com.ustadmobile.lib.db.entities.SELQuestionSetWithNumQuestions


class SELQuestionSetsPresenter(context: Any, arguments: Map<String, String>?, view:
SELQuestionSetsView, val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<SELQuestionSetsView>(context, arguments!!, view) {

    private var questionSetWithNumQuestionsUmProvider:
            DataSource.Factory<Int, SELQuestionSetWithNumQuestions>? = null
    internal var repository: UmAppDatabase
    private val selQuestionSetDao: SelQuestionSetDao


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        selQuestionSetDao = repository.selQuestionSetDao
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        questionSetWithNumQuestionsUmProvider = selQuestionSetDao.findAllQuestionSetsWithNumQuestions()
        view.setListProvider(questionSetWithNumQuestionsUmProvider!!)

    }

    fun handleGoToQuestionSet(questionSetUid: Long, questionSetName: String) {
        val args = HashMap<String, String>()
        args.put(SELQuestionSetDetailView.ARG_SEL_QUESTION_SET_UID, questionSetUid.toString())
        args.put(SELQuestionSetDetailView.ARG_SEL_QUESTION_SET_NAME, questionSetName)
        impl.go(SELQuestionSetDetailView.VIEW_NAME, args, context)
    }

    fun handleClickPrimaryActionButton() {
        val args = HashMap<String, String>()
        impl.go(AddQuestionSetDialogView.VIEW_NAME, args, context)

    }

}
