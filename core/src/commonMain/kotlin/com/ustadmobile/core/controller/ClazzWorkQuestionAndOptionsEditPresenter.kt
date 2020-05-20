package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzWorkQuestionAndOptionsEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWorkQuestion
import com.ustadmobile.lib.db.entities.ClazzWorkQuestion.Companion.CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT
import com.ustadmobile.lib.db.entities.ClazzWorkQuestion.Companion.CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptions
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionOption
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.serialization.json.Json


class ClazzWorkQuestionAndOptionsEditPresenter(context: Any,
                          arguments: Map<String, String>, view: ClazzWorkQuestionAndOptionsEditView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<ClazzWorkQuestionAndOptionsEditView, ClazzWorkQuestionAndOptions>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    enum class QuestionOptions(val optionVal: Int, val messageId: Int){
        NOMINATION(CLAZZ_WORK_QUESTION_TYPE_FREE_TEXT,
                MessageID.sel_question_type_free_text),
        MULTI_CHOICE(CLAZZ_WORK_QUESTION_TYPE_MULTIPLE_CHOICE,
                MessageID.sel_question_type_multiple_choise)
    }

    class ClazzWorkQuestionOptionTypeMessageIdOption(day: QuestionOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.typeOptions = QuestionOptions.values().map {
            ClazzWorkQuestionOptionTypeMessageIdOption(it, context)}
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWorkQuestionAndOptions? {
        return null
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzWorkQuestionAndOptions? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ClazzWorkQuestionAndOptions? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(ClazzWorkQuestionAndOptions.serializer(), entityJsonStr)
        }else {
            editEntity = ClazzWorkQuestionAndOptions(ClazzWorkQuestion().apply {
                clazzWorkQuestionActive = true }, listOf(), listOf() )
        }

        view.clazzWorkQuestionOptionList = DoorMutableLiveData(editEntity.options)

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    fun addNewBlankQuestionOption(){
        val selQuestionOption = ClazzWorkQuestionOption().apply {
            clazzWorkQuestionOptionText= ""
            clazzWorkQuestionOptionActive = true
        }
        val currentList =
                view.clazzWorkQuestionOptionList?.getValue()?.toMutableList()?:mutableListOf<ClazzWorkQuestionOption>()
        val newList = currentList + mutableListOf(selQuestionOption)
        view.clazzWorkQuestionOptionList?.setVal(newList)
    }

    fun removeQuestionOption(deleteMe: ClazzWorkQuestionOption){

        val currentList = view.clazzWorkQuestionOptionList?.getValue()?.toMutableList()
                ?: mutableListOf()

        if(deleteMe.clazzWorkQuestionOptionUid == 0L) {
            currentList.removeAll { it === deleteMe }
            view.clazzWorkQuestionOptionList?.sendValue(currentList)
        }else{
            val currentDeleteList = view.clazzWorkQuestionOptionDeactivateList?.getValue()?.toMutableList()
                    ?: mutableListOf()
            val newList =  currentDeleteList + listOf(deleteMe)
            view.clazzWorkQuestionOptionDeactivateList?.sendValue(newList)
        }

    }

    override fun handleClickSave(entity: ClazzWorkQuestionAndOptions) {

        entity.options = view.clazzWorkQuestionOptionList?.getValue()?: listOf()
        val etd = view.clazzWorkQuestionOptionDeactivateList?.getValue()?:listOf()
        entity.optionsToDeactivate = etd.flatMap { listOf(it.clazzWorkQuestionOptionUid) }

        view.finishWithResult(listOf(entity))
    }
}