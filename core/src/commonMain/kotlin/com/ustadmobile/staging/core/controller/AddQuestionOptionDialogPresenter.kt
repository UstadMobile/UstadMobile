package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SelQuestionOptionDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.AddQuestionOptionDialogView
import com.ustadmobile.core.view.SELQuestionDetail2View.Companion.ARG_QUESTION_OPTION_UID
import com.ustadmobile.core.view.SELQuestionDetail2View.Companion.ARG_QUESTION_UID_QUESTION_DETAIL
import com.ustadmobile.lib.db.entities.SelQuestionOption
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddQuestionOptionDialogPresenter (context: Any, arguments: Map<String, String>?,
                                        view: AddQuestionOptionDialogView)
    : UstadBaseController<AddQuestionOptionDialogView>(context, arguments!!, view) {

    private var currentOption: SelQuestionOption? = null
    internal var repository: UmAppDatabase
    private var currentQuestionUid: Long = 0
    private var currentQuestionOptionUid: Long = 0
    private val questionOptionDao: SelQuestionOptionDao

    init {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get question uid
        if (arguments!!.containsKey(ARG_QUESTION_UID_QUESTION_DETAIL)) {
            currentQuestionUid = arguments!!.get(ARG_QUESTION_UID_QUESTION_DETAIL)!!.toLong()
        }
        //Get Question option uid
        if (arguments!!.containsKey(ARG_QUESTION_OPTION_UID)) {
            currentQuestionOptionUid = arguments!!.get(ARG_QUESTION_OPTION_UID)!!.toLong()
        }

        questionOptionDao = repository.selQuestionOptionDao
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        GlobalScope.launch {
            //Get the question from database or create a new one and set it to the view
            val result = questionOptionDao.findByUidAsync(currentQuestionOptionUid)
            if (result == null) {
                currentOption = SelQuestionOption()
                currentOption!!.selQuestionOptionQuestionUid = currentQuestionUid
                currentOption!!.optionText = ""
            } else {
                currentOption = result
            }

            view.setOptionText(currentOption!!.optionText!!)
        }

    }

    /**
     * Nulls current option (effectively dismissing the progress done in this presenter)
     */
    fun handleCancelQuestionOption() {
        currentOption = null
    }

    /**
     * Persists the question with the new title to the database
     * @param newTitle  The new question title
     */
    fun handleAddQuestionOption(newTitle: String) {
        currentOption!!.optionText = newTitle
        currentOption!!.optionActive = true
        GlobalScope.launch {
            val result = questionOptionDao.findByUidAsync(currentOption!!.selQuestionOptionUid)
            if (result != null) {
                //exists. update
                questionOptionDao.updateAsync(currentOption!!)
                view.finish()

            } else {
                //new. insert
                questionOptionDao.insertAsync(currentOption!!)
                view.finish()

            }
        }
    }
}
