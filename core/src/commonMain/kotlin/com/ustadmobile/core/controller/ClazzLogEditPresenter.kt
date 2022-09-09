package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toLocalMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.core.view.ClazzLogEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class ClazzLogEditPresenter(context: Any,
        arguments: Map<String, String>, view: ClazzLogEditView,
        lifecycleOwner: LifecycleOwner,
        di: DI)
    : UstadEditPresenter<ClazzLogEditView, ClazzLog>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON



    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzLog? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ClazzLog? = null
        if(entityJsonStr != null) {
            editEntity =  safeParse(di, ClazzLog.serializer(), entityJsonStr)
        }else {
            editEntity = ClazzLog()
        }

        GlobalScope.launch(doorMainDispatcher()) {
            val timeZone = db.clazzDao.getClazzWithSchool(editEntity.clazzLogClazzUid)
                    .effectiveTimeZone()
            view.timeZone = timeZone
            val localMidnight = DateTime(editEntity.logDate).toLocalMidnight(timeZone).unixMillisLong
            view.date = localMidnight
            view.time = editEntity.logDate - localMidnight
        }


        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, ClazzLog.serializer(), entityVal)
    }

    override fun handleClickSave(entity: ClazzLog) {
        view.timeError = null
        view.dateError = null

        var hasError = false
        if(view.date == 0L){
            view.dateError = systemImpl.getString(MessageID.field_required_prompt, context)
            hasError = true
        }

        if(view.time == 0L) {
            view.timeError = systemImpl.getString(MessageID.field_required_prompt, context)
            hasError = true
        }

        if(hasError)
            return

        val presenter = this


        GlobalScope.launch(doorMainDispatcher()) {
            val effectiveTimeZone = db.clazzDao.getClazzWithSchool(entity.clazzLogClazzUid)
                    ?.effectiveTimeZone() ?: "UTC"

            entity.logDate = DateTime(view.date).toOffsetByTimezone(effectiveTimeZone)
                    .localMidnight.utc.unixMillisLong + view.time
            if(arguments[ARG_NEXT]?.startsWith(ClazzLogEditAttendanceView.VIEW_NAME) == true) {
                navigateForResult(
                    NavigateForResultOptions(
                        presenter, null,
                        ClazzLogEditAttendanceView.VIEW_NAME,
                        ClazzLog::class,
                        ClazzLog.serializer(),
                        arguments = mutableMapOf(ClazzLogEditAttendanceView.ARG_NEW_CLAZZLOG to
                                safeStringify(di, ClazzLog.serializer(), entity)))
                )
            }else {
                finishWithResult(safeStringify(di, ListSerializer(ClazzLog.serializer()), listOf(entity)))
            }
        }



    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}