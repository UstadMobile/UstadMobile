package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.db.dao.ClazzMemberDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.PersonDetailEnrollClazzView
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment



import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID

/**
 * PersonDetailsEnrollClazz's Presenter - responsible for the logic of showing all classes a person
 * given is enrolled to and handle when any of those classes' enrollment changes (added or removed)
 */
class PersonDetailEnrollClazzPresenter(context: Any,
                                       arguments: Map<String, String>?, view:
                                       PersonDetailEnrollClazzView) :
        UstadBaseController<PersonDetailEnrollClazzView>(context, arguments!!, view) {

    private var currentPersonUid = -1L

    private var clazzWithEnrollmentUmProvider: UmProvider<ClazzWithEnrollment>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        //Get the person and set it to this Presenter
        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            currentPersonUid = (arguments!!.get(ARG_PERSON_UID)!!.toString()).toLong()
        }
    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Populate classes
        val clazzDao = repository.clazzDao
        clazzWithEnrollmentUmProvider = clazzDao.findAllClazzesWithEnrollmentByPersonUid(currentPersonUid)

        setClazzProviderToView()

    }

    /**
     * Sets the class list with enrollment provider set to this presenter on the View
     */
    private fun setClazzProviderToView() {
        view.setClazzListProvider(clazzWithEnrollmentUmProvider!!)
    }

    /**
     * Handle done will close the view open (ie: go back to the edit page). All changes were in
     * real time and already persisted to the database.
     */
    fun handleClickDone() {
        view.finish()
    }

    /**
     * Blank method does nothing
     */
    fun handleClickClazz() {
        //does nothing
    }

    /**
     * Toggles the person being enrolled in the clazz.
     *
     * @param clazzUid      The clazz Uid
     * @param personUid     The person Uid
     */
    fun handleToggleClazzChecked(clazzUid: Long, personUid: Long, checked: Boolean) {
        val clazzMemberDao = repository.clazzMemberDao
        clazzMemberDao.findByPersonUidAndClazzUidAsync(personUid, clazzUid, object : UmCallback<ClazzMember> {
            override fun onSuccess(result: ClazzMember?) {
                if (result != null) {
                    result.clazzMemberActive = checked
                    clazzMemberDao.update(result)

                } else {
                    if (checked) {
                        //Create new
                        val newClazzMember = ClazzMember()
                        newClazzMember.clazzMemberClazzUid = clazzUid
                        newClazzMember.clazzMemberPersonUid = personUid
                        newClazzMember.clazzMemberActive = true
                        clazzMemberDao.insert(newClazzMember)
                    }
                    //else Don't create. false anyway

                }
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

}
