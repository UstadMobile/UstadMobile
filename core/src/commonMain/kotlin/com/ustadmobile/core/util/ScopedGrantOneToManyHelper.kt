package com.ustadmobile.core.util

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_GO_TO_COMPLETE
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.ScopedGrantAndName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

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
    val entityTableId: Int)
    : DefaultOneToManyJoinEditHelper<ScopedGrantAndName>(
    pkGetter = {it.scopedGrant?.sgUid ?: 0L},
    serializationKey = "ScopedGrantAndName",
    serializationStrategy = ListSerializer(ScopedGrantAndName.serializer()),
    deserializationStrategy = ListSerializer(ScopedGrantAndName.serializer()),
    editPresenter = editPresenter,
    entityClass = ScopedGrantAndName::class,
    pkSetter = {scopedGrant?.sgUid = it}), OneToManyJoinEditListener<ScopedGrantAndName> {


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
                    name = groupName
                })

                savedStateHandle[SAVEDSTATE_KEY_SCOPEDGRANT_RESULTS] = null
            }
        }
    }

    suspend fun commitToDatabase(repo: UmAppDatabase, entityUid: Long) {
        //function to set the table and entity uid on scopedgrants
        val scopedGrantForeignKeyFn : (ScopedGrant) -> Unit = {
            it.sgTableId = entityTableId
            it.sgEntityUid = entityUid

            //TODO here: replace special values for teacher/student groups to use correct persongroupuid
        }

        repo.scopedGrantDao.insertListAsync(
            entitiesToInsert.mapNotNull {
                it.scopedGrant?.also(scopedGrantForeignKeyFn)
            })

        repo.scopedGrantDao.updateListAsync(
            entitiesToUpdate.mapNotNull {
                it.scopedGrant?.also(scopedGrantForeignKeyFn)
            })
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