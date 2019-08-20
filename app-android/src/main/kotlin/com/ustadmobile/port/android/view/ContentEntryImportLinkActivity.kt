package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.widget.EditText
import android.widget.ProgressBar
import com.google.android.material.textfield.TextInputLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryImportLinkPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.ContentEntryImportLinkView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ContentEntryImportLinkActivity : UstadBaseActivity(), ContentEntryImportLinkView {


    private lateinit var presenter: ContentEntryImportLinkPresenter

    private lateinit var webView: WebView

    private lateinit var textInput: TextInputLayout

    private lateinit var editText: EditText

    private lateinit var titleInput: TextInputLayout

    private lateinit var titleEdiText: EditText

    private lateinit var progressBar: ProgressBar

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        setContentView(R.layout.activity_content_entry_import_link)

        setUMToolbar(R.id.entry_import_link_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        umToolbar.title = getString(R.string.activity_import_link)

        val endpoint = intent.getStringExtra(ContentEntryImportLinkView.END_POINT_URL)
                ?: UmAccountManager.getActiveEndpoint(viewContext)!!

        webView = findViewById(R.id.import_link_preview_webview)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        textInput = findViewById(R.id.entry_import_link_textInput)
        editText = findViewById(R.id.entry_import_link_editText)

        titleInput = findViewById(R.id.entry_import_link_titleInput)
        titleEdiText = findViewById(R.id.entry_import_link_title_editText)

        editText.addTextChangedListener(textWatcher)
        titleEdiText.addTextChangedListener(titleWatcher)

        progressBar = findViewById(R.id.import_link_progress)

        presenter = ContentEntryImportLinkPresenter(viewContext,
                Objects.requireNonNull(UMAndroidUtil.bundleToMap(intent.extras)),
                this, endpoint)
        presenter.onCreate(UMAndroidUtil.bundleToMap(saved))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_h5p_import_link_action, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.import_link_done -> {
                GlobalScope.launch {
                    presenter.handleClickImport()
                }
                return true
            }

        }
        return true
    }


    override fun showUrlStatus(isValid: Boolean, message: String) {
        runOnUiThread {
            textInput.error = if (isValid) null else message
        }

    }

    override fun displayUrl(url: String) {
        runOnUiThread {
            webView.loadUrl(url)
        }
    }

    override fun returnResult() {
        finish()
    }

    override fun showHideVideoTitle(showTitle: Boolean) {
        runOnUiThread {
            titleInput.visibility = if(showTitle) VISIBLE else GONE
        }
    }

    override fun showProgress(showProgress: Boolean) {
        runOnUiThread {
            progressBar.visibility = if(showProgress) VISIBLE else GONE
        }
    }

    override fun showNoTitleEntered(errorText: String) {
        runOnUiThread {
            titleInput.error = errorText
        }
    }


    private var textWatcher = object : TextWatcher {

        private var handler = Handler()
        private val DELAY: Long = 150 // milliseconds
        private var string: Editable? = null

        override fun afterTextChanged(s: Editable?) {
            string = s
            handler.removeCallbacks(myRunnable)
            handler.postDelayed(myRunnable, DELAY)
        }

        var myRunnable = Runnable {
            GlobalScope.launch {
                presenter.handleUrlTextUpdated(string.toString())
            }
        }


        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }

    private var titleWatcher = object : TextWatcher {


        override fun afterTextChanged(s: Editable?) {
            presenter.handleTitleChanged(s.toString())
        }


        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }

}