package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SchoolDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SchoolEditView
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

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if(arguments.containsKey(SchoolEditView.ARG_SCHOOL_DETAIL_SCHOOL_UID)){
            GlobalScope.launch {
                val schoolUid = arguments[SchoolEditView.ARG_SCHOOL_DETAIL_SCHOOL_UID]!!.toLong()
                val school = schoolDao.findByUidAsync(schoolUid)
                if(school!= null) {
                    currentSchool = school
                    view.runOnUiThread(Runnable {
                        view.updateSchoolOnView(currentSchool)
                    })
                }
            }
        }else if(arguments.containsKey(SchoolEditView.ARG_SCHOOL_NEW)){
            GlobalScope.launch {

                currentSchool = School()
                currentSchool.schoolName = "Test School A"
                currentSchool.schoolUid = schoolDao.insertAsync(currentSchool)
                view.runOnUiThread(Runnable {
                    view.updateSchoolOnView(currentSchool)
                })
            }
        }
    }

    fun updateName(name: String){
        currentSchool.schoolName = name
    }

    fun handleClickSave(){
        GlobalScope.launch {
            schoolDao.updateAsync(currentSchool)
        }
    }

}
