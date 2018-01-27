package com.ustadmobile.core.catalog.contenttype;

import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A ContentTypePlugin provides support to use a specific file type (e.g. EPUB, Xapi Package, etc) on
 * Ustad Mobile. Specifically a plugin is responsible to:
 *
 *  a. Read a file type and return information about it including a unique id and a title.
 *  b. Provide the view name that will be used to view that item.
 *
 * Created by mike on 9/9/17.
 */
public abstract class ContentTypePlugin {

    /**
     *
     */
    public interface EntryResult {

        UstadJSOPDSFeed getFeed();

        InputStream getThumbnail() throws IOException;

        String getThumbnailMimeType();
    }

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
    public abstract List<String> getMimeTypes();

    public abstract List<String> getFileExtensions();


}
