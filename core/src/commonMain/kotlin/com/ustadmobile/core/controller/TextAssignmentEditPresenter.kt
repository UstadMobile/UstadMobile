package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.countWords
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.TextAssignmentEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class TextAssignmentEditPresenter(context: Any,
                             arguments: Map<String, String>, view: TextAssignmentEditView,
                             lifecycleOwner: LifecycleOwner,
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
        val editEntity: CourseAssignmentSubmission = if(entityJsonStr != null) {
            safeParse(di, CourseAssignmentSubmission.serializer(), entityJsonStr)
        }else {
            CourseAssignmentSubmission().apply {
                casAssignmentUid = bundle[TextAssignmentEditView.ASSIGNMENT_ID]?.toLongOrNull() ?: 0L
                casSubmitterUid = accountManager.activeAccount.personUid
                casType = CourseAssignmentSubmission.SUBMISSION_TYPE_TEXT
                casUid = db.doorPrimaryKeyManager.nextId(CourseAssignmentSubmission.TABLE_ID)
            }
        }

        presenterScope.launch {
            view.clazzAssignment = db.clazzAssignmentDao.findByUidAsync(editEntity.casAssignmentUid)
        }


        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json,CourseAssignmentSubmission.serializer(),
                entity)
    }

    override fun handleClickSave(entity: CourseAssignmentSubmission) {

        val assignment = view.clazzAssignment ?: return

        val text = entity.casText ?: ""
        val textLength = if(assignment.caTextLimitType == ClazzAssignment.TEXT_CHAR_LIMIT)
                text.length
        else
            text.countWords()

        if(textLength > assignment.caTextLimit){
            view.showSnackBar(systemImpl.getString(MessageID.error_too_long_text, context))
            return
        }

        presenterScope.launch {
            finishWithResult(safeStringify(di,
                    ListSerializer(CourseAssignmentSubmission.serializer()),
                    listOf(entity)))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}