package com.ustadmobile.port.android.view

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.widget.Toast

import androidx.core.app.ActivityCompat

import com.ustadmobile.lib.db.entities.UmAccount


@TargetApi(Build.VERSION_CODES.M)
class FingerprintAuthenticationHelper : FingerprintManager.AuthenticationCallback {

    // You should use the CancellationSignal method whenever your app can no longer process user input, for example when your app goes
    // into the background. If you don’t use this method, then other apps will be unable to access the touch sensor, including the lockscreen!//

    private var cancellationSignal: CancellationSignal? = null
    private var context: Context? = null
    private var listener: FingerprintHelperListener? = null

    constructor(mContext: Context) {
        context = mContext
    }

    //Implement the startAuth method, which is responsible for starting the fingerprint authentication process//

    fun startAuth(manager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject) {

        cancellationSignal = CancellationSignal()
        if (ActivityCompat.checkSelfPermission(context!!,
                        Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    }

    override//onAuthenticationError is called when a fatal error has occurred. It provides the error code and error message as its parameters//
    fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {

        listener!!.authenticationFailed("AuthenticationError : $errString")
        Toast.makeText(context, "Authentication error\n$errString", Toast.LENGTH_LONG).show()
    }

    override//onAuthenticationFailed is called when the fingerprint doesn’t match with any of the fingerprints registered on the device//
    fun onAuthenticationFailed() {
        listener!!.authenticationFailed("Authentication failed")
        Toast.makeText(context, "Authentication failed", Toast.LENGTH_LONG).show()
    }

    override//onAuthenticationHelp is called when a non-fatal error has occurred. This method provides additional information about the error,
    //so to provide the user with as much feedback as possible I’m incorporating this information into my toast//
    fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
        Toast.makeText(context, "Authentication help\n$helpString", Toast.LENGTH_LONG).show()
    }

    override//onAuthenticationSucceeded is called when a fingerprint has been successfully matched to one of the fingerprints stored on the user’s device//
    fun onAuthenticationSucceeded(
            result: FingerprintManager.AuthenticationResult) {

        listener!!.authenticationSuccess(result)
    }


    constructor(listener: FingerprintHelperListener) {
        this.listener = listener
    }

    constructor(listener: FingerprintHelperListener, context: Context) {
        this.listener = listener
        this.context = context
    }

    //interface for the listener
    internal interface FingerprintHelperListener {
        fun authenticationFailed(error: String)
        fun authenticationSuccess(result: FingerprintManager.AuthenticationResult)
        fun authenticationSuccess(result: FingerprintManager.AuthenticationResult, account: UmAccount)
    }


}
