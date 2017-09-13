package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;

/**
 * Base class representing a supported file type e.g. epub, h5p, etc.
 *
 * Created by mike on 9/9/17.
 */
public abstract class ContentTypePlugin {

    /**
     * Return a String that will match the VIEW_NAME for the view that should be opened for this
     * type of content
     *
     * @return Name of view to open for this type of content
     */
    public abstract String getViewName();

    /**
     * Return an array of mime types that are used by this content type.
     *
     * @return
     */
    public abstract String[] getMimeTypes();

    public abstract String[] getFileExtensions();

    /**
     * Generate an UstadJSOPDSEntry for the givne fileUri.
     *
     * @param fileUri
     * @return
     */
    public abstract UstadJSOPDSFeed getEntry(String fileUri, String cacheEntryFileUri);

}
