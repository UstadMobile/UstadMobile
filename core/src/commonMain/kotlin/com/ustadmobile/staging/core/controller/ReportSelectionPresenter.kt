package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.ReportSelectionView

/**
 * The ReportSelection Presenter.
 */
class ReportSelectionPresenter(context: Any, arguments: Map<String, String>?,
                               view: ReportSelectionView,
                               val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<ReportSelectionView>(context, arguments!!, view) {

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    /**
     * Goes to report link and sets arguments according to arguments here
     * @param reportName                The report name literal
     * @param reportDesc                The report desc literal
     * @param reportLink                The report link (View.VIEW_NAME)
     * @param showThreshold             If we want to show thresholds
     * @param showRadioGroup            If we want to show radio group
     * @param showGenderDisaggregate    If we want to show gender disaggregated checkbox
     * @param showClazzes               If we want to show Classes and its picker
     * @param showLocations             If we want to show Locations and its picker
     */
    fun goToReport(reportName: String, reportDesc: String, reportLink: String,
                   showThreshold: Boolean, showRadioGroup: Boolean, showGenderDisaggregate: Boolean,
                   showClazzes: Boolean, showLocations: Boolean) {
        val args = HashMap<String, String>()
        args.put(ReportEditView.ARG_REPORT_NAME, reportName)
        args.put(ReportEditView.ARG_REPORT_DESC, reportDesc)
        args.put(ReportEditView.ARG_REPORT_LINK, reportLink)
        args.put(ReportEditView.ARG_SHOW_THERSHOLD, showThreshold.toString())
        args.put(ReportEditView.ARG_SHOW_RADIO_GROUP, showRadioGroup.toString())
        args.put(ReportEditView.ARG_SHOW_GENDER_DISAGGREGATE, showGenderDisaggregate.toString())
        args.put(ReportEditView.ARG_SHOW_CLAZZES, showClazzes.toString())
        args.put(ReportEditView.ARG_SHOW_LOCATIONS, showLocations.toString())
        impl.go(ReportEditView.VIEW_NAME, args, context)
    }

}
