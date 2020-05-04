package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.LoginPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.LoginView
import android.widget.LinearLayout
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager


class LoginActivity : UstadBaseActivity(), LoginView {

    private lateinit var mPresenter: LoginPresenter

    private var mServerUrl: String? = null

    private lateinit var mUsernameTextView: TextInputEditText

    private lateinit var mPasswordTextView: TextInputEditText

    private lateinit var mErrorTextView: TextView

    private lateinit var mProgressBar: ProgressBar

    private lateinit var mLoginButton: Button

    private lateinit var registerMessage : TextView

    private lateinit var registerNow: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        setUMToolbar(R.id.um_toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        registerMessage = findViewById(R.id.activity_register_label)
        registerNow = findViewById(R.id.activity_register_now)

        mPresenter = LoginPresenter(this, bundleToMap(intent.extras),
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getRepositoryForActiveAccount(this).personDao)
        mPresenter.onCreate(bundleToMap(savedInstanceState))
        mUsernameTextView = findViewById(R.id.activity_login_username)
        mPasswordTextView = findViewById(R.id.activity_login_password)
        mLoginButton = findViewById(R.id.activity_login_button_login)
        mErrorTextView = findViewById(R.id.activity_login_errormessage)
        mProgressBar = findViewById(R.id.progressBar)
        mProgressBar.isIndeterminate = true
        mProgressBar.scaleY = 3f
        findViewById<View>(R.id.activity_login_button_login).setOnClickListener { evt ->
            mPresenter.handleClickLogin(mUsernameTextView.text.toString(),
                    mPasswordTextView.text.toString(), mServerUrl!!)
        }

        registerNow.setOnClickListener {
            mPresenter.handleClickCreateAccount()
        }
    }

    override fun setInProgress(inProgress: Boolean) {
        mProgressBar.visibility = if (inProgress) View.VISIBLE else View.GONE
        mPasswordTextView.isEnabled = !inProgress
        mUsernameTextView.isEnabled = !inProgress
        mLoginButton.isEnabled = !inProgress
        mLoginButton.background.alpha = if (inProgress) 128 else 255
    }

    override fun setErrorMessage(errorMessage: String) {
        mErrorTextView.visibility = View.VISIBLE
        mErrorTextView.text = errorMessage
    }

    override fun setServerUrl(serverUrl: String) {
        this.mServerUrl = serverUrl
    }

    override fun setUsername(username: String) {
        mUsernameTextView.setText(username)
    }

    override fun setPassword(password: String) {
        mPasswordTextView.setText(password)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun showRegisterCodeDialog(title: String, okButtonText: String, cancelButtonText: String) {

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        val pixels = UMAndroidUtil.convertDpToPixel(24)
        lp.setMargins(pixels, 0, pixels, 0)
        val input = EditText(this)
        input.layoutParams = lp
        input.requestLayout()
        container.addView(input, lp)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setPositiveButton(okButtonText){  dialogInterface , _ ->
            mPresenter.handleRegisterCodeDialogEntered(input.text.toString())
        }
        builder.setNegativeButton(cancelButtonText){ dialogInterface, _ ->
            dialogInterface.cancel()
        }
        builder.setView(container)
        builder.show()

    }

    override fun setRegistrationLinkVisible(visible: Boolean) {
        val visibility = if(visible) View.VISIBLE else View.GONE
        registerMessage.visibility = visibility
        registerNow.visibility = visibility
    }
}
