package com.ustadmobile.core.view;


/**
 * ClazzActivityEdit Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzActivityEditView extends UstadView {

    String VIEW_NAME = "ClazzActivityEdit";
    String ARG_CLAZZACTIVITY_UID = "clazzActivityUid";

    /**
     * For Android: closes the activity.
     */
    void finish();

    void updateToolbarTitle(String title);

    void setClazzActivityChangesDropdownPresets(String[] presets);

}
