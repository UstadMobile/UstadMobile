package com.ustadmobile.core.domain.compress.video

import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertNotNull

class FindHandBrakeUseCaseTest {

    @Test
    fun givenHandBrakeCliOnSystem_whenInvoked_thenWillFindLatestVersion() {
        runBlocking {
            val version = FindHandBrakeUseCase(specifiedLocation = null).invoke()
            assertNotNull(version, "Can find HandBrakeCLI - got $version")
        }
    }

}