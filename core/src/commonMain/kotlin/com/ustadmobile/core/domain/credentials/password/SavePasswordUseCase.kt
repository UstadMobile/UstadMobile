package com.ustadmobile.core.domain.credentials.password

/**
 * This use case will save the username and password to the any password manager
 * available in your device later during login popup will be shown for easy login.
 *
 * This use case is bound to a specific LearningSpace (same as other dependencies).
 */
interface SavePasswordUseCase {

    suspend operator fun invoke(username:String, password: String)

}