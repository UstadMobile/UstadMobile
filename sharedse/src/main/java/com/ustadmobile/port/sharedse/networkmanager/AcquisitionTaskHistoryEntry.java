package com.ustadmobile.port.sharedse.networkmanager;

/**
 * An acquisition can be downloaded in multiple runs (e.g. when the first download fails at first,
 * is then resumed, possibly from another source, etc).
 *
 * Created by mike on 6/1/17.
 */

public class AcquisitionTaskHistoryEntry {

    private String url;

    private int mode;

    private long timeStarted;

    private long timeEnded;

    private int status;

    /**
     * Creates a new entry
     *
     * @param url URL the download ran from (e.g. the local peer url or cloud address)
     * @param mode flag as per NetworkManager.DOWNLOAD_FROM flags
     * @param timeStarted Time the download started
     * @param timeEnded Time the download finished
     * @param status Status at the end of the run (e.g. successful, fail, etc) flag as per UstadMobileSystemImpl.DLSTATUS_*
     */
    public AcquisitionTaskHistoryEntry(String url, int mode, long timeStarted, long timeEnded, int status) {
        this.url = url;
        this.mode = mode;
        this.timeStarted = timeStarted;
        this.timeEnded = timeEnded;
        this.status = status;
    }

    /**
     * Creates a new entry
     *
     * @param url URL the download ran from (e.g. the local peer url or cloud address)
     * @param mode flag as per NetworkManager.DOWNLOAD_FROM flags
     * @param timeStarted Time the download started
     */
    public AcquisitionTaskHistoryEntry(String url, int mode, long timeStarted) {
        this.url = url;
        this.mode = mode;
        this.timeStarted = timeStarted;
    }

    /**
     * Gets the URL this was downloaded from : in case this was a local peer it will be the address
     * of the peer
     *
     * @return URL this download ran from
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the mode of this download - this could be from the cloud, from a local peer on the same
     * network or from a peer on a different network using WiFi direct
     *
     * @return The mode of this download as per NetworkManager.DOWNLOAD_FROM flags
     */
    public int getMode() {
        return mode;
    }

    /**
     * The time the download was started in ms since the epoch.
     *
     * @return Time download started in ms since epoch.
     */
    public long getTimeStarted() {
        return timeStarted;
    }

    /**
     * The time the downloaded ended in ms since the epoch
     *
     * @return Time download ended in ms since the epoch
     */
    public long getTimeEnded() {
        return timeEnded;
    }

    protected void setTimeEnded(long timeEnded) {
        this.timeEnded = timeEnded;
    }

    protected void setStatus(int status) {
        this.status = status;
    }

    /**
     * Gets the status of the download at the end of the run (e.g. success, fail, etc)
     *
     * @return Status flag as per UstadMobileSystemImpl.DLSTATUS_ flags
     */
    public int getStatus() {
        return status;
    }
}
