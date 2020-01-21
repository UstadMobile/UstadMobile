package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonAuthDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonAuthDetailView
import com.ustadmobile.port.android.view.UstadBaseActivity
import ru.dimorinny.floatingtextbutton.FloatingTextButton

class PersonAuthDetailActivity : UstadBaseActivity(), PersonAuthDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: PersonAuthDetailPresenter? = null

    private var usernameET: EditText? = null
    private var passwordET: EditText? = null
    private var updatePasswordET: EditText? = null


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_personauth_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_person_auth_detail_toolbar)
        toolbar!!.setTitle(getText(R.string.update_username_password))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        usernameET = findViewById(R.id.activity_personauth_detail_username)
        passwordET = findViewById(R.id.activity_personauth_detail_password)
        updatePasswordET = findViewById(R.id.activity_personauth_detail_confirm_password)

        //Call the Presenter
        mPresenter = PersonAuthDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        usernameET!!.filters = arrayOf(object : InputFilter {
            override fun filter(source: CharSequence?, start: Int, end: Int,
                                dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                // eliminates single space
                if (end == 1) {
                    if (Character.isWhitespace(source?.get(0)!!)) {
                        return ""
                    }
                }
                return null
            }
        })

        usernameET!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.usernameSet = s.toString()
            }
        })

        passwordET!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.passwordSet = s.toString()
            }
        })

        updatePasswordET!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.confirmPasswordSet = s.toString()
            }
        })

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_person_auth_detail_fab)

        fab.setOnClickListener { v -> mPresenter!!.handleClickDone() }


    }


    override fun updateUsername(username: String) {
        usernameET!!.setText(username)
    }

    override fun sendMessage(messageId: Int) {
        val impl = UstadMobileSystemImpl.instance
        val toast = impl.getString(messageId, this)
        runOnUiThread {
            Toast.makeText(
                    this,
                    toast,
                    Toast.LENGTH_SHORT
            ).show()
        }
    }
}
