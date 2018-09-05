package com.ustadmobile.lib.db.entities;


/**
 * Convenience class used for sharing entries, connects with queries that use a join between
 * OpdsEntry and ContainerFile. Has containerMimeType field.
 */
public class OpdsEntryWithRelationsAndContainerMimeType extends OpdsEntryWithRelations {

    private String containerMimeType;

    public String getContainerMimeType() {
        return containerMimeType;
    }

    public void setContainerMimeType(String containerMimeType) {
        this.containerMimeType = containerMimeType;
    }
}
