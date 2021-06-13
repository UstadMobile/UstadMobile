package com.ustadmobile.core.util.ext

import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.db.dao.ErrorReportDao
import com.ustadmobile.core.impl.ErrorCodeException
import com.ustadmobile.core.impl.getOs
import com.ustadmobile.core.impl.getOsVersion
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.viewUri
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ErrorReport
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * Logs the given exception as an error report. This can then be used for issue analysis.
 */
suspend fun ErrorReportDao.logErrorReport(severityLevel: Int, e: Exception, presenter: UstadBaseController<*>? = null) : Long {
    val errorCodeException = (e as? ErrorCodeException)
    val navController: UstadNavController? = presenter?.di?.direct?.instance()

    val errorReport = ErrorReport().apply{
        errorCode = errorCodeException?.errorCode ?: 0
        severity = severityLevel
        message = e.message
        stackTrace = e.stackTraceToString()
        osVersion = getOsVersion()
        operatingSys = getOs()
        timestamp = systemTimeInMillis()
        presenterUri = navController?.currentBackStackEntry?.viewUri
    }

    return insertAsync(errorReport)
}
