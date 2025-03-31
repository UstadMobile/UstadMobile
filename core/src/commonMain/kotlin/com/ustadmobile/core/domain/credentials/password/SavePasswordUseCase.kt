package com.ustadmobile.core.domain.credentials.password
/**
 * this use case will save the username and password to the any password manager
 * available in your device later during login popup will be shown for easy login
 */
interface SavePasswordUseCase {

    suspend operator fun invoke(username:String, password: String, learningSpace: String)

}