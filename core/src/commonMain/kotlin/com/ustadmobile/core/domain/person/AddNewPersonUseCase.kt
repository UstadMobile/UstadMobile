package com.ustadmobile.core.domain.person

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.isDateOfBirthAMinor
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.SystemPermission
import kotlinx.datetime.Instant

class AddNewPersonUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?
) {

    suspend operator fun invoke(
        person: Person,
        addedByPersonUid: Long = 0,
        createPersonParentApprovalIfMinor: Boolean = true,
        systemPermissions: Long = SystemPermission.PERSON_DEFAULT_PERMISSIONS,
        accountType: Int = PersonParentJoin.RELATIONSHIP_OTHER,
    ) : Long {
        val effectiveDb = (repo ?: db)
        return effectiveDb.withDoorTransactionAsync {
            val insertedUid = effectiveDb.personDao().insertAsync(person)
            val personUid = if(person.personUid  == 0L) insertedUid else person.personUid

            effectiveDb.systemPermissionDao().upsertAsync(
                SystemPermission(
                    spToPersonUid = personUid,
                    spPermissionsFlag = systemPermissions,
                )
            )

            if(
                Instant.fromEpochMilliseconds(person.dateOfBirth).isDateOfBirthAMinor()
                && createPersonParentApprovalIfMinor
            ) {
                effectiveDb.personParentJoinDao().upsertAsync(
                    PersonParentJoin(
                        ppjMinorPersonUid = personUid,
                        ppjParentPersonUid = addedByPersonUid,
                        ppjStatus = PersonParentJoin.STATUS_APPROVED,
                        ppjRelationship = accountType,
                        ppjApprovalTiemstamp = systemTimeInMillis(),
                    )
                )
            }

            personUid
        }
    }

}