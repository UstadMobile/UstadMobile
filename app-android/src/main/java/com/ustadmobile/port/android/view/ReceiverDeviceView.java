package com.ustadmobile.port.android.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;

/**
 * Created by mike on 8/25/17.
 */

public class ReceiverDeviceView extends LinearLayout {

    public ReceiverDeviceView(Context context) {
        super(context);
        init();
    }

    public ReceiverDeviceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReceiverDeviceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_receiver_device, this);
    }

    public void setDeviceName(String deviceName) {
        TextView tv = findViewById(R.id.item_send_course_receiver_name);
        tv.setText(deviceName);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        findViewById(R.id.item_send_course_receiver_name).setEnabled(enabled);
        findViewById(R.id.item_send_course_receiver_icon).setEnabled(enabled);
    }
}
