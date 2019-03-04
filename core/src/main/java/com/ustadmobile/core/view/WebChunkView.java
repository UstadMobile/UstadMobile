package com.ustadmobile.core.view;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.db.entities.Container;

public interface WebChunkView extends UstadView {

    String ARG_CHUNK_PATH = "chunkpath";
    String VIEW_NAME = "webChunk";
    String ARG_CONTENT_ENTRY_ID = "entryId";
    String ARG_CONTAINER_UID = "containerUid";

    void mountChunk(Container webChunkPath, UmCallback<String> callback);

    void loadUrl(String url);

    void showError(String message);

    void setToolbarTitle(String title);
}
