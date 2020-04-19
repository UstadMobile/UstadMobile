package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.getCustomFieldValue
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*


class PersonDetailPresenter(context: Any,
                          arguments: Map<String, String>, view: PersonDetailView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadDetailPresenter<PersonDetailView, PersonWithDisplayDetails>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = TODO("PERSISTENCE_MODE.DB OR PERSISTENCE_MODE.JSON")

    private var person: Person? = null

    private var presenterFields: List<PresenterFieldRow>? = null

    private var displayPresenterFields = DoorMutableLiveData(listOf<PresenterFieldRow>())

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L


        //TODO: Set any additional fields (e.g. joinlist) on the view
        repo.personDao.findByUidLive(entityUid).observeWithPresenter(this,
                this::onPersonChanged)
        repo.personDetailPresenterFieldDao.findByPersonUidWithFieldAndValue(entityUid)
                .observeWithPresenter(this, this::onPresenterFieldsChanged)

    }

    fun onPersonChanged(person: Person?) {
        this.person = person
        onPresenterFieldsChanged(presenterFields)
    }

    fun onPresenterFieldsChanged(presenterFieldRows: List<PresenterFieldRow>?) {
        this.presenterFields = presenterFieldRows

        val displayFields = presenterFields?.map {
            if(it.customField == null
                    && it.presenterField?.fieldType == PersonDetailPresenterField.TYPE_FIELD) {
                val fieldAndValue = person?.getCustomFieldValue(it.presenterField?.fieldIndex ?: 0)
                PresenterFieldRow(it.presenterField, fieldAndValue?.first, fieldAndValue?.second)
            }else {
                it
            }
        }

        if(displayFields != null)
            displayPresenterFields.setVal(displayFields)
    }


    override fun onLoadLiveData(repo: UmAppDatabase): DoorLiveData<PersonWithDisplayDetails>? {
        return null
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonWithDisplayDetails? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L




        //TODO: Load the list for any one to many join helper here
        /* e.g.
         val person = withTimeoutOrNull {
             db.person.findByUid(entityUid)
         } ?: Person()
         return person
         */
        return TODO("Implement load from Database or return null if using PERSISTENCE_MODE.JSON")
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return true
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}