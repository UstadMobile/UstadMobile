package com.ustadmobile.lib.rest.domain.learningspace.create

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.xxhashkmp.XXStringHasher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.rest.domain.learningspace.LearningSpaceServerRepo
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.centralappconfigdb.model.LearningSpaceConfig
import com.ustadmobile.centralappconfigdb.model.LearningSpaceConfigAndInfo
import com.ustadmobile.centralappconfigdb.model.LearningSpaceInfo
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File

class CreateLearningSpaceUseCase(
    private val xxStringHasher: XXStringHasher,
    private val learningSpaceServerRepo: LearningSpaceServerRepo,
    private val serverDataDir: File,
    private val di: DI,
) {

    @Serializable
    data class CreateLearningSpaceRequest(
        val url: String,
        val title: String,
        val subdomain: String,
        val organisationLogo: String,
        val dbUrl: String?,
        val dbUsername: String?,
        val dbPassword: String?,
        val adminContact: String,
        val adminUsername: String,
        val adminPassword: String,
    )

    suspend operator fun invoke(request: CreateLearningSpaceRequest) {
        val uid = xxStringHasher.hash(request.url)
        val effectiveDbUrl = request.dbUrl ?:
            "jdbc:sqlite:${serverDataDir.absolutePath}/${sanitizeDbNameFromUrl(request.url)}.db"

        learningSpaceServerRepo.add(
            LearningSpaceConfigAndInfo(
                config = LearningSpaceConfig(
                    url = request.url,
                    dbUrl = effectiveDbUrl,
                    dbUsername = request.dbUsername,
                    dbPassword = request.dbPassword,
                ),
                info = LearningSpaceInfo(
                    url = request.url,
                    name = request.title,
                    description = request.title,
                    subdomain = request.subdomain,
                    organisationLogo = request.organisationLogo,
                    adminContact = request.adminContact,
                    lastModified = systemTimeInMillis()
                )
            )
        )

        val learningSpace = LearningSpace(request.url)

        val adminUsername = request.adminUsername
        val umAppDatabase: UmAppDatabase = di.on(learningSpace).direct.instance(tag = DoorTag.TAG_DB)
        val existingAdminUser = umAppDatabase.personDao().findByUsername(adminUsername)

        if(existingAdminUser == null) {
            val addPersonUseCase: AddNewPersonUseCase = di.on(learningSpace).direct.instance()
            val authManager: AuthManager = di.on(learningSpace).direct.instance()

            val personUid = addPersonUseCase(
                person = Person(
                    username = request.adminUsername,
                    firstNames = "Admin",
                    lastName = "User"
                ),
                systemPermissions = PermissionFlags.ALL,
            )

            authManager.setAuth(personUid, request.adminPassword)
        }
    }


}