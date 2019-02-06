package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.DummyView;
import com.ustadmobile.core.view.OnBoardingView;

import java.util.Hashtable;

public class OnBoardingPresenter extends UstadBaseController<OnBoardingView> {

    public OnBoardingPresenter(Object context, Hashtable arguments, OnBoardingView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        view.runOnUiThread(() -> view.setScreenList());
    }


    public void handleGetStarted() {
        UstadMobileSystemImpl.getInstance().go(DummyView.VIEW_NAME, getContext());
    }
}
