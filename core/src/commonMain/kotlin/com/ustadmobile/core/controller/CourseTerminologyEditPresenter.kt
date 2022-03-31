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
import com.ustadmobile.lib.db.entities.TerminologyEntry
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance
import kotlin.jvm.JvmStatic


class CourseTerminologyEditPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: CourseTerminologyEditView,
    lifecycleOwner: DoorLifecycleOwner,
    di: DI)
    : UstadEditPresenter<CourseTerminologyEditView, CourseTerminology>(
    context,
    arguments,
    view,
    di,
    lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val json: Json by instance()

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): CourseTerminology? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val entity =  db.onRepoWithFallbackToDb(2000) {
            it.takeIf { entityUid != 0L }?.courseTerminologyDao?.findByUidAsync(entityUid)
        } ?: CourseTerminology()

        makeTermList(entity)

        return entity
    }

    private fun makeTermList(terminology: CourseTerminology){
        val termMap: Map<String,String> = terminology.ctTerminology?.let {
            json.decodeFromString(
                MapSerializer(String.serializer(), String.serializer()), it)
        } ?: mapOf()

        val termList = TERMINOLOGY_ENTRY_MESSAGE_ID.entries.map {
            TerminologyEntry(it.key,it.value, termMap[it.key])
        }.sortedBy { it.id }

        view.terminologyTermList = termList
    }

    override fun onLoadFromJson(bundle: Map<String, String>): CourseTerminology? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val editEntity: CourseTerminology = if(entityJsonStr != null) {
            safeParse(di, CourseTerminology.serializer(), entityJsonStr)
        }else {
            CourseTerminology()
        }

        makeTermList(editEntity)


        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        entityVal?.ctTerminology = json.encodeToString(
            MapSerializer(String.serializer(), String.serializer()),
            view.terminologyTermList?.associate { it.id to it.term.toString() } ?: mapOf()
        )
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: CourseTerminology) {
        presenterScope.launch(doorMainDispatcher()) {

            val termList = view.terminologyTermList ?: listOf()

            var foundError = false
            if(entity.ctTitle == null){
                foundError = true
                view.titleErrorText = systemImpl.getString(MessageID.field_required_prompt, context)
            }else{
                view.titleErrorText = null
            }


            termList.forEach {
                 it.errorMessage = if(it.term.isNullOrEmpty()) {
                     foundError = true
                     systemImpl.getString(MessageID.field_required_prompt, context)
                 } else {
                     null
                 }
            }


            if(foundError){
                view.terminologyTermList = termList.toList()
                return@launch
            }

            entity.ctTerminology = json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                termList.associate { it.id to it.term.toString() }
            )

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



    companion object {

        @JvmStatic
        val TERMINOLOGY_ENTRY_MESSAGE_ID = mapOf(
            "Teacher" to MessageID.teacher,
            "Student" to MessageID.student,
            "Teachers" to MessageID.teachers,
            "Students" to MessageID.students,
            "AddTeacher" to MessageID.add_a_teacher,
            "AddStudent" to MessageID.add_a_student
        )

    }

}