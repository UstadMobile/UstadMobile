package com.ustadmobile.lib.rest.domain.learningspace.create

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.rest.domain.learningspace.LearningSpaceServerRepo
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.systemdb.model.LearningSpaceConfig
import com.ustadmobile.systemdb.model.LearningSpaceConfigAndInfo
import com.ustadmobile.systemdb.model.LearningSpaceInfo
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
                    lastModified = systemTimeInMillis()
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