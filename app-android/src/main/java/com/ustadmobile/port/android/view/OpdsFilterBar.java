package com.ustadmobile.port.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;

import com.ustadmobile.core.opds.OpdsFilterOptions;

/**
 * Created by mike on 12/17/17.
 */

public class OpdsFilterBar extends LinearLayout {

    private OpdsFilterOptions filterOptions;

    public OpdsFilterBar(Context context) {
        super(context);
    }

    public OpdsFilterBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OpdsFilterBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(26)
    public OpdsFilterBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public OpdsFilterOptions getFilterOptions() {
        return filterOptions;
    }

    public void setFilterOptions(OpdsFilterOptions filterOptions) {
        this.filterOptions = filterOptions;
        removeAllViews();

        if(filterOptions == null)
            return;

        for(int i = 0; i < filterOptions.getNumOptions(); i++) {
            AppCompatSpinner newSpinner = new AppCompatSpinner(getContext());
            SpinnerAdapter adapter = new ArrayAdapter(getContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    filterOptions.getFilter(i).getFilterOptions());
            newSpinner.setAdapter(adapter);
            addView(newSpinner);
        }
    }
}
