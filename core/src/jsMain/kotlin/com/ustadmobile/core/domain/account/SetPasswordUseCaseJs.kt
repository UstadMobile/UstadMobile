package com.ustadmobile.core.domain.account

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.UnauthorizedException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.doorNodeIdHeader
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

class SetPasswordUseCaseJs(
    private val learningSpace: LearningSpace,
    private val repo: UmAppDatabase,
    private val httpClient: HttpClient,
) : SetPasswordUseCase{

    override suspend fun invoke(
        activeUserPersonUid: Long,
        personUid: Long,
        username: String,
        newPassword: String,
        currentPassword: String?
    ) {
        val repo = repo as? DoorDatabaseRepository ?: throw IllegalArgumentException()
        try {
            val result = httpClient.post("${learningSpace.url}api/account/setpassword") {
                doorNodeIdHeader(repo)
                parameter("nodeActiveUserUid", activeUserPersonUid)
                parameter("personUid", personUid.toString())
                parameter("username", username)
                parameter("newPassword", newPassword)
                currentPassword?.also {
                    parameter("currentPassword", it)
                }

                expectSuccess = false
            }
            if(result.status == HttpStatusCode.Unauthorized) {
                throw UnauthorizedException()
            }else if(!result.status.isSuccess()) {
                throw IllegalStateException("Bad response to set password request: ${result.status}")
            }
        }catch(e: Throwable) {
            Napier.e(throwable = e) { "Exception setting password" }
            throw e
        }
    }
}