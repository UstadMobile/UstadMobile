package com.ustadmobile.core.util

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.ScopedGrantAndName
import kotlinx.serialization.builtins.ListSerializer

class ScopedGrantOneToManyHelper(editPresenter: UstadEditPresenter<*, *>)
    : DefaultOneToManyJoinEditHelper<ScopedGrantAndName>(
    pkGetter = {it.scopedGrant?.sgGroupUid ?: 0L},
    serializationKey = "ScopedGrantAndName",
    serializationStrategy = ListSerializer(ScopedGrantAndName.serializer()),
    deserializationStrategy = ListSerializer(ScopedGrantAndName.serializer()),
    editPresenter = editPresenter,
    entityClass = ScopedGrantAndName::class,
    pkSetter = {scopedGrant?.sgEntityUid = it}), ScopedGrantOneToManyListener {


    init {
        //Get the current back stack, then watch / observe for when the result comes back from the edit screen
        // e.g. what we would otherwise be doing in the Android fragment onViewCreated
    }

    suspend fun commitToDatabase(repo: UmAppDatabase, entityTableId: Int, entityUid: Long) {
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


    override fun onClickAddNewScopedGrant() {
        TODO("Show the multiplatform based dialog, then navigate accordingly")
    }

    override fun onClickEditScopedGrant(scopedGrantAndName: ScopedGrantAndName) {
        TODO("Not yet implemented")
    }

    override fun onClickDeleteScopedGrant(scopedGrantAndName: ScopedGrantAndName) {
        TODO("Not yet implemented")
    }
}