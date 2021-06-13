package com.ustadmobile.core.impl.nav

import com.ustadmobile.core.impl.ErrorCodeException
import com.ustadmobile.core.view.ErrorReportView

/**
 * Navigate to the error screen to display the given exception.
 */
fun UstadNavController.navigateToErrorScreen(exception: Exception) {
    val errorCode = (exception as? ErrorCodeException)?.errorCode ?: 0
    val message = exception.message

    navigate(ErrorReportView.VIEW_NAME, mapOf(
            ErrorReportView.ARG_ERR_NUM to errorCode.toString(),
            ErrorReportView.ARG_MESSAGE to (message ?: "")))

}