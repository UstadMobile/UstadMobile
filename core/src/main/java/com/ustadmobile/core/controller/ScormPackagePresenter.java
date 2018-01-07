package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.scorm.ScormManifest;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ScormPackageView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Hashtable;

/**
 *
 * Present a SCORM package. Scorm tech specs as per :
 *  https://scorm.com/scorm-explained/technical-scorm/
 *
 * Created by mike on 1/6/18.
 */
public class ScormPackagePresenter extends UstadBaseController {

    private ScormManifest scormManifest;

    private ScormPackageView scormPackageView;

    private String mountedPath;

    private UmCallback zipMountedCallback = new UmCallback() {
        @Override
        public void onSuccess(Object result) {
            mountedPath = (String)result;
            UstadMobileSystemImpl.getInstance().makeRequestAsync(new UmHttpRequest(
                            getContext(),
                            UMFileUtil.joinPaths(new String[]{mountedPath, "imsmanifest.xml"})),
                    manifestLoadedCallback);
        }

        @Override
        public void onFailure(Throwable exception) {

        }
    };

    private UmHttpResponseCallback manifestLoadedCallback = new UmHttpResponseCallback(){
        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            scormManifest = new ScormManifest();
            try {
                scormManifest.loadFromInputStream(response.getResponseAsStream());
                ScormManifest.Organization defaultOrg = scormManifest.getDefaultOrganization();
                final ScormManifest.Resource startRes = scormManifest.getResourceByIdentifier(
                        defaultOrg.getItems().get(0).getIdentifierRef());
                scormPackageView.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scormPackageView.setTitle(scormManifest.getDefaultOrganization().getTitle());
                        scormPackageView.loadUrl(UMFileUtil.joinPaths(new String[]{mountedPath,
                                startRes.getHref()}));
                    }
                });
            }catch(IOException e) {
                e.printStackTrace();
            }catch(XmlPullParserException x) {
                x.printStackTrace();
            }
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {

        }
    };

    public ScormPackagePresenter(Object context, ScormPackageView view) {
        super(context);
        this.scormPackageView = view;
    }

    public void onCreate(Hashtable args) {
        scormPackageView.mountZip((String)args.get(ContainerController.ARG_CONTAINERURI),
                zipMountedCallback);
    }

    @Override
    public void setUIStrings() {

    }
}
