package com.ustadmobile.core.util

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_GO_TO_COMPLETE
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.ScopedGrantAndName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer

/**
 * @param editPresenter The editpresenter that this helper is working for - used to listen for json
 * saved and restore events.
 * @param tableId the table id for which scopedgrants are being created for. This is passed to
 * ScopedGrantEdit to determine which permissions are displayed
 */
class ScopedGrantOneToManyHelper(
    val repo: UmAppDatabase,
    val editPresenter: UstadEditPresenter<*, *>,
    val savedStateHandle: UstadSavedStateHandle,
    val entityTableId: Int
) : DefaultOneToManyJoinEditHelper<ScopedGrantAndName>(
    pkGetter = {it.scopedGrant?.sgUid ?: 0L},
    serializationKey = "ScopedGrantAndName",
    serializationStrategy = ListSerializer(ScopedGrantAndName.serializer()),
    deserializationStrategy = ListSerializer(ScopedGrantAndName.serializer()),
    editPresenter = editPresenter,
    di = editPresenter.di,
    entityClass = ScopedGrantAndName::class,
    pkSetter = {scopedGrant?.sgUid = it}
), OneToManyJoinEditListener<ScopedGrantAndName> {

    val ScopedGrant.defaultNameByFlag: String?
        get() = when{
            sgFlags.hasFlag(ScopedGrant.FLAG_TEACHER_GROUP) -> "Teachers"
            sgFlags.hasFlag(ScopedGrant.FLAG_STUDENT_GROUP) -> "Students"
            else -> null
        }

    init {
        //Get the current back stack, then watch / observe for when the result comes back from the edit screen
        // e.g. what we would otherwise be doing in the Android fragment onViewCreated

        editPresenter.observeSavedStateResult(SAVEDSTATE_KEY_SCOPEDGRANT_RESULTS,
            ListSerializer(ScopedGrant.serializer()), ScopedGrant::class) {

            val newValue = it.firstOrNull() ?: return@observeSavedStateResult

            GlobalScope.launch(doorMainDispatcher()) {
                val groupName = repo.personGroupDao.findNameByGroupUid(newValue.sgGroupUid)
                onEditResult(ScopedGrantAndName().apply {
                    scopedGrant = newValue
                    name = groupName ?: scopedGrant?.defaultNameByFlag
                })

                savedStateHandle[SAVEDSTATE_KEY_SCOPEDGRANT_RESULTS] = null
            }
        }
    }

    /**
     * Commit the ScopedGrants to the database
     *
     * @param repo DatabaseRepository to save to
     * @param entityUid the ID that these scoped grants are related to e.g. the class uid or school uid
     * @param flagToGroupMap a map of scopedGrant flags to group uids. When a class or school etc. is
     * first created, the groups for teachers, students etc are not created yet. This map should be
     * provided to map the ScopedGrant flag (e.g. FLAG_TEACHER_GROUP ) to the actual group uid for
     * the related PersonGroup
     */
    suspend fun commitToDatabase(repo: UmAppDatabase, entityUid: Long,
        flagToGroupMap: Map<Int, Long> = mapOf()) {

        val entitiesToInsertVal = entitiesToInsert

        entitiesToInsertVal.forEach { grant ->
            grant.scopedGrant?.apply {
                if(sgGroupUid == 0L)
                    //Set the group uid to the first one that matches in flagToGroupMap
                    sgGroupUid = flagToGroupMap.entries.firstOrNull { sgFlags.hasFlag(it.key) }?.value ?: 0L
            }

            val groupToAssign: Long = flagToGroupMap[grant.scopedGrant?.sgFlags ?: 0] ?: -1
            grant.scopedGrant?.takeIf { it.sgGroupUid == 0L && groupToAssign != -1L  }?.sgGroupUid = groupToAssign

            //Set the primary key of the new ScopedGrant to zero so it can insert as expected
            grant.scopedGrant?.sgUid = 0

            grant.scopedGrant?.apply {
                sgTableId = entityTableId
                sgEntityUid = entityUid
            }
        }

        repo.scopedGrantDao.insertListAsync(entitiesToInsertVal.mapNotNull { it.scopedGrant })

        repo.scopedGrantDao.updateListAsync(entitiesToUpdate.mapNotNull { it.scopedGrant })
    }


    override fun onClickNew() {
        editPresenter.saveStateToNavController()

        val args = mutableMapOf(
            ScopedGrantEditView.ARG_PERMISSION_LIST to entityTableId.toString(),
            ARG_GO_TO_COMPLETE to ScopedGrantEditView.VIEW_NAME,
            UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString())

        editPresenter.navigateForResult(NavigateForResultOptions(
            fromPresenter = editPresenter,
            currentEntityValue = null,
            destinationViewName = PersonListView.VIEW_NAME,
            entityClass = ScopedGrant::class,
            serializationStrategy = ScopedGrant.serializer(),
            destinationResultKey = SAVEDSTATE_KEY_SCOPEDGRANT_RESULTS,
            arguments = args))

        //TODO("Show the multiplatform based dialog, then navigate accordingly")
    }

    override fun onClickEdit(joinedEntity: ScopedGrantAndName) {
        val args = mutableMapOf(
            ScopedGrantEditView.ARG_PERMISSION_LIST to entityTableId.toString())

        editPresenter.navigateForResult(
            NavigateForResultOptions(
            fromPresenter = editPresenter,
            currentEntityValue = joinedEntity.scopedGrant,
            destinationViewName = ScopedGrantEditView.VIEW_NAME,
            entityClass = ScopedGrant::class,
            serializationStrategy = ScopedGrant.serializer(),
            destinationResultKey = SAVEDSTATE_KEY_SCOPEDGRANT_RESULTS,
            arguments = args
        ))
    }

    override fun onClickDelete(joinedEntity: ScopedGrantAndName) {
        onDeactivateEntity(joinedEntity)
    }

    companion object {
        const val SAVEDSTATE_KEY_SCOPEDGRANT_RESULTS = "ScopedGrant_result"
    }
}