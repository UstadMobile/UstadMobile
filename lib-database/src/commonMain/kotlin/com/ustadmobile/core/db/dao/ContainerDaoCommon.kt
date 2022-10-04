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

}

