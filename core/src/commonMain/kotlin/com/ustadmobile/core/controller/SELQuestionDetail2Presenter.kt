package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.db.dao.SelQuestionOptionDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddQuestionOptionDialogView
import com.ustadmobile.core.view.SELQuestionDetail2View
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.lib.db.entities.SelQuestionOption

import com.ustadmobile.core.view.SELQuestionDetail2View.Companion.ARG_QUESTION_OPTION_UID
import com.ustadmobile.core.view.SELQuestionDetail2View.Companion.ARG_QUESTION_UID_QUESTION_DETAIL
import com.ustadmobile.core.view.SELQuestionSetDetailView.Companion.ARG_SEL_QUESTION_SET_UID

class SELQuestionDetail2Presenter(context: Any, arguments: Map<String, String>?,
                                  view: SELQuestionDetail2View,
                                  val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<SELQuestionDetail2View>(context, arguments!!, view) {


    //Provider
    private var providerList: UmProvider<SelQuestionOption>? = null
    internal var repository: UmAppDatabase
    private var currentQuestionUid: Long = 0
    private var currentQuestionSetUid: Long = 0
    internal var questionUmLiveData: UmLiveData<SelQuestion>

    private var mOriginalQuestion: SelQuestion? = null
    private var mUpdatedQuestion: SelQuestion? = null
    internal var questionDao: SelQuestionDao
    internal var questionOptionDao: SelQuestionOptionDao

    private var questionTypePresets: Array<String>? = null

    init {


        if (arguments!!.containsKey(ARG_SEL_QUESTION_SET_UID)) {
            currentQuestionSetUid = arguments!!.get(ARG_SEL_QUESTION_SET_UID)

        }
        if (arguments!!.containsKey(ARG_QUESTION_UID_QUESTION_DETAIL)) {
            currentQuestionUid = arguments!!.get(ARG_QUESTION_UID_QUESTION_DETAIL)
        }
    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        questionDao = repository.getSocialNominationQuestionDao()
        questionOptionDao = repository.getSELQuestionOptionDao()

        providerList = repository.getSELQuestionOptionDao()
                .findAllActiveOptionsByQuestionUidProvider(currentQuestionUid)


        //Set questionType preset
        questionTypePresets = arrayOf(impl.getString(MessageID.sel_question_type_nomination, context), impl.getString(MessageID.sel_question_type_multiple_choise, context), impl.getString(MessageID.sel_question_type_free_text, context))

        //Set to view
        view.setQuestionTypePresets(questionTypePresets!!)

        //Create / Get question
        questionUmLiveData = repository.getSocialNominationQuestionDao().findByUidLive(currentQuestionUid)

        //Observe the live data :
        questionUmLiveData.observe(this@SELQuestionDetail2Presenter,
                UmObserver<SelQuestion> { this@SELQuestionDetail2Presenter.handleSELQuestionValueChanged(it) })

        val selQuestionDao = repository.getSocialNominationQuestionDao()
        selQuestionDao.findByUidAsync(currentQuestionUid,
                object : UmCallback<SelQuestion> {
                    override fun onSuccess(selQuestion: SelQuestion?) {
                        if (selQuestion != null) {
                            mUpdatedQuestion = selQuestion
                            view.setQuestionOnView(mUpdatedQuestion!!)
                        } else {

                            //Set index
                            selQuestionDao.getMaxIndexByQuestionSetAsync(currentQuestionSetUid,
                                    object : UmCallback<Int> {
                                        override fun onSuccess(result: Int?) {
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

                                        override fun onFailure(exception: Throwable?) {
                                            print(exception!!.message)
                                        }
                                    })
                        }
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })

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
        args.put(ARG_QUESTION_UID_QUESTION_DETAIL, currentQuestionUid)

        impl.go(AddQuestionOptionDialogView.VIEW_NAME, args, context)
    }

    fun handleClickDone() {

        mUpdatedQuestion!!.isQuestionActive = true
        mUpdatedQuestion!!.selQuestionSelQuestionSetUid = currentQuestionSetUid
        questionDao.findByUidAsync(mUpdatedQuestion!!.selQuestionUid, object : UmCallback<SelQuestion> {
            override fun onSuccess(questionInDB: SelQuestion?) {
                if (questionInDB != null) {
                    questionDao.updateAsync(mUpdatedQuestion!!, object : UmCallback<Int> {
                        override fun onSuccess(result: Int?) {
                            view.finish()
                        }

                        override fun onFailure(exception: Throwable?) {
                            print(exception!!.message)
                        }
                    })
                } else {
                    questionDao.insertAsync(mUpdatedQuestion!!, object : UmCallback<Long> {
                        override fun onSuccess(result: Long?) {
                            view.finish()
                        }

                        override fun onFailure(exception: Throwable?) {

                        }
                    })
                }
            }

            override fun onFailure(exception: Throwable?) {

            }
        })

    }

    fun handleQuestionOptionEdit(questionOptionUid: Long) {
        questionOptionDao.findByUidAsync(questionOptionUid, object : UmCallback<SelQuestionOption> {
            override fun onSuccess(result: SelQuestionOption?) {
                if (result != null) {
                    val args = HashMap<String, String>()
                    args.put(ARG_QUESTION_UID_QUESTION_DETAIL, result.selQuestionOptionQuestionUid)
                    args.put(ARG_QUESTION_OPTION_UID, result.selQuestionOptionUid)
                    impl.go(AddQuestionOptionDialogView.VIEW_NAME, args, context)

                }
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

    fun handleQuestionOptionDelete(questionOptionUid: Long) {
        questionOptionDao.findByUidAsync(questionOptionUid, object : UmCallback<SelQuestionOption> {
            override fun onSuccess(result: SelQuestionOption?) {
                if (result != null) {
                    result.isOptionActive = false
                    questionOptionDao.updateAsync(result, object : UmCallback<Int> {
                        override fun onSuccess(result: Int?) {

                        }

                        override fun onFailure(exception: Throwable?) {
                            print(exception!!.message)
                        }
                    })
                }
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

}
