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

public class AboutController extends UstadBaseController<AboutView>  {

    private String aboutHTMLStr;

    public AboutController(Object context, Hashtable args, AboutView view){
        super(context, args, view);
    }

    public void onCreate(Hashtable savedState) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        impl.getAsset(context, "com/ustadmobile/core/about.html", new UmCallback<InputStream>() {
            @Override
            public void onSuccess(InputStream result) {
                try {
                    aboutHTMLStr = UMIOUtils.readStreamToString(result);
                    view.setAboutHTML(aboutHTMLStr);
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });


        view.setVersionInfo(impl.getVersion(context) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(context)));

        view.setVersionInfo(impl.getVersion(context) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(context)));
        view.setAboutHTML(aboutHTMLStr);
    }

}
