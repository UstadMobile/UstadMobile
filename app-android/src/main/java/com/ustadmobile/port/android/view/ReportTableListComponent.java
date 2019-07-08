package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ReportTableListComponentView;
import com.ustadmobile.lib.db.entities.ReportSalesLog;
import com.ustadmobile.lib.db.entities.ReportTopLEs;

import java.util.List;

/**
 * Custom view for table charts Sales log
 */
public class ReportTableListComponent extends LinearLayout implements ReportTableListComponentView {

    Context mContext;

    public ReportTableListComponent(Context context) {
        super(context);
        mContext = context;
    }

    public ReportTableListComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReportTableListComponent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void setSalesLogData(List<Object> dataSet) {
        runOnUiThread(() -> {
            removeAllViews();
            LinearLayout logs = createSalesLog(dataSet);
            addView(logs);
        });
    }

    @Override
    public void setTopLEsData(List<Object> dataSet) {
        runOnUiThread(() -> {
            removeAllViews();
            LinearLayout logs = createTopLEs(dataSet);
            addView(logs);
        });
    }

    private LinearLayout createSalesLog(List<Object> dataSet){
        LinearLayout topLL = new LinearLayout(mContext);
        topLL.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams wrapParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        topLL.setLayoutParams(params);

        for(Object data:dataSet){
            ReportSalesLog entry = (ReportSalesLog)data;

            TextView t1 = new TextView(mContext);
            t1.setText(entry.getLeName() + ", at location " + entry.getLocationName());
            t1.setPadding(0,8,0,0);

            topLL.addView(t1);
            TextView v1 = new TextView(mContext);
            v1.setTextSize(18);
            v1.setTextColor(Color.parseColor("#F57C00"));
            v1.setText(String.valueOf(entry.getSaleValue()));

            LinearLayout tLL = new LinearLayout(mContext);
            tLL.setOrientation(LinearLayout.HORIZONTAL);
            tLL.setLayoutParams(wrapParams);
            TextView l1 = new TextView(mContext);
            l1.setText(entry.getProductNames());

            v1.setPadding(0,0, 32, 0);
            tLL.addView(v1);
            l1.setPadding(32,0,0,0);
            tLL.addView(l1);

            topLL.addView(tLL);

            v1.setPadding(0,0,0,8);
            TextView d1 = new TextView(mContext);
            d1.setText(UMCalendarUtil.getPrettyDateSuperSimpleFromLong(entry.getSaleDate(), null));
            d1.setPadding(0,0,0,8);
            topLL.addView(d1);
            topLL.addView(getHorizontalLine());
        }

        return topLL;

    }

    private LinearLayout createTopLEs(List<Object> dataSet){

        LinearLayout topLL = new LinearLayout(mContext);
        topLL.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        topLL.setLayoutParams(params);

        for(Object data:dataSet){
            ReportTopLEs entry = (ReportTopLEs) data;
            TextView t1 = new TextView(mContext);
            t1.setText(entry.getLeName());
            t1.setPadding(0,8,0,0);
            topLL.addView(t1);
            TextView v1 = new TextView(mContext);
            v1.setTextSize(18);
            v1.setTextColor(Color.parseColor("#F57C00"));
            v1.setText(String.valueOf(entry.getTotalSalesValue()));
            topLL.addView(v1);
            v1.setPadding(0,0,0,8);
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
        View hl = new View(mContext);
        hl.setBackgroundColor(Color.GRAY);
        hl.setLayoutParams(hlineParams);
        return hl;
    }

    @Override
    public void runOnUiThread(Runnable r) {
        ((Activity)mContext).runOnUiThread(r);
    }
}
