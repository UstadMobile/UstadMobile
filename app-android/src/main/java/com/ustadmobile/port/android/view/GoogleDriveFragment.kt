/*
package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.toughra.ustadmobile.databinding.FragmentGoogleDriveContentBinding
import com.ustadmobile.core.view.GoogleDriveView
import com.ustadmobile.port.android.util.DriveServiceHelper
import com.ustadmobile.port.android.util.DriveServiceHelper.Companion.getGoogleDriveService
import java.util.*


class GoogleDriveFragment : UstadBaseFragment(), GoogleDriveView {

    private val REQUEST_CODE_SIGN_IN = 100
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mDriveServiceHelper: DriveServiceHelper? = null
    private val TAG = "MainActivity"


    private var mBinding: FragmentGoogleDriveContentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentGoogleDriveContentBinding.inflate(inflater, container, false)

        return mBinding?.root
    }

    override fun onStart() {
        super.onStart()
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

        if (account == null) {
            signIn()
        } else {
            val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
                    context, Collections.singleton(DriveScopes.DRIVE_METADATA))
            credential.selectedAccount = account.account
            val googleDriveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential)
                    .setApplicationName("AppName")
                    .build()
            mDriveServiceHelper = DriveServiceHelper(googleDriveService)
        }
    }

    private fun signIn() {
        mGoogleSignInClient = buildGoogleSignInClient()
        val intent = mGoogleSignInClient?.signInIntent
        if (intent != null) {
            val contract = ActivityResultContracts.StartActivityForResult()
            contract.createIntent(requireContext(), intent)
            registerForActivityResult(contract) { result ->
                val data = result.data
                when (result.resultCode) {
                    REQUEST_CODE_SIGN_IN -> if (result.resultCode == Activity.RESULT_OK && data != null) {
                        handleSignInResult(data)
                    }
                }
            }
        }
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .requestEmail()
                .build()
        return GoogleSignIn.getClient(requireContext(), signInOptions)
    }


    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener {
                    mDriveServiceHelper = DriveServiceHelper(getGoogleDriveService(requireContext(), it, "appName"))
                }
    }

}*/
