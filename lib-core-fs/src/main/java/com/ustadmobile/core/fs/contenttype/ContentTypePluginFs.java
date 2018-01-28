package com.ustadmobile.core.fs.contenttype;

import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import java.io.File;
import java.util.List;

/**
 * Created by mike on 1/26/18.
 */
public interface ContentTypePluginFs {


    /**
     * Generate an UstadJSOPDSEntry for the givne fileUri.
     *
     * @param file The path to the file to retrieve a
     * @return
     */
    List<OpdsEntryWithRelations> getEntries(File file, Object context);
}
