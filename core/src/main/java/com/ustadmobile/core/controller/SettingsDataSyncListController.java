package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SettingsDataSyncListView;
import com.ustadmobile.core.view.UstadView;
import java.util.LinkedHashMap;

/**
 * Created by kileha3 on 13/02/2017.
 */

public class SettingsDataSyncListController extends UstadBaseController{
    private SettingsDataSyncListView view = null;

    public SettingsDataSyncListController(Object context) {
        super(context);
    }

    public void setView(UstadView view) {
        //UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        super.setView(view);
        this.view = (SettingsDataSyncListView)view;
    }

    public LinkedHashMap<String, String> getMainNodeSyncHistory(Object context){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        return impl.getMainNodeSyncHistory(context);
    }

    public void setUIStrings() {
    }

}
