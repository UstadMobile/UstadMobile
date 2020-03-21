package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.PersonDetailEnrollClazzView
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.ClazzWithEnrollment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * PersonDetailsEnrollClazz's Presenter - responsible for the logic of showing all classes a person
 * given is enrolled to and handle when any of those classes' enrollment changes (added or removed)
 */
class PersonDetailEnrollClazzPresenter(context: Any,
                                       arguments: Map<String, String>?, view:
                                       PersonDetailEnrollClazzView) :
        UstadBaseController<PersonDetailEnrollClazzView>(context, arguments!!, view) {

    private var currentPersonUid = -1L

    private var enrollmentFactory: DataSource.Factory<Int, ClazzWithEnrollment>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        //Get the person and set it to this Presenter
        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            currentPersonUid = (arguments!!.get(ARG_PERSON_UID)!!.toString()).toLong()
        }
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Populate classes
        val clazzDao = repository.clazzDao

        enrollmentFactory =
                clazzDao.findAllClazzesWithEnrollmentByPersonUid(currentPersonUid)

        setClazzProviderToView()
    }

    /**
     * Sets the class list with enrollment provider set to this presenter on the View
     */
    private fun setClazzProviderToView() {
        view.setClazzListProvider(enrollmentFactory!!)
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
        GlobalScope.launch {
            val result = clazzMemberDao.findByPersonUidAndClazzUidAsync(personUid, clazzUid)
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

                    //TODO: Also create an EntityRole

                }
                //else Don't create. false anyway
            }
        }
    }

}
