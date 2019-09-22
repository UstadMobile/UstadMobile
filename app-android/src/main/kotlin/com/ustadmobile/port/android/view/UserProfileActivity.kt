package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.UserProfilePresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UserProfileView

class UserProfileActivity : UstadBaseActivity(), UserProfileView {


    private lateinit var presenter: UserProfilePresenter

    private lateinit var logout: RelativeLayout

    private lateinit var languageOption: RelativeLayout

    private lateinit var languageName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        logout = findViewById(R.id.activity_user_profile_logout_ll)
        languageOption = findViewById(R.id.activity_user_profile_language_ll)
        languageName = findViewById(R.id.activity_user_profile_language_selection)

        setUMToolbar(R.id.um_toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        presenter = UserProfilePresenter(this, UMAndroidUtil.bundleToMap(intent.extras),
                this, UstadMobileSystemImpl.instance)
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        logout.setOnClickListener {
            presenter.handleUserLogout()
        }

        languageOption.setOnClickListener {
            presenter.handleShowLanguageOptions()
        }
    }

    override fun setUsername(username: String) {
        supportActionBar!!.title = username
    }

    override fun showLanguageOptions() {
        
    }

    override fun restartUI() {
        onResume()
    }

    override fun setCurrentLanguage(language: String?) {
        languageName.text = language
    }

    override fun setLanguageOption(languages: MutableList<String>) {
        val popupMenu = PopupMenu(this, languageName)
        languages.forEach {
            popupMenu.menu.add(it)
        }

        popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
            presenter.handleLanguageSelected(languages.indexOf(item!!.title))
            true
        }
        popupMenu.show()

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
