package com.ustadmobile.core.opds;

/**
 * Created by mike on 7/21/17.
 */

public interface OPDSEntryFilter  {

    /**
     *
     * @param entry
     * @return
     */
    boolean accept(UstadJSOPDSEntry entry);

}
