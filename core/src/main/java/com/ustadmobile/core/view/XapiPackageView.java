package com.ustadmobile.core.view;

import com.ustadmobile.core.impl.ZipFileHandle;

/**
 * Created by mike on 9/13/17.
 */

public interface XapiPackageView extends UstadView{

    static String VIEW_NAME = "XapiPackage";

    void setTitle(String title);

    void loadUrl(String url);

    String mountZip(String zipUri);

    ZipFileHandle getMountedZipHandle();
}
