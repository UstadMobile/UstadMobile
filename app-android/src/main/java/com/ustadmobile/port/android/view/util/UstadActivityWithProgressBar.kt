package com.ustadmobile.port.android.view.util

import android.widget.ProgressBar

/**
 * Interface implemented by MainActivity that enables the fragment to get a reference to the
 * ProgressBar
 */
interface UstadActivityWithProgressBar {

    val activityProgressBar: ProgressBar?
}