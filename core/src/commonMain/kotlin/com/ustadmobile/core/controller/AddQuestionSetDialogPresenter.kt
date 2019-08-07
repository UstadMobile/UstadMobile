package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.SelQuestionSetDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.AddQuestionSetDialogView
import com.ustadmobile.lib.db.entities.SelQuestionSet


class AddQuestionSetDialogPresenter(context: Any, arguments:Map<String, String>?,
                                    view: AddQuestionSetDialogView)
    : UstadBaseController<AddQuestionSetDialogView>(context, arguments!!, view) {

    private var questionSet: SelQuestionSet? = null
    private val selQuestionSetDao: SelQuestionSetDao

    init {

        val repository = UmAccountManager.getRepositoryForActiveAccount(context)
        selQuestionSetDao = repository.selQuestionSetDao
    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    /**
     * Creates a new question Set with the title given here
     * @param title The title of the question
     */
    fun handleAddQuestionSet(title: String) {
        questionSet = SelQuestionSet()
        questionSet!!.title = title
        selQuestionSetDao.insertAsync(questionSet!!, object : UmCallback<Long> {
            override fun onSuccess(result: Long?) {
                //Do nothing
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

    /**
     * Nulls current question (effectively dismissing the progress done in this presenter)
     */
    fun handleCancelSchedule() {
        //Do nothing
        questionSet = null
    }

}
