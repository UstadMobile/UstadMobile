package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Site
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class AuthManagerTest {

    lateinit var di: DI

    val endpoint = Endpoint("https://test.ustadmobile.app/")

    @JvmField
    @Rule
    val ustadTestRule = UstadTestRule()

    lateinit var repo: UmAppDatabase

    private val testUserUid = 42L

    private val testUserPassword = "secret"

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        repo = di.on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)

        runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                personUid = testUserUid
                username = "testuser"
            })
            repo.siteDao().insert(Site().apply {
                siteName = "Site"
            })

        }
    }

    @Test
    fun givenAuthSet_whenAuthenticatedWithValidPassword_thenShouldAccept() {
        val authManager = AuthManager(endpoint, di)

        runBlocking {
            authManager.setAuth(testUserUid, testUserPassword)
        }

        val result = runBlocking {
            authManager.authenticate("testuser", testUserPassword)
        }

        Assert.assertTrue("Authentication passes", result.success)
    }

    @Test
    fun givenAuthSet_whenAuthenticatedWithWrongPassword_thenShouldReject() {
        val authManager = AuthManager(endpoint, di)

        runBlocking {
            authManager.setAuth(testUserUid, testUserPassword)
        }

        val result = runBlocking {
            authManager.authenticate("testuser", "wrong")
        }

        Assert.assertFalse("Authentication fails with wrong password", result.success)
    }

}