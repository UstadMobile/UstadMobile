package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.view.SelectAttendanceThresholdsDialogView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;


/**
 * The SelectAttendanceThresholdsDialog Presenter.
 */
public class SelectAttendanceThresholdsDialogPresenter
        extends UstadBaseController<SelectAttendanceThresholdsDialogView> {


    public SelectAttendanceThresholdsDialogPresenter(Object context, Hashtable arguments,
                                                     SelectAttendanceThresholdsDialogView view) {
        super(context, arguments, view);

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


    }

    public void handleClickPrimaryActionButton(long selectedObjectUid) {
        //TODO: Check if we still ned this
    }


}
