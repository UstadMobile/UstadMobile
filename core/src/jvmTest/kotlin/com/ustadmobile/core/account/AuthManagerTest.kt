package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.insertPersonAndGroup
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
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.EncryptedPrivateKeyInfo
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec
import kotlin.test.assertTrue

class AuthManagerTest {

    lateinit var di: DI

    val learningSpace = LearningSpace("https://test.ustadmobile.app/")

    @JvmField
    @Rule
    val ustadTestRule = UstadTestRule()

    lateinit var dataLayer: UmAppDataLayer

    lateinit var repo: UmAppDatabase

    private val testUserUid = 42L

    private val testUserPassword = "secret"

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        dataLayer = di.on(learningSpace).direct.instance()
        repo = dataLayer.repository!!

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

    /**
     * https://developer.android.com/privacy-and-security/keystore#kotlin
     *
     * Overall approach:
     * When account is created:
     *  a) Generate a public / private key pair. Then encrypt the private key with password based
     *     encryption (where the password is the password as set by the user). Store the public key
     *     and encrypted private key.
     *
     * When logging in:
     *  a) Decrypt the private key using the password provided by the user
     *  b) Sign the UserSession authentication with the start time and personuid
     *  Note: this can work offline provided the encrypted private key is available
     *
     * When session is received on server:
     *  a) Verify the signature with the public key
     *
     * Additional note: what we should really do here is generate a random salt per user,
     * that avoids issues with the Site entity being updated. Change password, change salt.
     */
    @Test
    fun keyFlowTest() {
        val password = "secr3t"
        val pbeIterations = 65536
        val salt = "someSalt".toByteArray()
        val pbeAlgorithm = "PBEWithSHA1AndDESede"
        val message = "time=12121211;personUid=232313231".toByteArray()

        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)

        val keyPair = keyPairGenerator.generateKeyPair()

        val pbeKeySpec = PBEKeySpec(password.toCharArray())
        val secretKeyFactory = SecretKeyFactory.getInstance(pbeAlgorithm)
        val pbeKey = secretKeyFactory.generateSecret(pbeKeySpec)

        val pbeParamSpec = PBEParameterSpec(salt, pbeIterations)
        val pbeCipher = Cipher.getInstance(pbeAlgorithm)
        pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec)
        val encryptedPrivateKeyBytes = pbeCipher.doFinal(keyPair.private.encoded)
        val encryptAlgParams = AlgorithmParameters.getInstance(pbeAlgorithm)
        encryptAlgParams.init(pbeParamSpec)
        val encryptedPrivateKeyInfo = EncryptedPrivateKeyInfo(
            encryptAlgParams, encryptedPrivateKeyBytes
        )

        val decryptKeySpec = PBEKeySpec(password.toCharArray())
        val decryptSecretKeyFactory = SecretKeyFactory.getInstance(pbeAlgorithm)
        val decryptPbeKey = decryptSecretKeyFactory.generateSecret(decryptKeySpec)
        val decyptPbeCipher = Cipher.getInstance(pbeAlgorithm)
        decyptPbeCipher.init(Cipher.DECRYPT_MODE, decryptPbeKey, pbeParamSpec)
        val pkcs8EncodedKey = encryptedPrivateKeyInfo.getKeySpec(decyptPbeCipher)
        val decryptKeyFactory = KeyFactory.getInstance("RSA")
        val decryptedPrivateKey = decryptKeyFactory.generatePrivate(pkcs8EncodedKey)

        val signature = Signature.getInstance("SHA256WithRSA").apply {
            initSign(decryptedPrivateKey)
            update(message)
        }
        val sigBytes = signature.sign()

        val verification = Signature.getInstance("SHA256WithRSA").apply {
            initVerify(keyPair.public)
            update(message)
        }
        val isValid = verification.verify(sigBytes)

        assertTrue(isValid)
    }


    @Test
    fun givenAuthSet_whenAuthenticatedWithValidPassword_thenShouldAccept() {
        val authManager = AuthManager(learningSpace, di)

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
        val authManager = AuthManager(learningSpace, di)

        runBlocking {
            authManager.setAuth(testUserUid, testUserPassword)
        }

        val result = runBlocking {
            authManager.authenticate("testuser", "wrong")
        }

        Assert.assertFalse("Authentication fails with wrong password", result.success)
    }

}