package com.ustadmobile.port.android.util.ext

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Extension function to request for the permission
 * @param permission Permission to be request
 * @param runAfterFun function to executed after permission is granted
 */
fun Fragment.runAfterPermissionGranted(permission: String, runAfterFun: () -> Unit) {
    if(ContextCompat.checkSelfPermission(requireContext(),permission) == PackageManager.PERMISSION_GRANTED) {
        runAfterFun.invoke()
    }else {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if(granted) runAfterFun.invoke()
        }.launch(permission)
    }
}