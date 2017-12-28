package com.ustadmobile.core.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.UstadView;

import java.io.IOException;
import java.util.Hashtable;


/**
 * Created by mike on 12/27/16.
 */

public class AboutController extends UstadBaseController  {

    AboutView aboutView;

    private String aboutHTMLStr;

    public AboutController(Object context, AboutView view){
        super(context);
        this.aboutView = view;
    }

    public void onCreate(Hashtable args, Hashtable savedState) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        try {
            aboutHTMLStr = UMIOUtils.readStreamToString(impl.openResourceInputStream(
                    "com/ustadmobile/core/about.html", context));
            aboutView.setAboutHTML(aboutHTMLStr);
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 0, null, e);
        }

        aboutView.setVersionInfo(impl.getVersion(context) + " - " +
                UMCalendarUtil.makeHTTPDate(CoreBuildConfig.BUILD_TIME_MILLIS));
    }

    public void setUIStrings() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        aboutView.setVersionInfo(impl.getVersion(context) + " - " +
                UMCalendarUtil.makeHTTPDate(CoreBuildConfig.BUILD_TIME_MILLIS));
        aboutView.setAboutHTML(aboutHTMLStr);
    }

    public void setView(UstadView view) {
        super.setView(view);
        aboutView = (AboutView)view;
    }



}
