package com.ustadmobile.core.domain.credentials

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.util.ext.requirePostfix
import io.ktor.http.Url
import io.ktor.http.authority
import io.ktor.http.isSecure

/**
 * Use case to get a saved credential - passkey or username/password. This is a non-scoped
 * singleton (because there is one credential manager on the system).
 */
interface GetCredentialUseCase {

    sealed class CredentialResult

    /**
     * Result that represents a user selecting a saved username/password from credential
     * manager.
     *
     * @param credentialUsername saved username as per credentialUsernameForUserAndLearningSpace e.g.
     *        username@learningspace.example.org
     * @param password account saved password.
     */
    data class PasswordCredentialResult(
        val credentialUsername: String,
        val password: String
    ) : CredentialResult()

    /**
     * Passkey result
     */
    data class PasskeyCredentialResult(
        val passKeySignInData: PassKeySignInData
    ) : CredentialResult()

    data class Error(
        val message: String?
    ) : CredentialResult()

    suspend operator fun invoke(): CredentialResult

    companion object {

        /**
         * When saving credentials we can only save a 'username' and a 'password'. The learning space
         * URL needs to be included in the username (e.g. after an @ sign) so that if/when it is
         * selected by a user we know what learning space to use.
         *
         * The username is visible to the user so we want this to be as user-friendly as possible.
         * The credential username will be as follows:
         *
         * username@example.org where the LearningSpace uses https and the standard port
         * username@example.org:port where the LearningSpace uses https and a non-standard port
         * username@http://example.org/ where the LearningSpace does not use https
         *
         * @param username the username as entered by the user (e.g. 'bobjones')
         * @param learningSpace the learning space that is being connected to
         *
         * @return the credential username as outlined above
         */
        fun credentialUsernameForUserAndLearningSpace(
            username: String,
            learningSpace: LearningSpace
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

        /**
         * When using a saved credential, the username will include the learning space as outlined
         * in credentialUsernameForUserAndLearningSpace
         *
         * @param credentialUsername the credential username as per
         * credentialUsernameForUserAndLearningSpace doc
         *
         * @return a pair containing the learning space and username itself
         */
        fun learningSpaceAndUsernameForCredentialUsername(
            credentialUsername: String
        ): Pair<LearningSpace, String> {
            val (username, learningSpacePart) = credentialUsername.split("@", limit = 2)
            val learningSpacePartLower = learningSpacePart.lowercase()

            return if(learningSpacePartLower.startsWith("http://")) {
                Pair(LearningSpace(learningSpacePart), username)
            }else {
                Pair(LearningSpace("https://$learningSpacePart".requirePostfix("/")), username)
            }
        }

    }

}