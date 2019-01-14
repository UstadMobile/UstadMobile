package com.ustadmobile.core.view;

import com.ustadmobile.core.controller.XapiPackagePresenter;
import com.ustadmobile.core.impl.UmCallback;

/**
 * Created by mike on 9/13/17.
 */

public interface XapiPackageView extends UstadView{

    String VIEW_NAME = "XapiPackage";

    void setTitle(String title);

    void loadUrl(String url);

    void mountZip(String zipUri, UmCallback callback);

    XapiPackagePresenter getPresenter();

}
