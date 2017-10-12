package com.ustadmobile.core.view;

/**
 * Created by mike on 10/10/17.
 */

public interface AddFeedDialogView extends UstadView, DismissableDialog {

    String VIEW_NAME = "AddFeedDialog";

    void setDropdownPresets(String[] presets);

    void setUrlFieldVisible(boolean visible);

    void setProgressVisible(boolean visible);

    /**
     * If false, all interactive components, except for the cancel button, should be disabled.
     *
     * @param enabled
     */
    void setUiEnabled(boolean enabled);

    String getOpdsUrl();

    void setOpdsUrl(String opdsUrl);

    void setError(String errorMessage);

}
