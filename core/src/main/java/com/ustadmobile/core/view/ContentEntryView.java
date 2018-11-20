package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ContentEntry;

public interface ContentEntryView extends UstadView {

    public static final String VIEW_NAME = "ContentEntry";

    void setContentEntryProvider(UmProvider<ContentEntry> entryProvider);

    void setToolbarTitle(String title);

    void showError();

}
