package com.ustadmobile.lib.rest.domain.learningspace.create

import com.ustadmobile.appconfigdb.composites.LearningSpaceConfigAndInfo
import com.ustadmobile.appconfigdb.entities.LearningSpaceConfig
import com.ustadmobile.appconfigdb.entities.LearningSpaceInfo
import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.rest.domain.learningspace.LearningSpaceServerRepo
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
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
        val dbUrl: String?,
        val dbUsername: String?,
        val dbPassword: String?,
        val adminUsername: String,
        val adminPassword: String,
        val isSelfRegistered: Boolean = false,
        val subdomain: String?,
        val adminContact: String?,
        val organisationLogo: String?
    )

    suspend operator fun invoke(request: CreateLearningSpaceRequest) {
        val uid = xxStringHasher.hash(request.url)
        val effectiveDbUrl = request.dbUrl ?:
            "jdbc:sqlite:${serverDataDir.absolutePath}/${sanitizeDbNameFromUrl(request.url)}.db"

        learningSpaceServerRepo.add(
            LearningSpaceConfigAndInfo(
                config = LearningSpaceConfig(
                    lscUid = uid,
                    lscUrl = request.url,
                    lscDbUrl = effectiveDbUrl,
                    lscDbUsername = request.dbUsername,
                    lscDbPassword = request.dbPassword,
                    lscSelfRegistered = request.isSelfRegistered
                ),
                info = LearningSpaceInfo(
                    lsiUid = uid,
                    lsiUrl = request.url,
                    lsiName = request.title,
                    subdomain = request.subdomain,
                    adminContact = request.adminContact,
                    organisationLogo = request.organisationLogo
                )
            )
        )

        val learningSpace = LearningSpace(request.url)
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