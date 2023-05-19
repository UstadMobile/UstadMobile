package com.ustadmobile.port.android.view

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

/**
 * Interface that should be implemented by an activity which hosts a FAB to be controlled by a
 * fragment.
 */
interface UstadActivityWithFab {

    val activityFloatingActionButton: ExtendedFloatingActionButton?

}