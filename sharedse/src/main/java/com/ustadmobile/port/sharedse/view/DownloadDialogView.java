package com.ustadmobile.port.sharedse.view;

import com.ustadmobile.core.view.UstadView;

import java.util.List;

/**
 * Created by mike on 3/5/18.
 */

public interface DownloadDialogView extends UstadView {

    String VIEW_NAME = "DownloadDialog";

    void setAvailableOptions(int options, boolean showChoices);


    void setProgressVisible(boolean visible);

    void setProgress(float progress);

    void setProgressStatusText(String statusText);

    void setMainText(String downloadSize);

}
