package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppErrorCode
import com.ustadmobile.core.impl.ErrorCodeException
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.formatDate
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ParentalConsentManagementView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher

import kotlinx.coroutines.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import org.kodein.di.DI
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.CURRENT_DEST
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinWithMinorPerson
import com.ustadmobile.lib.db.entities.Role


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
            MessageIdOption(MessageID.other_legal_guardian, context, PersonParentJoin.RELATIONSHIP_OTHER)
        )
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonParentJoinWithMinorPerson? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val personParentJoin = db.onRepoWithFallbackToDb(5000) {
            it.personParentJoinDao.findByUidWithMinorAsync(entityUid)
        }

        if(personParentJoin == null && db !is DoorDatabaseRepository) {
            //Not available in the local db, just return and wait for repo load
            return null
        }else if(personParentJoin == null) {
            throw ErrorCodeException(AppErrorCode.ERROR_PARENT_JOIN_NOT_FOUND,
                "Child registration not found for $entityUid")
        }

        val minorPerson = personParentJoin.minorPerson

        if(minorPerson == null && db is DoorDatabaseRepository) {
            throw IllegalStateException("1022: Could not find minor for ppj $entityUid")
        }

        view.siteTerms = db.siteTermsDao.findSiteTerms(systemImpl.getDisplayedLocale(context))

        if(personParentJoin.ppjParentPersonUid == 0L) {
            view.infoText = systemImpl.getString(MessageID.parent_consent_explanation, context)
                .replace("%1\$s", minorPerson?.fullName() ?: "")
                .replace("%2\$s", minorPerson?.dateOfBirth?.formatDate(context) ?: "")
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
            view.relationshipFieldError = null

            if(entity.ppjRelationship == 0) {
                view.relationshipFieldError = systemImpl.getString(MessageID.field_required_prompt,
                    context)
                return@launch
            }

            if(entity.ppjParentPersonUid == 0L) {
                val activePersonGroupUid = repo.onRepoWithFallbackToDb(2000) {
                    it.personDao.findByUid(accountManager.activeSession?.userSession?.usPersonUid ?: 0L)
                        ?.personGroupUid
                } ?: throw IllegalStateException("Could not find person group uid!")

                entity.ppjParentPersonUid = accountManager.activeSession?.userSession?.usPersonUid
                    ?: throw IllegalStateException("No active session!")

                repo.grantScopedPermission(activePersonGroupUid,
                    Role.ROLE_PARENT_PERMISSIONS_DEFAULT, Person.TABLE_ID, entity.ppjMinorPersonUid)
            }

            entity.ppjApprovalTiemstamp = systemTimeInMillis()
            repo.personParentJoinDao.updateAsync(entity)

            if(arguments[UstadView.ARG_NEXT] == CURRENT_DEST) {
                //Where this screen was accessed from another directly (e.g. PersonDetail) then the
                // POPUPTO should simply by CURRENT_DEST, in which case we can just use popBack
                systemImpl.popBack(CURRENT_DEST, popUpInclusive = true, context)
            }else {
                //Where the screen was accessed using a link, then we need to go to the PersonList screen
                systemImpl.go(PersonListView.VIEW_NAME, mapOf(), context, UstadMobileSystemCommon.UstadGoOptions(
                    popUpToViewName = UstadView.ROOT_DEST,
                    popUpToInclusive = false
                ))
            }
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}