package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BaseReportView;
import com.ustadmobile.core.view.ReportEditView;
import com.ustadmobile.core.view.ReportSelectionView;

import java.util.Hashtable;


/**
 * The ReportSelection Presenter.
 */
public class BaseReportPresenter
        extends UstadBaseController<BaseReportView> {


    public BaseReportPresenter(Object context, Hashtable arguments, BaseReportView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
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
    public void goToReport(String reportName, String reportDesc, String reportLink,
                       boolean showThreshold,boolean showRadioGroup, boolean showGenderDisaggregate,
                       boolean showClazzes, boolean showLocations){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, Object> args = new Hashtable<>();
        args.put(ReportEditView.ARG_REPORT_NAME, reportName);
        args.put(ReportEditView.ARG_REPORT_DESC, reportDesc);
        args.put(ReportEditView.ARG_REPORT_LINK, reportLink);
        args.put(ReportEditView.ARG_SHOW_THERSHOLD, showThreshold);
        args.put(ReportEditView.ARG_SHOW_RADIO_GROUP, showRadioGroup);
        args.put(ReportEditView.ARG_SHOW_GENDER_DISAGGREGATE, showGenderDisaggregate);
        args.put(ReportEditView.ARG_SHOW_CLAZZES, showClazzes);
        args.put(ReportEditView.ARG_SHOW_LOCATIONS, showLocations);
        impl.go(ReportEditView.VIEW_NAME, args, context);
    }

}
