package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.SelQuestionSetDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.AddQuestionSetDialogView
import com.ustadmobile.lib.db.entities.SelQuestionSet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class AddQuestionSetDialogPresenter(context: Any, arguments:Map<String, String>?,
                                    view: AddQuestionSetDialogView)
    : UstadBaseController<AddQuestionSetDialogView>(context, arguments!!, view) {

    private var questionSet: SelQuestionSet? = null
    private val selQuestionSetDao: SelQuestionSetDao

    init {

        val repository = UmAccountManager.getRepositoryForActiveAccount(context)
        selQuestionSetDao = repository.selQuestionSetDao
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
    }

    /**
     * Creates a new question Set with the title given here
     * @param title The title of the question
     */
    fun handleAddQuestionSet(title: String) {
        questionSet = SelQuestionSet()
        questionSet!!.title = title
        GlobalScope.launch {
            selQuestionSetDao.insertAsync(questionSet!!)
        }
    }

    /**
     * Nulls current question (effectively dismissing the progress done in this presenter)
     */
    fun handleCancelSchedule() {
        //Do nothing
        questionSet = null
    }

}
