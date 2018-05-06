package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithStatusCache;

import java.util.List;

/**
 * Implementations of this class manage the retrieval of OPDS feeds (in Atom XML as per
 * www.opds-spec.org) from the network.
 *
 * When Atom ODPS is retrieved from the network, the root element (feed or entry) is persisted to as
 * an OpdsEntry entity. If the url is a feed, all child entries are loaded and are also persisted as
 * OpdsEntry entities. They are related to the parent feed using an OpdsEntryParentToChildJoin entity.
 *
 */
public interface OpdsAtomFeedRepository {

    /**
     * Retrieve an entry from the given URL as LiveData. If the URL contains a feed, then the child
     * entries should be found using the OpdsEntryWithRelationsDao getEntriesByParent method.
     *
     * @param url the http/https url of the xml
     * @param uuid If not null, then use this as the uuid for the loaded entry (otherwise generate a new UUID)
     * @param callback If not null, the OpdsItemLoadCallback will be used for progress monitoring
     *
     * @return A LiveData object representing the root element of the OPDS XML from the given URL
     */
    UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String uuid,
                                                            OpdsEntry.OpdsItemLoadCallback callback);

    /**
     * Retrieve an entry from the given URL as LiveData, with the associated OpdsEntryStatusCache.
     *
     * If the URL contains a feed, then the child entries should be found using the
     * OpdsEntryWithRelationsDao getEntriesByParent method.
     *
     * @param url the http/https url of the xml
     * @param uuid If not null, then use this as the uuid for the loaded entry (otherwise generate a new UUID)
     * @param callback If not null, the OpdsItemLoadCallback will be used for progress monitoring
     *
     * @return A LiveData object representing the root element of the OPDS XML from the given URL
     */
    UmLiveData<OpdsEntryWithStatusCache> getEntryWithStatusCacheByUrl(String url, String uuid,
                                                            OpdsEntry.OpdsItemLoadCallback callback);


    /**
     * Retrieve an entry from the given URL as LiveData. If the URL contains a feed, then the child
     * entries should be found using the OpdsEntryWithRelationsDao getEntriesByParent method.
     *
     * Synonamous to getEntryByUrl(url, null, null).
     *
     * @param url the http/https url of the xml
     *
     * @return A LiveData object representing the root element of the OPDS element from the given URL
     */
    UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url);

    /**
     * Retrieve an entry from the given URL as LiveData. If the URL contains a feed, then the child
     * entries should be found using the OpdsEntryWithRelationsDao getEntriesByParent method.
     *
     * @param url the http/https url of the xml
     * @param uuid If not null, then use this as the uuid for the loaded entry (otherwise generate a new UUID)
     *
     * @return A LiveData object representing the root element of the OPDS XML from the given URL
     */
    UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String uuid);

    /**
     * Retrieve an entry from the given URL as LiveData. If the URL contains a feed, then the child
     * entries should be found using the OpdsEntryWithRelationsDao getEntriesByParent method.
     *
     * @param url the http/https url of the xml
     *
     * @return The OpdsEntryWithRelations object representing the root OPDS element at the given URL
     */
    OpdsEntryWithRelations getEntryByUrlStatic(String url);

    /**
     * Retrieve a list of the entries that are residing within container files, within given directories.
     * This is not recursive.
     *
     * A scan will be initiated which will use the known content type plugins to search through
     * the given directories.
     *
     * @param dirList A list of directory paths that should be scanned
     * @param callback If not null, the OpdsItemLoadCallback will be used for progress monitoring
     *
     * @return A LiveData list of entries found within the given directory.
     */
    UmLiveData<List<OpdsEntryWithStatusCache>> findEntriesByContainerFileDirectoryAsList(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback);

    /**
     * Retrieve a list of the entries that are residing within container files, within given directories.
     * This is not recursive.
     *
     * A scan will be initiated which will use the known content type plugins to search through
     * the given directories.
     *
     * @param dirList A list of directory paths that should be scanned
     * @param callback If not null, the OpdsItemLoadCallback will be used for progress monitoring
     *
     * @return A UmProvider object, for the current platform, for the entries found within a given directory.
     */
    UmProvider<OpdsEntryWithStatusCache> findEntriesByContainerFileDirectoryAsProvider(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback);

    /**
     * Retrieve a list of the entries that are residing within container files, within given directories.
     * This is not recursive.
     *
     * A scan will be initiated which will use the known content type plugins to search through
     * the given directories.
     *
     * @param containerFilePath A directory which should be scanned for containers
     *
     * @return A list of OpdsEntryWithRelations objects representing the entries found in containers in the given directory
     */
    List<OpdsEntryWithRelations> findEntriesByContainerFileNormalizedPath(String containerFilePath);


}
