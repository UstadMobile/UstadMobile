package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.TextView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.AboutController
import com.ustadmobile.core.view.AboutView

import java.util.Objects

import com.ustadmobile.port.android.util.UMAndroidUtil.bundleToMap
import com.ustadmobile.port.android.util.UMAndroidUtil.mapToBundle

class AboutActivity : UstadBaseActivity(), AboutView {

    private var mAboutController: AboutController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setUMToolbar(R.id.um_toolbar)
        setTitle(R.string.about)
        mAboutController = AboutController(this,
                Objects.requireNonNull(bundleToMap(intent.extras)),
                this)
        mAboutController!!.onCreate(bundleToMap(savedInstanceState))
    }

    override fun setVersionInfo(versionInfo: String) {
        runOnUiThread { (findViewById<View>(R.id.about_version_text) as TextView).text = versionInfo }
    }

    override fun setAboutHTML(aboutHTML: String) {
        runOnUiThread {
            (findViewById<View>(R.id.about_html) as WebView)
                    .loadData(aboutHTML, "text/html", "UTF-8")
        }
    }

    //TODO: change this to using the standard up navigation arrow, then remove this menu and it's drawable
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_finish) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}