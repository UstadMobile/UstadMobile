package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin

data class RecursiveContentEntrySet(
        var rootEntry: ContentEntry,
        var rootContainer: Container,
        val childEntries : List<ContentEntry>,
        val childContainers: List<Container>,
        var totalBytesToDownload: Long
)

/**
 * Inserts a set of ContentEntry objects into the database, so that they can be used to test
 * download related code (e.g. recursively setting up downloadjobitems etc)
 */
fun insertTestContentEntries(db: UmAppDatabase, lastModifiedTime: Long): RecursiveContentEntrySet {
    val rootEntry = ContentEntry("Lorem ipsum title",
            "Lorem ipsum description", leaf = false, publik = true)
    rootEntry.contentEntryUid = db.contentEntryDao.insert(rootEntry)
    println("Insert root entry uid = ${rootEntry.contentEntryUid}")


    val container = Container()
    container.containerContentEntryUid = rootEntry.contentEntryUid
    container.cntLastModified = lastModifiedTime
    container.fileSize = 0
    container.containerUid = db.containerDao.insert(container)

    val entry2 = ContentEntry("title 2", "title 2", leaf = true, publik = true)
    val entry3 = ContentEntry("title 2", "title 2", leaf = false, publik = true)
    val entry4 = ContentEntry("title 4", "title 4", leaf = true, publik = false)

    entry2.contentEntryUid = db.contentEntryDao.insert(entry2)
    entry3.contentEntryUid = db.contentEntryDao.insert(entry3)
    entry4.contentEntryUid = db.contentEntryDao.insert(entry4)

    val cEntry2 = Container()
    cEntry2.containerContentEntryUid = entry2.contentEntryUid
    cEntry2.cntLastModified = lastModifiedTime
    cEntry2.fileSize = 500
    cEntry2.containerUid = db.containerDao.insert(cEntry2)

    val cEntry4 = Container()
    cEntry4.containerContentEntryUid = entry4.contentEntryUid
    cEntry4.cntLastModified = lastModifiedTime
    cEntry4.fileSize = 500
    cEntry4.containerUid = db.containerDao.insert(cEntry4)

    val totalBytesToDownload = cEntry2.fileSize + cEntry4.fileSize

    db.contentEntryParentChildJoinDao.insertList(
            listOf(ContentEntryParentChildJoin(rootEntry, entry2, 0),
                    ContentEntryParentChildJoin(rootEntry, entry3, 0),
                    ContentEntryParentChildJoin(entry3, entry4, 0)))
    return RecursiveContentEntrySet(rootEntry, container, listOf(entry2, entry3, entry4),
            listOf(cEntry2, cEntry4), totalBytesToDownload)
}