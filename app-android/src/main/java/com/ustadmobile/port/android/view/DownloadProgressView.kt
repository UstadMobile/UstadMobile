package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

import com.toughra.ustadmobile.R
import kotlin.math.roundToInt

/**
 * Created by mike on 9/22/17.
 */
class DownloadProgressView : LinearLayout, View.OnClickListener {

    private var progressBar: ProgressBar? = null

    private var downloadPercentageTextView: TextView? = null

    private var downloadStatusTextView: TextView? = null

    private var downloadStopListener: OnStopDownloadListener? = null

    var progress: Float = 0f
        set(progress) {
            val progressPercentage = (progress * 100).roundToInt()
            field = progressPercentage.toFloat()
            progressBar?.progress = progressPercentage
            downloadPercentageTextView?.text = "$progressPercentage%"
        }

    var statusText: String
        get() = downloadStatusTextView!!.text.toString()
        set(statusText) {
            downloadStatusTextView!!.text = statusText
        }

    interface OnStopDownloadListener {
        fun onClickStopDownload(view: DownloadProgressView)
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
        View.inflate(context, R.layout.view_download_progress, this)
        progressBar = findViewById(R.id.view_download_progress_progressbar)
        downloadPercentageTextView = findViewById(R.id.view_download_progress_status_percentage_text)
        downloadStatusTextView = findViewById(R.id.view_download_progress_status_text)
    }

    override fun onClick(view: View) {
        downloadStopListener?.onClickStopDownload(this)
    }

    fun setOnStopDownloadListener(listener: OnStopDownloadListener) {
        this.downloadStopListener = listener
    }
}
