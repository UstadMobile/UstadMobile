package com.ustadmobile.staging.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SchoolDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.staging.core.view.SchoolEditView
import com.ustadmobile.lib.db.entities.School
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 *  Presenter for MyWomenEntrepreneurs view
 **/
class SchoolEditPresenter(context: Any,
                          arguments: Map<String, String>?,
                          view: SchoolEditView,
                          val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                          repository: UmAppDatabase =
                                    UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<SchoolEditView>(context, arguments!!, view) {


    private var schoolDao: SchoolDao = repository.schoolDao

    private lateinit var factory: DataSource.Factory<Int, School>

    private lateinit var currentSchool : School

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        GlobalScope.launch {
            val schoolUid = arguments[SchoolEditView.ARG_SCHOOL_DETAIL_SCHOOL_UID]?.toLong() ?: 0
            val school = if(schoolUid != 0L) schoolDao.findByUidAsync(schoolUid) else School()
            if(school!= null) {
                currentSchool = school
                view.runOnUiThread(Runnable {
                    view.setSchool(currentSchool)
                })
            }
        }

    }

    fun handleClickSave(school: School){
        GlobalScope.launch {
            if(school.schoolUid != 0L){
                schoolDao.insertAsync(school)
            }else{
                schoolDao.updateAsync(school)
            }

        }
    }

}
