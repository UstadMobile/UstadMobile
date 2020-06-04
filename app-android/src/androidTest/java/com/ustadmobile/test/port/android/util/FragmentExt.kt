package com.ustadmobile.test.port.android.util

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation


/**
 *  Setup the NavController so it is ready immediately after the view is created
 *
 *  as per:
 *  https://developer.android.com/guide/navigation/navigation-testing
 */
fun Fragment.installNavController(navController: NavController) {
    viewLifecycleOwnerLiveData.observeForever {viewLifecycleOwner ->
        if (viewLifecycleOwner != null) {
            // The fragment’s view has just been created
            Navigation.setViewNavController(requireView(), navController)
        }
    }
}

