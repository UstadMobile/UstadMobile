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
class DownloadProgressView : LinearLayout {

    private lateinit var progressBar: ProgressBar

    private lateinit var downloadPercentageTextView: TextView

    private lateinit var downloadStatusTextView: TextView

    var progress: Float = 0f
        set(progress) {
            val progressPercentage = (progress * 100).roundToInt()
            field = progressPercentage.toFloat()
            progressBar.progress = progressPercentage
            downloadPercentageTextView.text = "$progressPercentage%"
        }

    var statusText: String
        get() = downloadStatusTextView.text.toString()
        set(statusText) {
            if(downloadPercentageTextView.text != statusText)
                downloadStatusTextView.text = statusText
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

}
