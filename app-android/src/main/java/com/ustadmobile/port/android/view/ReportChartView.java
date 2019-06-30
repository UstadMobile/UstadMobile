package com.ustadmobile.port.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.support.annotation.Nullable;

import com.github.mikephil.charting.charts.BarChart;

public class ReportChartView extends LinearLayout {

    BarChart barChart;

    public ReportChartView(Context context) {
        super(context);
    }

    public ReportChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReportChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ReportChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }




}
