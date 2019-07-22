package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView

import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.DashboardEntryListPresenter
import com.ustadmobile.core.controller.ReportChartViewComponentPresenter
import com.ustadmobile.core.controller.ReportSalesLogComponentPresenter
import com.ustadmobile.core.controller.ReportTopLEsComponentPresenter
import com.ustadmobile.core.view.ReportOptionsDetailView
import com.ustadmobile.lib.db.entities.DashboardEntry

import java.util.Hashtable


class DashboardEntryListRecyclerAdapter internal constructor(
        diffCallback: DiffUtil.ItemCallback<DashboardEntry>,
        internal var mPresenter: DashboardEntryListPresenter,
        internal var theContext: Context)
    : PagedListAdapter<DashboardEntry,
        DashboardEntryListRecyclerAdapter.DashboardEntryListViewHolder>(diffCallback) {
    internal var theActivity: Activity? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardEntryListViewHolder {
        val list = LayoutInflater.from(theContext).inflate(
                R.layout.item_dashboard_entry, parent, false)
        return DashboardEntryListViewHolder(list)

    }

    override fun onBindViewHolder(holder: DashboardEntryListViewHolder, position: Int) {

        val dashboardEntry = getItem(position)
        val entryUid = dashboardEntry!!.dashboardEntryUid
        val existingTitle = dashboardEntry.dashboardEntryTitle
        val entryParams = dashboardEntry.dashboardEntryReportParam
        val reportType = dashboardEntry.dashboardEntryReportType

        val dots = holder.itemView.findViewById<AppCompatImageView>(R.id.item_dashboard_entry_dots)
        val pin = holder.itemView.findViewById<AppCompatImageView>(R.id.item_dashboard_entry_flag)
        val title = holder.itemView.findViewById<TextView>(R.id.item_dashboard_entry_title)

        //Title
        title.text = existingTitle

        //Pinned
        val pinned: Boolean
        if (dashboardEntry.dashboardEntryIndex < 0) {
            pinned = true
            pin.setColorFilter(ContextCompat.getColor(theContext, R.color.primary_dark))
        } else {
            pinned = false
            pin.setColorFilter(ContextCompat.getColor(theContext, R.color.text_primary))
        }
        pin.setOnClickListener { v -> mPresenter.handlePinEntry(entryUid, pinned) }


        //Options to Edit/Delete every schedule in the list
        dots.setOnClickListener { v: View ->
            //creating a popup menu
            val popup = PopupMenu(theContext, v)
            popup.setOnMenuItemClickListener { item ->
                val i = item.itemId
                if (i == R.id.edit) {
                    mPresenter.handleEditEntry(entryUid)
                    true
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteEntry(entryUid)
                    true
                } else if (i == R.id.set_title) {
                    mPresenter.handleChangeTitle(entryUid, existingTitle!!)
                    true
                } else {
                    false
                }
            }
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_edit_delete_set_title)

            popup.menu.findItem(R.id.edit).isVisible = true

            //displaying the popup
            popup.show()
        }

        //The actual report
        val reportPlaceholder = holder.itemView.findViewById<View>(R.id.item_dashboard_entry_report_view_placeholder)
        val chartLL = holder.itemView.findViewById<LinearLayout>(R.id.item_dashboard_entry_ll)
        val currencyTV = holder.itemView.findViewById<TextView>(R.id.item_dahboard_entry_currency)

        //Common Layout params for chart views to match parent.
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        //Creating the args to be sent to the Chart/Report view presenter
        val args = Hashtable<String, String>()
        args[ReportOptionsDetailView.ARG_REPORT_OPTIONS] = entryParams


        //Creating a different chart/report view depending on the report type of the current
        //dashboard entry.
        when (dashboardEntry.dashboardEntryReportType) {
            DashboardEntry.REPORT_TYPE_SALES_PERFORMANCE -> {

                //Have currency visible and placeholder gone.
                if (reportPlaceholder != null)
                    reportPlaceholder.visibility = View.GONE
                currencyTV.visibility = View.VISIBLE

                //Clear before adding.
                chartLL.removeAllViews()

                //Create the chart component, sets its layout and call the presenter on the view.
                val chartComponent = ReportSalesPerformanceChartComponent(theContext)
                chartComponent.layoutParams = params
                val cPresenter = ReportChartViewComponentPresenter(this, args, chartComponent)
                cPresenter.onCreate(args)

                chartLL.addView(chartComponent)
            }
            DashboardEntry.REPORT_TYPE_SALES_LOG -> {
                if (reportPlaceholder != null)
                    reportPlaceholder.visibility = View.GONE
                currencyTV.visibility = View.INVISIBLE

                //Clear before adding.
                chartLL.removeAllViews()

                //Create View, presenter and add it to the view
                val salesLogComponent = ReportTableListComponent(theContext)
                salesLogComponent.layoutParams = params
                val lPresenter = ReportSalesLogComponentPresenter(this, args, salesLogComponent)
                lPresenter.onCreate(args)

                chartLL.addView(salesLogComponent)
            }
            DashboardEntry.REPORT_TYPE_TOP_LES -> {
                if (reportPlaceholder != null)
                    reportPlaceholder.visibility = View.GONE
                currencyTV.visibility = View.INVISIBLE

                //
                //Create View, presenter and add it to the view
                val topLEsComponent = ReportTableListComponent(theContext)
                topLEsComponent.layoutParams = params
                val tPresenter = ReportTopLEsComponentPresenter(this, args, topLEsComponent)
                tPresenter.onCreate(args)

                chartLL.addView(topLEsComponent)
            }
            else -> {
                chartLL.removeAllViews()
                if (reportPlaceholder != null)
                    reportPlaceholder.visibility = View.VISIBLE
                currencyTV.visibility = View.INVISIBLE
            }
        }

        chartLL.setOnClickListener { v -> mPresenter.handleClickReport(entryUid, entryParams!!, reportType) }
    }


    inner class DashboardEntryListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}
