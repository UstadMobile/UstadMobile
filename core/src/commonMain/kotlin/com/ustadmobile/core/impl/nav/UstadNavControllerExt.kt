package com.ustadmobile.core.impl.nav

import com.ustadmobile.core.impl.ErrorCodeException
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.view.ErrorReportView
import com.ustadmobile.door.util.systemTimeInMillis
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * Navigate to the error screen to display the given exception.
 */
fun UstadNavController.navigateToErrorScreen(exception: Exception, di: DI, context: Any) {
    val errorCode = (exception as? ErrorCodeException)?.errorCode ?: 0
    val message = exception.message

    val systemImpl: UstadMobileSystemImpl = di.direct.instance()
    val stackTraceKey = "trace_${systemTimeInMillis()}"
    systemImpl.setAppPref(stackTraceKey, exception.stackTraceToString())

    val presenterUri = currentBackStackEntry?.let {
        "${it.viewName}?${it.arguments.toQueryString()}"
    } ?: "unknown-uri"

    navigate(ErrorReportView.VIEW_NAME, mapOf(
            ErrorReportView.ARG_PRESENTER_URI to presenterUri,
            ErrorReportView.ARG_STACKTRACE_PREFKEY to stackTraceKey,
            ErrorReportView.ARG_ERR_CODE to errorCode.toString(),
            ErrorReportView.ARG_MESSAGE to (message ?: "")))

}