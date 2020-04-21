package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SelQuestionDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.SelQuestionAndOptionsEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.SelQuestion
import com.ustadmobile.lib.db.entities.SelQuestionAndOptions
import com.ustadmobile.lib.db.entities.SelQuestionOption
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.list


class SelQuestionAndOptionsEditPresenter(context: Any,
                          arguments: Map<String, String>, view: SelQuestionAndOptionsEditView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<SelQuestionAndOptionsEditView, SelQuestionAndOptions>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    private val selQuestionOptionOneToManyJoinEditHelper =
            DefaultOneToManyJoinEditHelper<SelQuestionOption>(SelQuestionOption::selQuestionOptionUid,
            "state_selquestionoption_list", SelQuestionOption.serializer().list,
            SelQuestionOption.serializer().list, this) { selQuestionOptionUid = it }

    fun handleAddOrEditSelQuestionOption(entityClass: SelQuestionOption) {
        selQuestionOptionOneToManyJoinEditHelper.onEditResult(entityClass)
    }

    fun handleRemoveSelQuestionOption(entityClass: SelQuestionOption) {
        selQuestionOptionOneToManyJoinEditHelper.onDeactivateEntity(entityClass)
    }


    enum class QuestionOptions(val optionVal: Int, val messageId: Int){
        NOMINATION(SelQuestionDao.SEL_QUESTION_TYPE_NOMINATION, MessageID.sel_question_type_nomination),
        MULTI_CHOICE(SelQuestionDao.SEL_QUESTION_TYPE_MULTI_CHOICE, MessageID.sel_question_type_multiple_choise),
        FREE_TEXT(SelQuestionDao.SEL_QUESTION_TYPE_FREE_TEXT, MessageID.sel_question_type_free_text)
    }

    class OptionTypeMessageIdOption(day: QuestionOptions, context: Any) : MessageIdOption(day.messageId, context, day.optionVal)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.selQuestionOptionList = selQuestionOptionOneToManyJoinEditHelper.liveList

        view.typeOptions = QuestionOptions.values().map { OptionTypeMessageIdOption(it, context) }

        selQuestionOptionOneToManyJoinEditHelper.liveList.sendValue(
                entity?.options?: mutableListOf())

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
                    listOf())
        }

        selQuestionOptionOneToManyJoinEditHelper.onLoadFromJsonSavedState(bundle)

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    fun updateQuestionOptionTitle(questionOption: SelQuestionOption, title: String){
        questionOption.optionText = title
        handleAddOrEditSelQuestionOption(questionOption)

    }

    fun addNewBlankQuestionOption(){
        val newQuestionOption = SelQuestionOption()
        newQuestionOption.optionText = ""
        newQuestionOption.selQuestionOptionQuestionUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        newQuestionOption.optionActive = true

        handleAddOrEditSelQuestionOption(newQuestionOption)

    }

    fun removeQuestionOption(selQuestionOption: SelQuestionOption){
        selQuestionOption.optionActive = false
        GlobalScope.launch {
            if(selQuestionOption.selQuestionOptionUid != 0L) {
                repo.selQuestionOptionDao.updateAsync(selQuestionOption)
            }
        }
    }

    override fun handleClickSave(entity: SelQuestionAndOptions) {
        //Build options
        val eti = selQuestionOptionOneToManyJoinEditHelper.entitiesToInsert
        entity.options = eti

        view.finishWithResult(listOf(entity))
    }


    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}