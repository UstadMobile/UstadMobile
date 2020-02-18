package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SchoolDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SchoolDetailView
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.core.view.SchoolEditView.Companion.ARG_SCHOOL_DETAIL_SCHOOL_UID
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.School.Companion.SCHOOL_FEATURE_ATTENDANCE
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 *  Presenter for SchoolDetail view
 **/
class SchoolDetailPresenter(context: Any,
                            arguments: Map<String, String>?,
                            view: SchoolDetailView,
                            val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                            val repository: UmAppDatabase =
                                    UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<SchoolDetailView>(context, arguments!!, view) {

    private var schoolDao: SchoolDao = repository.schoolDao
    private lateinit var currentSchool : School
    private lateinit var factory: DataSource.Factory<Int, School>

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if(arguments.containsKey(ARG_SCHOOL_DETAIL_SCHOOL_UID)){
            GlobalScope.launch {
                val school =
                        schoolDao.findByUidAsync(arguments[ARG_SCHOOL_DETAIL_SCHOOL_UID]!!.toLong())
                if (school != null) {
                    currentSchool = school
                    view.runOnUiThread(Runnable {
                        view.updateSchoolOnView(school)
                        view.setAttendanceVisibility(
                                school.schoolFeatures and SCHOOL_FEATURE_ATTENDANCE > 0 )
                        //Only set up the view pager after we get the school object
                        view.setupViewPager()
                    })
                }
            }
        }
    }

    /**
     * Handle what happens when the gear edit button is clicked - should go to schooledit page
     */
    fun handleClickSchoolEdit(){
        val args = HashMap<String, String>()
        args[ARG_SCHOOL_DETAIL_SCHOOL_UID] = currentSchool.schoolUid.toString()
        impl.go(SchoolEditView.VIEW_NAME, args, context)
    }

}
