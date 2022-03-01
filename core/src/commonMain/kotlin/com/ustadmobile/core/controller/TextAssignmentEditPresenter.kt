package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.TextAssignmentEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import kotlinx.coroutines.launch
import org.kodein.di.DI


class TextAssignmentEditPresenter(context: Any,
                             arguments: Map<String, String>, view: TextAssignmentEditView,
                             lifecycleOwner: DoorLifecycleOwner,
                             di: DI)
    : UstadEditPresenter<TextAssignmentEditView, CourseAssignmentSubmission>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    override fun onLoadFromJson(bundle: Map<String, String>): CourseAssignmentSubmission {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: CourseAssignmentSubmission? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, CourseAssignmentSubmission.serializer(), entityJsonStr)
        }else {
            editEntity = CourseAssignmentSubmission().apply {
                casText = systemImpl.getString(MessageID.terms_and_policies_text, context)
            }
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: CourseAssignmentSubmission) {
        presenterScope.launch {
            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}