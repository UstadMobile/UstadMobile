package com.ustadmobile.port.sharedse.view;

import com.ustadmobile.core.view.UstadView;

/**
 * Created by mike on 3/5/18.
 */

public interface StartDownloadView extends UstadView {

    String VIEW_NAME = "StartDownload";

    void setProgressVisible(boolean visible);

    void setProgress(float progress);

    void setProgressStatusText(String statusText);

    void setDownloadText(String downloadSize);

}
