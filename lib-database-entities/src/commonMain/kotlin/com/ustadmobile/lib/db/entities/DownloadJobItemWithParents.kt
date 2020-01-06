package com.ustadmobile.lib.db.entities

class DownloadJobItemWithParents(val parents: MutableList<DownloadJobItemParentChildJoin> = mutableListOf()): DownloadJobItem() {

    constructor(downloadJob: DownloadJob, contentEntryUid: Long, containerUid: Long, downloadLength: Long,
                parents: MutableList<DownloadJobItemParentChildJoin>) : this(parents) {
        djiDjUid = downloadJob.djUid
        djiContentEntryUid = contentEntryUid
        djiContainerUid = containerUid
        this.downloadLength = downloadLength
    }

}