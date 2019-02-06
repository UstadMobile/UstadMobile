package com.ustadmobile.port.sharedse.view;

import com.ustadmobile.core.view.UstadView;

public interface DownloadDialogView extends UstadView {

    String VIEW_NAME = "DownloadDialog";

    void setBottomButtonsVisible(boolean visible);

    void setBottomButtonPositiveText(String text);

    void setBottomButtonNegativeText(String text);

    void setDownloadOverWifiOnly(boolean wifiOnly);

    void setStatusText(String statusText);

    void setStackedOptions(int[] optionIds, String[] optionTexts);

    void setStackOptionsVisible(boolean visible);

}
