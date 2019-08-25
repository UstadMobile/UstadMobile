package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.LoginPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.LoginView

class LoginActivity : UstadBaseActivity(), LoginView {

    private var mPresenter: LoginPresenter? = null

    private var mServerUrl: String? = null

    private var mUsernameTextView: TextView? = null

    private var mPasswordTextView: TextView? = null

    private var mErrorTextView: TextView? = null

    private var mProgressBar: ProgressBar? = null

    private var mLoginButton: Button? = null

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
                this, UstadMobileSystemImpl.instance)
        mPresenter!!.onCreate(bundleToMap(savedInstanceState))
        mUsernameTextView = findViewById(R.id.activity_login_username)
        mPasswordTextView = findViewById(R.id.activity_login_password)
        mLoginButton = findViewById(R.id.activity_login_button_login)
        mErrorTextView = findViewById(R.id.activity_login_errormessage)
        mProgressBar = findViewById(R.id.progressBar)
        mProgressBar!!.isIndeterminate = true
        mProgressBar!!.scaleY = 3f
        findViewById<View>(R.id.activity_login_button_login).setOnClickListener { evt ->
            mPresenter!!.handleClickLogin(mUsernameTextView!!.text.toString(),
                    mPasswordTextView!!.text.toString(), mServerUrl!!)
        }

        registerNow.setOnClickListener {
            mPresenter!!.handleCreateAccount()
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
        mErrorTextView!!.visibility = View.VISIBLE
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun setRegistrationLinkVisible(visible: Boolean) {
        val visibility = if(visible) View.VISIBLE else View.GONE
        registerMessage.visibility = visibility
        registerNow.visibility = visibility
    }
}
