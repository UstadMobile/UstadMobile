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


    public ReportSelectionPresenter(Object context, Hashtable arguments, ReportSelectionView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

    }

    public void goToReport(String reportName, String reportDesc, String reportLink,
                       boolean showThreshold,boolean showRadioGroup, boolean showGenderDisaggregate,
                       boolean showClazzes, boolean showLocations){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
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


    @Override
    public void setUIStrings() {

    }

}
