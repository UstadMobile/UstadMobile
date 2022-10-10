package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.SchoolDetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class SchoolDetailPresenter(context: Any,
                            arguments: Map<String, String>, view: SchoolDetailView,
                            di: DI,
                            lifecycleOwner: LifecycleOwner)
    : UstadDetailPresenter<SchoolDetailView, School>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): School? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val school =  withTimeoutOrNull(2000) {
            db.schoolDao.findByUidAsync(entityUid)
        } ?: School()

        view.title = school.schoolName?:""
        return school
    }

    //This has no effect because the button is controlled by the overview presenter
    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false
    }

}