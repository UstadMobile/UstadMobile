package com.ustadmobile.core.impl.nav

import android.R
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.ustadmobile.core.impl.DestinationProvider
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.view.UstadView

/**
 * Convert the given multiplatform go options into Android NavController NavOptions
 */
fun UstadMobileSystemCommon.UstadGoOptions.toNavOptions(droidNavController: NavController,
                                                        destinationProvider: DestinationProvider) = navOptions {
    anim {
        enter = R.anim.slide_in_left
        exit = R.anim.slide_out_right
        popEnter = R.anim.slide_in_left
        popExit = R.anim.slide_out_right
    }

    val popUpToViewName = popUpToViewName
    if(popUpToViewName != null) {
        val popUpToDestId = if(popUpToViewName == UstadView.CURRENT_DEST) {
            droidNavController.currentDestination?.id ?: 0
        }else if(popUpToViewName == UstadView.ROOT_DEST) {
            droidNavController.graph.startDestinationId
        }else {
            destinationProvider.lookupDestinationName(popUpToViewName)
                ?.destinationId ?: 0
        }

        popUpTo(popUpToDestId) { inclusive = popUpToInclusive }
    }
}
