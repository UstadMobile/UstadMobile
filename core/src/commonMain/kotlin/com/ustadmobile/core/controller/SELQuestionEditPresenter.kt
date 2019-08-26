package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.SELQuestionEditView
import com.ustadmobile.lib.db.entities.SelQuestion
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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
        selQuestionDao = repository.selQuestionDao

    }

    override fun onCreate(savedState: Map<String, String?>?) {
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

        GlobalScope.launch {
            val result = selQuestionDao.getMaxIndexAsync()
            val socialNominationQuestion = SelQuestion()
            socialNominationQuestion.questionText = newQuestion
            socialNominationQuestion.questionIndex = result
            socialNominationQuestion.assignToAllClasses = allClasses
            socialNominationQuestion.multiNominations = multiNominations
            socialNominationQuestion.selQuestionSelQuestionSetUid = DEFAULT_QUESTION_SET_UID

            selQuestionDao.insertAsync(socialNominationQuestion)
            view.finish()

        }

    }


}
