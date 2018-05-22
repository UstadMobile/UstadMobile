package com.ustadmobile.core.view;

import com.ustadmobile.core.impl.UmCallback;

/**
 * Created by mike on 2/15/18.
 */

public interface H5PContentView extends UstadView{

    String VIEW_NAME = "H5PContentView";

    /**
     * Mount the h5p standalone dist directory, return the url to it
     * @return
     */
    void mountH5PDist(UmCallback<String> callback);

    void mountH5PFile(String zipFile, UmCallback<String> callback);

    void setTitle(String title);

    void setContentHtml(String baseUrl, String html);



}
