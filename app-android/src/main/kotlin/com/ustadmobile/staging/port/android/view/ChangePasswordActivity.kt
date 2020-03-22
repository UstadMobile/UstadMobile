package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ChangePasswordPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ChangePasswordView
import com.ustadmobile.port.android.view.UstadBaseActivity

class ChangePasswordActivity : UstadBaseActivity(), ChangePasswordView {

    private var toolbar: Toolbar? = null
    private var mPresenter: ChangePasswordPresenter? = null

    private var currentPasswordET: EditText? = null
    private var updatePasswordET: EditText? = null
    private var updatePasswordConfirmET: EditText? = null
    private var menu: Menu? = null


    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_save, menu)

        menu.findItem(R.id.menu_save).isVisible = true
        return true
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == android.R.id.home) {
            onBackPressed()
            return true

        } else if (i == R.id.menu_save) {
            mPresenter!!.handleClickSave()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_change_password)

        //Toolbar:
        toolbar = findViewById(R.id.activity_change_password_toolbar)
        toolbar!!.title = getText(R.string.change_password)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        currentPasswordET = findViewById(R.id.activity_change_password_current)
        updatePasswordET = findViewById(R.id.activity_change_password_new_password)
        updatePasswordConfirmET = findViewById(R.id.activity_change_password_new_password_confirm)


        //Call the Presenter
        mPresenter = ChangePasswordPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))


        currentPasswordET!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.currentPassword = s.toString()
            }
        })

        updatePasswordET!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updatePassword = s.toString()
            }
        })

        updatePasswordET!!.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            source.toString().filterNot { it.isWhitespace() }
        })

        updatePasswordConfirmET!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updatePasswordConfirm = s.toString()
            }
        })

        updatePasswordET!!.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            source.toString().filterNot { it.isWhitespace() }
        })


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
