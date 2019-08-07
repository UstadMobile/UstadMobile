package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.db.dao.SelQuestionSetDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SELQuestionDetail2View
import com.ustadmobile.core.view.SELQuestionSetDetailView
import com.ustadmobile.lib.db.entities.SelQuestion

import com.ustadmobile.core.view.SELQuestionSetDetailView.Companion.ARG_SEL_QUESTION_SET_NAME
import com.ustadmobile.core.view.SELQuestionSetDetailView.Companion.ARG_SEL_QUESTION_SET_UID

class SELQuestionSetDetailPresenter(context: Any, arguments: Map<String, String>?, view: 
SELQuestionSetDetailView, val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<SELQuestionSetDetailView>(context, arguments!!,
        view) {

    private var questionUmProvider: UmProvider<SelQuestion>? = null
    internal var repository: UmAppDatabase
    private val selQuestionDao: SelQuestionDao
    private val selQuestionSetDao: SelQuestionSetDao? = null
    private var questionSetUid = 0L
    private var questionSetName = ""

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        selQuestionDao = repository.getSocialNominationQuestionDao()

        if (arguments!!.containsKey(ARG_SEL_QUESTION_SET_UID)) {
            questionSetUid = arguments!!.get(ARG_SEL_QUESTION_SET_UID)
        }

        if (arguments!!.containsKey(ARG_SEL_QUESTION_SET_NAME)) {
            questionSetName = arguments!!.get(ARG_SEL_QUESTION_SET_NAME)
        }
    }

    fun handleQuestionEdit(question: SelQuestion) {

        goToQuestionDetail(question)
    }

    fun handleQuestionDelete(selQuestionUid: Long) {
        selQuestionDao.findByUidAsync(selQuestionUid, object : UmCallback<SelQuestion> {
            override fun onSuccess(selQuestionObj: SelQuestion?) {
                if (selQuestionObj != null) {
                    selQuestionObj.isQuestionActive = false
                    selQuestionDao.updateAsync(selQuestionObj, object : UmCallback<Int> {
                        override fun onSuccess(result: Int?) {
                            //ola
                        }

                        override fun onFailure(exception: Throwable?) {
                            print(exception!!.message)
                        }
                    })
                }
            }

            override fun onFailure(exception: Throwable?) {

            }
        })
    }

    fun goToQuestionDetail(question: SelQuestion) {
        val questionUid = question.selQuestionUid
        val args = HashMap<String, String>()
        args.put(SELQuestionDetail2View.ARG_QUESTION_UID_QUESTION_DETAIL, questionUid)
        args.put(ARG_SEL_QUESTION_SET_UID, question.selQuestionSelQuestionSetUid)
        impl.go(SELQuestionDetail2View.VIEW_NAME, args, context)
    }

    fun handleClickPrimaryActionButton() {
        val args = HashMap<String, String>()
        args.put(ARG_SEL_QUESTION_SET_UID, questionSetUid)
        impl.go(SELQuestionDetail2View.VIEW_NAME, args, context)
    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        questionUmProvider = selQuestionDao.findAllActivrQuestionsInSet(questionSetUid)
        view.setListProvider(questionUmProvider!!)
        view.updateToolbarTitle(questionSetName)
    }
}
