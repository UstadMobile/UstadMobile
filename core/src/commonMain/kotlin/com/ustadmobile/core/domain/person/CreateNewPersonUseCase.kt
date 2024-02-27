package com.ustadmobile.core.domain.person

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.SystemPermission

class CreateNewPersonUseCase(
    private val db: UmAppDatabase,
    private val repo: UmAppDatabase?
) {

    suspend operator fun invoke(person: Person) : Long {
        val effectiveDb = (repo ?: db)
        return effectiveDb.withDoorTransactionAsync {
            val personUid = effectiveDb.personDao.insertAsync(person)
            effectiveDb.systemPermissionDao.upsertAsync(
                SystemPermission(
                    spToPersonUid = personUid,
                    spPermissionsFlag = SystemPermission.PERSON_DEFAULT_PERMISSIONS,
                )
            )

            personUid
        }
    }

}