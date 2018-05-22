package com.ustadmobile.port.android.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.networkmanager.NetworkNode;

/**
 * This view represents a device that the user can send an entry to. When the status of the device
 * is 'invited' a button 'cancel invite' will be shown. This can be used if the invite process, as
 * can occassionally happen, gets stuck.
 *
 * Created by mike on 8/25/17.
 */

public class ReceiverDeviceView extends LinearLayout implements View.OnClickListener {

    private boolean receiverEnabled;

    private int deviceStatus;

    private String deviceId;

    private OnClickCancelInviteListener cancelInviteListener;

    public interface OnClickCancelInviteListener {

        void onClickCancelInvite(ReceiverDeviceView src);

    }

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

    /**
     * Get the device id e.g. it's wifi direct mac address.
     *
     * @return The device id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Set the device id e.g. it's wifi direct mac address
     *
     * @param deviceId The device id
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    private void init() {
        inflate(getContext(), R.layout.view_receiver_device, this);
        findViewById(R.id.view_receiver_device_cancel_button).setOnClickListener(this);
    }

    /**
     * Set the device name.
     *
     * @param deviceName The device name to be shown
     */
    public void setDeviceName(String deviceName) {
        TextView tv = findViewById(R.id.item_send_course_receiver_name);
        tv.setText(deviceName);
    }

    /**
     * Sets whether or not this item is enabled for the user to click on. This is only one part of
     * whether this view is available to be clicked on. The device is only really available when
     * the has the status 'AVAILABLE' as per the STATUS_ constants of NetworkNode.
     *
     * Even if the device is avialable, once the user clicks on any device on the list, the other
     * devices are disabled.
     *
     * @param enabled True if this item should be clickable if it's status is available. False to make it disabled even when the status is available.
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.receiverEnabled = enabled;
        boolean reallyEnabled = receiverEnabled && deviceStatus == NetworkNode.STATUS_AVAILABLE;
        super.setEnabled(reallyEnabled);
        findViewById(R.id.item_send_course_receiver_name).setEnabled(reallyEnabled);
        findViewById(R.id.item_send_course_receiver_icon).setEnabled(reallyEnabled);
    }

    /**
     * The device status as per NetworkNode.STATUS_ flags
     *
     * @param deviceStatus
     */
    public void setDeviceStatus(int deviceStatus) {
        this.deviceStatus = deviceStatus;
        if(deviceStatus == NetworkNode.STATUS_INVITED) {
            findViewById(R.id.view_receiver_device_cancel_button).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.view_receiver_device_cancel_button).setVisibility(View.GONE);
        }

        setEnabled(receiverEnabled);
    }

    @Override
    public void onClick(View view) {
        if(cancelInviteListener != null) {
            cancelInviteListener.onClickCancelInvite(this);
        }
    }

    public OnClickCancelInviteListener getOnCancelInviteListener() {
        return cancelInviteListener;
    }

    public void setOnCancelInviteListener(OnClickCancelInviteListener cancelInviteListener) {
        this.cancelInviteListener = cancelInviteListener;
    }
}
