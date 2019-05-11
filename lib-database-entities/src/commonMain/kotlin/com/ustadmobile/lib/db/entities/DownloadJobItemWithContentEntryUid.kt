package com.ustadmobile.lib.db.entities

@Deprecated("This is already included in DownloadJobItem now")
class DownloadJobItemWithContentEntryUid : DownloadJobItem {


    constructor() {

    }

    constructor(src: DownloadJobItemWithContentEntryUid) : super(src)

    constructor(djiContentEntryUid: Long, downloadLength: Long) {
        this.djiContentEntryUid = djiContentEntryUid
        this.downloadLength = downloadLength
    }
}
