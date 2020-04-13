package com.ustadmobile.port.android.util.ext

import android.os.Bundle
import androidx.navigation.NavBackStackEntry
import com.ustadmobile.port.android.view.UstadBaseFragment

/**
 * Save arguments to the bundle to tell the destination fragment where it should save results. If
 * these arguments are already in the backState arguments, then they will simply be copied over. If
 * not the arguments are set so that the data is saved to the SavedStateHandle of the given NavBackStateEntry
 *
 * e.g. When the user goes directly from fragment a to b, b saves the result to the backstackentry
 * of a.
 * When the user goes from fragment a to b, and then b to c, c saves the result to the backstackentry
 * of a directly (e.g. when the user is presented with a list, and then chooses to create a new entity).
 */
fun Bundle.putResultDestInfo(backState: NavBackStackEntry, destinationResultKey: String) {
    val backStateArgs = backState.arguments
    putString(UstadBaseFragment.ARG_RESULT_DEST_ID,
            backStateArgs?.getString(UstadBaseFragment.ARG_RESULT_DEST_ID)
            ?: backState.destination.id.toString())
    putString(UstadBaseFragment.ARG_RESULT_DEST_KEY,
            backStateArgs?.getString(UstadBaseFragment.ARG_RESULT_DEST_KEY)
            ?: destinationResultKey)
}
