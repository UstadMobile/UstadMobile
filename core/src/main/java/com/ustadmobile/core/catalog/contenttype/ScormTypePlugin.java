package com.ustadmobile.core.catalog.contenttype;

/**
 * Created by mike on 1/6/18.
 */

public class ScormTypePlugin extends ZippedContentTypePlugin {

    @Override
    public String getViewName() {
        return null;
    }

    @Override
    public String[] getMimeTypes() {
        return new String[]{"application/zip"};
    }

    @Override
    public String[] getFileExtensions() {
        return new String[]{"zip"};
    }

    @Override
    public EntryResult getEntry(String fileUri, String cacheEntryFileUri) {
        return null;
    }
}
