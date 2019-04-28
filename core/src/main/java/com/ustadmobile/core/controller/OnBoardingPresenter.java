package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePoint2View;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.core.view.OnBoardingView;

import java.util.Hashtable;

import static com.ustadmobile.core.view.OnBoardingView.PREF_TAG;

public class OnBoardingPresenter extends UstadBaseController<OnBoardingView> {

    public OnBoardingPresenter(Object context, Hashtable arguments, OnBoardingView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        view.runOnUiThread(() -> view.setScreenList());
        boolean wasShown = Boolean.parseBoolean(UstadMobileSystemImpl.getInstance()
                .getAppPref(PREF_TAG,view.getContext()));
        if(wasShown){
            handleGetStarted();
        }
    }


    public void handleGetStarted() {

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.setAppPref(PREF_TAG, String.valueOf(true)
                , view.getContext());

        //impl.go(Login2View.VIEW_NAME, null, context);
        impl.startUI(context);

        //UstadMobileSystemImpl.getInstance().go(BasePoint2View.VIEW_NAME, getContext());

    }
}
