package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.SELQuestionEditView
import com.ustadmobile.lib.db.entities.SelQuestion


/**
 * The SELQuestionEdit's Presenter - Responsible for the logic to add a new SocialNomination
 * Question. This is part of Class Management.
 *
 */
class SELQuestionEditPresenter(context: Any, arguments: Map<String, String>?, view:
SELQuestionEditView) : UstadBaseController<SELQuestionEditView>(context, arguments!!, view) {

    private val DEFAULT_QUESTION_SET_UID: Long = 1

    private val selQuestionDao: SelQuestionDao


    init {

        val repository = UmAccountManager.getRepositoryForActiveAccount(context)
        selQuestionDao = repository.getSocialNominationQuestionDao()

    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    /**
     * Done click handler when a new Question has been filled in -  This will add the new Question
     * in to the database where SEL task will pick it up for future SEL runs.
     *
     * @param newQuestion   The string of the new Question
     * @param allClasses    The checkbox value if this question is for every class (default true)
     * @param multiNominations  The checkbox value to check if mulitple nominations for this
     * Question is allowed (default true)
     */
    fun handleClickDone(newQuestion: String, allClasses: Boolean, multiNominations: Boolean) {

        selQuestionDao.getMaxIndexAsync(object : UmCallback<Int> {
            override fun onSuccess(result: Int?) {
                val socialNominationQuestion = SelQuestion()
                socialNominationQuestion.questionText = newQuestion
                socialNominationQuestion.questionIndex = result
                socialNominationQuestion.isAssignToAllClasses = allClasses
                socialNominationQuestion.isMultiNominations = multiNominations
                socialNominationQuestion.selQuestionSelQuestionSetUid = DEFAULT_QUESTION_SET_UID

                selQuestionDao.insertAsync(socialNominationQuestion,
                        object : UmCallback<Long> {
                            override fun onSuccess(result: Long?) {
                                view.finish()
                            }

                            override fun onFailure(exception: Throwable?) {
                                print(exception!!.message)
                            }
                        })
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }


}
