package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.view.CourseGroupSetDetailView
import com.ustadmobile.core.view.CourseGroupSetEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DI


class CourseGroupSetDetailPresenter(context: Any,
                                    arguments: Map<String, String>, view: CourseGroupSetDetailView,
                                    lifecycleOwner: LifecycleOwner,
                                    di: DI)
    : UstadDetailPresenter<CourseGroupSetDetailView, CourseGroupSet>(context, arguments, view, di, lifecycleOwner) {

    private var clazzUid: Long = 0L
    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        clazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L
        return repo.clazzDao.personHasPermissionWithClazz(
            account?.personUid ?: 0L, clazzUid,
            Role.PERMISSION_CLAZZ_UPDATE)
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): CourseGroupSet? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val entity = db.courseGroupSetDao.findByUidAsync(entityUid)
        val groupMemberList = db.courseGroupMemberDao.findByGroupSetOrderedAsync(
            entityUid, entity?.cgsClazzUid ?: 0L)

        val groupMap = groupMemberList.groupBy { it.member?.cgmGroupNumber ?: 0 }
        val memberList = mutableListOf<CourseGroupMemberPerson>()
        groupMap.entries.forEach {
            memberList.add(CourseGroupMemberPerson().apply {
                member = CourseGroupMember().apply {
                    cgmGroupNumber = it.key
                }
            })
            memberList.addAll(it.value)
        }

        view.memberList = memberList

        return entity
    }

    override fun handleClickEdit() {
        navigateForResult(
            NavigateForResultOptions(
                this, null,
                CourseGroupSetEditView.VIEW_NAME,
                CourseGroupSet::class,
                CourseGroupSet.serializer(),
                ReportDetailPresenter.RESULT_REPORT_KEY,
                arguments = mutableMapOf(
                    ARG_ENTITY_UID to entity?.cgsUid.toString(),
                    ARG_CLAZZUID to entity?.cgsClazzUid.toString()
                )
            )
        )
    }


    companion object {

    }

}