package com.ustadmobile.port.sharedse.impl.http;

/**
 * Represents what a receiver needs to know to get a shared entry from a sender.
 */
public class SharedEntryInfo {

    private String entryUuid;

    private String originUrl;

    private int mirrorServerPort;

    private String mirrorPath;

    public SharedEntryInfo(String entryUuid, String originUrl, int mirrorServerPort,
                           String mirrorPath) {
        this.entryUuid = entryUuid;
        this.originUrl = originUrl;
        this.mirrorServerPort = mirrorServerPort;
        this.mirrorPath = mirrorPath;
    }

    /**
     * Get the entry uuid - as it is on the senders database
     *
     * @return the entry uuid - as it is on the senders database
     */
    public String getEntryUuid() {
        return entryUuid;
    }

    /**
     *  Set the entry uuid - as it is on the senders database
     *
     * @param entryUuid the entry uuid - as it is on the senders database
     */
    public void setEntryUuid(String entryUuid) {
        this.entryUuid = entryUuid;
    }

    /**
     * Get the origin url, e.g. the OPDS url from the cloud from which this entry originally came from
     *
     * @return the entry uuid - as it is on the senders database
     */
    public String getOriginUrl() {
        return originUrl;
    }

    /**
     * Set the entry uuid - as it is on the senders database
     * @param originUrl the entry uuid - as it is on the senders database
     */
    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public int getMirrorServerPort() {
        return mirrorServerPort;
    }

    /**
     * Set the port of the mirror server - e.g. the main internal http server port
     *
     * @param mirrorServerPort the port of the mirror server - e.g. the main internal http server port
     */
    public void setMirrorServerPort(int mirrorServerPort) {
        this.mirrorServerPort = mirrorServerPort;
    }

    /**
     * Get the path component of the url of the mirror server.
     *
     * @return the path component of the url of the mirror server.
     */
    public String getMirrorPath() {
        return mirrorPath;
    }

    /**
     * Set the path component of the url of the mirror server.
     * @param mirrorPath the path component of the url of the mirror server.
     */
    public void setMirrorPath(String mirrorPath) {
        this.mirrorPath = mirrorPath;
    }
}
