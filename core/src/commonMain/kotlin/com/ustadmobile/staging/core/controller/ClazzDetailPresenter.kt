package com.ustadmobile.core.controller


import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.PersonListSearchView.Companion.ARGUMENT_CURRNET_CLAZZ_UID
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.staging.core.view.ClazzDetailView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * The ClazzDetail Presenter - responsible for displaying the details of the Clazz who's detail we
 * want to see.
 * This is usually called first when we click on a Class from a list of Classes to get into it.
 */
class ClazzDetailPresenter(context: Any, arguments: Map<String, String>, view: ClazzDetailView,
                           val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<ClazzDetailView>(context, arguments, view) {

    //Any arguments stored as variables here
    private var currentClazzUid: Long = 0
    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    private val clazzDao = repository.clazzDao
    private lateinit var currentClazz: Clazz
    private val loggedInPersonUid: Long

    init {
        //Get Clazz Uid and set them.
        if (arguments.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments[ARG_CLAZZ_UID]?.toLong() ?:0
        }
        loggedInPersonUid = UmAccountManager.getActiveAccount(context)?.personUid?:0
    }

    fun checkPermissions() {

        val clazzLive = clazzDao.findByUidLive(currentClazzUid)
        clazzLive.observeWithPresenter(this, this::handleClazzChanged)
    }

    private fun handleClazzChanged(clazz: Clazz?){
        if(clazz != null) {
            currentClazz = clazz
            GlobalScope.launch {
                val settingsVisibility =
                        clazzDao.personHasPermissionWithClazz(loggedInPersonUid, currentClazz.clazzUid,
                                Role.PERMISSION_CLAZZ_UPDATE)
                val attendancePermission =
                        clazzDao.personHasPermissionWithClazz(loggedInPersonUid, currentClazz.clazzUid,
                                Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT)
                val selPermission  = clazzDao.personHasPermissionWithClazz(loggedInPersonUid,
                        currentClazz.clazzUid, Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT)
                val clazzActivityPermission =
                        clazzDao.personHasPermissionWithClazz(loggedInPersonUid, currentClazz.clazzUid,
                                Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT)
                val clazzAssignmentPermission =
                        clazzDao.personHasPermissionWithClazz(loggedInPersonUid, currentClazz.clazzUid,
                                Role.PERMISSION_CLAZZ_ASSIGNMENT_VIEW)


                view.runOnUiThread(Runnable {

                    view.setSettingsVisibility(settingsVisibility)

                    val tabs = mutableListOf<String>()
                    tabs.add(ClazzStudentListView.VIEW_NAME)
                    if(currentClazz.isAttendanceFeature() && attendancePermission){
                        tabs.add(ClassLogListView.VIEW_NAME)
                    }
                    if(currentClazz.isActivityFeature() && clazzActivityPermission){
                        tabs.add(ClazzActivityListView.VIEW_NAME)
                    }
                    if(currentClazz.isSelFeature() && selPermission){
                        tabs.add(SELAnswerListView.VIEW_NAME)
                    }
                    if(currentClazz.isAssignmentFeature() && clazzAssignmentPermission){
                        tabs.add(ClazzAssignmentListView.VIEW_NAME)
                    }
                    view.setupTabs(tabs)
                    view.setClazz(currentClazz)
                })
            }
        }
    }

    /**
     * Handles what happens when Class Edit is clicked. This takes the class to the edit page.
     */
    fun handleClickClazzEdit() {
        //Disabled until this is put into the new fragment system
//        val args = HashMap<String, String>()
//        args[ARG_CLAZZ_UID] = currentClazzUid.toString()
//        impl.go(ClazzEditView.VIEW_NAME, args, view.viewContext)
    }

    /**
     * Opens the search view for Clazz Members
     */
    fun handleClickSearch() {
        val args = HashMap<String, String>()
        args.put(ARGUMENT_CURRNET_CLAZZ_UID, currentClazzUid.toString())
        impl.go(PersonListSearchView.VIEW_NAME, args, context)
    }

}
