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
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class CreateLearningSpaceUseCase(
    private val xxStringHasher: XXStringHasher,
    private val learningSpaceServerRepo: LearningSpaceServerRepo,
    private val di: DI,
) {

    @Serializable
    data class CreateLearningSpaceRequest(
        val url: String,
        val title: String,
        val dbUrl: String,
        val dbUsername: String,
        val dbPassword: String,
        val adminUsername: String,
        val adminPassword: String,
    )

    suspend operator fun invoke(request: CreateLearningSpaceRequest) {
        val uid = xxStringHasher.hash(request.url)

        learningSpaceServerRepo.add(
            LearningSpaceConfigAndInfo(
                config = LearningSpaceConfig(
                    lscUid = uid,
                    lscUrl = request.url,
                    lscDbUrl = request.dbUrl,
                    lscDbUsername = request.dbUsername,
                    lscDbPassword = request.dbPassword,
                ),
                info = LearningSpaceInfo(
                    lsiUid = uid,
                    lsiUrl = request.url,
                    lsiName = request.title,
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
                lastName = "Person"
            ),
            systemPermissions = PermissionFlags.ALL,
        )

        authManager.setAuth(personUid, request.adminPassword)
    }


}