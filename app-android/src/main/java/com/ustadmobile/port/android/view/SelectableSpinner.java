package com.ustadmobile.port.android.view;

import android.content.Context;
import android.util.AttributeSet;

public class SelectableSpinner extends android.support.v7.widget.AppCompatSpinner {

    OnItemSelectedListener listener;

    public SelectableSpinner(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void setSelection(int position)
    {
        super.setSelection(position);

        if (position == getSelectedItemPosition())
        {
            listener.onItemSelected(null, null, position, 0);
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener)
    {
        this.listener = listener;
    }
}
