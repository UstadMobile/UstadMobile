package com.ustadmobile.core.controller;

import java.util.Hashtable;

import com.ustadmobile.core.view.SelectTwoDatesDialogView;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;


/**
 * The SelectTwoDatesDialog Presenter.
 */
public class SelectTwoDatesDialogPresenter
        extends UstadBaseController<SelectTwoDatesDialogView> {

    public SelectTwoDatesDialogPresenter(Object context, Hashtable arguments, SelectTwoDatesDialogView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }

    public void handleClickPrimaryActionButton() {
       view.finish();
    }

    @Override
    public void setUIStrings() {

    }

}
