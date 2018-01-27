package com.ustadmobile.core.fs.contenttype;

import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import java.util.List;

/**
 * Created by mike on 1/26/18.
 */
public interface ContentTypePluginFs {


    /**
     * Generate an UstadJSOPDSEntry for the givne fileUri.
     *
     * @param fileUri
     * @return
     */
    List<OpdsEntryWithRelations> getEntries(String fileUri, Object context);
}
