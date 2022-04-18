package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.TextView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.AboutPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.view.AboutView
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.CompletableDeferred
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI

class AboutActivity : UstadBaseActivity(), AboutView, DIAware {

    private lateinit var mAboutPresenter: AboutPresenter

    override val di by closestDI()

    //There isn't really any loading done here
    override var loading: Boolean
        get() = false
        set(value) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setUMToolbar(R.id.toolbar)
        setTitle(R.string.about)
        mAboutPresenter = AboutPresenter(this,
                bundleToMap(intent.extras),
                this, di)
        mAboutPresenter.onCreate(bundleToMap(savedInstanceState))

        setUMToolbar(R.id.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        (findViewById<View>(R.id.about_html) as WebView)
                .loadUrl("file:///android_asset/about.html")
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

    override var networkManager: CompletableDeferred<NetworkManagerBle>? = null
}