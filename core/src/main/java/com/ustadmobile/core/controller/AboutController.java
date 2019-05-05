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
 * Presenter for About screen.
 *
 */
public class AboutController extends UstadBaseController<AboutView>  {

    private String aboutHTMLStr;

    public AboutController(Object context, Hashtable args, AboutView view){
        super(context, args, view);
    }

    /**
     * This will load the about.html file in Asset and read the stream to string (Html) and output to
     *  the view. It also updates the version code and other build version info to the view.
     * @param savedState savedState if any
     */
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
            public void onFailure(Throwable exception) { exception.printStackTrace();}
        });
        view.setAboutHTML(aboutHTMLStr);

        String currentApiUrl = UstadMobileSystemImpl.getInstance().getAppConfigString("apiUrl",
                "http://localhost", context);
        view.setVersionInfo(impl.getVersion(context) + " - " +
                UMCalendarUtil.makeHTTPDate(impl.getBuildTimestamp(context)) + "\n" +
                "API: " + currentApiUrl + "\n");

    }

}
