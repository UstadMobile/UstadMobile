package com.ustadmobile.core.view

interface UstadViewWithNotifications {


    /**
     * Show a text notification at the bottom of the screen (e.g. Toast on Android)
     *
     * @param notification Notification to show
     * @param length Length to show for
     */
    fun showNotification(notification: String, length: Int)

    companion object {

        /**
         * Short duration to show notification for - matches the android constant
         *
         * http://developer.android.com/reference/android/widget/Toast.html#LENGTH_SHORT
         */
        val LENGTH_SHORT = 0

        /**
         * Long duration to show a notification for - matches the android constnat
         *
         * http://developer.android.com/reference/android/widget/Toast.html#LENGTH_LONG
         */
        val LENGTH_LONG = 1
    }


}
