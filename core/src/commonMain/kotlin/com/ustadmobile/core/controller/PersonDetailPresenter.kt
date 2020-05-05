package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.TYPE_FIELD


class PersonDetailPresenter(context: Any,
                          arguments: Map<String, String>, view: PersonDetailView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadDetailPresenter<PersonDetailView, PersonWithDisplayDetails>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private var person: Person? = null

    private var personPicture: PersonPicture? = null

    private var presenterFields: List<PresenterFieldRow>? = null

    private var displayPresenterFields = DoorMutableLiveData(listOf<PresenterFieldRow>())

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L


        view.presenterFieldRows = displayPresenterFields
        //TODO: Set any additional fields (e.g. joinlist) on the view
        repo.personDao.findByUidLive(entityUid).observeWithLifecycleOwner(lifecycleOwner,
                this::onPersonChanged)
        repo.personDetailPresenterFieldDao.findByPersonUidWithFieldAndValue(entityUid)
                .observeWithLifecycleOwner(lifecycleOwner, this::onPresenterFieldsChanged)
        repo.personPictureDao.findByPersonUidLive(entityUid).observeWithLifecycleOwner(
                lifecycleOwner, this::onPersonPictureChanged)
    }

    fun onPersonChanged(person: Person?) {
        this.person = person
        onPersonOrFieldsChanged(person, personPicture, presenterFields)
    }

    fun onPersonPictureChanged(personPicture: PersonPicture?) {
        this.personPicture = personPicture
        onPersonOrFieldsChanged(person, personPicture, presenterFields)
    }

    fun onPresenterFieldsChanged(presenterFieldRows: List<PresenterFieldQueryRow>?) {
        this.presenterFields = presenterFieldRows?.toPresenterFieldRows()
        onPersonOrFieldsChanged(person, personPicture, presenterFields)
    }

    fun onPersonOrFieldsChanged(person: Person?, personPicture: PersonPicture?, presenterFieldRows: List<PresenterFieldRow>?) {
        //combine them and send them to the view here
        if(person != null && presenterFieldRows != null) {
            person.populatePresenterFields(presenterFieldRows)
            personPicture.populatePresenterFields(presenterFieldRows, repo.personPictureDao)

            //Remove fields that have no value to display
            val displayPresenterFieldRows = presenterFieldRows.filter {
                !(it.presenterField?.fieldType == TYPE_FIELD &&
                        (it.customFieldValue?.customFieldValueValue == null || it.customFieldValue?.customFieldValueValue?.trim() == "") &&
                        it.customFieldValue?.customFieldValueCustomFieldValueOptionUid == 0L)
            }

            displayPresenterFields.setVal(displayPresenterFieldRows)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonWithDisplayDetails? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        //Dummy value - not really used
        return PersonWithDisplayDetails()
    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return true
    }

    override fun handleClickEdit() {
        val personUid = person?.personUid ?: return
        systemImpl.go(PersonEditView.VIEW_NAME, mapOf(ARG_ENTITY_UID to personUid.toString()),
            context)
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}