package com.ustadmobile.core.impl;

import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;

import java.util.HashMap;

/**
 * The acquisition manager takes care of downloading catalog entries as efficiently as possible on
 * any given platform.
 *
 * This is a singleton class.
 */
public abstract class AcquisitionManager {

    private static AcquisitionManager self;

    public static final String LINK_REL_DOWNLOAD_DESTINATION = "http://www.ustadmobile.com/namespace/opds/download-dest";

    private static HashMap<Integer, Integer> downloadStatusToStringId = new HashMap<>();

    static {
        downloadStatusToStringId.put(UstadMobileSystemImpl.DLSTATUS_RUNNING,
                MessageIDConstants.download_in_progress);
    }

    public static int getStringIdForDownloadStatus(int status) {
        Integer strId = downloadStatusToStringId.get(status);
        if(strId != null)
            return strId;
        else
            return -1;
    }


    /**
     * Get a reference to the acquisition manager
     *
     * @return
     */
    public static AcquisitionManager getInstance() {
        if(self == null){
            self = AcquisitionManagerFactory.makeAcquisitionManager();
        }

        return self;
    }


    /**
     * Acquire the given catalog entries.
     *
     * @param acquireFeed An OPDS acquisition feed. The feed should have only ONE acquisition
     *                    link per entry. Feeds that have more than one acquisition link per entry
     *                    should be filtered by the UI before being handed over to this method.
     *
     *                    The feed must also have a LINK_REL_DOWNLOAD_DESTINATION in the feed itself
     *                    that should point to the directory where acquired entries should be saved.
     *
     * @param context System context
     */
    public abstract void acquireCatalogEntries(UstadJSOPDSFeed acquireFeed, Object context);

    /**
     * Get the status of the acquisition of the given entry.
     *
     * @param entryId The Entry ID of the entry being acquired
     * @param context System context
     *
     * @return Array of ints representing the status of the entry: as per
     */
    public abstract int[] getEntryStatusById(String entryId, Object context);

    /**
     * Register a listener to receive entry status updates
     *
     * @param listener
     */
    public abstract void registerEntryAquisitionStatusListener(AcquisitionStatusListener listener, Object context);

    /**
     * Unregister a listener to receive entry status updates
     *
     * @param listener
     */
    public abstract void unregisterEntryAquisitionStatusListener(AcquisitionStatusListener listener, Object context);


}
