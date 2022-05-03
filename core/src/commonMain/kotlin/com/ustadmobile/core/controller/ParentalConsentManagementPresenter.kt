package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppErrorCode
import com.ustadmobile.core.impl.ErrorCodeException
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
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
import com.ustadmobile.lib.db.entities.*
import io.github.aakira.napier.Napier


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
            MessageIdOption(MessageID.mother, context, PersonParentJoin.RELATIONSHIP_MOTHER, di),
            MessageIdOption(MessageID.father, context, PersonParentJoin.RELATIONSHIP_FATHER, di),
            MessageIdOption(MessageID.other_legal_guardian, context, PersonParentJoin.RELATIONSHIP_OTHER, di)
        )
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): PersonParentJoinWithMinorPerson? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L




        var personParentJoin = db.onRepoWithFallbackToDb(5000) {
            it.personParentJoinDao.findByUidWithMinorAsync(entityUid)
        }

        if(personParentJoin == null && db is DoorDatabaseRepository) {
            //try fetching from the server
            try {
                personParentJoin  = db.personParentJoinDao.findByUidWithMinorAsyncFromWeb(entityUid)
                if(personParentJoin != null)
                    db.personParentJoinDao.insertAsync(personParentJoin)

                val minorPersonFromWeb = personParentJoin?.minorPerson
                val minorPerson = db.personDao.findByUidAsync(personParentJoin?.ppjMinorPersonUid ?: 0L)
                if(minorPerson == null && minorPersonFromWeb != null)
                    db.personDao.insertAsync(minorPersonFromWeb)

            }catch (e: Exception) {
                Napier.w("Could not load personparentjoin from web for $entityUid", e)
            }
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

    override fun onLoadFromJson(bundle: Map<String, String>): PersonParentJoinWithMinorPerson {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        return if(entityJsonStr != null) {
            safeParse(di, PersonParentJoinWithMinorPerson.serializer(), entityJsonStr)
        }else {
            PersonParentJoinWithMinorPerson()
        }
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    override fun handleClickSave(entity: PersonParentJoinWithMinorPerson) {
        presenterScope.launch(doorMainDispatcher()) {
            view.relationshipFieldError = null

            if(entity.ppjRelationship == 0) {
                view.relationshipFieldError = systemImpl.getString(MessageID.field_required_prompt,
                    context)
                return@launch
            }

            val activeSession = accountManager.activeSession
                ?: throw IllegalStateException("Could not find person group uid!")

            var classCheckRequired = false
            if(entity.ppjParentPersonUid == 0L) {
                entity.ppjParentPersonUid = activeSession.person.personUid

                repo.grantScopedPermission(activeSession.person,
                    Role.ROLE_PARENT_PERSON_PERMISSIONS_DEFAULT, Person.TABLE_ID,
                    entity.ppjMinorPersonUid)

                classCheckRequired = true
            }

            entity.ppjApprovalTiemstamp = systemTimeInMillis()
            repo.personParentJoinDao.updateAsync(entity)

            if(classCheckRequired) {
                //Enrol the parent into any classes that the minor has been enroled into
                val parentEnrolmentsRequired = repo.personParentJoinDao
                    .findByMinorPersonUidWhereParentNotEnrolledInClazz(
                        entity.ppjMinorPersonUid,0L)

                parentEnrolmentsRequired.forEach { parentEnrolmentRequired ->
                    repo.enrolPersonIntoClazzAtLocalTimezone(activeSession.person,
                        parentEnrolmentRequired.clazzUid, ClazzEnrolment.ROLE_PARENT)
                }
            }

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