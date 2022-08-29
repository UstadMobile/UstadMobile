package com.ustadmobile.core.db.dao

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.lib.db.entities.ContentEntry

object ContentEntryDaoCommon {

    const val PLUGIN_ID_DOWNLOAD = 10

    const val PLUGIN_ID_DELETE = 14

    const val SORT_TITLE_ASC = 1

    const val SORT_TITLE_DESC = 2

    internal const val LATEST_DOWNLOADED_CONTAINER_CTE_SQL = """
            LatestDownloadedContainer(containerUid) AS
             (SELECT COALESCE(
                     (SELECT containerUid
                        FROM Container
                       WHERE Container.containerContentEntryUid = :contentEntryUid 
                         AND EXISTS(
                             SELECT 1
                               FROM ContainerEntry
                              WHERE ContainerEntry.ceContainerUid = Container.containerUid)
                    ORDER BY cntLastModified DESC
                       LIMIT 1), 0))
        """

    internal const val ACTIVE_CONTENT_JOB_ITEMS_CTE_SQL = """
            ActiveContentJobItems(cjiRecursiveStatus, cjiPluginId) AS
             (SELECT cjiRecursiveStatus, cjiPluginId
                FROM ContentJobItem
               WHERE cjiContentEntryUid = :contentEntryUid
                 AND cjiStatus BETWEEN ${JobStatus.QUEUED} AND ${JobStatus.RUNNING_MAX})
        """


    const val ENTITY_PERSONS_WITH_PERMISSION_PT1 = """
            SELECT DISTINCT Person.PersonUid FROM Person
            LEFT JOIN PersonGroupMember ON Person.personUid = PersonGroupMember.groupMemberPersonUid
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid
            WHERE 
            CAST(Person.admin AS INTEGER) = 1
            OR 
            (EntityRole.ertableId = ${ContentEntry.TABLE_ID} AND 
            EntityRole.erEntityUid = ContentEntry.contentEntryUid AND
            (Role.rolePermissions &  
        """

    const val ENTITY_PERSONS_WITH_PERMISSION_PT2 = ") > 0)"

    const val ENTITY_PERSONS_WITH_PERMISSION = "$ENTITY_PERSONS_WITH_PERMISSION_PT1 :permission $ENTITY_PERSONS_WITH_PERMISSION_PT2"

