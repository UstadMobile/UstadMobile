package com.ustadmobile.core.view;

public interface UstadViewWithNotifications {

    /**
     * Short duration to show notification for - matches the android constant
     *
     * http://developer.android.com/reference/android/widget/Toast.html#LENGTH_SHORT
     */
    int LENGTH_SHORT = 0;

    /**
     * Long duration to show a notification for - matches the android constnat
     *
     * http://developer.android.com/reference/android/widget/Toast.html#LENGTH_LONG
     */
    int LENGTH_LONG = 1;


    /**
     * Show a text notification at the bottom of the screen (e.g. Toast on Android)
     *
     * @param notification Notification to show
     * @param length Length to show for
     */
    void showNotification(String notification, int length);


}
