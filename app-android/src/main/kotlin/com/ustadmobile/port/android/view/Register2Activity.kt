package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.Register2Presenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.Register2View
import com.ustadmobile.lib.db.entities.Person
import java.util.*


class Register2Activity : UstadBaseActivity(), Register2View {


    private var errorMessageView: TextView? = null

    private var systemImpl: UstadMobileSystemImpl? = null

    private val fieldToViewIdMap = HashMap<Int, Int>()

    private var presenter: Register2Presenter? = null

    private var serverUrl: String? = null

    private var registerUser: Button? = null

    private var progressDialog: ProgressBar? = null

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            errorMessageView!!.visibility = View.GONE
            checkRegisterButtonStatus()
        }

        override fun afterTextChanged(s: Editable) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register2)

        setUMToolbar(R.id.um_toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }
        fieldToViewIdMap[Register2View.FIELD_FIRST_NAME] = R.id.activity_create_account_firstname_text
        fieldToViewIdMap[Register2View.FIELD_LAST_NAME] = R.id.activity_create_account_lastname_text
        fieldToViewIdMap[Register2View.FIELD_EMAIL] = R.id.activity_create_account_email_text
        fieldToViewIdMap[Register2View.FIELD_USERNAME] = R.id.activity_create_account_username_text
        fieldToViewIdMap[Register2View.FIELD_PASSWORD] = R.id.activity_create_account_password_text
        fieldToViewIdMap[Register2View.FIELD_CONFIRM_PASSWORD] = R.id.activity_create_account_password_confirmpassword_text

        registerUser = findViewById(R.id.activity_create_account_create_account_button)
        errorMessageView = findViewById(R.id.activity_create_account_error_text)
        progressDialog = findViewById(R.id.progressBar)
        progressDialog!!.isIndeterminate = true
        progressDialog!!.scaleY = 3f

        systemImpl = UstadMobileSystemImpl.instance

        presenter = Register2Presenter(this,
                bundleToMap(intent.extras), this,
                UmAccountManager.getRepositoryForActiveAccount(this).personDao)
        presenter!!.onCreate(bundleToMap(savedInstanceState))

        for (fieldId in fieldToViewIdMap.values) {
            (findViewById<View>(fieldId) as TextInputEditText).addTextChangedListener(textWatcher)
        }

        registerUser!!.setOnClickListener { v -> checkAccountFields() }

    }


    private fun checkAccountFields() {
        for (fieldCode in fieldToViewIdMap.keys) {
            if (getFieldValue(fieldCode).isEmpty()) {
                setErrorMessageView(systemImpl!!.getString(MessageID.register_empty_fields, this))
                return
            }
        }

        checkRegisterButtonStatus()

        if (getFieldValue(Register2View.FIELD_PASSWORD) == getFieldValue(Register2View.FIELD_CONFIRM_PASSWORD)) {

            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

            if (getFieldValue(Register2View.FIELD_EMAIL).matches(emailPattern.toRegex())) {
                if (getFieldValue(Register2View.FIELD_PASSWORD).length < 5) {
                    disableButton(true)
                    setErrorMessageView(systemImpl!!.getString(MessageID.field_password_error_min, this))
                } else {
                    val person = Person()
                    person.firstNames = getFieldValue(Register2View.FIELD_FIRST_NAME)
                    person.lastName = getFieldValue(Register2View.FIELD_LAST_NAME)
                    person.emailAddr = getFieldValue(Register2View.FIELD_EMAIL)
                    person.username = getFieldValue(Register2View.FIELD_USERNAME)
                    Thread {
                        presenter!!.handleClickRegister(person,
                                getFieldValue(Register2View.FIELD_PASSWORD), serverUrl!!)
                    }.start()
                }
            } else {
                disableButton(true)
                setErrorMessageView(systemImpl!!.getString(MessageID.register_incorrect_email, this))
            }
        } else {
            disableButton(true)
            setErrorMessageView(systemImpl!!.getString(MessageID.filed_password_no_match, this))
        }
    }

    private fun disableButton(disable: Boolean) {
        registerUser!!.setBackgroundColor(ContextCompat.getColor(this,
                if (disable) R.color.divider else R.color.accent))
        registerUser!!.isEnabled = !disable
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    override fun setErrorMessageView(errorMessageView: String) {
        this.errorMessageView!!.text = errorMessageView
        this.errorMessageView!!.visibility = View.VISIBLE
        disableButton(true)
    }

    override fun setServerUrl(url: String) {
        this.serverUrl = url
    }

    override fun setInProgress(inProgress: Boolean) {
        progressDialog!!.visibility = if (inProgress) View.VISIBLE else View.GONE
    }

    private fun getFieldValue(fieldCode: Int): String {
        return (findViewById<View>(fieldToViewIdMap[fieldCode]!!) as TextInputEditText).text!!.toString()
    }

    private fun checkRegisterButtonStatus() {
        var isEnabled = true
        for (fieldCode in fieldToViewIdMap.keys) {
            if (getFieldValue(fieldCode).isEmpty()) {
                isEnabled = false
                break
            }
        }
        disableButton(!isEnabled)
    }


}