    const val ALL_ENTRIES_RECURSIVE_SQL = """WITH RECURSIVE ContentEntry_recursive(
            contentEntryUid, title, ceInactive, contentFlags, description, entryId, author, publisher, licenseType, licenseName, licenseUrl, sourceUrl, thumbnailUrl, lastModified, primaryLanguageUid, languageVariantUid, leaf, publik,  completionCriteria, minScore, contentOwner, contentTypeFlag, contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum, contentEntryLastChangedBy, contentEntryLct,
            
            cepcjUid, cepcjChildContentEntryUid, cepcjParentContentEntryUid, childIndex, cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum, cepcjLastChangedBy, cepcjLct,
            
            containerUid, cntLocalCsn, cntMasterCsn, cntLastModBy, fileSize, containerContentEntryUid, cntLastModified, mimeType, remarks, mobileOptimized, cntNumEntries, cntLct
            ) AS (
            SELECT ContentEntry.contentEntryUid, ContentEntry.title, ContentEntry.ceInactive, ContentEntry.contentFlags, ContentEntry.description, ContentEntry.entryId, ContentEntry.author, ContentEntry.publisher, ContentEntry.licenseType, ContentEntry.licenseName, ContentEntry.licenseUrl, ContentEntry.sourceUrl, ContentEntry.thumbnailUrl, ContentEntry.lastModified, ContentEntry.primaryLanguageUid, ContentEntry.languageVariantUid, ContentEntry.leaf, ContentEntry.publik, ContentEntry.completionCriteria, ContentEntry.minScore, ContentEntry.contentOwner, ContentEntry.contentTypeFlag, ContentEntry.contentEntryLocalChangeSeqNum, ContentEntry.contentEntryMasterChangeSeqNum, ContentEntry.contentEntryLastChangedBy, ContentEntry.contentEntryLct,
            ContentEntryParentChildJoin.cepcjUid, ContentEntryParentChildJoin.cepcjChildContentEntryUid, ContentEntryParentChildJoin.cepcjParentContentEntryUid, ContentEntryParentChildJoin.childIndex, ContentEntryParentChildJoin.cepcjLocalChangeSeqNum, ContentEntryParentChildJoin.cepcjMasterChangeSeqNum, ContentEntryParentChildJoin.cepcjLastChangedBy, ContentEntryParentChildJoin.cepcjLct,
            Container.containerUid, Container.cntLocalCsn, Container.cntMasterCsn, Container.cntLastModBy, Container.fileSize, Container.containerContentEntryUid, Container.cntLastModified, Container.mimeType, Container.remarks, Container.mobileOptimized, Container.cntNumEntries, Container.cntLct
            FROM 
            ContentEntry
            LEFT JOIN ContentEntryParentChildJoin ON ContentEntry.contentEntryUid = ContentEntryParentChildJoin.cepcjChildContentEntryUid 
            LEFT JOIN Container ON Container.containerUid = (SELECT COALESCE((SELECT containerUid FROM Container WHERE containerContentEntryUid = ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1), 0))
            WHERE ContentEntry.contentEntryUid = :contentEntryUid
            UNION
            SELECT ContentEntry.contentEntryUid, ContentEntry.title, ContentEntry.ceInactive, ContentEntry.contentFlags, ContentEntry.description, ContentEntry.entryId, ContentEntry.author, ContentEntry.publisher, ContentEntry.licenseType, ContentEntry.licenseName, ContentEntry.licenseUrl, ContentEntry.sourceUrl, ContentEntry.thumbnailUrl, ContentEntry.lastModified, ContentEntry.primaryLanguageUid, ContentEntry.languageVariantUid, ContentEntry.leaf, ContentEntry.publik, ContentEntry.completionCriteria, ContentEntry.minScore, ContentEntry.contentOwner, ContentEntry.contentTypeFlag, ContentEntry.contentEntryLocalChangeSeqNum, ContentEntry.contentEntryMasterChangeSeqNum, ContentEntry.contentEntryLastChangedBy, ContentEntry.contentEntryLct,
            ContentEntryParentChildJoin.cepcjUid, ContentEntryParentChildJoin.cepcjChildContentEntryUid, ContentEntryParentChildJoin.cepcjParentContentEntryUid, ContentEntryParentChildJoin.childIndex, ContentEntryParentChildJoin.cepcjLocalChangeSeqNum, ContentEntryParentChildJoin.cepcjMasterChangeSeqNum, ContentEntryParentChildJoin.cepcjLastChangedBy, ContentEntryParentChildJoin.cepcjLct, 
            Container.containerUid, Container.cntLocalCsn, Container.cntMasterCsn, Container.cntLastModBy, Container.fileSize, Container.containerContentEntryUid, Container.cntLastModified, Container.mimeType, Container.remarks, Container.mobileOptimized, Container.cntNumEntries, Container.cntLct
            FROM 
            ContentEntry
            LEFT JOIN ContentEntryParentChildJoin ON ContentEntry.contentEntryUid = ContentEntryParentChildJoin.cepcjChildContentEntryUid 
            LEFT JOIN Container ON Container.containerUid = (SELECT COALESCE((SELECT containerUid FROM Container WHERE containerContentEntryUid = ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1), 0)),
            ContentEntry_recursive
            WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = ContentEntry_recursive.contentEntryUid)
            SELECT * FROM ContentEntry_recursive"""

    const val ENTRY_WITH_CONTAINER_QUERY = """
            SELECT ContentEntry.*, Container.* FROM ContentEntry LEFT 
                JOIN Container ON Container.containerUid = (
                    SELECT containerUid FROM Container WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1) 
            WHERE ContentEntry.contentEntryUid=:entryUuid
            """


}