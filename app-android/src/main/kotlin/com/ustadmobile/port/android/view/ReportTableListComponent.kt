package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.FileProvider

import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ReportTableListComponentView
import com.ustadmobile.lib.db.entities.ReportSalesLog
import com.ustadmobile.lib.db.entities.ReportTopLEs
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.ArrayList

/**
 * Custom view for table charts Sales log
 */
class ReportTableListComponent : LinearLayout, ReportTableListComponentView {

    var currentDataSet: List<Any>? = null


    override val viewContext: Any
        get() = context!!

    internal lateinit var mContext: Context

    /**
     * Creates a new Horizontal line for a table's row.
     * @return  The horizontal line view.
     */
    //Horizontal line
    val horizontalLine: View
        get() {
            val hlineParams = ViewGroup.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, 1)
            val hl = View(mContext)
            hl.setBackgroundColor(Color.GRAY)
            hl.layoutParams = hlineParams
            return hl
        }

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    override fun setSalesLogData(dataSet: List<Any>) {
        currentDataSet = dataSet
        runOnUiThread (Runnable{
            removeAllViews()
            val logs = createSalesLog(dataSet)
            addView(logs)
        })
    }

    override fun setTopLEsData(dataSet: List<Any>) {
        runOnUiThread (Runnable{
            removeAllViews()
            val logs = createTopLEs(dataSet)
            addView(logs)
        })
    }

    /**
     * Used to construct the export report (has line by line information)
     */
    internal lateinit var tableTextData: MutableList<Array<String>>


    override fun downloadReport() {



        val csvReportFilePath: String
        //Create the file.

        val dir = context.filesDir
        val output = File(dir, "report_table_log_" +
                System.currentTimeMillis() + ".csv")
        csvReportFilePath = output.absolutePath

        try {
            val fileWriter = FileWriter(csvReportFilePath)

            for (aTableTextData in tableTextData) {
                var firstDone = false
                for (aLineArray in aTableTextData) {
                    if (firstDone) {
                        fileWriter.append(",")
                    }
                    firstDone = true
                    fileWriter.append(aLineArray)
                }
                fileWriter.append("\n")
            }
            fileWriter.close()


        } catch (e: IOException) {
            e.printStackTrace()
        }

        val applicationId = context.packageName
        val sharedUri = FileProvider.getUriForFile(context,
                "$applicationId.provider",
                File(csvReportFilePath))
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "*/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, sharedUri)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (shareIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(shareIntent)
        }


    }

    private fun createSalesLog(dataSet: List<Any>): LinearLayout {
        tableTextData = ArrayList()

        val topLL = LinearLayout(mContext)
        topLL.orientation = LinearLayout.VERTICAL
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        val wrapParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        topLL.layoutParams = params

        for (data in dataSet) {
            val entry = data as ReportSalesLog

            val t1 = TextView(mContext)
            t1.text = entry.leName + ", at location " + entry.locationName
            t1.setPadding(0, 8, 0, 0)

            topLL.addView(t1)
            val v1 = TextView(mContext)
            v1.textSize = 18f
            v1.setTextColor(Color.parseColor("#F57C00"))
            v1.text = entry.saleValue.toString()

            val tLL = LinearLayout(mContext)
            tLL.orientation = LinearLayout.HORIZONTAL
            tLL.layoutParams = wrapParams
            val l1 = TextView(mContext)
            l1.text = entry.productNames

            v1.setPadding(0, 0, 32, 0)
            tLL.addView(v1)
            l1.setPadding(32, 0, 0, 0)
            tLL.addView(l1)

            topLL.addView(tLL)

            v1.setPadding(0, 0, 0, 8)
            val d1 = TextView(mContext)
            d1.text = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(entry.saleDate, null)
            d1.setPadding(0, 0, 0, 8)
            topLL.addView(d1)
            topLL.addView(horizontalLine)


            val array = arrayOf(t1.text.toString(), v1.text.toString(), l1.text.toString(),
                    d1.text.toString())
            tableTextData.add(array)
        }

        return topLL

    }

    private fun createTopLEs(dataSet: List<Any>): LinearLayout {
        tableTextData = ArrayList()

        val topLL = LinearLayout(mContext)
        topLL.orientation = LinearLayout.VERTICAL
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        topLL.layoutParams = params

        for (data in dataSet) {
            val entry = data as ReportTopLEs
            val t1 = TextView(mContext)
            t1.text = entry.leName
            t1.setPadding(0, 8, 0, 0)
            topLL.addView(t1)
            val v1 = TextView(mContext)
            v1.textSize = 18f
            v1.setTextColor(Color.parseColor("#F57C00"))
            v1.text = entry.totalSalesValue.toString()
            topLL.addView(v1)
            v1.setPadding(0, 0, 0, 8)
            topLL.addView(horizontalLine)

            val array = arrayOf(t1.text.toString(), v1.text.toString())
            tableTextData.add(array)
        }


        return topLL

    }

    override fun runOnUiThread(r: Runnable?) {
        (mContext as Activity).runOnUiThread(r)
    }
}
