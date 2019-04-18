package com.ustadmobile.core.view;

import com.ustadmobile.core.impl.UmCallback;

public interface WebChunkView extends UstadView {

    String ARG_CHUNK_PATH = "chunkpath";
    String VIEW_NAME = "webChunk";
    String ARG_CONTENT_ENTRY_ID = "entryId";

    void mountChunk(String webChunkPath, UmCallback<String> callback);

    void loadUrl(String url);

    void showError(String message);

    void setToolbarTitle(String title);
}
