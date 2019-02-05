package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SELQuestionSetsView;
import com.ustadmobile.core.view.SettingsView;

import java.util.Hashtable;

public class SettingsPresenter extends UstadBaseController<SettingsView> {


    public SettingsPresenter(Object context, Hashtable arguments, SettingsView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }

    public void goToSELQuestionSets(){
        Hashtable args = new Hashtable();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(SELQuestionSetsView.VIEW_NAME, args, context);
    }
}
