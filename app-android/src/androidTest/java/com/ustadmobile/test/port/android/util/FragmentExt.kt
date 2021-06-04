package com.ustadmobile.test.port.android.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation


/**
 *  Setup the NavController so it is ready immediately after the view is created
 *
 *  as per:
 *  https://developer.android.com/guide/navigation/navigation-testing
 *
 *  @param navController NavController to install
 *  @param initialDestId if not -1, then the navcontroller will navigate to set the initial
 *  destination.
 *  @param initialArgs if not null, will be provided to the navcontroller to set the initial
 *  arguments.
 */
fun Fragment.installNavController(navController: NavController,
                                  initialDestId: Int = -1,
                                  initialArgs: Bundle? = null
) {
    if(initialDestId != -1) {
        navController.navigate(initialDestId, initialArgs)
    }

    viewLifecycleOwnerLiveData.observeForever {viewLifecycleOwner ->
        if (viewLifecycleOwner != null) {
            // The fragment’s view has just been created
            Navigation.setViewNavController(requireView(), navController)
        }
    }
}

