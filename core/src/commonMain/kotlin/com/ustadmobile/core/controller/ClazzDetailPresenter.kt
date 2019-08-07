package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClassDetailView
import com.ustadmobile.core.view.ClazzEditView
import com.ustadmobile.core.view.PersonListSearchView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role



import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.core.view.PersonListSearchView.Companion.ARGUMENT_CURRNET_CLAZZ_UID


/**
 * The ClazzDetail Presenter - responsible for displaying the details of the Clazz who's detail we
 * want to see.
 * This is usually called first when we click on a Class from a list of Classes to get into it.
 */
class ClazzDetailPresenter(context: Any, arguments: Map<String, String>?, view: ClassDetailView,
                           val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<ClassDetailView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var currentClazzUid: Long = -1
    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    private val clazzDao = repository.clazzDao
    private var currentClazz: Clazz? = null
    private val loggedInPersonUid: Long?

    init {

        //Get Clazz Uid and set them.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            if (arguments!!.get(ARG_CLAZZ_UID) is String) {
                currentClazzUid = java.lang.Long.valueOf(arguments!!.get(ARG_CLAZZ_UID) as String)
            } else {
                currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)
            }
        }

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid
    }

    /**
     * In Order:
     * 1. Just set the title of the toolbar.
     *
     * @param savedState    The savedState
     */
    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Update toolbar title
        updateToolbarTitle()

        //Permission check
        checkPermissions()

    }

    fun checkPermissions() {

        clazzDao.findByUidAsync(currentClazzUid, object : UmCallback<Clazz> {
            override fun onSuccess(result: Clazz?) {
                currentClazz = result
                clazzDao.personHasPermission(loggedInPersonUid!!, currentClazzUid,
                        Role.PERMISSION_CLAZZ_UPDATE,
                        UmCallbackWithDefaultValue(false, object : UmCallback<Boolean> {
                            override fun onSuccess(result: Boolean?) {
                                view.setSettingsVisibility(result!!)
                                clazzDao.personHasPermission(loggedInPersonUid, currentClazzUid,
                                        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT, UmCallbackWithDefaultValue(false,
                                        object : UmCallback<Boolean> {
                                            override fun onSuccess(result: Boolean?) {
                                                view.setAttendanceVisibility(if (currentClazz!!.isAttendanceFeature) result else false)

                                                clazzDao.personHasPermission(loggedInPersonUid, currentClazzUid,
                                                        Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT, UmCallbackWithDefaultValue(false,
                                                        object : UmCallback<Boolean> {
                                                            override fun onSuccess(result: Boolean?) {
                                                                view.setSELVisibility(if (currentClazz!!.isSelFeature) result else false)
                                                                clazzDao.personHasPermission(loggedInPersonUid, currentClazzUid,
                                                                        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT, UmCallbackWithDefaultValue(false,
                                                                        object : UmCallback<Boolean> {
                                                                            override fun onSuccess(result: Boolean?) {
                                                                                view.setActivityVisibility(if (currentClazz!!.isActivityFeature) result else false)
                                                                                //Setup view pager after all permissions
                                                                                view.setupViewPager()
                                                                            }

                                                                            override fun onFailure(exception: Throwable?) {
                                                                                print(exception!!.message)
                                                                            }
                                                                        }))
                                                            }

                                                            override fun onFailure(exception: Throwable?) {
                                                                print(exception!!.message)
                                                            }
                                                        }))
                                            }

                                            override fun onFailure(exception: Throwable?) {
                                                print(exception!!.message)
                                            }
                                        }))
                            }

                            override fun onFailure(exception: Throwable?) {
                                print(exception!!.message)
                            }
                        }))
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })


    }

    /**
     * Updates the title of the Clazz after finding it from the database.
     */
    fun updateToolbarTitle() {

        clazzDao.findByUidAsync(currentClazzUid, object : UmCallback<Clazz> {
            override fun onSuccess(result: Clazz?) {
                view.setToolbarTitle(result!!.clazzName!!)
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

    /**
     * Handles what happens when Class Edit is clicked. This takes the class to the edit page.
     */
    fun handleClickClazzEdit() {
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzUid)
        impl.go(ClazzEditView.VIEW_NAME, args, view.getContext())
    }

    /**
     * Opens the search view for Clazz Members
     */
    fun handleClickSearch() {
        val args = Hashtable<String, Long>()
        args.put(ARGUMENT_CURRNET_CLAZZ_UID, currentClazzUid)
        impl.go(PersonListSearchView.VIEW_NAME, args, context)
    }

}
