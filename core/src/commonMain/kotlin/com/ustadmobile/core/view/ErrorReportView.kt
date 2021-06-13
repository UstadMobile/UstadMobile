package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ErrorReport

interface ErrorReportView : UstadView {

    var errorReport: ErrorReport?

    companion object {

        const val ARG_ERR_NUM = "errNum"

        const val ARG_MESSAGE = "msg"

        const val ARG_STACKTRACE_PREFKEY = "stacktraceKey"


        const val VIEW_NAME = "ErrorReport"

    }

}