package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toLocalMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.TextCourseBlockEditView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.lib.db.entities.ClazzWithSchool
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

class TextCourseBlockEditPresenter(context: Any, args: Map<String, String>, view: TextCourseBlockEditView,
                                     di: DI, lifecycleOwner: LifecycleOwner)
    : UstadEditPresenter<TextCourseBlockEditView, CourseBlock>(context, args, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): CourseBlock {
        val entityUid = arguments[UstadView.ARG_ENTITY_UID]?.toLongOrNull() ?: 0L
        val clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLongOrNull() ?: 0L

        val entity = db.onRepoWithFallbackToDb(2000) {
            it.takeIf { entityUid != 0L }?.courseBlockDao?.findByUidAsync(entityUid)
        }?: CourseBlock().apply {
            cbUid = db.doorPrimaryKeyManager.nextIdAsync(CourseBlock.TABLE_ID)
            cbClazzUid = clazzUid
            cbType = CourseBlock.BLOCK_TEXT_TYPE
            cbEntityUid = cbUid
        }

        val clazzWithSchool = db.onRepoWithFallbackToDb(2000) {
            it.clazzDao.getClazzWithSchool(entity.cbClazzUid)
        } ?: ClazzWithSchool()

        val timeZone = clazzWithSchool.effectiveTimeZone()
        view.timeZone = timeZone

        if(entity.cbHideUntilDate != 0L){
            val startDateTimeMidnight = DateTime(entity.cbHideUntilDate)
                .toLocalMidnight(timeZone).unixMillisLong
            view.startDate = startDateTimeMidnight
            view.startTime = entity.cbHideUntilDate - startDateTimeMidnight
        }else{
            view.startDate = 0L
        }


        return entity
    }

    override fun onLoadFromJson(bundle: Map<String, String>): CourseBlock? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[UstadEditView.ARG_ENTITY_JSON]

        val entity =  if(entityJsonStr != null) {
            safeParse(di, CourseBlock.serializer(), entityJsonStr)
        }else {
            CourseBlock()
        }
        presenterScope.launch {
            val caClazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: entity.cbClazzUid
            val clazzWithSchool = db.onRepoWithFallbackToDb(2000) {
                it.clazzDao.getClazzWithSchool(caClazzUid)
            } ?: ClazzWithSchool()

            val timeZone = clazzWithSchool.effectiveTimeZone()
            view.timeZone = timeZone

            if(entity.cbHideUntilDate != 0L){
                val startDateTimeMidnight = DateTime(entity.cbHideUntilDate)
                    .toLocalMidnight(timeZone).unixMillisLong
                view.startDate = startDateTimeMidnight
                view.startTime = entity.cbHideUntilDate - startDateTimeMidnight
            }else{
                view.startDate = 0L
            }
        }

        return entity
    }

    override fun handleClickSave(entity: CourseBlock) {
        //Remove any previous error messages
        view.fieldsEnabled = false
        presenterScope.launch {
            if(entity.cbTitle.isNullOrEmpty()){
                view.blockTitleError = systemImpl.getString(MessageID.field_required_prompt, context)
                view.fieldsEnabled = true
                return@launch
            }

            val timeZone = view.timeZone ?: "UTC"
            entity.cbHideUntilDate = DateTime(view.startDate).toOffsetByTimezone(timeZone)
                .localMidnight.utc.unixMillisLong + view.startTime

            finishWithResult(safeStringify(di,
                ListSerializer(CourseBlock.serializer()),
                listOf(entity)))

            view.loading = false
        }

    }
}