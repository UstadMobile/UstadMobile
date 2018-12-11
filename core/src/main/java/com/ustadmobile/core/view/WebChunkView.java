package com.ustadmobile.core.view;

import com.ustadmobile.core.impl.UmCallback;

public interface WebChunkView extends UstadView {

    String ARG_CHUNK_PATH = "chunkpath";

    void mountChunk(String webChunkPath, UmCallback<String> callback);

    void loadUrl(String url);

}
