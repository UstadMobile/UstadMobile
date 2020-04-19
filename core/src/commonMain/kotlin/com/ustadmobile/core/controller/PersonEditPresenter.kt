package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.client.features.json.defaultSerializer
import io.ktor.http.content.TextContent
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField.Companion.TYPE_FIELD
import com.ustadmobile.lib.db.entities.PresenterFieldRow


class PersonEditPresenter(context: Any,
                          arguments: Map<String, String>, view: PersonEditView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<PersonEditView, Person>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val presenterFieldRows = DoorMutableLiveData(listOf<PresenterFieldRow>())

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.presenterFieldRows = presenterFieldRows

        //TODO: Set any additional fields (e.g. joinlist) on the view
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): Person? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val person = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.personDao?.findByUid(entityUid)
        } ?: Person()

        val dbPresenterFieldRows = db.personDetailPresenterFieldDao
                .findByPersonUidWithFieldAndValueAsList(entityUid)
        presenterFieldRows.sendValue(person.asPresenterFieldList(dbPresenterFieldRows))

        return person
    }

    override fun onLoadFromJson(bundle: Map<String, String>): Person? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: PersonWithDisplayDetails? = null
        if(entityJsonStr != null) {
            editEntity = Json.parse(PersonWithDisplayDetails.serializer(), entityJsonStr)
        }else {
            editEntity = PersonWithDisplayDetails()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: Person) {
        val fieldList = presenterFieldRows.getValue() ?: return
        entity.updateFromFieldList(fieldList)

        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.personUid == 0L) {
                entity.personUid = repo.personDao.insertAsync(entity)
            }else {
                repo.personDao.updateAsync(entity)
            }

            val customFieldValuesParted = fieldList.filter { it.presenterField?.isCustomField() ?: false  && it.customFieldValue != null}
                    .map { it.customFieldValue!! }
                    .partition { it.customFieldValueFieldUid == 0L }
            repo.customFieldValueDao.insertListAsync(customFieldValuesParted.first)
            repo.customFieldValueDao.updateListAsync(customFieldValuesParted.second)

            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}