package com.ustadmobile.core.domain.credentials

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.credentials.username.CreateCredentialUsernameUseCase
import com.ustadmobile.core.domain.credentials.username.ParseCredentialUsernameUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GetCredentialUseCaseTest {

    private fun assertConvertedAsExpected(
        username: String,
        learningSpace: LearningSpace,
        expectedCredentialUsername: String
    ) {
        val createCredentialUsernameUseCase = CreateCredentialUsernameUseCase(learningSpace)
        val credentialUsername = createCredentialUsernameUseCase(username = username)
        assertEquals(expectedCredentialUsername, credentialUsername)
        val (convertedLearningSpace, convertedUsername) = ParseCredentialUsernameUseCase().invoke(credentialUsername)

        assertEquals(username, convertedUsername)
        assertEquals(learningSpace, convertedLearningSpace)
    }

    @Test
    fun givenHttpsLearningSpace_whenConvertedBack_thenWillMatch() {
        assertConvertedAsExpected("janedoe",
            LearningSpace("https://learningspace.example.org/"),
            "janedoe@learningspace.example.org"
        )
    }

    @Test
    fun givenHttpsLearningSpaceWithPath_whenConvertedBack_thenWillMatch() {
        assertConvertedAsExpected("bobdoe",
            LearningSpace("https://learningspace.example.org/path/"),
            "bobdoe@learningspace.example.org/path"
        )
    }

    @Test
    fun givenPlainHttpLearningSpace_whenConvertedBack_thenWillMatch() {
        assertConvertedAsExpected("bobdoe",
            LearningSpace("http://learningspace.example.org/"),
            "bobdoe@http://learningspace.example.org/"
        )
    }

    @Test
    fun givenPlainHttpLearningSpaceWithPath_whenConvertedBack_thenWillMatch() {
        assertConvertedAsExpected("bobdoe",
            LearningSpace("http://learningspace.example.org/path/"),
            "bobdoe@http://learningspace.example.org/path/"
        )
    }

}