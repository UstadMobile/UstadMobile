package com.ustadmobile.core.fs.presenter;

import com.ustadmobile.core.controller.UstadBaseController;
import com.ustadmobile.core.fs.view.StartDownloadView;

import java.util.Hashtable;

/**
 * Created by mike on 3/5/18.
 */

public class StartDownloadPresenter extends UstadBaseController {

    private StartDownloadView view;

    public static final String ARG_ROOT_URIS = "r_uris";

    public StartDownloadPresenter(Object context, StartDownloadView view, Hashtable args) {
        super(context);
        this.view = view;
    }

    public void onCreate(Hashtable savedState) {
        
    }

    @Override
    public void setUIStrings() {

    }
}
