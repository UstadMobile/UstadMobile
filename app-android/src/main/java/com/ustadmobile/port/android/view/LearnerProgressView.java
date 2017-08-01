package com.ustadmobile.port.android.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.txusballesteros.widgets.FitChart;
import com.txusballesteros.widgets.FitChartValue;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.CourseProgress;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mike on 7/25/17.
 */

public class LearnerProgressView extends LinearLayout {

    private CourseProgress progress;

    FitChart chart;

    private static final HashMap<Integer, Integer> STATUS_TO_COLOR_MAP = new HashMap<>();

    static {
        STATUS_TO_COLOR_MAP.put(MessageID.in_progress, R.color.entry_learner_progress_in_progress);
        STATUS_TO_COLOR_MAP.put(MessageID.failed_message, R.color.entry_learner_progresss_failed);
        STATUS_TO_COLOR_MAP.put(MessageID.passed, R.color.entry_learner_progress_passed);
    }


    public LearnerProgressView(Context context) {
        super(context);
        init();
    }

    public LearnerProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LearnerProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.view_learner_progress, this);
        this.chart = (FitChart)findViewById(R.id.opds_item_learner_progress_fitchart);
        chart.setMinValue(0f);
        chart.setMaxValue(100);
    }


    public void setProgress(CourseProgress progress){
        this.progress = progress;

        int percentageToShow =progress.getStatus() == MessageID.in_progress
                ? progress.getProgress() : Math.round(progress.getScore() * 100);

        int statusColorId = ContextCompat.getColor(getContext(),
                STATUS_TO_COLOR_MAP.get(progress.getStatus()));
        FitChartValue chartValue = new FitChartValue(percentageToShow, statusColorId);
        ArrayList<FitChartValue> chartValues = new ArrayList<>();
        chartValues.add(chartValue);
        chart.setValues(chartValues);
        TextView progressNumTextView = (TextView)findViewById(
                R.id.opds_item_learner_progress_text);
        progressNumTextView.setText(percentageToShow + "%");
        progressNumTextView.setTextColor(statusColorId);

        TextView progressTextView = (TextView)findViewById(R.id.opds_item_learner_progress_status_text);
        progressTextView.setText(UstadMobileSystemImpl.getInstance().getString(
                progress.getStatus(), getContext()));
        progressTextView.setTextColor(statusColorId);
    }




}
