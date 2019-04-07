package com.ustadmobile.core.view;

import com.ustadmobile.core.impl.UmCallback;

/**
 * Created by mike on 9/13/17.
 */

public interface XapiPackageContentView extends UstadView, ViewWithErrorNotifier{

    String VIEW_NAME = "XapiPackage";

    String ARG_CONTAINER_UID = "containerUid";

    void setTitle(String title);

    void loadUrl(String url);

    void mountContainer(long containerUid, UmCallback<String> callback);

}
