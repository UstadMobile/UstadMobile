package com.ustadmobile.sharedse.util

import android.os.Debug

actual fun startDebugMethodTracing(traceName: String) {
    Debug.startMethodTracing(traceName)
}

actual fun stopDebugMethodTracing() {
    Debug.stopMethodTracing()
}
