package com.ustadmobile.core.domain.account

import com.benasher44.uuid.uuid4
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.util.ext.ifNullOrBlank
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.TYPE_NORMAL_PERSON
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.util.randomString
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

/**
 * Creates a new local account - see ARCHITECTURE.md for more information on local accounts. This
 * will generate a new local endpoint ( domain ending with .local ).
 */
class CreateNewLocalAccountUseCase(private val di: DI) {

    data class NewLocalAccountResult(
        val endpoint: Endpoint,
        val person: Person,
    )

    suspend operator fun invoke(
        person: Person
    ): NewLocalAccountResult {
        val localEndpoint = Endpoint("http://${uuid4()}.local/")
        val dataLayer: UmAppDataLayer = di.on(localEndpoint).direct.instance()
        val addNewPersonUseCase: AddNewPersonUseCase = di.on(localEndpoint).direct.instance()

        return dataLayer.localDb.withDoorTransactionAsync {
            val newSite = Site().apply {
                siteName = "Local Site"
                authSalt = "local_${randomString(10)}"
            }
            dataLayer.localDb.siteDao().insertAsync(newSite)

            val newPerson = person.copy(
                username = person.username ?: "localuser",
                firstNames = person.firstNames?.ifNullOrBlank { "Local" },
                lastName = person.lastName?.ifNullOrBlank { "User" },
                personType = TYPE_NORMAL_PERSON
            )

            val personUid = addNewPersonUseCase(
                person = newPerson,
                systemPermissions = PermissionFlags.ALL
            )

            NewLocalAccountResult(
                endpoint = localEndpoint,
                person = newPerson.copy(personUid = personUid)
            )
        }
    }

}