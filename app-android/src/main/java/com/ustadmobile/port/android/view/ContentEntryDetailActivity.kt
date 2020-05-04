package com.ustadmobile.port.android.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityContentEntryDetailBinding
import com.ustadmobile.core.controller.ContentEntryDetailPresenter
import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.LOCALLY_AVAILABLE_ICON
import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.LOCALLY_NOT_AVAILABLE_ICON
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.isStatusCompletedSuccessfully
import com.ustadmobile.core.util.ext.isStatusQueuedOrDownloading
import com.ustadmobile.core.util.ext.toStatusString
import com.ustadmobile.core.util.goToContentEntry
import com.ustadmobile.core.util.mimeTypeToPlayStoreIdMap
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.port.android.view.ext.makeSnackbarIfRequired
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.android.synthetic.main.activity_content_entry_detail.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ContentEntryDetailActivity : UstadBaseWithContentOptionsActivity(),
        ContentEntryDetailView, ContentEntryDetailLanguageAdapter.AdapterViewListener,
        DownloadProgressView.OnStopDownloadListener {

    private lateinit var presenter: ContentEntryDetailPresenter

    private lateinit var managerAndroidBle: NetworkManagerBle

    private var localAvailabilityStatusText: TextView? = null

    private var entryDetailsTitle: TextView? = null

    private var entryDetailsDesc: TextView? = null

    private var entryDetailsLicense: TextView? = null

    private var entryDetailsAuthor: TextView? = null

    private var translationAvailableLabel: TextView? = null

    private var downloadSize: TextView? = null

    private var localAvailabilityStatusIcon: ImageView? = null

    private lateinit var editButton: FloatingActionButton

    private lateinit var flexBox: RecyclerView

    private lateinit var downloadButton: Button

    private lateinit var umAppRepository: UmAppDatabase

    private var downloadProgress: DownloadProgressView? = null

    private var fileStatusIcon = HashMap<Int, Int>()

    private var showControls : Boolean = false

    private var showExportIcon: Boolean = false

    private var currentDownloadJobItemStatus: Int = -1

    private lateinit var viewBinding: ActivityContentEntryDetailBinding

    class ContentEntryModel(val contentEntry: ContentEntry) {

        val descriptionHtml: CharSequence
            get() {
                val descriptionVal = contentEntry.description
                return if(descriptionVal != null) { Html.fromHtml(descriptionVal) } else { "" }
            }

    }


    override fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle)
        if (networkManagerBle.isVersionKitKatOrBelow) {
            downloadButton.setBackgroundResource(
                    R.drawable.pre_lollipop_btn_selector_bg_entry_details)
        }

        managerAndroidBle = networkManagerBle
        presenter = ContentEntryDetailPresenter(this,
                bundleToMap(intent.extras), this, true,
                umAppRepository, UmAccountManager.getActiveDatabase(this),
                networkManagerBle.localAvailabilityManager,
                networkManagerBle.containerDownloadManager,
                UmAccountManager.getActiveAccount(viewContext),
                UstadMobileSystemImpl.instance, ::goToContentEntry)
        presenter.handleShowEditControls(showControls)
        presenter.onCreate(bundleToMap(Bundle()))

        managerAndroidBle.enablePromptsSnackbarManager.makeSnackbarIfRequired(
                findViewById(R.id.coordinationLayout), this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        umAppRepository = UmAccountManager.getRepositoryForActiveAccount(this)
        showControls = UstadMobileSystemImpl.instance.getAppConfigString(
                AppConfig.KEY_SHOW_CONTENT_EDITOR_CONTROLS, "false", this)!!.toBoolean()

        viewBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_content_entry_detail)

        localAvailabilityStatusText = findViewById(R.id.content_status_text)
        localAvailabilityStatusIcon = findViewById(R.id.content_status_icon)
        downloadButton = findViewById(R.id.entry_download_open_button)
        downloadProgress = findViewById(R.id.entry_detail_progress)
        entryDetailsTitle = findViewById(R.id.entry_detail_title)
        entryDetailsDesc = findViewById(R.id.entry_detail_description)
        entryDetailsLicense = findViewById(R.id.entry_detail_license)
        entryDetailsAuthor = findViewById(R.id.entry_detail_author)
        downloadSize = findViewById(R.id.entry_detail_content_size)
        translationAvailableLabel = findViewById(R.id.entry_detail_available_label)
        flexBox = findViewById(R.id.entry_detail_flex)
        coordinatorLayout = findViewById(R.id.coordinationLayout)
        editButton = findViewById(R.id.edit_content)

        fileStatusIcon[LOCALLY_AVAILABLE_ICON] = R.drawable.ic_nearby_black_24px
        fileStatusIcon[LOCALLY_NOT_AVAILABLE_ICON] = R.drawable.ic_cloud_download_black_24dp

        setUMToolbar(R.id.um_toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setProgressBar()
        showBaseProgressBar(false)

        downloadProgress!!.setOnStopDownloadListener(this)

        findViewById<NestedScrollView>(R.id.nested_scroll).setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if(showControls && ::editButton.isInitialized && editButton.visibility == View.VISIBLE){
                if (scrollY > oldScrollY) {
                    editButton.hide()
                } else {
                    editButton.show()
                }
            }
        }

        editButton.setOnClickListener {
            presenter.handleStartEditingContent()
        }
        downloadButton.setOnClickListener {
            presenter.handleDownloadButtonClick()
        }
    }

    override fun onResume() {
        super.onResume()
        if(::managerAndroidBle.isInitialized) {
            managerAndroidBle.enablePromptsSnackbarManager.makeSnackbarIfRequired(
                    findViewById(R.id.coordinationLayout), this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_content_entry_details, menu)
        return super.onCreateOptionsMenu(menu)
    }


    @SuppressLint("RestrictedApi")
    override fun setEditButtonVisible(show: Boolean) {
       if(::editButton.isInitialized){
           editButton.visibility = if(show) View.VISIBLE else View.GONE
       }
    }

    override fun setExportContentIconVisible(visible: Boolean){
        this.showExportIcon = visible
        invalidateOptionsMenu()
    }


    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu!!.findItem(R.id.export_content).isVisible = showExportIcon
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                clickUpNavigation()
                return true
            }

            R.id.export_content ->{
                presenter.handleContentEntryExport()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clickUpNavigation() {
        runOnUiThread {
            presenter.handleUpNavigation()
        }
    }

    override fun setContentEntry(contentEntry: ContentEntry) {
        viewBinding.contentEntryModel = ContentEntryModel(contentEntry)
        supportActionBar?.title = contentEntry.title

        UMAndroidUtil.loadImage(contentEntry.thumbnailUrl,R.drawable.img_placeholder,
                findViewById<View>(R.id.entry_detail_thumbnail) as ImageView)
    }

    override fun setDownloadJobItemStatus(downloadJobItem: DownloadJobItem?) {
        if(currentDownloadJobItemStatus != downloadJobItem?.djiStatus) {
            when {
                downloadJobItem.isStatusCompletedSuccessfully() -> {
                    entry_download_open_button.visibility = View.VISIBLE
                    entry_download_open_button.text = resources.getText(R.string.open)
                    entry_detail_progress.visibility = View.GONE
                }

                downloadJobItem.isStatusQueuedOrDownloading() -> {
                    entry_download_open_button.visibility = View.GONE
                    entry_detail_progress.visibility = View.VISIBLE
                }

                else -> {
                    entry_download_open_button.text = resources.getText(R.string.download)
                    entry_download_open_button.visibility = View.VISIBLE
                    entry_detail_progress.visibility = View.GONE
                }
            }

            currentDownloadJobItemStatus = downloadJobItem?.djiStatus ?: 0
        }

        if(downloadJobItem != null && downloadJobItem.isStatusQueuedOrDownloading()) {
            entry_detail_progress.statusText = downloadJobItem.toStatusString(
                    UstadMobileSystemImpl.instance, this)
            entry_detail_progress.progress = if(downloadJobItem.downloadLength > 0) {
                (downloadJobItem.downloadedSoFar.toFloat()) / (downloadJobItem.downloadLength.toFloat())
            }else {
                0f
            }
        }
    }

    override fun setContentEntryLicense(license: String) {
        entryDetailsLicense!!.text = license
    }

    override fun setMainButtonEnabled(enabled: Boolean) {
        downloadButton.isEnabled = enabled
    }

    override fun setDownloadSize(fileSize: Long) {
        downloadSize!!.text = UMFileUtil.formatFileSize(fileSize)
    }

    override fun showFileOpenError(message: String, actionMessageId: Int, mimeType: String) {
        showSnackBarNotification(message, {
            var appPackageName = mimeTypeToPlayStoreIdMap[mimeType]
            if (appPackageName == null) {
                appPackageName = "cn.wps.moffice_eng"
            }
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (anfe: android.content.ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }, actionMessageId)
    }

    override fun showFileOpenError(message: String) {
        showSnackBarNotification(message, {}, 0)
    }


    override fun updateLocalAvailabilityViews(icon: Int, status: String) {
        localAvailabilityStatusText!!.visibility = View.VISIBLE
        localAvailabilityStatusIcon!!.visibility = View.VISIBLE
        localAvailabilityStatusIcon!!.setImageResource(fileStatusIcon[icon]!!)
        localAvailabilityStatusText!!.text = status
    }

    override fun setLocalAvailabilityStatusViewVisible(visible: Boolean) {
        localAvailabilityStatusIcon!!.visibility = if (visible) View.VISIBLE else View.GONE
        localAvailabilityStatusText!!.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun showDownloadOptionsDialog(map: Map<String, String>) {
        val impl = UstadMobileSystemImpl.instance
        runAfterGrantingPermission(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Runnable { impl.go("DownloadDialog", map, this) },
                impl.getString(MessageID.download_storage_permission_title, this),
                impl.getString(MessageID.download_storage_permission_message, this))
    }

    override fun selectContentEntryOfLanguage(contentEntryUid: Long) {
        presenter.handleClickTranslatedEntry(contentEntryUid)
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun setAvailableTranslations(result: List<ContentEntryRelatedEntryJoinWithLanguage>) {

        translationAvailableLabel!!.visibility = if (result.isNotEmpty()) View.VISIBLE else View.GONE
        flexBox.visibility = if (result.isNotEmpty()) View.VISIBLE else View.GONE

        val flexboxLayoutManager = FlexboxLayoutManager(applicationContext)
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        flexBox.layoutManager = flexboxLayoutManager
        val adapter = ContentEntryDetailLanguageAdapter(result, this)
        flexBox.adapter = adapter
    }

    override fun onClickStopDownload(view: DownloadProgressView) {
        GlobalScope.launch {
            presenter.handleCancelDownload()
        }
    }
}
