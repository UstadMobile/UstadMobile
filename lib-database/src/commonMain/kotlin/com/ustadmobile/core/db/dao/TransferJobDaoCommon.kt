package com.ustadmobile.core.db.dao

object TransferJobDaoCommon {

    const val SELECT_TRANSFER_JOB_TOTALS_SQL = """
        (SELECT SUM(TransferJobItem.tjTotalSize)
                   FROM TransferJobItem
                  WHERE TransferJobItem.tjiTjUid =  TransferJob.tjUid) AS totalSize,
                (SELECT SUM(TransferJobItem.tjTransferred)
                   FROM TransferJobItem
                  WHERE TransferJobItem.tjiTjUid =  TransferJob.tjUid) AS transferred 
    """

    const val SELECT_CONTENT_ENTRY_VERSION_UIDS_FOR_CONTENT_ENTRY_UID_SQL = """
        (SELECT ContentEntryVersion.cevUid
                         FROM ContentEntryVersion
                        WHERE ContentEntryVersion.cevContentEntryUid = :contentEntryUid)
    """

}