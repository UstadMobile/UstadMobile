package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.TextView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.AboutController
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.view.AboutView
import java.util.*

class AboutActivity : UstadBaseActivity(), AboutView {

    private var mAboutController: AboutController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setUMToolbar(R.id.um_toolbar)
        setTitle(R.string.about)
        mAboutController = AboutController(this,
                bundleToMap(intent.extras),
                this)
        mAboutController!!.onCreate(bundleToMap(savedInstanceState))

        setUMToolbar(R.id.um_toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
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


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}