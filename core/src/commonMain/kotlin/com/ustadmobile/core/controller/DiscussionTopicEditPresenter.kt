package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.DiscussionTopicEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.DiscussionTopic
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class DiscussionTopicEditPresenter(context: Any,
                                   arguments: Map<String, String>,
                                   view: DiscussionTopicEditView,
                                   lifecycleOwner: DoorLifecycleOwner,
                                   di: DI)
    : UstadEditPresenter<DiscussionTopicEditView, DiscussionTopic>(  context,
                                                                            arguments,
                                                                            view,
                                                                            di,
                                                                            lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON


    override fun onLoadFromJson(bundle: Map<String, String>): DiscussionTopic {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]

        val editEntity = if (entityJsonStr != null) {
             safeParse(di, DiscussionTopic.serializer(), entityJsonStr)
        }else{
            DiscussionTopic().apply {
                discussionTopicUid = db.doorPrimaryKeyManager.nextId(DiscussionTopic.TABLE_ID)

                discussionTopicStartDate = systemTimeInMillis()

            }

        }
        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity

        savedState.putEntityAsJson(ARG_ENTITY_JSON, null, entityVal)
    }


    override fun handleClickSave(entity: DiscussionTopic) {
        presenterScope.launch {


            if (entity.discussionTopicTitle.isNullOrEmpty()) {
                view.blockTitleError =
                    systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }else{
                view.blockTitleError = null
            }

            view.loading = true
            view.fieldsEnabled = false

            finishWithResult(
                safeStringify(
                    di,
                    ListSerializer(DiscussionTopic.serializer()),
                    listOf(entity)
                )
            )

            view.loading = false
            view.fieldsEnabled = true

        }
    }



}