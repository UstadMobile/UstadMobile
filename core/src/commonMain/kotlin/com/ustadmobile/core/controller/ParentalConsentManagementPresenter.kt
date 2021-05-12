package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.formatDate
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ParentalConsentManagementView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher

import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinWithMinorPerson


class ParentalConsentManagementPresenter(context: Any,
                                         arguments: Map<String, String>, view: ParentalConsentManagementView,
                                         lifecycleOwner: DoorLifecycleOwner,
                                         di: DI)
    : UstadEditPresenter<ParentalConsentManagementView, PersonParentJoinWithMinorPerson>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        view.relationshipFieldOptions = listOf(
            MessageIdOption(MessageID.mother, context, PersonParentJoin.RELATIONSHIP_MOTHER),
            MessageIdOption(MessageID.father, context, PersonParentJoin.RELATIONSHIP_FATHER),
            MessageIdOption(MessageID.other, context, PersonParentJoin.RELATIONSHIP_OTHER)
        )
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonParentJoinWithMinorPerson? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val personParentJoin = db.onRepoWithFallbackToDb(5000) {
            it.personParentJoinDao.findByUidWithMinorAsync(entityUid)
        }  ?: throw IllegalArgumentException("Should go to error page")

        val minorPerson = personParentJoin?.minorPerson ?: throw IllegalArgumentException("Go to error page")
        view.siteTerms = db.siteTermsDao.findSiteTerms(systemImpl.getDisplayedLocale(context))

        if(personParentJoin.ppjParentPersonUid == 0L) {
            view.infoText = systemImpl.getString(MessageID.parent_consent_explanation, context)
                .replace("%1\$s", minorPerson.fullName())
                .replace("%2\$s", minorPerson.dateOfBirth.formatDate(context))
                .replace("%3\$s", systemImpl.getString(MessageID.app_name, context))
        }else if(db is DoorDatabaseRepository && personParentJoin.ppjParentPersonUid != 0L) {
            if(personParentJoin.ppjParentPersonUid == accountManager.activeAccount.personUid) {
                val messageId = if(personParentJoin.ppjStatus == PersonParentJoin.STATUS_APPROVED) {
                    MessageID.status_consent_granted
                }else {
                    MessageID.status_consent_denied
                }

                view.infoText = systemImpl.getString(messageId, context)
                    .replace("%1\$s", personParentJoin.ppjApprovalTiemstamp.formatDate(context))
            }else {
                throw IllegalStateException("Active user is not the parent!")
            }
        }

        return personParentJoin
    }

    override fun onLoadFromJson(bundle: Map<String, String>): PersonParentJoinWithMinorPerson? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: PersonParentJoinWithMinorPerson? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, PersonParentJoinWithMinorPerson.serializer(), entityJsonStr)
        }else {
            editEntity = PersonParentJoinWithMinorPerson()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: PersonParentJoinWithMinorPerson) {
        GlobalScope.launch(doorMainDispatcher()) {
            if(entity.ppjParentPersonUid == 0L)
                entity.ppjParentPersonUid = accountManager.activeAccount.personUid

            entity.ppjApprovalTiemstamp = systemTimeInMillis()
            repo.personParentJoinDao.updateAsync(entity)

            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}