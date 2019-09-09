package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.PersonListSearchView
import com.ustadmobile.core.view.PersonListSearchView.Companion.ARGUMENT_CURRNET_CLAZZ_UID
import com.ustadmobile.lib.db.entities.PersonWithEnrollment

class PersonListSearchPresenter(context: Any, arguments: Map<String, String>?, view:
PersonListSearchView) : CommonHandlerPresenter<PersonListSearchView>(context, arguments!!, view) {

    //Provider
    private var personWithEnrollmentUmProvider: DataSource.Factory<Int, PersonWithEnrollment>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    private var currentClazzUid: Long = 0

    init {

        if (arguments!!.containsKey(ARGUMENT_CURRNET_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARGUMENT_CURRNET_CLAZZ_UID)!!.toLong()
        }

    }

    /**
     * In order:
     * 1. Gets all people via the database as UmProvider and sets it to the view.
     *
     * @param savedState The saved state.
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        personWithEnrollmentUmProvider = repository.clazzMemberDao
                .findAllPersonWithEnrollmentInClazzByClazzUidWithSearchFilter(currentClazzUid,
                        0f, 1f, "%")
        setPeopleProviderToView()

    }

    fun updateFilter(apl: Float, aph: Float, value: String) {
        val stringQuery = "%$value%"
        personWithEnrollmentUmProvider = repository.clazzMemberDao
                .findAllPersonWithEnrollmentInClazzByClazzUidWithSearchFilter(currentClazzUid,
                        apl, aph, stringQuery)
        setPeopleProviderToView()
    }

    /**
     * Sets the people list provider set in the Presenter to the View.
     */
    private fun setPeopleProviderToView() {
        view.setPeopleListProvider(personWithEnrollmentUmProvider!!)
    }

    override fun handleCommonPressed(arg: Any, arg2:Any) {

    }

    override fun handleSecondaryPressed(arg: Any) {

    }
}
