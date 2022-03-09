package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewDebug
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.util.ext.isStatusPaused
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemProgress

/**
 * A button that shows the download status of an item. It consists of an icon (a download icon or
 * offline pin, depending on the status), and a determinate circular progress widget to show download
 * progress.
 */
class DownloadStatusButton : RelativeLayout {

    private var mProgressBar: ProgressBar? = null

    private var currentDownloadStatus: Int = -1

    var contentJobItemProgress: ContentJobItemProgress? = null
        set(value){
            field = value
            val downloadLength = value?.total ?: 0
            val downloadedSoFar = value?.progress ?: 0
            progress = if(downloadLength > 0) {
                ((downloadedSoFar.toFloat() / downloadLength) * 100).toInt()
            }else {
                0
            }
        }

    var contentJobItemStatus: Int? = null
        set(value) {
            field = value

            val statusChanged = currentDownloadStatus != value ?: -1
            currentDownloadStatus = value ?: -1

            when {
                statusChanged && currentDownloadStatus.isStatusPaused() -> {
                    setImageResource(R.drawable.ic_baseline_pause_24)
                    contentDescription = context.getString(R.string.download_entry_state_paused)
                }

                statusChanged && currentDownloadStatus == JobStatus.COMPLETE -> {
                    setImageResource(R.drawable.ic_baseline_offline_pin_24)
                    contentDescription = context.getString(R.string.downloaded)
                }

                statusChanged -> {
                    setImageResource(R.drawable.ic_file_download_black_24dp)
                    contentDescription = context.getString(R.string.download)
                }
            }

            takeIf { statusChanged }?.progressVisibility =
                if(currentDownloadStatus != JobStatus.COMPLETE) {
                View.VISIBLE
            }else {
                View.INVISIBLE
            }

        }


    var imageResource: ImageView? = null
        private set

    /**
     * Setter for the progress property
     *
     * @param progress The progress of the circular progress widget that wraps around the icon (0-100)
     */
    /**
     * Getter for the progress property
     *
     * @return The progress of the circular progress widget that wraps around the icon (0-100)
     */
    var progress: Int
        @ViewDebug.ExportedProperty(category = "progress")
        get() = mProgressBar!!.progress
        set(progress) {
            mProgressBar!!.progress = progress
        }

    val max: Int
        @ViewDebug.ExportedProperty(category = "progress")
        get() = mProgressBar!!.max

    /**
     * Sets whether or not the progress elements of the download status are visible, so that these
     * components are only visible if a download is in progress
     *
     * @param visibility visibility flag e.g. View.GONE, View.VISIBLE, etc
     */
    var progressVisibility: Int
        get() = mProgressBar?.visibility ?: View.GONE
        set(visibility) {
            mProgressBar!!.visibility = visibility
        }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.view_download_status_button, this)
        mProgressBar = findViewById(R.id.view_download_status_button_progressbar)
        imageResource = findViewById(R.id.view_download_status_button_img)
    }

    /**
     * Setter for the imageResource property
     *
     * @param resId The resource ID to use for the image to be displayed (e.g. for the download icon, offline pin, etc)
     */
    fun setImageResource(resId: Int) {
        imageResource!!.setImageResource(resId)
    }
}
