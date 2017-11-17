package com.ustadmobile.core.impl;

/**
 * Represents a request to mount a container (e.g. epub, xapi package, etc).
 */
public class ContainerMountRequest {

    private boolean epubMode;

    private String containerUri;


    /**
     * Create a new mount request
     *
     * @param containerUri The file to be mounted
     * @param epubMode true if the file is an epub and will have scripts injected for pagination etc
     */
    public ContainerMountRequest(String containerUri, boolean epubMode) {
        this.epubMode = epubMode;
        this.containerUri = containerUri;
    }

    /**
     *
     * @return
     */
    public String getContainerUri() {
        return containerUri;
    }

    public void setContainerUri(String containerUri) {
        this.containerUri = containerUri;
    }

    public boolean isEpubMode() {
        return epubMode;
    }

    public void setEpubMode(boolean epubMode) {
        this.epubMode = epubMode;
    }
}
