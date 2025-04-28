package com.ustadmobile.core.domain.credentials.username

import com.ustadmobile.core.account.LearningSpace
import io.ktor.http.Url
import io.ktor.http.authority
import io.ktor.http.isSecure

/**
 * When saving credentials we can only save a 'username', we also need to know the learning space
 * url. Linking each credential strictly to a learning space origin would make it impossible to show
 * a simple dialog when the user first starts the app to select an account from any learning space.
 *
 * The credential username is used both as user.name for a passkey and the username when saving a
 * username and password to a password manager.
 *
 * The username is visible to the user so we want this to be as user-friendly as possible.
 * The credential username will be as follows:
 *
 * username@example.org where the LearningSpace uses https and the standard port
 * username@example.org:port where the LearningSpace uses https and a non-standard port
 * username@http://example.org/ where the LearningSpace does not use https
 *
 * @param learningSpace the learning space that is being connected to
 */
class CreateCredentialUsernameUseCase(
    private val learningSpace: LearningSpace,
) {

    /**
     * @param username the username as entered by the user (e.g. 'bobjones')
     *
     * @return the credential username as outlined above
     */
    operator fun invoke(
        username: String,
    ): String {
        val learningSpaceUrl = Url(learningSpace.url)
        return when {
            learningSpaceUrl.protocol.isSecure() -> {
                "$username@${learningSpaceUrl.authority}${learningSpaceUrl.encodedPath.removeSuffix("/")}"
            }

            else -> {
                "$username@$learningSpaceUrl"
            }
        }
    }
}