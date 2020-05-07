package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.SelQuestionAndOptionsEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.lib.db.entities.SelQuestionAndOptions
import com.ustadmobile.lib.db.entities.SelQuestionOption
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.serialization.json.Json

class SelQuestionAndOptionsEditPresenter(context: Any,
              arguments: Map<String, String>, view: SelQuestionAndOptionsEditView,
              lifecycleOwner: DoorLifecycleOwner,
              systemImpl: UstadMobileSystemImpl,
              db: UmAppDatabase, repo: UmAppDatabase,
              activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<SelQuestionAndOptionsEditView, SelQuestionAndOptions>(
        context, arguments, view, lifecycleOwner, systemImpl, db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    enum class QuestionOptions(val optionVal: Int, val messageId: Int){
        NOMINATION(SelQuestionDao.SEL_QUESTION_TYPE_NOMINATION,
                MessageID.sel_question_type_nomination),
        MULTI_CHOICE(SelQuestionDao.SEL_QUESTION_TYPE_MULTI_CHOICE,
                MessageID.sel_question_type_multiple_choise),
        FREE_TEXT(SelQuestionDao.SEL_QUESTION_TYPE_FREE_TEXT,
                MessageID.sel_question_type_free_text)
    }

    class OptionTypeMessageIdOption(day: QuestionOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.typeOptions = QuestionOptions.values().map { OptionTypeMessageIdOption(it, context) }

    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): SelQuestionAndOptions? {
        return null
    }

    override fun onLoadFromJson(bundle: Map<String, String>): SelQuestionAndOptions? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: SelQuestionAndOptions? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(SelQuestionAndOptions.serializer(), entityJsonStr)
        }else {
            editEntity = SelQuestionAndOptions(SelQuestion().apply { questionActive = true },
                    listOf(), listOf())
        }

        view.selQuestionOptionList = DoorMutableLiveData(editEntity.options)

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    fun addNewBlankQuestionOption(){
        val selQuestionOption = SelQuestionOption().apply {
            optionText = ""
            optionActive = true
        }
        val currentList = view.selQuestionOptionList?.getValue()?.toMutableList()?:mutableListOf<SelQuestionOption>()
        val newList = currentList + mutableListOf(selQuestionOption)
        view.selQuestionOptionList?.setVal(newList)
    }

    fun removeQuestionOption(deleteMe: SelQuestionOption){

        val currentList = view.selQuestionOptionList?.getValue()?.toMutableList()
                ?: mutableListOf()

        if(deleteMe.selQuestionOptionUid == 0L) {
            currentList.removeAll { it === deleteMe }
            view.selQuestionOptionList?.sendValue(currentList)
        }else{
            val currentDeleteList = view.selQuestionOptionDeactivateList?.getValue()?.toMutableList()
                    ?: mutableListOf()
            val newList =  currentDeleteList + listOf(deleteMe)
            view.selQuestionOptionDeactivateList?.sendValue(newList)
        }

    }

    override fun handleClickSave(entity: SelQuestionAndOptions) {
        entity.options = view.selQuestionOptionList?.getValue()?: listOf()
        val etd = view.selQuestionOptionDeactivateList?.getValue()?: listOf()
        entity.optionsToDeactivate = etd.flatMap { listOf(it.selQuestionOptionUid) }

        view.finishWithResult(listOf(entity))
    }

}