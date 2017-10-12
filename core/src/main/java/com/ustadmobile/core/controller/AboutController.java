package com.ustadmobile.core.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.HTTPCacheDir;
import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.UstadView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;


/**
 * Created by mike on 12/27/16.
 */

public class AboutController extends UstadBaseController implements AsyncLoadableController {

    AboutView aboutView;

    private String aboutHTMLStr;

    public AboutController(Object context){
        super(context);
    }

    public UstadController loadController(Hashtable args, Object context) throws Exception {
        AboutController controller = new AboutController(context);
        InputStream aboutIn = UstadMobileSystemImpl.getInstance().openResourceInputStream(
            "com/ustadmobile/core/about.html", context);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int bytesRead;
        while((bytesRead = aboutIn.read(buf)) != -1) {
            bout.write(buf, 0, bytesRead);
        }

        aboutIn.close();
        controller.aboutHTMLStr = new String(bout.toByteArray());

        return controller;
    }

    public void setUIStrings() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        aboutView.setVersionInfo(impl.getVersion(context) + " - " +
                HTTPCacheDir.makeHTTPDate(CoreBuildConfig.BUILD_TIME_MILLIS));
        aboutView.setAboutHTML(aboutHTMLStr);
    }

    public static void makeControllerForView(Hashtable args, ControllerReadyListener listener, UstadView view) {
        AboutController loadCtrl = new AboutController(view.getContext());
        new LoadControllerThread(args,loadCtrl, listener, view).start();
    }

    public void setView(UstadView view) {
        super.setView(view);
        aboutView = (AboutView)view;
        setUIStrings();
    }



}
