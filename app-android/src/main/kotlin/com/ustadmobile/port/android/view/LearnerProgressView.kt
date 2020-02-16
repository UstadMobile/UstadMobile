package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.toughra.ustadmobile.R
//import com.txusballesteros.widgets.FitChart
//import com.txusballesteros.widgets.FitChartValue
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.CourseProgress
import java.util.*

/**
 * Created by mike on 7/25/17.
 */

class LearnerProgressView : LinearLayout {

    private var progress: CourseProgress? = null

    //internal lateinit var chart: FitChart


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
        View.inflate(context, R.layout.view_learner_progress, this)
//        this.chart = findViewById(R.id.opds_item_learner_progress_fitchart)
//        chart.minValue = 0f
//        chart.maxValue = 100f
    }


    fun setProgress(progress: CourseProgress) {
//        this.progress = progress
//
//        val percentageToShow = if (progress.status == MessageID.in_progress)
//            progress.progress
//        else
//            Math.round(progress.score * 100)
//
//        val statusColorId = ContextCompat.getColor(context,
//                STATUS_TO_COLOR_MAP[progress.status]!!)
//        val chartValue = FitChartValue(percentageToShow.toFloat(), statusColorId)
//        val chartValues = ArrayList<FitChartValue>()
//        chartValues.add(chartValue)
//        chart.setValues(chartValues)
//        val progressNumTextView = findViewById<View>(
//                R.id.opds_item_learner_progress_text) as TextView
//        progressNumTextView.text = "$percentageToShow%"
//        progressNumTextView.setTextColor(statusColorId)
//
//        val progressTextView = findViewById<View>(R.id.opds_item_learner_progress_status_text) as TextView
//        progressTextView.text = UstadMobileSystemImpl.instance.getString(
//                progress.status, context)
//        progressTextView.setTextColor(statusColorId)
    }

    companion object {

        private val STATUS_TO_COLOR_MAP = HashMap<Int, Int>()

        init {
            STATUS_TO_COLOR_MAP[MessageID.in_progress] = R.color.entry_learner_progress_in_progress
            STATUS_TO_COLOR_MAP[MessageID.failed] = R.color.entry_learner_progresss_failed
            STATUS_TO_COLOR_MAP[MessageID.passed] = R.color.entry_learner_progress_passed
        }
    }


}
