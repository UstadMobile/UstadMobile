package com.ustadmobile.core.db.dao

object ContainerDaoCommon  {

    //Containers in process will not have their filesize set.
    internal const val CONTAINER_READY_WHERE_CLAUSE = """
            Container.fileSize > 0
        """

    internal const val FROM_CONTAINER_WHERE_MOST_RECENT_AND_READY = """
            FROM Container
             WHERE Container.containerContentEntryUid = :contentEntryUid
               AND $CONTAINER_READY_WHERE_CLAUSE     
          ORDER BY Container.cntLastModified DESC 
          LIMIT 1
        """

    internal const val SELECT_MOST_RECENT_READY_CONTAINER = """
            SELECT Container.*
            $FROM_CONTAINER_WHERE_MOST_RECENT_AND_READY
        """

    internal const val UPDATE_SIZE_AND_NUM_ENTRIES_SQL = """
            UPDATE Container 
               SET cntNumEntries = COALESCE(
                   (SELECT COUNT(*) 
                      FROM ContainerEntry 
                     WHERE ceContainerUid = Container.containerUid), 0),
                   fileSize = COALESCE(
                   (SELECT SUM(ContainerEntryFile.ceCompressedSize) AS totalSize 
                      FROM ContainerEntry
                      JOIN ContainerEntryFile ON ContainerEntry.ceCefUid = ContainerEntryFile.cefUid
                     WHERE ContainerEntry.ceContainerUid = Container.containerUid), 0),
                   cntLct = :changeTime   
                     
             WHERE containerUid = :containerUid
        """
}

