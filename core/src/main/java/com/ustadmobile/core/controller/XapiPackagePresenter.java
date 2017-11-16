package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipEntryHandle;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.tincan.TinCanXML;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.core.util.UMUUID;
import com.ustadmobile.core.util.URLTextUtil;
import com.ustadmobile.core.view.XapiPackageView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by mike on 9/13/17.
 *
 * Displays an XAPI Zip Package.
 *
 * Pass ContainerController.ARG_CONTAINERURI when creating to provide the location of the xAPI
 * zip to open
 *
 * Uses the Rustici launch method to find the URL to launch:
 *  https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md
 *
 */
public class XapiPackagePresenter extends UstadBaseController implements Runnable{

    private Hashtable args;

    private Thread loaderThread;

    private XapiPackageView view;

    String mountedPath;

    private TinCanXML tinCanXml;

    private String launchHref;

    private String launchUrl;

    private String registrationUUID;

    public XapiPackagePresenter(Object context, XapiPackageView view) {
        super(context);
        this.view = view;
    }

    public void onCreate(Hashtable args) {
        this.args = args;
        registrationUUID = UMUUID.randomUUID().toString();
        loaderThread = new Thread(this);
        loaderThread.start();
    }

    @Override
    public void run() {
        InputStream tinCanXmlIn = null;
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        try {
            mountedPath = view.mountZip((String)args.get(ContainerController.ARG_CONTAINERURI));
            ZipFileHandle fileHandle= view.getMountedZipHandle();
            Enumeration zipEntries = fileHandle.entries();
            ZipEntryHandle entryHandle;
            while(zipEntries.hasMoreElements()) {
                entryHandle = (ZipEntryHandle)zipEntries.nextElement();
                if(entryHandle.getName().endsWith("tincan.xml")){
                    //found the tincan.xml entry
                    tinCanXmlIn = fileHandle.openInputStream(entryHandle.getName());
                    XmlPullParser xpp = impl.newPullParser(tinCanXmlIn, "UTF-8");
                    tinCanXml = TinCanXML.loadFromXML(xpp);
                    launchHref = tinCanXml.getLaunchActivity().getLaunchUrl();
                    launchUrl = UMFileUtil.joinPaths(new String[]{mountedPath, launchHref});
                    launchUrl += getXAPIQuery();
                    view.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view.setTitle(tinCanXml.getLaunchActivity().getName());
                            view.loadUrl(launchUrl);
                        }
                    });
                }
            }
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 675, launchUrl, e);
        }catch(XmlPullParserException x) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 676, launchUrl, x);
        }
    }

    /**
     * Returns a Query String for the xAPI parameters for the container including
     * the tincan actor, authorization, endpoint, and registration UUID
     *
     * @return Query string as above
     */
    public String getXAPIQuery() {
        String username = UstadMobileSystemImpl.getInstance().getActiveUser(getContext());
        String password = UstadMobileSystemImpl.getInstance().getActiveUserAuth(getContext());
        String xapiEndpoint = UMFileUtil.resolveLink(mountedPath, "/xapi/");
        String activityArgs = tinCanXml != null && tinCanXml.getLaunchActivity() != null ?
                "&activity_id=" + URLTextUtil.urlEncodeUTF8(tinCanXml.getLaunchActivity().getId()) : "";

        return "?actor=" +
                URLTextUtil.urlEncodeUTF8(UMTinCanUtil.makeActorFromActiveUser(getContext()).toString()) +
                "&auth=" + URLTextUtil.urlEncodeUTF8(LoginController.encodeBasicAuth(username, password)) +
                "&endpoint=" + URLTextUtil.urlEncodeUTF8(xapiEndpoint) +
                "&registration=" + registrationUUID +
                activityArgs;
    }

    @Override
    public void setUIStrings() {

    }
}
