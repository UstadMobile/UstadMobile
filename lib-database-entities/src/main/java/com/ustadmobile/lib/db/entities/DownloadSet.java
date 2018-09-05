package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents a set of entries that will be downloaded, with a single root entry. The DownloadSet
 * will often also include the descendant entries of the root entry (which are discovered by the
 * CrawlTask)
 *
 * <ol>
 * <li>
 *    A DownloadJob has a 1:many relationship with DownloadJobRun, which represents a download run of
 *    the set. It can be rerun (e.g. to update), which leads to a second DownloadJobRun entity.
 *   </li>
 *   <li>
 *       A DownloadJob has a 1:many relationship with DownloadJobItem, each of which represents a
 *       single entry in the download set. Each DownloadJobItem has a 1:many relationship with a
 *       DownloadJobItemRun
 *   </li>
 * </ol>
 */

@UmEntity
public class DownloadSet {

    @UmPrimaryKey(autoIncrement = true)
    private int id;

    private String destinationDir;

    private boolean wifiDirectDownloadEnabled;

    private boolean lanDownloadEnabled;

    private boolean mobileDataEnabled = true;

    private String rootOpdsUuid;

    public DownloadSet(){

    }


    /**
     * Get the primary key value
     *
     * @return the primary key value
     */
    public int getId() {
        return id;
    }

    /**
     * Set the primary key value
     *
     * @param id the primary key value
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the destination directory into which this downloadset should be saved (file path)
     * @return the destination directory into which this downloadset should be saved (file path)
     */
    public String getDestinationDir() {
        return destinationDir;
    }

    /**
     * Set the destination directory into which this downloadset should be saved (file path)
     * @param destinationDir the destination directory into which this downloadset should be saved (file path)
     */
    public void setDestinationDir(String destinationDir) {
        this.destinationDir = destinationDir;
    }

    /**
     * Get the uuid of the root OPDS entry of this DownloadSet.
     * @return the uuid of the root OPDS entry of this DownloadSet.
     */
    public String getRootOpdsUuid() {
        return rootOpdsUuid;
    }

    /**
     * Set the uuid of the root OPDS entry of this DownloadSet.
     * @param rootOpdsUuid
     */
    public void setRootOpdsUuid(String rootOpdsUuid) {
        this.rootOpdsUuid = rootOpdsUuid;
    }

    /**
     * If enabled the task will attempt to acquire the requested entries from another node using
     * wifi direct. The node will be contacted using bluetooth and then a wifi group connection
     * will be created.
     *
     * @return boolean: True if enabled, false otherwise
     */
    public boolean isWifiDirectDownloadEnabled() {
        return wifiDirectDownloadEnabled;
    }

    /**
     * Set if wifi direct downnload is enabled or not for this DownloadSet
     *
     * @param wifiDirectDownloadEnabled true if enabled, false otherwise
     */
    public void setWifiDirectDownloadEnabled(boolean wifiDirectDownloadEnabled) {
        this.wifiDirectDownloadEnabled = wifiDirectDownloadEnabled;
    }

    /**
     * If enabled the task will attempt to acquire the requested entries from another node on the same
     * wifi network directly (nodes discovered using Network Service Discovery - NSD).
     *
     * @return boolean: True if enabled, false otherwise
     */
    public boolean isLanDownloadEnabled() {
        return lanDownloadEnabled;
    }

    /**
     * Set if download over the LAN is enabled
     *
     * @param lanDownloadEnabled true if enabled, false otherwise
     */
    public void setLanDownloadEnabled(boolean lanDownloadEnabled) {
        this.lanDownloadEnabled = lanDownloadEnabled;
    }

    /**
     * Set if downloading using mobile data is enabled for this DownloadSet or not. If using mobile
     * data is not enabled for this DownloadSet, the download will not proceed until the DownloadSet
     * can be downloaded using wifi (from the Internet or nearby peer devices)
     *
     * @return true if downloading using mobile data is enabled, false otherwise
     */
    public boolean isMobileDataEnabled() {
        return mobileDataEnabled;
    }

    /**
     * Get if downloading using mobile data is enabled for this DownloadSet or not. If using mobile
     * data is not enabled for this DownloadSet, the download will not proceed until the DownloadSet
     * can be downloaded using wifi (from the Internet or nearby peer devices)
     *
     * @param mobileDataEnabled true if downloading using mobile data is enabled, false otherwise
     */
    public void setMobileDataEnabled(boolean mobileDataEnabled) {
        this.mobileDataEnabled = mobileDataEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DownloadSet)) return false;

        DownloadSet that = (DownloadSet) o;

        if (id != that.id) return false;
        return destinationDir.equals(that.destinationDir);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + destinationDir.hashCode();
        return result;
    }
}
