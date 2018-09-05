package com.ustadmobile.core.view;

/**
 * Created by mike on 12/27/16.
 */

public interface AboutView extends UstadView {

    String VIEW_NAME = "About";

    void setVersionInfo(String versionInfo);

    void setAboutHTML(String aboutHTML);
}
