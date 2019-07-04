package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.DashboardEntryListPresenter;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.DashboardEntry;
import com.ustadmobile.lib.db.entities.ReportSalesPerformance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        chartLL.removeAllViews();
        switch (dashboardEntry.getDashboardEntryReportType()){
            case DashboardEntry.REPORT_TYPE_SALES_PERFORMANCE:
                if(reportPlaceholder!=null)
                    reportPlaceholder.setVisibility(View.GONE);
                mPresenter.getSalesPerformanceReport(entryUid, entryParams);
                BarChart barChart = createBarChart();
                chartLL.addView(barChart);
                currencyTV.setVisibility(View.VISIBLE);

                break;
            case DashboardEntry.REPORT_TYPE_SALES_LOG:
                if(reportPlaceholder!=null)
                    reportPlaceholder.setVisibility(View.GONE);
                LinearLayout logs = createSalesLog();
                chartLL.addView(logs);
                currencyTV.setVisibility(View.INVISIBLE);
                break;
            case DashboardEntry.REPORT_TYPE_TOP_LES:
                if(reportPlaceholder!=null)
                    reportPlaceholder.setVisibility(View.GONE);
                LinearLayout topLEs = createTopLEs();
                chartLL.addView(topLEs);
                currencyTV.setVisibility(View.INVISIBLE);
                break;
            default:
                if(reportPlaceholder!=null)
                    reportPlaceholder.setVisibility(View.VISIBLE);
                currencyTV.setVisibility(View.INVISIBLE);
                break;
        }

        chartLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.handleClickReport(entryUid, entryParams, reportType);
            }
        });
    }

    public void updateMe(long uid, List<ReportSalesPerformance> data){

        int x;
    }

    private BarChart hideEverythingInBarChart(BarChart barChart){

        //Hide all lines from x, left and right
        //Top values on X Axis
        barChart.getXAxis().setEnabled(true);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawLabels(true);

        //Left Values
        barChart.getAxisLeft().setEnabled(true);
        barChart.getAxisLeft().setDrawTopYLabelEntry(true);

        //Right Values:
        barChart.getAxisRight().setEnabled(false);

        //Legend:
        barChart.getLegend().setEnabled(true);
        barChart.getLegend().setPosition(Legend.LegendPosition.RIGHT_OF_CHART);

        //Label Description
        barChart.getDescription().setEnabled(false);

        barChart.setTouchEnabled(false);



        return barChart;
    }


    private LinearLayout createTopLEs(){
        LinearLayout topLL = new LinearLayout(theContext);
        topLL.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        topLL.setLayoutParams(params);

        String[] names = new String[]{"Roya Rahimi","Laila Gulzar","Meena Hotaki", "Nargis Yousafzai"};
        String[] values = new String[]{"81,756 Afs","70,865 Afs","51,162 Afs", "48,900 Afs"};
        for(int i=0; i<4;i++){

            TextView t1 = new TextView(theContext);
            t1.setText(names[i]);
            t1.setPadding(0,8,0,0);
            topLL.addView(t1);
            TextView v1 = new TextView(theContext);
            v1.setTextSize(18);
            v1.setTextColor(Color.parseColor("#F57C00"));
            v1.setText(values[i]);
            topLL.addView(v1);
            v1.setPadding(0,0,0,8);
            topLL.addView(getHorizontalLine());
        }

        return topLL;

    }

    private LinearLayout createSalesLog(){
        LinearLayout topLL = new LinearLayout(theContext);
        topLL.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams wrapParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        topLL.setLayoutParams(params);

        String[] names = new String[]{"Anoosha", "Leena", "Freshta"};
        String[] products = new String[]{"1x pink hat, 1x maxi dress","10x pink hat","100x pink hat"};
        String[] date = new String[]{"01/June/2019","26/May/2019","20/May/2019"};
        String[] values = new String[]{"4,000 Afs","3,500 Afs","40,000 Afs"};
        String[] location = new String[]{"Kote-Senghi Kabul","Khakrez, Kandahat","Froshga, Kabul"};
        for(int i=0; i<3;i++){

            TextView t1 = new TextView(theContext);
            t1.setText(names[i] + ", at location " + location[i]);
            t1.setPadding(0,8,0,0);

            topLL.addView(t1);
            TextView v1 = new TextView(theContext);
            v1.setTextSize(18);
            v1.setTextColor(Color.parseColor("#F57C00"));
            v1.setText(values[i]);

            LinearLayout tLL = new LinearLayout(theContext);
            tLL.setOrientation(LinearLayout.HORIZONTAL);
            tLL.setLayoutParams(wrapParams);
            TextView l1 = new TextView(theContext);
            l1.setText(products[i]);

            v1.setPadding(0,0, 32, 0);
            tLL.addView(v1);
            l1.setPadding(32,0,0,0);
            tLL.addView(l1);

            topLL.addView(tLL);

            v1.setPadding(0,0,0,8);
            TextView d1 = new TextView(theContext);
            d1.setText(date[i]);
            d1.setPadding(0,0,0,8);
            topLL.addView(d1);
            topLL.addView(getHorizontalLine());
        }

        return topLL;

    }

    /**
     * Creates a new Horizontal line for a table's row.
     * @return  The horizontal line view.
     */
    public View getHorizontalLine(){
        //Horizontal line
        ViewGroup.LayoutParams hlineParams = new ViewGroup.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, 1);
        View hl = new View(theContext);
        hl.setBackgroundColor(Color.GRAY);
        hl.setLayoutParams(hlineParams);
        return hl;
    }

    private BarChart createBarChart(){

        BarChart barChart = new BarChart(theContext);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        barChart.setLayoutParams(params);

        //barChart = setUpCharts(barChart);
        barChart = hideEverythingInBarChart(barChart);

        ArrayList<BarEntry> heratEntries = new ArrayList<>();
        ArrayList<BarEntry> kabulEntries = new ArrayList<>();
        ArrayList<BarEntry> khostEntries = new ArrayList<>();
        ArrayList<BarEntry> kunduzEntries = new ArrayList<>();
        ArrayList<BarEntry> paktikaEntries = new ArrayList<>();

        heratEntries.add(new BarEntry(1,18000));
        heratEntries.add(new BarEntry(2,17000));
        heratEntries.add(new BarEntry(3,17000));
        heratEntries.add(new BarEntry(4,16000));


        kabulEntries.add(new BarEntry(1,95000));
        kabulEntries.add(new BarEntry(2,120000));
        kabulEntries.add(new BarEntry(3,130000));
        kabulEntries.add(new BarEntry(4,122000));


        khostEntries.add(new BarEntry(1,50500));
        khostEntries.add(new BarEntry(2,60000));
        khostEntries.add(new BarEntry(3,59000));
        khostEntries.add(new BarEntry(4,6000));


        kunduzEntries.add(new BarEntry(1,100000));
        kunduzEntries.add(new BarEntry(2,130000));
        kunduzEntries.add(new BarEntry(3,70000));
        kunduzEntries.add(new BarEntry(4,90000));


        paktikaEntries.add(new BarEntry(1,40000));
        paktikaEntries.add(new BarEntry(2,25000));
        paktikaEntries.add(new BarEntry(3,30000));
        paktikaEntries.add(new BarEntry(4,20000));


        BarDataSet barDataSet = new BarDataSet(heratEntries,"Herat");
        barDataSet.setColor(Color.parseColor("#FF9800"));
        BarDataSet barDataSet1 = new BarDataSet(kabulEntries,"Kabul");
        barDataSet1.setColors(Color.parseColor("#FF6D00"));
        BarDataSet barDataSet2 = new BarDataSet(khostEntries,"Khost");
        barDataSet2.setColors(Color.parseColor("#FF5722"));
        BarDataSet barDataSet3 = new BarDataSet(kunduzEntries,"Kunduz");
        barDataSet3.setColors(Color.parseColor("#918F8F"));
        BarDataSet barDataSet4 = new BarDataSet(paktikaEntries,"Paktika");
        barDataSet4.setColors(Color.parseColor("#666666"));

        String[] months = new String[] {"5-May", "12-May", "19-May", "26-May"};
        BarData data = new BarData(barDataSet,barDataSet1,barDataSet2,barDataSet3, barDataSet4);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        barChart.getAxisLeft().setAxisMinimum(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularityEnabled(true);

        float barSpace = 0.02f;
        float groupSpace = 0.3f;
        int groupCount = 4;

        //IMPORTANT *****
        data.setBarWidth(0.15f);
        barChart.getXAxis().setAxisMinimum(0);
        barChart.getXAxis().setAxisMaximum(0 + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);
        barChart.groupBars(0, groupSpace, barSpace); // perform the "explicit" grouping
        //***** IMPORTANT


        //Hide values on top of every bar
        barChart.getBarData().setDrawValues(false);

        return barChart;
    }


    protected class DashboardEntryListViewHolder extends RecyclerView.ViewHolder {
        protected DashboardEntryListViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected DashboardEntryListRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<DashboardEntry> diffCallback,
            DashboardEntryListPresenter thePresenter,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
    }


}
