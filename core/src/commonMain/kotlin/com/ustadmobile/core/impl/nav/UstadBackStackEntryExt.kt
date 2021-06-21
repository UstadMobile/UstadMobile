package com.ustadmobile.core.impl.nav

import com.ustadmobile.core.util.ext.toQueryString

/**
 * Convenience function to get a view uri e.g. ViewName?arg=value ...
 */
val UstadBackStackEntry.viewUri: String
    get() = "$viewName?${arguments.toQueryString()}"
