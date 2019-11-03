package com.ustadmobile.port.android.view

import android.Manifest
import android.app.KeyguardManager
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.View
import android.widget.*
import com.ustadmobile.core.controller.Login2Presenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.view.LoginView
import com.ustadmobile.lib.db.entities.UmAccount
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import android.widget.Toast
import com.ustadmobile.port.android.sync.UmAppDatabaseSyncWorker
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.Login2View
import kotlinx.io.IOException
import java.security.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.security.cert.CertificateException


class Login2Activity : UstadBaseActivity(), Login2View,
        FingerprintAuthenticationHelper.FingerprintHelperListener {
    override fun authenticationFailed(error: String) {
        //Does nothing
    }

    override fun updateVersionOnLogin(version: String) {
        mVersionTextView!!.visibility = View.VISIBLE
        mVersionTextView!!.setTextColor(getResources().getColor(R.color.text_primary))
        mVersionTextView!!.text = version
    }

    private class FingerprintException(e:Exception):Exception(e)

    override fun authenticationSuccess(result: FingerprintManager.AuthenticationResult) {
        val impl = UstadMobileSystemImpl.instance
        val fpUsername = UmAccountManager.getFingerprintUsername(viewContext, impl)
        val fpAuth = UmAccountManager.getFingerprintAuth(viewContext, impl)
        if (fpUsername != null && !fpUsername.isEmpty()) {
            mPresenter!!.loginOKFromOtherSource(mServerUrl!!, fpAuth!!)
        } else {
            sendToast("Login not registered with fingerprint.")
        }
    }

    fun sendToast(message: String) {
        runOnUiThread {
            Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun authenticationSuccess(result: FingerprintManager.AuthenticationResult, account: UmAccount) {
        //Does nothing
    }

    override fun setFinishAfficinityOnView() {
        runOnUiThread(Runnable { finishAffinity() })
    }

    override fun forceSync() {
        WorkManager.getInstance().cancelAllWorkByTag(UmAppDatabaseSyncWorker.TAG)
        UmAppDatabaseSyncWorker.queueSyncWorkerWithPolicy(100, TimeUnit.MILLISECONDS,
                ExistingWorkPolicy.APPEND)
        sendToast("Sync started")
        //TODO: KMP: Sync worker, etc
//        WorkManager.getInstance().getWorkInfosByTagLiveData(UmAppDatabaseSyncWorker.TAG).observe(
//                this, { workInfos ->
//            for (wi in workInfos) {
//                if (wi.getState().isFinished()) {
//                    //sendToast("Sync finished");
//                }
//            }
//        })
    }

    override fun updateLastActive() {
        val systemTime = AtomicLong(System.currentTimeMillis())
        updateLastActive(systemTime)
    }

    override fun updateUsername(username: String) {
        if(mUsernameTextView != null) {
            mUsernameTextView!!.setText(username)
            mUsernameTextView!!.setFocusable(false)
            mPasswordTextView!!.setFocusable(true)

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    private var mPresenter: Login2Presenter? = null

    private var mServerUrl: String? = null

    private lateinit var mUsernameTextView: TextView

    private lateinit var mPasswordTextView: TextView

    private lateinit var mErrorTextView: TextView

    private var mVersionTextView: TextView? = null

    private lateinit var mProgressBar: ProgressBar

    private lateinit var mLoginButton: Button

    private lateinit var assignToFingerprintCB: CheckBox

    private var assignToFingerprint = false

    private lateinit var fingerprintIV: ImageView

    // Declare a string variable for the key we’re going to use in our fingerprint authentication
    private var KEY_NAME = "yourKey"
    private var cipher: Cipher ?= null
    private var keyStore: KeyStore ?= null
    private var keyGenerator: KeyGenerator ?= null
    private var cryptoObject: FingerprintManager.CryptoObject ?= null
    private var fingerprintManager: FingerprintManager ?= null
    private var keyguardManager: KeyguardManager ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLogout = false

        setContentView(R.layout.activity_login2)
        setSupportActionBar(findViewById(R.id.um_toolbar))

        mUsernameTextView = findViewById(R.id.activity_login_username)
        mPasswordTextView = findViewById(R.id.activity_login_password)

        mPresenter = Login2Presenter(this, bundleToMap(intent.extras),
                this)
        mPresenter!!.onCreate(bundleToMap(savedInstanceState))

        mLoginButton = findViewById(R.id.activity_login_button_login)
        mErrorTextView = findViewById(R.id.activity_login_errormessage)
        mVersionTextView = findViewById(R.id.activity_login_version)
        mProgressBar = findViewById(R.id.progressBar)
        mProgressBar.isIndeterminate = true
        mProgressBar.scaleY = 3f

        assignToFingerprintCB = findViewById(R.id.activity_login2_assign_fingerprint_checbox)
        assignToFingerprintCB.setOnCheckedChangeListener(
                { buttonView, isChecked ->
                    assignToFingerprint = isChecked})

        findViewById<View>(R.id.activity_login_button_login).setOnClickListener { evt ->
            //            mPresenter!!.handleClickLogin(mUsernameTextView!!.text.toString(),
//                    mPasswordTextView!!.text.toString(), mServerUrl!!, assignToFingerprint)
            mPresenter!!.handleClickLogin(mUsernameTextView.text.toString(),
                    mPasswordTextView.text.toString(), mServerUrl!!)
        }

        fingerprintIV = findViewById(R.id.activity_login2_fingerprint_imageview)
        fingerprintIV.setOnClickListener({ v: View -> mPresenter!!.handleClickFingerPrint() })

        //checkFingerprint()
    }

    private fun checkFingerprint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            fingerprintManager = getSystemService(FINGERPRINT_SERVICE) as FingerprintManager
            var fpok = false
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT))
            {
                //Check whether the device has a fingerprint sensor//
                fingerprintIV.setVisibility(View.GONE)
                assignToFingerprintCB.setVisibility(View.GONE)
                //("Your device doesn't support fingerprint authentication");
            }
            else
            {
                if (!fingerprintManager!!.hasEnrolledFingerprints())
                {
                    sendToast(("No fingerprint configured. " +
                            "Please register at least one fingerprint in your device's Settings"))
                }
                else
                {
                    fingerprintIV.setVisibility(View.VISIBLE)
                    assignToFingerprintCB.setVisibility(View.VISIBLE)
                    fpok = true
                    //Check that the user has registered at least one fingerprint//
                }
            }
            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if ((ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.USE_FINGERPRINT) !== PackageManager.PERMISSION_GRANTED))
            {
                // If your app doesn't have this permission, then display the following text//
                sendToast("Please enable the fingerprint permission")
            }
            //Check that the lock screen is secured//
            if (!keyguardManager!!.isKeyguardSecure() && fpok == true)
            {
                sendToast("Please enable lock screen security in your device's Settings")
            }
            else
            {
                try
                {
                    generateKey()
                }
                catch (e:FingerprintException) {
                    e.printStackTrace()
                }
                if (initCipher())
                {
                    //If the cipher is initialized successfully, then create a CryptoObject instance//
                    cryptoObject = FingerprintManager.CryptoObject(cipher!!)
                    val helper = FingerprintAuthenticationHelper(this, this)
                    helper.startAuth(fingerprintManager!!, cryptoObject!!)
                }
            }
        }
    }

    /**
     * Create the generateKey method that we’ll use to gain access to the Android
     * keystore and generate the encryption key
     * @throws FingerprintException Exception fingerprint
     */
    @Throws(FingerprintException::class)
    private fun generateKey() {
        try
        {
            // Obtain a reference to the Keystore
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore")
            //Initialize an empty KeyStore//
            keyStore!!.load(null)
            //Initialize the KeyGenerator//
            keyGenerator!!.init(
                    //Specify the operation(s) this key can be used for//
                    KeyGenParameterSpec.Builder(KEY_NAME,
                            (KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT))
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                            .setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(
                                    KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .build()
            )
            //Generate the key//
            keyGenerator!!.generateKey()
        }
        catch (exc:KeyStoreException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        }
        catch (exc:NoSuchAlgorithmException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        }
        catch (exc:NoSuchProviderException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        }
        catch (exc:InvalidAlgorithmParameterException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        }
        catch (exc:CertificateException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        }
        catch (exc:IOException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        }
    }
    //Create a new method that we’ll use to initialize our cipher//
    fun initCipher():Boolean {
        try
        {
            cipher = Cipher.getInstance(
                    (KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7))
        }
        catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        }
        catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }
        try
        {
            keyStore!!.load(
                    null)
            val key = keyStore!!.getKey(KEY_NAME, null) as SecretKey
            cipher!!.init(Cipher.ENCRYPT_MODE, key)
            //Return true if the cipher has been initialized successfully//
            return true
        }
        catch (e:Exception) {
            //Return false if cipher initialization failed//
            return false
        }
    }

    override fun setInProgress(inProgress: Boolean) {
        mProgressBar!!.visibility = if (inProgress) View.VISIBLE else View.GONE
        mPasswordTextView!!.isEnabled = !inProgress
        mUsernameTextView!!.isEnabled = !inProgress
        mLoginButton!!.isEnabled = !inProgress
        mLoginButton!!.background.alpha = if (inProgress) 128 else 255
    }

    override fun setErrorMessage(errorMessage: String) {
        mErrorTextView!!.text = errorMessage
    }

    override fun setServerUrl(serverUrl: String) {
        this.mServerUrl = serverUrl
    }

    override fun setUsername(username: String) {
        mUsernameTextView!!.text = username
    }

    override fun setPassword(password: String) {
        mPasswordTextView!!.text = password
    }
}
