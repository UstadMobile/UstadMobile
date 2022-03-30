package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.CourseTerminologyEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.CourseTerminologyLabel
import com.ustadmobile.lib.db.entities.CourseTerminologyWithLabel
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class CourseTerminologyEditPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: CourseTerminologyEditView,
    lifecycleOwner: DoorLifecycleOwner,
    di: DI)
    : UstadEditPresenter<CourseTerminologyEditView, CourseTerminologyWithLabel>(
    context,
    arguments,
    view,
    di,
    lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): CourseTerminologyWithLabel? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val entity =  db.onRepoWithFallbackToDb(2000) {
            it.takeIf { entityUid != 0L }?.courseTerminologyDao?.findByUidAsync(entityUid)
        }

        val entityWithLabel = CourseTerminologyWithLabel().apply {
            ctTitle = entity?.ctTitle
            ctUid = entity?.ctUid ?: 0L
            ctTerminology = entity?.ctTerminology
            ctLct = entity?.ctLct ?: 0
            label = ctTerminology?.let { safeParse(di, CourseTerminologyLabel.serializer(), it) } ?: CourseTerminologyLabel()
        }

        return entityWithLabel
    }

    override fun onLoadFromJson(bundle: Map<String, String>): CourseTerminologyWithLabel? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: CourseTerminologyWithLabel? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, CourseTerminologyWithLabel.serializer(), entityJsonStr)
        }else {
            editEntity = CourseTerminologyWithLabel()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: CourseTerminologyWithLabel) {
        presenterScope.launch(doorMainDispatcher()) {

            if(entity.ctTitle == null){
                view.titleErrorText = systemImpl.getString(MessageID.field_required_prompt, this)
            }else{
                view.titleErrorText = null
            }

            if(entity.label?.ctTeacher == null){
                view.teacherErrorText =  systemImpl.getString(MessageID.field_required_prompt, this)
            }else{
                view.teacherErrorText = null
            }

            if(entity.label?.ctStudent == null){
                view.studentErrorText =  systemImpl.getString(MessageID.field_required_prompt, this)
            }else{
                view.studentErrorText = null
            }

            if(entity.label?.ctAddTeacher == null){
                view.addTeacherErrorText =  systemImpl.getString(MessageID.field_required_prompt, this)
            }else{
                view.addTeacherErrorText = null
            }

            if(entity.label?.ctAddStudent == null){
                view.addStudentErrorText =  systemImpl.getString(MessageID.field_required_prompt, this)
            }else{
                view.addStudentErrorText = null
            }

            val label = entity.label
            if(entityNotFilled(entity) || label == null){
                return@launch
            }

            entity.ctTerminology = safeStringify(di, CourseTerminologyLabel.serializer(), label)

            if(entity.ctUid == 0L) {
                entity.ctUid = repo.courseTerminologyDao.insertAsync(entity)
            }else {
                repo.courseTerminologyDao.updateAsync(entity)
            }

            finishWithResult(
                safeStringify(di,
                ListSerializer(CourseTerminology.serializer()), listOf(entity))
            )
        }
    }

    private fun entityNotFilled(entity: CourseTerminologyWithLabel): Boolean {
        return entity.ctTitle == null || entity.label?.ctTeacher == null ||
                entity.label?.ctTeacher == null || entity.label?.ctStudent == null ||
                entity.label?.ctAddTeacher == null || entity.label?.ctAddStudent == null
    }


    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}