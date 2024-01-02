package com.ustadmobile.core.wrappers.window

import web.window.Window

/**
 * Not included in kotlin wrapper at the moment
 * As per https://developer.mozilla.org/en-US/docs/Web/Security/Secure_Contexts#feature_detection
 */
inline val Window.isSecureContext: Boolean
    get() = asDynamic().isSecureContext as Boolean
