package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.view.ReportEditView;
import com.ustadmobile.core.view.ReportSelectionView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;


/**
 * The ReportSelection Presenter.
 */
public class ReportSelectionPresenter
        extends UstadBaseController<ReportSelectionView> {

    //Any arguments stored as variables here
    //eg: private long clazzUid = -1;


    public ReportSelectionPresenter(Object context, Hashtable arguments, ReportSelectionView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }

    public void handleClickPrimaryActionButton(long selectedObjectUid) {

    }

    public void goToOverallAttendanceReport(){

    }

    public void goToAttendanceGroupedByThresholdReport(){

    }

    public void goToAtRiskStudentsReport(){

    }

    public void goToNumberOfDaysClassesOpenReport(){

    }


    public void goToReport(String reportName, String reportLink){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        args.put(ReportEditView.ARG_REPORT_NAME, reportName);

        args.put(ReportEditView.ARG_REPORT_LINK, reportLink);
        impl.go(ReportEditView.VIEW_NAME, args, context);
    }


    @Override
    public void setUIStrings() {

    }

}
