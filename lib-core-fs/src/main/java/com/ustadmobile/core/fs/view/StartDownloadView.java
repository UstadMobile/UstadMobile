package com.ustadmobile.core.fs.view;

/**
 * Created by mike on 3/5/18.
 */

public interface StartDownloadView {

    void setProgressVisible(boolean visible);

    void setProgress(float progress);

    void setProgressStatusText(String statusText);

    void setDownloadSize(String downloadSize);

}
