package com.ustadmobile.core.view;

/**
 * Created by mike on 4/17/17.
 */

public interface CatalogEntryView extends UstadView {

    int MODE_ENTRY_DOWNLOADABLE = 0;

    int MODE_ENTRY_DOWNLOADED = 1;

    String VIEW_NAME = "CatalogEntry";

    void setHeader(String headerFileUri);

    void setIcon(String iconFileUri);

    void setMode(int mode);

    void setProgress(float progress);

    void setSize(long downloadSize);

    void setDescription(String description);

    void setTitle(String title);
}
