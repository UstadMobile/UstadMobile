package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePointView2;

import java.util.Hashtable;

public class BasePointActivity2Presenter extends UstadBaseController<BasePointView2> {


    public BasePointActivity2Presenter(Object context, Hashtable arguments, BasePointView2 view) {
        super(context, arguments, view);
    }


    public void handleClickShareIcon(){
        view.showShareAppDialog();
    }


    public void handleConfirmShareApp(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        //Get setup file
        impl.getAppSetupFile(getContext(), false, new UmCallback() {

            @Override
            public void onSuccess(Object result) {

                view.shareAppSetupFile(result.toString());

            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    @Override
    public void setUIStrings() {

    }
}
