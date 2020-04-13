package com.ustadmobile.port.android.util.ext

import android.os.Bundle
import androidx.navigation.NavBackStackEntry
import com.ustadmobile.port.android.view.UstadBaseFragment

/**
 * Save arguments to the bundle to tell the destination fragment to deposit it's result
 * to the given NavBackStackEntry (if not already set).
 *
 * e.g. When the user goes directly from fragment a to b, b saves the result to the backstackentry
 * of a.
 * When the user goes from fragment a to b, and then b to c, c saves the result to the backstackentry
 * of a directly (e.g. when the user is presented with a list, and then chooses to create a new entity).
 */
fun Bundle.putResultDestInfo(backState: NavBackStackEntry, destinationResultKey: String) {
    if(backState.arguments?.containsKey(UstadBaseFragment.ARG_RESULT_DEST_ID) != true) {
        putString(UstadBaseFragment.ARG_RESULT_DEST_ID,
                backState.destination.id.toString())
        putString(UstadBaseFragment.ARG_RESULT_DEST_KEY, destinationResultKey)
    }
}
