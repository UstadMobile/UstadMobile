package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.CourseBlockEditView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlinx.coroutines.launch
import org.kodein.di.DI

class CourseBlockEditPresenter(context: Any, args: Map<String, String>, view: CourseBlockEditView,
                            di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<CourseBlockEditView, CourseBlock>(context, args, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): CourseBlock {
        val entityUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L

        return db.onRepoWithFallbackToDb(2000) {
            it.takeIf { entityUid != 0L }?.courseBlockDao?.findByUidAsync(entityUid)
        }?: CourseBlock()
    }

    override fun onLoadFromJson(bundle: Map<String, String>): CourseBlock? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[UstadEditView.ARG_ENTITY_JSON]

        return if(entityJsonStr != null) {
            safeParse(di, CourseBlock.serializer(), entityJsonStr)
        }else {
            CourseBlock()
        }
    }

    override fun handleClickSave(entity: CourseBlock) {
        //Remove any previous error messages
        presenterScope.launch {
            if(entity.cbTitle.isNullOrEmpty()){
                view.blockTitleError = systemImpl.getString(MessageID.field_required_prompt, context)
                return@launch
            }

            if(entity.cbUid == 0L) {
                entity.cbUid = repo.courseBlockDao.insertAsync(entity)
            }else {
                repo.courseBlockDao.updateAsync(entity)
            }

            view.finishWithResult(listOf(entity))
        }

    }
}