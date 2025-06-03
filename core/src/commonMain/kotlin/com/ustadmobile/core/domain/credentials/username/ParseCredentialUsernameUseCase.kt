package com.ustadmobile.core.domain.credentials.username

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.util.ext.requirePostfix

/**
 * When using a saved credential, the username will include the learning space as outlined
 * in CreateCredentialUsernameUseCase
 */
class ParseCredentialUsernameUseCase {

    /**
     * @param credentialUsername the credential username as per CreateCredentialUsernameUseCase doc
     * @return a pair containing the learning space and username itself
     */
    operator fun invoke(credentialUsername: String): Pair<LearningSpace, String> {
        val (username, learningSpacePart) = credentialUsername.split("@", limit = 2)
        val learningSpacePartLower = learningSpacePart.lowercase()

        return if(learningSpacePartLower.startsWith("http://")) {
            Pair(LearningSpace(learningSpacePart), username)
        }else {
            Pair(LearningSpace("https://$learningSpacePart".requirePostfix("/")), username)
        }
    }

}