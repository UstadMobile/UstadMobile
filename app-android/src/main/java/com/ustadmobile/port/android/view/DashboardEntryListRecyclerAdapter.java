package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.DashboardEntryListPresenter;
import com.ustadmobile.core.controller.ReportChartViewComponentPresenter;
import com.ustadmobile.core.controller.ReportSalesLogComponentPresenter;
import com.ustadmobile.core.controller.ReportTopLEsComponentPresenter;
import com.ustadmobile.lib.db.entities.DashboardEntry;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ReportOptionsDetailView.ARG_REPORT_OPTIONS;

public class DashboardEntryListRecyclerAdapter extends
        PagedListAdapter<DashboardEntry,
                DashboardEntryListRecyclerAdapter.DashboardEntryListViewHolder> {

    Context theContext;
    Activity theActivity;
    DashboardEntryListPresenter mPresenter;

    @NonNull
    @Override
    public DashboardEntryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_dashboard_entry, parent, false);
        return new DashboardEntryListViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull DashboardEntryListViewHolder holder, int position) {

        DashboardEntry dashboardEntry = getItem(position);
        long entryUid = dashboardEntry.getDashboardEntryUid();
        String existingTitle = dashboardEntry.getDashboardEntryTitle();
        String entryParams = dashboardEntry.getDashboardEntryReportParam();
        int reportType = dashboardEntry.getDashboardEntryReportType();

        AppCompatImageView dots = holder.itemView.findViewById(R.id.item_dashboard_entry_dots);
        AppCompatImageView pin = holder.itemView.findViewById(R.id.item_dashboard_entry_flag);
        TextView title = holder.itemView.findViewById(R.id.item_dashboard_entry_title);

        //Title
        title.setText(existingTitle);

        //Pinned
        boolean pinned;
        if(dashboardEntry.getDashboardEntryIndex() < 0 ){
            pinned=true;
            pin.setColorFilter(ContextCompat.getColor(theContext, R.color.primary_dark));
        }else{
            pinned = false;
            pin.setColorFilter(ContextCompat.getColor(theContext, R.color.text_primary));
        }
        boolean finalPinned = pinned;
        pin.setOnClickListener(v -> mPresenter.handlePinEntry(entryUid, finalPinned));


        //Options to Edit/Delete every schedule in the list
        dots.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theContext, v);
            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    mPresenter.handleEditEntry(entryUid);
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteEntry(entryUid);
                    return true;
                } else if (i == R.id.set_title) {
                    mPresenter.handleChangeTitle(entryUid, existingTitle);
                    return true;
                } else {
                    return false;
                }
            });
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_edit_delete_set_title);

            popup.getMenu().findItem(R.id.edit).setVisible(true);

            //displaying the popup
            popup.show();
        });

        //The actual report
        View reportPlaceholder =
                holder.itemView.findViewById(R.id.item_dashboard_entry_report_view_placeholder);
        LinearLayout chartLL =
                holder.itemView.findViewById(R.id.item_dashboard_entry_ll);
        TextView currencyTV =
                holder.itemView.findViewById(R.id.item_dahboard_entry_currency);

        //Common Layout params for chart views to match parent.
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        //Creating the args to be sent to the Chart/Report view presenter
        Hashtable<String, String> args = new Hashtable<>();
        args.put(ARG_REPORT_OPTIONS, entryParams);


        //Creating a different chart/report view depending on the report type of the current
        //dashboard entry.
        switch (dashboardEntry.getDashboardEntryReportType()){
            case DashboardEntry.REPORT_TYPE_SALES_PERFORMANCE:

                //Have currency visible and placeholder gone.
                if(reportPlaceholder!=null)
                    reportPlaceholder.setVisibility(View.GONE);
                currencyTV.setVisibility(View.VISIBLE);

                //Clear before adding.
                chartLL.removeAllViews();

                //Create the chart component, sets its layout and call the presenter on the view.
                ReportSalesPerformanceChartComponent chartComponent =
                        new ReportSalesPerformanceChartComponent(theContext);
                chartComponent.setLayoutParams(params);
                ReportChartViewComponentPresenter cPresenter =
                        new ReportChartViewComponentPresenter(this,args, chartComponent);
                cPresenter.onCreate(args);

                chartLL.addView(chartComponent);
                break;
            case DashboardEntry.REPORT_TYPE_SALES_LOG:
                if(reportPlaceholder!=null)
                    reportPlaceholder.setVisibility(View.GONE);
                currencyTV.setVisibility(View.INVISIBLE);

                //Clear before adding.
                chartLL.removeAllViews();

                //Create View, presenter and add it to the view
                ReportTableListComponent salesLogComponent =
                        new ReportTableListComponent(theContext);
                salesLogComponent.setLayoutParams(params);
                ReportSalesLogComponentPresenter lPresenter =
                        new ReportSalesLogComponentPresenter(this, args, salesLogComponent);
                lPresenter.onCreate(args);

                chartLL.addView(salesLogComponent);


                break;
            case DashboardEntry.REPORT_TYPE_TOP_LES:
                if(reportPlaceholder!=null)
                    reportPlaceholder.setVisibility(View.GONE);
                currencyTV.setVisibility(View.INVISIBLE);

                //
                //Create View, presenter and add it to the view
                ReportTableListComponent topLEsComponent =
                        new ReportTableListComponent(theContext);
                topLEsComponent.setLayoutParams(params);
                ReportTopLEsComponentPresenter tPresenter =
                        new ReportTopLEsComponentPresenter(this, args, topLEsComponent);
                tPresenter.onCreate(args);

                chartLL.addView(topLEsComponent);


                break;
            default:
                chartLL.removeAllViews();
                if(reportPlaceholder!=null)
                    reportPlaceholder.setVisibility(View.VISIBLE);
                currencyTV.setVisibility(View.INVISIBLE);
                break;
        }

        chartLL.setOnClickListener(v ->
                mPresenter.handleClickReport(entryUid, entryParams, reportType));
    }


    class DashboardEntryListViewHolder extends RecyclerView.ViewHolder {
        DashboardEntryListViewHolder(View itemView) {
            super(itemView);
        }
    }

    DashboardEntryListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<DashboardEntry> diffCallback,
            DashboardEntryListPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
