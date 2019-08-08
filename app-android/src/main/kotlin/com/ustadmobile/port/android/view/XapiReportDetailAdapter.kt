package com.ustadmobile.port.android.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.dao.StatementDao
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.StatementEntity
import java.text.DateFormat
import java.util.*

class XapiReportDetailAdapter(var context: Context) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var subgroup: Map<String, String> = mapOf()
    private var xAxisLabels: Map<String, String> = mapOf()
    private var options: XapiReportOptions = XapiReportOptions()
    private var chartData: List<StatementDao.ReportData> = listOf()
    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1
    private var myDataset: List<StatementDao.ReportListData> = listOf()
    private var verticalText: String = ""
    private var locale: Locale
    private var df: DateFormat


    init {
        val languageSetting = UstadMobileSystemImpl.instance.getLocale(context)
        locale = if (languageSetting == UstadMobileSystemCommon.LOCALE_USE_SYSTEM)
            Locale.getDefault()
        else
            Locale(languageSetting)

        df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
    }

    fun submitList(list: List<StatementDao.ReportListData>) {
        myDataset = list
        notifyDataSetChanged()
    }

    fun showVerticalTextView(yAxisLabel: String) {
        verticalText = yAxisLabel
        notifyDataSetChanged()
    }

    fun showChart(chartData: List<StatementDao.ReportData>, options: XapiReportOptions, xAxisLabels: Map<String, String>, subgroupLabels: Map<String, String>) {
        this.chartData = chartData
        this.options = options
        this.xAxisLabels = xAxisLabels
        this.subgroup = subgroupLabels
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) {
            val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.xapi_header_report, parent, false)
            return HeaderViewHolder(layoutView)
        } else if (viewType == TYPE_ITEM) {
            val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_xapi_report, parent, false)
            return ItemViewHolder(layoutView)
        }
        throw RuntimeException("No match for $viewType.")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            //show the header
            holder.verticalTextView.text = verticalText
            holder.chartView.setChartData(chartData, options, xAxisLabels, subgroup)
        } else {
            // do the normal
            var holder = holder as ItemViewHolder
            var data = myDataset[position - 1]
            holder.person.text = data.name
            holder.verb.text = data.verb
            holder.result.text = when (data.result) {
                StatementEntity.RESULT_SUCCESS -> "Success"
                StatementEntity.RESULT_FAILURE -> "Failed"
                else -> "-"
            }
            holder.whenDate.text = df.format(Date(data.whenDate))
        }
    }

    override fun getItemCount() = myDataset.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val person: TextView = view.findViewById(R.id.xapi_person)
        val verb: TextView = view.findViewById(R.id.xapi_verb)
        val result: TextView = view.findViewById(R.id.xapi_result)
        val whenDate: TextView = view.findViewById(R.id.xapi_when)
    }

    inner class HeaderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val chartView: XapiChartView = view.findViewById(R.id.preview_chart_view)
        val verticalTextView: VerticalTextView = view.findViewById(R.id.preview_ylabel)
    }

}