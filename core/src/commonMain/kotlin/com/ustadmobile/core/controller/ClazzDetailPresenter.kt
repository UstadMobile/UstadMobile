package com.ustadmobile.core.controller


import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClazzEditView
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.PersonListSearchView
import com.ustadmobile.core.view.PersonListSearchView.Companion.ARGUMENT_CURRNET_CLAZZ_UID
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * The ClazzDetail Presenter - responsible for displaying the details of the Clazz who's detail we
 * want to see.
 * This is usually called first when we click on a Class from a list of Classes to get into it.
 */
class ClazzDetailPresenter(context: Any, arguments: Map<String, String>?, view: ClazzDetailView,
                           val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<ClazzDetailView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var currentClazzUid: Long = -1
    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    private val clazzDao = repository.clazzDao
    private var currentClazz: Clazz? = null
    private val loggedInPersonUid: Long?

    init {

        //Get Clazz Uid and set them.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments.get(ARG_CLAZZ_UID)!!.toLong()
        }

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid
    }

    /**
     * In Order:
     * 1. Just set the title of the toolbar.
     *
     * @param savedState    The savedState
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Update view and Permission check
        checkPermissions()

    }

    fun checkPermissions() {

        GlobalScope.launch {
            val result = clazzDao.findByUidAsync(currentClazzUid)
            view.setToolbarTitle(result!!.clazzName!!)

            currentClazz = result

            val result2 = clazzDao.personHasPermission(loggedInPersonUid!!, currentClazzUid,
                    Role.PERMISSION_CLAZZ_UPDATE)
            view.setSettingsVisibility(result2!!)
            val result3 = clazzDao.personHasPermission(loggedInPersonUid, currentClazzUid,
                    Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT)
            view.setAttendanceVisibility(if (currentClazz!!.isAttendanceFeature) result3 else false)

            val result4 = clazzDao.personHasPermission(loggedInPersonUid, currentClazzUid,Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT)
            view.setSELVisibility(if (currentClazz!!.isSelFeature) result4 else false)
            val result5 = clazzDao.personHasPermission(loggedInPersonUid, currentClazzUid,
                    Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT)
            view.setActivityVisibility(if (currentClazz!!.isActivityFeature) result5 else false)
            //Setup view pager after all permissions
            view.setupViewPager()

        }

    }


    /**
     * Handles what happens when Class Edit is clicked. This takes the class to the edit page.
     */
    fun handleClickClazzEdit() {
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
        impl.go(ClazzEditView.VIEW_NAME, args, view.viewContext)
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
