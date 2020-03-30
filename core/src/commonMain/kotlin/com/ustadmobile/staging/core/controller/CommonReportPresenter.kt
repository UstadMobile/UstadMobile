package com.ustadmobile.core.controller

import com.ustadmobile.core.view.CommonReportView
import com.ustadmobile.core.view.ReportDetailView

/**
 * So that we can add click listener for different use cases.
 * @param <V>   The view
</V> */
abstract class CommonReportPresenter<V : CommonReportView> : UstadBaseController<V> {

    //The constructor will throw an uncast check warning. That is expected.
    constructor(context: Any, arguments: Map<String, String>?, view: CommonReportView)
            : super(context, arguments!!, view as V) {}

    /**
     * Primary action on item.
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    abstract fun downloadReport()


}