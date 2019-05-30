package com.ustadmobile.port.android.view

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ContentEntryDetailPresenter
import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.LOCALLY_AVAILABLE_ICON
import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.LOCALLY_NOT_AVAILABLE_ICON
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.LocalAvailabilityListener
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.util.ContentEntryUtil
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroidBle
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle
import java.util.*

class ContentEntryDetailActivity : UstadBaseActivity(), ContentEntryDetailView, ContentEntryDetailLanguageAdapter.AdapterViewListener, LocalAvailabilityMonitor, LocalAvailabilityListener {

    private var entryDetailPresenter: ContentEntryDetailPresenter? = null

    private var managerAndroidBle: NetworkManagerAndroidBle? = null

    private var localAvailabilityStatusText: TextView? = null

    private var entryDetailsTitle: TextView? = null

    private var entryDetailsDesc: TextView? = null

    private var entryDetailsLicense: TextView? = null

    private var entryDetailsAuthor: TextView? = null

    private var translationAvailableLabel: TextView? = null

    private var downloadSize: TextView? = null

    private var localAvailabilityStatusIcon: ImageView? = null

    private var flexBox: RecyclerView? = null

    private var downloadButton: Button? = null

    private var downloadProgress: DownloadProgressView? = null

    internal var fileStatusIcon = HashMap<Int, Int>()

    override val allKnowAvailabilityStatus: Set<Long>
        get() = managerAndroidBle!!.getLocallyAvailableContainerUids()


    override fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle?) {
        super.onBleNetworkServiceBound(networkManagerBle)
        if (networkManagerBle != null && networkManagerBle.isVersionKitKatOrBelow) {
            downloadButton!!.setBackgroundResource(
                    R.drawable.pre_lollipop_btn_selector_bg_entry_details)
        }

        managerAndroidBle = networkManagerBle as NetworkManagerAndroidBle?
        entryDetailPresenter = ContentEntryDetailPresenter(this,
                bundleToMap(intent.extras), this,
                this, networkManagerBle)
        entryDetailPresenter!!.onCreate(bundleToMap(Bundle()))
        entryDetailPresenter!!.onStart()
        managerAndroidBle!!.addLocalAvailabilityListener(this)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_detail)

        localAvailabilityStatusText = findViewById(R.id.content_status_text)
        localAvailabilityStatusIcon = findViewById(R.id.content_status_icon)
        downloadButton = findViewById(R.id.entry_detail_button)
        downloadProgress = findViewById(R.id.entry_detail_progress)
        entryDetailsTitle = findViewById(R.id.entry_detail_title)
        entryDetailsDesc = findViewById(R.id.entry_detail_description)
        entryDetailsLicense = findViewById(R.id.entry_detail_license)
        entryDetailsAuthor = findViewById(R.id.entry_detail_author)
        downloadSize = findViewById(R.id.entry_detail_content_size)
        translationAvailableLabel = findViewById(R.id.entry_detail_available_label)
        flexBox = findViewById(R.id.entry_detail_flex)

        fileStatusIcon[LOCALLY_AVAILABLE_ICON] = R.drawable.ic_nearby_black_24px
        fileStatusIcon[LOCALLY_NOT_AVAILABLE_ICON] = R.drawable.ic_cloud_download_black_24dp

        setUMToolbar(R.id.entry_detail_toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                clickUpNavigation()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clickUpNavigation() {
        runOnUiThread {
            if (entryDetailPresenter != null) {
                entryDetailPresenter!!.handleUpNavigation()
            }
        }


    }


    override fun setContentEntryTitle(title: String) {
        entryDetailsTitle!!.text = title
        supportActionBar!!.title = title
    }

    override fun setContentEntryDesc(desc: String) {
        entryDetailsDesc!!.text = desc
    }

    override fun setContentEntryLicense(license: String) {
        entryDetailsLicense!!.text = license
    }

    override fun setContentEntryAuthor(author: String) {
        entryDetailsAuthor!!.text = author
    }

    override fun setDetailsButtonEnabled(enabled: Boolean) {
        downloadButton!!.isEnabled = enabled
    }

    override fun setDownloadSize(fileSize: Long) {
        downloadSize!!.text = UMFileUtil.formatFileSize(fileSize)
    }

    override fun loadEntryDetailsThumbnail(thumbnailUrl: String) {

        Picasso.get()
                .load(thumbnailUrl)
                .into(findViewById<View>(R.id.entry_detail_thumbnail) as ImageView)
    }


    override fun updateDownloadProgress(progressValue: Float) {
        downloadProgress!!.progress = progressValue
    }

    override fun setDownloadButtonVisible(visible: Boolean) {
        downloadButton!!.visibility = if (visible) View.VISIBLE else View.GONE

    }


    override fun setButtonTextLabel(textLabel: String) {
        downloadButton!!.text = textLabel
    }

    override fun showFileOpenError(message: String, actionMessageId: Int, mimeType: String) {
        showErrorNotification(message, {
            var appPackageName = ContentEntryUtil.mimeTypeToPlayStoreIdMap[mimeType]
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
        showErrorNotification(message, {}, 0)
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

    override fun setTranslationLabelVisible(visible: Boolean) {
        translationAvailableLabel!!.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun setFlexBoxVisible(visible: Boolean) {
        flexBox!!.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun setDownloadProgressVisible(visible: Boolean) {
        downloadProgress!!.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun setDownloadProgressLabel(progressLabel: String) {
        downloadProgress!!.statusText = progressLabel
    }

    override fun setDownloadButtonClickableListener(isDownloadComplete: Boolean) {
        downloadButton!!.setOnClickListener { view ->
            entryDetailPresenter!!.handleDownloadButtonClick(isDownloadComplete,
                    entryDetailPresenter!!.entryUuid)
        }
    }

    override fun showDownloadOptionsDialog(args: HashMap<String, String>) {
        val impl = UstadMobileSystemImpl.instance
        runAfterGrantingPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Runnable { impl.go("DownloadDialog", args, this) },
                impl.getString(MessageID.download_storage_permission_title, this),
                impl.getString(MessageID.download_storage_permission_message, this))
    }

    override fun selectContentEntryOfLanguage(uid: Long) {
        entryDetailPresenter!!.handleClickTranslatedEntry(uid)
    }

    override fun startMonitoringAvailability(monitor: Any, entryUidsToMonitor: List<Long>) {
        managerAndroidBle!!.startMonitoringAvailability(monitor, entryUidsToMonitor)
    }

    override fun stopMonitoringAvailability(monitor: Any) {
        managerAndroidBle!!.stopMonitoringAvailability(monitor)
    }

    override fun onDestroy() {
        entryDetailPresenter!!.onDestroy()
        networkManagerBle?.removeLocalAvailabilityListener(this)
        super.onDestroy()
    }

    override fun onLocalAvailabilityChanged(locallyAvailableEntries: Set<Long>) {
        entryDetailPresenter!!.handleLocalAvailabilityStatus(locallyAvailableEntries)
    }

    override fun setAvailableTranslations(result: List<ContentEntryRelatedEntryJoinWithLanguage>, entryUuid: Long) {
        val flexboxLayoutManager = FlexboxLayoutManager(applicationContext)
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        flexBox!!.layoutManager = flexboxLayoutManager

        val adapter = ContentEntryDetailLanguageAdapter(result,
                this, entryUuid)
        flexBox!!.adapter = adapter
    }
}
