package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELSelectConsentPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SELSelectConsentView
import com.ustadmobile.port.android.view.UstadBaseActivity
import ru.dimorinny.floatingtextbutton.FloatingTextButton


/**
 * The SELSelectConsent activity. This Activity extends UstadBaseActivity and implements
 * SELSelectConsentView. This activity is responsible for showing explicitly to seek for student
 * consent and will only allow to proceed if consent check box is explicitly checked.
 */
class SELSelectConsentActivity : UstadBaseActivity(), SELSelectConsentView {

    private var mPresenter: SELSelectConsentPresenter? = null

    /**
     * Handles every item selected on the toolbar. Here handles back button pressed.
     *
     * @param item  The menu item pressed
     * @return  true if accounted for.
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
        setContentView(R.layout.activity_selselect_consent)

        //Toolbar:
        val toolbar = findViewById<Toolbar>(R.id.activity_selselect_consent_toolbar)
        toolbar.title = getText(R.string.social_nomination)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Call the Presenter
        mPresenter = SELSelectConsentPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        val consentCheckBox = findViewById<CheckBox>(R.id.activity_selselect_consent_checkbox)

        //FAB:
        val fab = findViewById<FloatingTextButton>(R.id.activity_selselect_consent_fab)
        fab.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton(consentCheckBox.isChecked) }

    }

    override fun toastMessage(message: String) {
        Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
        ).show()
    }
}
