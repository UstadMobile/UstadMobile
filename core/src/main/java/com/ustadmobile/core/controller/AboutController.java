package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.UstadView;

import java.io.IOException;
import java.io.InputStream;
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

        impl.getAsset(context, "com/ustadmobile/core/about.html", new UmCallback<InputStream>() {
            @Override
            public void onSuccess(InputStream result) {
                try {
                    aboutHTMLStr = UMIOUtils.readStreamToString(result);
                    aboutView.setAboutHTML(aboutHTMLStr);
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });


        aboutView.setVersionInfo(impl.getVersion(context) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(context)));

        aboutView.setVersionInfo(impl.getVersion(context) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(context)));
        aboutView.setAboutHTML(aboutHTMLStr);
    }

    public void setUIStrings() {

    }

    public void setView(UstadView view) {
        super.setView(view);
        aboutView = (AboutView)view;
    }



}
