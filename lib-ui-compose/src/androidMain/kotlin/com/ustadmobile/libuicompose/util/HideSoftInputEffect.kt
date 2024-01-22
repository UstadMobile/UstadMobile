package com.ustadmobile.libuicompose.util

import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.ustadmobile.libuicompose.util.ext.getActivityContext

@Composable
actual fun HideSoftInputEffect() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            val activity = context.getActivityContext()
            val currentFocusView = activity.currentFocus
            if(currentFocusView != null) {
                ContextCompat.getSystemService(activity, InputMethodManager::class.java)
                    ?.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
            }
        }
    }
}
