package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.networkmanager.EntryCheckResponse;

import java.util.List;

/**
 * This interface represents an object that can provide a list of network nodes that contain a particular
 * entry. This is normally the NetworkManager itself. However this can be implemented by another
 * class for testing purposes.
 */

public interface LocalMirrorFinder {

    /**
     * Get response from response list which contains a file we a looking for and can be downloaded locally,
     * first priority is given to node on the same network.
     * If no matching node then check for the node on different network.
     * @param entryId
     * @return
     */
    List<EntryCheckResponse> getEntryResponsesWithLocalFile(String entryId);

}
