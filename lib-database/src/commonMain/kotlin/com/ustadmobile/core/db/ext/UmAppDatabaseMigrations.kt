package com.ustadmobile.core.db.ext

import com.ustadmobile.core.db.UmAppDatabaseReplicationMigration91_92
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.migration.DoorMigrationStatementList
import com.ustadmobile.lib.util.ext.fixTincan
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.migration.DoorMigration
import com.ustadmobile.door.migration.DoorMigrationSync

val MIGRATION_92_93 = DoorMigrationStatementList(92, 93) { db ->
    if(db.dbType() == DoorDbType.SQLITE) {
        listOf("ALTER TABLE SyncNode RENAME to SyncNode_OLD",
            "CREATE TABLE IF NOT EXISTS SyncNode (  nodeClientId  INTEGER  PRIMARY KEY NOT NULL)",
            "INSERT INTO SyncNode (nodeClientId) SELECT nodeClientId FROM SyncNode_OLD",
            "DROP TABLE SyncNode_OLD")
    }else {
        listOf("ALTER TABLE SyncNode DROP COLUMN master")
    }
}

val MIGRATION_93_94 = DoorMigrationStatementList(93, 94) { db ->
    if(db.dbType() == DoorDbType.SQLITE) {
        listOf(
            "CREATE VIEW IF NOT EXISTS ClazzLogAttendanceRecord_ReceiveView AS  SELECT ClazzLogAttendanceRecord.*, ClazzLogAttendanceRecordReplicate.* FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLogAttendanceRecordReplicate ON ClazzLogAttendanceRecordReplicate.clarPk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid ",
            "DROP TRIGGER IF EXISTS clazzlogattendancerecord_remote_insert_ins",
            "CREATE TRIGGER clazzlogattendancerecord_remote_insert_ins INSTEAD OF INSERT ON ClazzLogAttendanceRecord_ReceiveView FOR EACH ROW BEGIN REPLACE INTO ClazzLogAttendanceRecord(clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordPersonUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy, clazzLogAttendanceRecordLastChangedTime) VALUES (NEW.clazzLogAttendanceRecordUid, NEW.clazzLogAttendanceRecordClazzLogUid, NEW.clazzLogAttendanceRecordPersonUid, NEW.attendanceStatus, NEW.clazzLogAttendanceRecordMasterChangeSeqNum, NEW.clazzLogAttendanceRecordLocalChangeSeqNum, NEW.clazzLogAttendanceRecordLastChangedBy, NEW.clazzLogAttendanceRecordLastChangedTime) /*psql ON CONFLICT (clazzLogAttendanceRecordUid) DO UPDATE SET clazzLogAttendanceRecordClazzLogUid = EXCLUDED.clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordPersonUid = EXCLUDED.clazzLogAttendanceRecordPersonUid, attendanceStatus = EXCLUDED.attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum = EXCLUDED.clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum = EXCLUDED.clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy = EXCLUDED.clazzLogAttendanceRecordLastChangedBy, clazzLogAttendanceRecordLastChangedTime = EXCLUDED.clazzLogAttendanceRecordLastChangedTime */; END")
    }else {
        listOf("CREATE OR REPLACE FUNCTION clazzlogattendancerecord_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ClazzLogAttendanceRecord(clazzLogAttendanceRecordUid, clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordPersonUid, attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy, clazzLogAttendanceRecordLastChangedTime) VALUES (NEW.clazzLogAttendanceRecordUid, NEW.clazzLogAttendanceRecordClazzLogUid, NEW.clazzLogAttendanceRecordPersonUid, NEW.attendanceStatus, NEW.clazzLogAttendanceRecordMasterChangeSeqNum, NEW.clazzLogAttendanceRecordLocalChangeSeqNum, NEW.clazzLogAttendanceRecordLastChangedBy, NEW.clazzLogAttendanceRecordLastChangedTime) ON CONFLICT (clazzLogAttendanceRecordUid) DO UPDATE SET clazzLogAttendanceRecordClazzLogUid = EXCLUDED.clazzLogAttendanceRecordClazzLogUid, clazzLogAttendanceRecordPersonUid = EXCLUDED.clazzLogAttendanceRecordPersonUid, attendanceStatus = EXCLUDED.attendanceStatus, clazzLogAttendanceRecordMasterChangeSeqNum = EXCLUDED.clazzLogAttendanceRecordMasterChangeSeqNum, clazzLogAttendanceRecordLocalChangeSeqNum = EXCLUDED.clazzLogAttendanceRecordLocalChangeSeqNum, clazzLogAttendanceRecordLastChangedBy = EXCLUDED.clazzLogAttendanceRecordLastChangedBy, clazzLogAttendanceRecordLastChangedTime = EXCLUDED.clazzLogAttendanceRecordLastChangedTime ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql",
            "DROP TRIGGER IF EXISTS clazzlogattendancerecord_remote_insert_trig ON ClazzLogAttendanceRecord_ReceiveView",
            " CREATE TRIGGER clazzlogattendancerecord_remote_insert_trig INSTEAD OF INSERT ON ClazzLogAttendanceRecord_ReceiveView FOR EACH ROW EXECUTE PROCEDURE clazzlogattendancerecord_remote_insert_fn() ")
    }
}

val MIGRATION_94_95 = DoorMigrationStatementList(94, 95) { db ->
    if(db.dbType() == DoorDbType.SQLITE) {
        listOf(
            "DROP TRIGGER IF EXISTS ATTUPD_PersonPicture",
            "DROP TABLE IF EXISTS ZombieAttachmentData",
            "CREATE TABLE IF NOT EXISTS ZombieAttachmentData (  zaTableId  INTEGER  NOT NULL , zaPrimaryKey  INTEGER  NOT NULL , zaMd5  TEXT , zaUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )",
            """
        |
        |        CREATE TRIGGER ATTUPD_PersonPicture
        |        AFTER UPDATE ON PersonPicture FOR EACH ROW WHEN
        |        OLD.personPictureMd5 IS NOT NULL
        |        BEGIN
        |        
        |        INSERT INTO ZombieAttachmentData(zaTableId, zaPrimaryKey, zaMd5) 
        |        SELECT 50 AS zaTableId, OLD.personPictureUid AS zaPrimaryKey, OLD.personPictureMd5 AS zaMd5
        |          FROM PersonPicture   
        |         WHERE PersonPicture.personPictureUid = OLD.personPictureUid
        |           AND (SELECT COUNT(*) 
        |                  FROM PersonPicture
        |                 WHERE personPictureMd5 = OLD.personPictureMd5) = 0
        |    ; 
        |        END
        |    
        """.trimMargin()
        )
    }else {
        listOf(
            "DROP TRIGGER IF EXISTS attach_PersonPicture_trig ON PersonPicture",
            "DROP TABLE IF EXISTS ZombieAttachmentData",
            "CREATE TABLE IF NOT EXISTS ZombieAttachmentData (  zaTableId  INTEGER  NOT NULL , zaPrimaryKey  BIGINT  NOT NULL , zaMd5  TEXT , zaUid  BIGSERIAL  PRIMARY KEY  NOT NULL )",
            """
        |    CREATE OR REPLACE FUNCTION attach_PersonPicture_fn() RETURNS trigger AS ${'$'}${'$'}
        |    BEGIN
        |    
        |    INSERT INTO ZombieAttachmentData(zaTableId, zaPrimaryKey, zaMd5) 
        |    SELECT 50 AS zaTableId, OLD.personPictureUid AS zaPrimaryKey, OLD.personPictureMd5 AS zaMd5
        |      FROM PersonPicture   
        |     WHERE PersonPicture.personPictureUid = OLD.personPictureUid
        |       AND (SELECT COUNT(*) 
        |              FROM PersonPicture
        |             WHERE personPictureMd5 = OLD.personPictureMd5) = 0
        |;
        |    RETURN NEW;
        |    END ${'$'}${'$'}
        |    LANGUAGE plpgsql
        """.trimMargin(),

            """
        |CREATE TRIGGER attach_PersonPicture_trig
        |AFTER UPDATE ON PersonPicture
        |FOR EACH ROW WHEN (OLD.personPictureMd5 IS NOT NULL)
        |EXECUTE PROCEDURE attach_PersonPicture_fn();
        """.trimMargin())
    }
}

val MIGRATION_95_96 = DoorMigrationStatementList(95, 96) { db ->
    if(db.dbType() == DoorDbType.POSTGRES) {
        listOf(
            "DROP VIEW PersonAuth2_receiveview",
            "ALTER TABLE PersonAuth2 ALTER COLUMN pauthLcb TYPE BIGINT",
            "CREATE VIEW PersonAuth2_ReceiveView AS  SELECT PersonAuth2.*, PersonAuth2Replicate.* FROM PersonAuth2 LEFT JOIN PersonAuth2Replicate ON PersonAuth2Replicate.paPk = PersonAuth2.pauthUid",
            "CREATE OR REPLACE FUNCTION personauth2_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO PersonAuth2(pauthUid, pauthMechanism, pauthAuth, pauthLcsn, pauthPcsn, pauthLcb, pauthLct) VALUES (NEW.pauthUid, NEW.pauthMechanism, NEW.pauthAuth, NEW.pauthLcsn, NEW.pauthPcsn, NEW.pauthLcb, NEW.pauthLct) ON CONFLICT (pauthUid) DO UPDATE SET pauthMechanism = EXCLUDED.pauthMechanism, pauthAuth = EXCLUDED.pauthAuth, pauthLcsn = EXCLUDED.pauthLcsn, pauthPcsn = EXCLUDED.pauthPcsn, pauthLcb = EXCLUDED.pauthLcb, pauthLct = EXCLUDED.pauthLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql",                    "CREATE TRIGGER personauth2_remote_insert_trig INSTEAD OF INSERT ON PersonAuth2_ReceiveView FOR EACH ROW EXECUTE PROCEDURE personauth2_remote_insert_fn()")
    }else {
        listOf()
    }
}

val MIGRATION_96_97 = DoorMigrationStatementList(96, 97) { db ->
    if(db.dbType() == DoorDbType.SQLITE) {
        listOf("DROP TABLE ZombieAttachmentData",
            "CREATE TABLE ZombieAttachmentData (  zaUri  TEXT , zaUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )",
            "DROP TRIGGER IF EXISTS ATTUPD_PersonPicture",
            """
                    |
                    |        CREATE TRIGGER ATTUPD_PersonPicture
                    |        AFTER UPDATE ON PersonPicture FOR EACH ROW WHEN
                    |        OLD.personPictureMd5 IS NOT NULL
                    |        BEGIN
                    |        
                    |        INSERT INTO ZombieAttachmentData(zaUri) 
                    |        SELECT OLD.personPictureUri AS zaUri
                    |          FROM PersonPicture   
                    |         WHERE PersonPicture.personPictureUid = OLD.personPictureUid
                    |           AND (SELECT COUNT(*) 
                    |                  FROM PersonPicture
                    |                 WHERE personPictureMd5 = OLD.personPictureMd5) = 0
                    |    ; 
                    |        END
                    |    
                    """.trimMargin(),
        )
    }else {
        listOf("DROP TABLE IF EXISTS ZombieAttachmentData",
            "CREATE TABLE IF NOT EXISTS ZombieAttachmentData (  zaUri  TEXT , zaUid  SERIAL  PRIMARY KEY  NOT NULL )",
            """
                    |    CREATE OR REPLACE FUNCTION attach_PersonPicture_fn() RETURNS trigger AS ${'$'}${'$'}
                    |    BEGIN
                    |    
                    |    INSERT INTO ZombieAttachmentData(zaUri) 
                    |    SELECT OLD.personPictureUri AS zaUri
                    |      FROM PersonPicture   
                    |     WHERE PersonPicture.personPictureUid = OLD.personPictureUid
                    |       AND (SELECT COUNT(*) 
                    |              FROM PersonPicture
                    |             WHERE personPictureMd5 = OLD.personPictureMd5) = 0
                    |;
                    |    RETURN NEW;
                    |    END ${'$'}${'$'}
                    |    LANGUAGE plpgsql
                    """.trimMargin()
        )
    }
}

/***
 *  added 16/Feb/2022 to remove special html characters from text - & > <
 */
val MIGRATION_97_98 = DoorMigrationStatementList(97, 98) { db ->
    if(db.dbType() == DoorDbType.POSTGRES) {
        db.fixTincan()
        listOf()
    }else {
        listOf()
    }
}

val MIGRATION_98_99 = DoorMigrationStatementList(98, 99) {db ->
    if(db.dbType() == DoorDbType.POSTGRES) {
        listOf("ALTER TABLE ContentJobItem ALTER COLUMN cjiFinishTime TYPE BIGINT")
    }else {
        listOf()
    }
}

/**
 * 27/Feb/2022 - Fixes an issue where there could be multiple ContainerEntryFile entities
 * for the same file (particularly if downloading was done simultaneously). This could lead
 * to problems identifying actual Zombie files, and then deleting real data.
 */
val MIGRATION_99_100 = DoorMigrationStatementList(99, 100) {db ->
    listOf("""
  UPDATE ContainerEntry
     SET ceCefUid = 
         (SELECT CefOuter.cefUid
            FROM ContainerEntryFile CefOuter
           WHERE CefOuter.cefMd5 = 
		         (SELECT CefInner.cefMd5
				    FROM ContainerEntryFile CefInner
				   WHERE CefInner.cefUid = ContainerEntry.ceCefUid)
		ORDER BY CefOuter.cefUid
           LIMIT 1)
            """,
        """
DELETE FROM ContainerEntryFile 
      WHERE ContainerEntryFile.cefUid != 
            (SELECT CefInner.cefUid 
               FROM ContainerEntryFile CefInner
              WHERE CefInner.cefMd5 = ContainerEntryFile.cefMd5
           ORDER BY CefInner.cefUid
              LIMIT 1)
            """)
}

val MIGRATION_100_101 = DoorMigrationStatementList(100, 101) { db ->
    if(db.dbType() == DoorDbType.SQLITE) {
        listOf(
            "CREATE VIEW IF NOT EXISTS Container_ReceiveView AS  SELECT Container.*, ContainerReplicate.* FROM Container LEFT JOIN ContainerReplicate ON ContainerReplicate.containerPk = Container.containerUid ",
            "DROP TRIGGER IF EXISTS container_remote_insert_ins",
            "CREATE TRIGGER container_remote_insert_ins INSTEAD OF INSERT ON Container_ReceiveView FOR EACH ROW BEGIN REPLACE INTO Container(containerUid, cntLocalCsn, cntMasterCsn, cntLastModBy, cntLct, fileSize, containerContentEntryUid, cntLastModified, mimeType, remarks, mobileOptimized, cntNumEntries) SELECT NEW.containerUid, NEW.cntLocalCsn, NEW.cntMasterCsn, NEW.cntLastModBy, NEW.cntLct, NEW.fileSize, NEW.containerContentEntryUid, NEW.cntLastModified, NEW.mimeType, NEW.remarks, NEW.mobileOptimized, NEW.cntNumEntries WHERE NEW.cntLct > (SELECT COALESCE( (SELECT ContainerInt.cntLct FROM Container ContainerInt WHERE ContainerInt.containerUid = NEW.containerUid), 0)) /*psql ON CONFLICT (containerUid) DO UPDATE SET cntLocalCsn = EXCLUDED.cntLocalCsn, cntMasterCsn = EXCLUDED.cntMasterCsn, cntLastModBy = EXCLUDED.cntLastModBy, cntLct = EXCLUDED.cntLct, fileSize = EXCLUDED.fileSize, containerContentEntryUid = EXCLUDED.containerContentEntryUid, cntLastModified = EXCLUDED.cntLastModified, mimeType = EXCLUDED.mimeType, remarks = EXCLUDED.remarks, mobileOptimized = EXCLUDED.mobileOptimized, cntNumEntries = EXCLUDED.cntNumEntries */; END "
        )
    }else {
        listOf(
            "CREATE OR REPLACE FUNCTION container_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO Container(containerUid, cntLocalCsn, cntMasterCsn, cntLastModBy, cntLct, fileSize, containerContentEntryUid, cntLastModified, mimeType, remarks, mobileOptimized, cntNumEntries) SELECT NEW.containerUid, NEW.cntLocalCsn, NEW.cntMasterCsn, NEW.cntLastModBy, NEW.cntLct, NEW.fileSize, NEW.containerContentEntryUid, NEW.cntLastModified, NEW.mimeType, NEW.remarks, NEW.mobileOptimized, NEW.cntNumEntries WHERE NEW.cntLct > (SELECT COALESCE( (SELECT ContainerInt.cntLct FROM Container ContainerInt WHERE ContainerInt.containerUid = NEW.containerUid), 0)) ON CONFLICT (containerUid) DO UPDATE SET cntLocalCsn = EXCLUDED.cntLocalCsn, cntMasterCsn = EXCLUDED.cntMasterCsn, cntLastModBy = EXCLUDED.cntLastModBy, cntLct = EXCLUDED.cntLct, fileSize = EXCLUDED.fileSize, containerContentEntryUid = EXCLUDED.containerContentEntryUid, cntLastModified = EXCLUDED.cntLastModified, mimeType = EXCLUDED.mimeType, remarks = EXCLUDED.remarks, mobileOptimized = EXCLUDED.mobileOptimized, cntNumEntries = EXCLUDED.cntNumEntries ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        )
    }
}

/**
 * 07/March/2022: Fix ContentJobItem triggers to handle canceled status
 */
val MIGRATION_101_102 = DoorMigrationStatementList(101, 102) { db ->
    if(db.dbType() == DoorDbType.SQLITE) {
        val triggerNames = listOf("ContentJobItem_InsertTrigger",
            "ContentJobItem_UpdateRecursiveTotals",
            "ContentJobItem_UpdateRecursiveStatus",
            "ContentJobItem_UpdateParents",
            "ContentJobItem_UpdateStatusParent")
        triggerNames.map { "DROP TRIGGER IF EXISTS $it" } + listOf(
            " CREATE TRIGGER ContentJobItem_InsertTrigger AFTER INSERT ON ContentJobItem BEGIN UPDATE ContentJobItem SET cjiRecursiveProgress = NEW.cjiItemProgress, cjiRecursiveTotal = NEW.cjiItemTotal WHERE ContentJobItem.cjiUid = NEW.cjiUid; END; ",
            " CREATE TRIGGER ContentJobItem_UpdateRecursiveTotals AFTER UPDATE ON ContentJobItem FOR EACH ROW WHEN ( NEW.cjiItemProgress != OLD.cjiItemProgress OR NEW.cjiItemTotal != OLD.cjiItemTotal) BEGIN UPDATE ContentJobItem SET cjiRecursiveProgress = (cjiRecursiveProgress + (NEW.cjiItemProgress - OLD.cjiItemProgress)), cjiRecursiveTotal = (cjiRecursiveTotal + (NEW.cjiItemTotal - OLD.cjiItemTotal)) WHERE ContentJobItem.cjiUid = NEW.cjiUid; END; ",
            " CREATE TRIGGER ContentJobItem_UpdateRecursiveStatus AFTER UPDATE ON ContentJobItem FOR EACH ROW WHEN (NEW.cjiStatus != OLD.cjiStatus) BEGIN UPDATE ContentJobItem SET cjiRecursiveStatus = (CASE WHEN (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus ) = (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE status = 24) THEN 24 WHEN (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus ) = (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE status = 25) THEN 25 WHEN(SELECT COUNT(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus ) = (SELECT COUNT(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE status = 28) THEN 28 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE status = 12) THEN 12 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE (status = 25 OR status = 23)) THEN 23 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE status = 5) THEN 5 ELSE 4 END) WHERE contentJobItem.cjiUid = NEW.cjiUid; END; ",
            " CREATE TRIGGER ContentJobItem_UpdateParents AFTER UPDATE ON ContentJobItem FOR EACH ROW WHEN ( NEW.cjiParentCjiUid != 0 AND (NEW.cjiRecursiveProgress != OLD.cjiRecursiveProgress OR NEW.cjiRecursiveTotal != OLD.cjiRecursiveTotal)) BEGIN UPDATE ContentJobItem SET cjiRecursiveProgress = (cjiRecursiveProgress + (NEW.cjiRecursiveProgress - OLD.cjiRecursiveProgress)), cjiRecursiveTotal = (cjiRecursiveTotal + (NEW.cjiRecursiveTotal - OLD.cjiRecursiveTotal)) WHERE ContentJobItem.cjiUid = NEW.cjiParentCjiUid; END; ",
            " CREATE TRIGGER ContentJobItem_UpdateStatusParent AFTER UPDATE ON ContentJobItem FOR EACH ROW WHEN ( NEW.cjiParentCjiUid != 0 AND (New.cjiRecursiveStatus != OLD.cjiRecursiveStatus)) BEGIN UPDATE ContentJobItem SET cjiRecursiveStatus = (CASE WHEN (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus ) = (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE status = 24) THEN 24 WHEN (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus ) = (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE status = 25) THEN 25 WHEN(SELECT COUNT(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus ) = (SELECT COUNT(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE status = 28) THEN 28 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE status = 12) THEN 12 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE (status = 25 OR status = 23)) THEN 23 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE status = 5) THEN 5 ELSE 4 END) WHERE ContentJobItem.cjiUid = NEW.cjiParentCjiUid; END; "
        )
    }else {
        val triggerNames = listOf("contentjobiteminsert_trig",
            "contentjobitem_updaterecursivetotals_trig", "contentjobitem_updateparents_trig",
            "contentjobitem_updatestatus_trig", "contentjobitem_updatestatusparents_trig")
        triggerNames.map { "DROP TRIGGER IF EXISTS $it ON ContentJobItem" } +  listOf(
            " CREATE OR REPLACE FUNCTION contentjobiteminsert_fn() RETURNS TRIGGER AS $$ BEGIN UPDATE ContentJobItem SET cjiRecursiveProgress = NEW.cjiItemProgress, cjiRecursiveTotal = NEW.cjiItemTotal WHERE ContentJobItem.cjiUid = NEW.cjiUid; RETURN NEW; END $$ LANGUAGE plpgsql ",
            " CREATE TRIGGER contentjobiteminsert_trig AFTER INSERT ON ContentJobItem FOR EACH ROW EXECUTE PROCEDURE contentjobiteminsert_fn() ",
            " CREATE OR REPLACE FUNCTION contentjobitem_updaterecursivetotals_fn() RETURNS TRIGGER AS $$ BEGIN UPDATE ContentJobItem SET cjiRecursiveProgress = (cjiRecursiveProgress + (NEW.cjiItemProgress - OLD.cjiItemProgress)), cjiRecursiveTotal = (cjiRecursiveTotal + (NEW.cjiItemTotal - OLD.cjiItemTotal)) WHERE (NEW.cjiItemProgress != OLD.cjiItemProgress OR NEW.cjiItemTotal != OLD.cjiItemTotal) AND ContentJobItem.cjiUid = NEW.cjiUid; RETURN NEW; END $$ LANGUAGE plpgsql ",
            " CREATE TRIGGER contentjobitem_updaterecursivetotals_trig AFTER UPDATE ON ContentJobItem FOR EACH ROW EXECUTE PROCEDURE contentjobitem_updaterecursivetotals_fn(); ",
            " CREATE OR REPLACE FUNCTION contentjobitem_updateparents_fn() RETURNS TRIGGER AS $$ BEGIN UPDATE ContentJobItem SET cjiRecursiveProgress = (cjiRecursiveProgress + (NEW.cjiRecursiveProgress - OLD.cjiRecursiveProgress)), cjiRecursiveTotal = (cjiRecursiveTotal + (NEW.cjiRecursiveTotal - OLD.cjiRecursiveTotal)) WHERE (NEW.cjiRecursiveProgress != OLD.cjiRecursiveProgress OR NEW.cjiRecursiveTotal != OLD.cjiRecursiveTotal) AND ContentJobItem.cjiUid = NEW.cjiParentCjiUid AND NEW.cjiParentCjiUid != 0; RETURN NEW; END $$ LANGUAGE plpgsql ",
            " CREATE TRIGGER contentjobitem_updateparents_trig AFTER UPDATE ON ContentJobItem FOR EACH ROW EXECUTE PROCEDURE contentjobitem_updateparents_fn(); ",
            " CREATE OR REPLACE FUNCTION contentjobitem_updatestatus_fn() RETURNS TRIGGER AS $$ BEGIN UPDATE ContentJobItem SET cjiRecursiveStatus = (CASE WHEN (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus ) = (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE status = 24) THEN 24 WHEN (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus ) = (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE status = 25) THEN 25 WHEN(SELECT COUNT(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus ) = (SELECT COUNT(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE status = 28) THEN 28 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE status = 12) THEN 12 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE (status = 25 OR status = 23)) THEN 23 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiUid) AS JobStatus WHERE status = 5) THEN 5 ELSE 4 END) WHERE contentJobItem.cjiUid = NEW.cjiUid AND NEW.cjiStatus != OLD.cjiStatus; RETURN NEW; END $$ LANGUAGE plpgsql ",
            " CREATE TRIGGER contentjobitem_updatestatus_trig AFTER UPDATE ON ContentJobItem FOR EACH ROW EXECUTE PROCEDURE contentjobitem_updatestatus_fn(); ",
            " CREATE OR REPLACE FUNCTION contentjobitem_updatestatusparents_fn() RETURNS TRIGGER AS $$ BEGIN UPDATE ContentJobItem SET cjiRecursiveStatus = (CASE WHEN (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus ) = (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE status = 24) THEN 24 WHEN (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus ) = (SELECT Count(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE status = 25) THEN 25 WHEN(SELECT COUNT(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus ) = (SELECT COUNT(*) FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE status = 28) THEN 28 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE status = 12) THEN 12 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE (status = 25 OR status = 23)) THEN 23 WHEN EXISTS (SELECT status FROM (SELECT cjiRecursiveStatus AS status FROM ContentJobItem WHERE cjiParentCjiUid = NEW.cjiParentCjiUid UNION SELECT cjiStatus AS status FROM ContentJobItem WHERE cjiUid = NEW.cjiParentCjiUid) AS JobStatus WHERE status = 5) THEN 5 ELSE 4 END) WHERE NEW.cjiParentCjiUid != 0 AND NEW.cjiRecursiveStatus != OLD.cjiRecursiveStatus AND ContentJobItem.cjiUid = NEW.cjiParentCjiUid; RETURN NEW; END $$ LANGUAGE plpgsql ",
            " CREATE TRIGGER contentjobitem_updatestatusparents_trig AFTER UPDATE ON ContentJobItem FOR EACH ROW EXECUTE PROCEDURE contentjobitem_updatestatusparents_fn(); "
        )
    }
}

val MIGRATION_102_103 = DoorMigrationStatementList(102, 103) { db ->
    val stmtList = mutableListOf<String>()
    if(db.dbType() == DoorDbType.SQLITE) {
        //New entities
        stmtList +=
            "CREATE TABLE IF NOT EXISTS Chat (  chatStartDate  INTEGER  NOT NULL , chatTitle  TEXT , chatGroup  INTEGER  NOT NULL , chatLct  INTEGER  NOT NULL , chatUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS ChatReplicate (  chatPk  INTEGER  NOT NULL , chatVersionId  INTEGER  NOT NULL  DEFAULT 0 , chatDestination  INTEGER  NOT NULL , chatPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (chatPk, chatDestination) )"
        stmtList +=
            "CREATE INDEX index_ChatReplicate_chatPk_chatDestination_chatVersionId ON ChatReplicate (chatPk, chatDestination, chatVersionId)"
        stmtList +=
            "CREATE INDEX index_ChatReplicate_chatDestination_chatPending ON ChatReplicate (chatDestination, chatPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS ChatMember (  chatMemberChatUid  INTEGER  NOT NULL , chatMemberPersonUid  INTEGER  NOT NULL , chatMemberJoinedDate  INTEGER  NOT NULL , chatMemberLeftDate  INTEGER  NOT NULL , chatMemberLct  INTEGER  NOT NULL , chatMemberUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS ChatMemberReplicate (  chatMemberPk  INTEGER  NOT NULL , chatMemberVersionId  INTEGER  NOT NULL  DEFAULT 0 , chatMemberDestination  INTEGER  NOT NULL , chatMemberPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (chatMemberPk, chatMemberDestination) )"
        stmtList +=
            "CREATE INDEX index_ChatMemberReplicate_chatMemberPk_chatMemberDestination_chatMemberVersionId ON ChatMemberReplicate (chatMemberPk, chatMemberDestination, chatMemberVersionId)"
        stmtList +=
            "CREATE INDEX index_ChatMemberReplicate_chatMemberDestination_chatMemberPending ON ChatMemberReplicate (chatMemberDestination, chatMemberPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentMark (  camAssignmentUid  INTEGER  NOT NULL , camSubmitterUid  INTEGER  NOT NULL , camMark  REAL  NOT NULL , camPenalty  INTEGER  NOT NULL , camLct  INTEGER  NOT NULL , camUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentMarkReplicate (  camPk  INTEGER  NOT NULL , camVersionId  INTEGER  NOT NULL  DEFAULT 0 , camDestination  INTEGER  NOT NULL , camPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (camPk, camDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentMarkReplicate_camPk_camDestination_camVersionId ON CourseAssignmentMarkReplicate (camPk, camDestination, camVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentMarkReplicate_camDestination_camPending ON CourseAssignmentMarkReplicate (camDestination, camPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentSubmission (  casAssignmentUid  INTEGER  NOT NULL , casSubmitterUid  INTEGER  NOT NULL , casSubmitterPersonUid  INTEGER  NOT NULL , casText  TEXT , casType  INTEGER  NOT NULL , casTimestamp  INTEGER  NOT NULL , casUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentSubmissionAttachment (  casaSubmissionUid  INTEGER  NOT NULL , casaMimeType  TEXT , casaUri  TEXT , casaMd5  TEXT , casaSize  INTEGER  NOT NULL , casaTimestamp  INTEGER  NOT NULL , casaUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentSubmissionAttachmentReplicate (  casaPk  INTEGER  NOT NULL , casaVersionId  INTEGER  NOT NULL  DEFAULT 0 , casaDestination  INTEGER  NOT NULL , casaPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (casaPk, casaDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentSubmissionAttachmentReplicate_casaPk_casaDestination_casaVersionId ON CourseAssignmentSubmissionAttachmentReplicate (casaPk, casaDestination, casaVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentSubmissionAttachmentReplicate_casaDestination_casaPending ON CourseAssignmentSubmissionAttachmentReplicate (casaDestination, casaPending)"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentSubmissionReplicate (  casPk  INTEGER  NOT NULL , casVersionId  INTEGER  NOT NULL  DEFAULT 0 , casDestination  INTEGER  NOT NULL , casPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (casPk, casDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentSubmissionReplicate_casPk_casDestination_casVersionId ON CourseAssignmentSubmissionReplicate (casPk, casDestination, casVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentSubmissionReplicate_casDestination_casPending ON CourseAssignmentSubmissionReplicate (casDestination, casPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseBlock (  cbType  INTEGER  NOT NULL , cbIndentLevel  INTEGER  NOT NULL , cbModuleParentBlockUid  INTEGER  NOT NULL , cbTitle  TEXT , cbDescription  TEXT , cbCompletionCriteria  INTEGER  NOT NULL , cbHideUntilDate  INTEGER  NOT NULL , cbDeadlineDate  INTEGER  NOT NULL , cbLateSubmissionPenalty  INTEGER  NOT NULL , cbGracePeriodDate  INTEGER  NOT NULL , cbMaxPoints  INTEGER  NOT NULL , cbMinPoints  INTEGER  NOT NULL , cbIndex  INTEGER  NOT NULL , cbClazzUid  INTEGER  NOT NULL , cbActive  INTEGER  NOT NULL , cbHidden  INTEGER  NOT NULL , cbEntityUid  INTEGER  NOT NULL , cbLct  INTEGER  NOT NULL , cbUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList += "CREATE INDEX index_CourseBlock_cbClazzUid ON CourseBlock (cbClazzUid)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseBlockReplicate (  cbPk  INTEGER  NOT NULL , cbVersionId  INTEGER  NOT NULL  DEFAULT 0 , cbDestination  INTEGER  NOT NULL , cbPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (cbPk, cbDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseBlockReplicate_cbPk_cbDestination_cbVersionId ON CourseBlockReplicate (cbPk, cbDestination, cbVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseBlockReplicate_cbDestination_cbPending ON CourseBlockReplicate (cbDestination, cbPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseDiscussion (  courseDiscussionTitle  TEXT , courseDiscussionDesc  TEXT , courseDiscussionClazzUid  INTEGER  NOT NULL , courseDiscussionActive  INTEGER  NOT NULL , courseDiscussionLct  INTEGER  NOT NULL , courseDiscussionUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseDiscussionReplicate (  courseDiscussionPk  INTEGER  NOT NULL , courseDiscussionVersionId  INTEGER  NOT NULL  DEFAULT 0 , courseDiscussionDestination  INTEGER  NOT NULL , courseDiscussionPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (courseDiscussionPk, courseDiscussionDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseDiscussionReplicate_courseDiscussionPk_courseDiscussionDestination_courseDiscussionVersionId ON CourseDiscussionReplicate (courseDiscussionPk, courseDiscussionDestination, courseDiscussionVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseDiscussionReplicate_courseDiscussionDestination_courseDiscussionPending ON CourseDiscussionReplicate (courseDiscussionDestination, courseDiscussionPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseGroupMember (  cgmSetUid  INTEGER  NOT NULL , cgmGroupNumber  INTEGER  NOT NULL , cgmPersonUid  INTEGER  NOT NULL , cgmLct  INTEGER  NOT NULL , cgmUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseGroupMemberReplicate (  cgmPk  INTEGER  NOT NULL , cgmVersionId  INTEGER  NOT NULL  DEFAULT 0 , cgmDestination  INTEGER  NOT NULL , cgmPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (cgmPk, cgmDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseGroupMemberReplicate_cgmPk_cgmDestination_cgmVersionId ON CourseGroupMemberReplicate (cgmPk, cgmDestination, cgmVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseGroupMemberReplicate_cgmDestination_cgmPending ON CourseGroupMemberReplicate (cgmDestination, cgmPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseGroupSet (  cgsName  TEXT , cgsTotalGroups  INTEGER  NOT NULL , cgsActive  INTEGER  NOT NULL , cgsClazzUid  INTEGER  NOT NULL , cgsLct  INTEGER  NOT NULL , cgsUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList += "CREATE INDEX index_CourseGroupSet_cgsClazzUid ON CourseGroupSet (cgsClazzUid)"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseGroupSetReplicate (  cgsPk  INTEGER  NOT NULL , cgsVersionId  INTEGER  NOT NULL  DEFAULT 0 , cgsDestination  INTEGER  NOT NULL , cgsPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (cgsPk, cgsDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseGroupSetReplicate_cgsPk_cgsDestination_cgsVersionId ON CourseGroupSetReplicate (cgsPk, cgsDestination, cgsVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseGroupSetReplicate_cgsDestination_cgsPending ON CourseGroupSetReplicate (cgsDestination, cgsPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CoursePicture (  coursePictureClazzUid  INTEGER  NOT NULL , coursePictureMasterCsn  INTEGER  NOT NULL , coursePictureLocalCsn  INTEGER  NOT NULL , coursePictureLastChangedBy  INTEGER  NOT NULL , coursePictureLct  INTEGER  NOT NULL , coursePictureUri  TEXT , coursePictureMd5  TEXT , coursePictureFileSize  INTEGER  NOT NULL , coursePictureTimestamp  INTEGER  NOT NULL , coursePictureMimeType  TEXT , coursePictureActive  INTEGER  NOT NULL , coursePictureUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS CoursePictureReplicate (  cpPk  INTEGER  NOT NULL , cpVersionId  INTEGER  NOT NULL  DEFAULT 0 , cpDestination  INTEGER  NOT NULL , cpPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (cpPk, cpDestination) )"
        stmtList +=
            "CREATE INDEX index_CoursePictureReplicate_cpPk_cpDestination_cpVersionId ON CoursePictureReplicate (cpPk, cpDestination, cpVersionId)"
        stmtList +=
            "CREATE INDEX index_CoursePictureReplicate_cpDestination_cpPending ON CoursePictureReplicate (cpDestination, cpPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseTerminology (  ctTitle  TEXT , ctTerminology  TEXT , ctLct  INTEGER  NOT NULL , ctUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseTerminologyReplicate (  ctPk  INTEGER  NOT NULL , ctVersionId  INTEGER  NOT NULL  DEFAULT 0 , ctDestination  INTEGER  NOT NULL , ctPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (ctPk, ctDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseTerminologyReplicate_ctPk_ctDestination_ctVersionId ON CourseTerminologyReplicate (ctPk, ctDestination, ctVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseTerminologyReplicate_ctDestination_ctPending ON CourseTerminologyReplicate (ctDestination, ctPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS DiscussionPost (  discussionPostTitle  TEXT , discussionPostMessage  TEXT , discussionPostStartDate  INTEGER  NOT NULL , discussionPostDiscussionTopicUid  INTEGER  NOT NULL , discussionPostVisible  INTEGER  NOT NULL , discussionPostArchive  INTEGER  NOT NULL , discussionPostStartedPersonUid  INTEGER  NOT NULL , discussionPostClazzUid  INTEGER  NOT NULL , discussionPostLct  INTEGER  NOT NULL , discussionPostUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS DiscussionPostReplicate (  discussionPostPk  INTEGER  NOT NULL , discussionPostVersionId  INTEGER  NOT NULL  DEFAULT 0 , discussionPostDestination  INTEGER  NOT NULL , discussionPostPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (discussionPostPk, discussionPostDestination) )"
        stmtList +=
            "CREATE INDEX index_DiscussionPostReplicate_discussionPostPk_discussionPostDestination_discussionPostVersionId ON DiscussionPostReplicate (discussionPostPk, discussionPostDestination, discussionPostVersionId)"
        stmtList +=
            "CREATE INDEX index_DiscussionPostReplicate_discussionPostDestination_discussionPostPending ON DiscussionPostReplicate (discussionPostDestination, discussionPostPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS DiscussionTopic (  discussionTopicTitle  TEXT , discussionTopicDesc  TEXT , discussionTopicStartDate  INTEGER  NOT NULL , discussionTopicCourseDiscussionUid  INTEGER  NOT NULL , discussionTopicVisible  INTEGER  NOT NULL , discussionTopicArchive  INTEGER  NOT NULL , discussionTopicIndex  INTEGER  NOT NULL , discussionTopicClazzUid  INTEGER  NOT NULL , discussionTopicLct  INTEGER  NOT NULL , discussionTopicUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS DiscussionTopicReplicate (  discussionTopicPk  INTEGER  NOT NULL , discussionTopicVersionId  INTEGER  NOT NULL  DEFAULT 0 , discussionTopicDestination  INTEGER  NOT NULL , discussionTopicPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (discussionTopicPk, discussionTopicDestination) )"
        stmtList +=
            "CREATE INDEX index_DiscussionTopicReplicate_discussionTopicPk_discussionTopicDestination_discussionTopicVersionId ON DiscussionTopicReplicate (discussionTopicPk, discussionTopicDestination, discussionTopicVersionId)"
        stmtList +=
            "CREATE INDEX index_DiscussionTopicReplicate_discussionTopicDestination_discussionTopicPending ON DiscussionTopicReplicate (discussionTopicDestination, discussionTopicPending)"


        stmtList +=
            "CREATE TABLE IF NOT EXISTS Message (  messageSenderPersonUid  INTEGER  NOT NULL , messageTableId  INTEGER  NOT NULL , messageEntityUid  INTEGER  NOT NULL , messageText  TEXT , messageTimestamp  INTEGER  NOT NULL , messageClazzUid  INTEGER  NOT NULL , messageLct  INTEGER  NOT NULL , messageUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS MessageRead (  messageReadPersonUid  INTEGER  NOT NULL , messageReadMessageUid  INTEGER  NOT NULL , messageReadEntityUid  INTEGER  NOT NULL , messageReadLct  INTEGER  NOT NULL , messageReadUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS MessageReadReplicate (  messageReadPk  INTEGER  NOT NULL , messageReadVersionId  INTEGER  NOT NULL  DEFAULT 0 , messageReadDestination  INTEGER  NOT NULL , messageReadPending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (messageReadPk, messageReadDestination) )"
        stmtList +=
            "CREATE INDEX index_MessageReadReplicate_messageReadPk_messageReadDestination_messageReadVersionId ON MessageReadReplicate (messageReadPk, messageReadDestination, messageReadVersionId)"
        stmtList +=
            "CREATE INDEX index_MessageReadReplicate_messageReadDestination_messageReadPending ON MessageReadReplicate (messageReadDestination, messageReadPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS MessageReplicate (  messagePk  INTEGER  NOT NULL , messageVersionId  INTEGER  NOT NULL  DEFAULT 0 , messageDestination  INTEGER  NOT NULL , messagePending  INTEGER  NOT NULL  DEFAULT 1 , PRIMARY KEY (messagePk, messageDestination) )"
        stmtList +=
            "CREATE INDEX index_MessageReplicate_messagePk_messageDestination_messageVersionId ON MessageReplicate (messagePk, messageDestination, messageVersionId)"
        stmtList +=
            "CREATE INDEX index_MessageReplicate_messageDestination_messagePending ON MessageReplicate (messageDestination, messagePending)"

        //Destructive migration (tables where old data is obsolete)
        stmtList += "DROP TABLE ClazzAssignment"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS ClazzAssignment (  caTitle  TEXT , caDescription  TEXT , caGroupUid  INTEGER  NOT NULL  DEFAULT 0 , caActive  INTEGER  NOT NULL , caClassCommentEnabled  INTEGER  NOT NULL , caPrivateCommentsEnabled  INTEGER  NOT NULL  DEFAULT 1 , caCompletionCriteria  INTEGER  NOT NULL  DEFAULT 100 , caRequireFileSubmission  INTEGER  NOT NULL  DEFAULT 1 , caFileType  INTEGER  NOT NULL  DEFAULT 0 , caSizeLimit  INTEGER  NOT NULL  DEFAULT 50 , caNumberOfFiles  INTEGER  NOT NULL  DEFAULT 1 , caSubmissionPolicy  INTEGER  NOT NULL  DEFAULT 1 , caMarkingType  INTEGER  NOT NULL  DEFAULT 1 , caRequireTextSubmission  INTEGER  NOT NULL  DEFAULT 1 , caTextLimitType  INTEGER  NOT NULL  DEFAULT 1 , caTextLimit  INTEGER  NOT NULL  DEFAULT 500 , caXObjectUid  INTEGER  NOT NULL  DEFAULT 0 , caClazzUid  INTEGER  NOT NULL , caLocalChangeSeqNum  INTEGER  NOT NULL , caMasterChangeSeqNum  INTEGER  NOT NULL , caLastChangedBy  INTEGER  NOT NULL , caLct  INTEGER  NOT NULL , caUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList += "CREATE INDEX index_ClazzAssignment_caClazzUid ON ClazzAssignment (caClazzUid)"

        stmtList += "DROP TABLE ClazzAssignmentContentJoin"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoin (  cacjContentUid  INTEGER  NOT NULL , cacjAssignmentUid  INTEGER  NOT NULL , cacjActive  INTEGER  NOT NULL , cacjWeight  INTEGER  NOT NULL  DEFAULT 0 , cacjMCSN  INTEGER  NOT NULL , cacjLCSN  INTEGER  NOT NULL , cacjLCB  INTEGER  NOT NULL , cacjLct  INTEGER  NOT NULL , cacjUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"

        stmtList += "DROP TABLE ClazzAssignmentRollUp"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS ClazzAssignmentRollUp (  cachePersonUid  INTEGER  NOT NULL , cacheContentEntryUid  INTEGER  NOT NULL , cacheClazzAssignmentUid  INTEGER  NOT NULL , cacheStudentScore  INTEGER  NOT NULL , cacheMaxScore  INTEGER  NOT NULL , cacheFinalWeightScoreWithPenalty  REAL  NOT NULL  DEFAULT 0 , cacheWeight  INTEGER  NOT NULL  DEFAULT 0 , cacheProgress  INTEGER  NOT NULL , cacheContentComplete  INTEGER  NOT NULL , cacheSuccess  INTEGER  NOT NULL , cachePenalty  INTEGER  NOT NULL , lastCsnChecked  INTEGER  NOT NULL , cacheUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"
        stmtList +=
            "CREATE UNIQUE INDEX index_ClazzAssignmentRollUp_cachePersonUid_cacheContentEntryUid_cacheClazzAssignmentUid ON ClazzAssignmentRollUp (cachePersonUid, cacheContentEntryUid, cacheClazzAssignmentUid)"

        stmtList += "DROP TABLE Comments"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS Comments (  commentsText  TEXT , commentsEntityType  INTEGER  NOT NULL , commentsEntityUid  INTEGER  NOT NULL , commentsPublic  INTEGER  NOT NULL , commentsStatus  INTEGER  NOT NULL , commentsPersonUid  INTEGER  NOT NULL , commentsToPersonUid  INTEGER  NOT NULL , commentSubmitterUid  INTEGER  NOT NULL , commentsFlagged  INTEGER  NOT NULL , commentsInActive  INTEGER  NOT NULL , commentsDateTimeAdded  INTEGER  NOT NULL , commentsDateTimeUpdated  INTEGER  NOT NULL , commentsMCSN  INTEGER  NOT NULL , commentsLCSN  INTEGER  NOT NULL , commentsLCB  INTEGER  NOT NULL , commentsLct  INTEGER  NOT NULL , commentsUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )"

        //Default policy is open
        stmtList += "ALTER TABLE Clazz ADD COLUMN clazzEnrolmentPolicy INTEGER NOT NULL DEFAULT 102 "
        //Default use English terminology
        stmtList += "ALTER TABLE Clazz ADD COLUMN clazzTerminologyUid INTEGER NOT NULL DEFAULT ${('e'.code shl(8)) + 'n'.code}"

        stmtList += "ALTER TABLE XObjectEntity ADD COLUMN objectStatementRefUid INTEGER NOT NULL DEFAULT 0"


        //Triggers for new entities
        stmtList +=
            " CREATE TRIGGER ch_ins_127 AFTER INSERT ON Chat BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 127 AS chTableId, NEW.chatUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 127 AND chEntityPk = NEW.chatUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_127 AFTER UPDATE ON Chat BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 127 AS chTableId, NEW.chatUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 127 AND chEntityPk = NEW.chatUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_127 AFTER DELETE ON Chat BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 127 AS chTableId, OLD.chatUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 127 AND chEntityPk = OLD.chatUid); END "
        stmtList +=
            "CREATE VIEW Chat_ReceiveView AS  SELECT Chat.*, ChatReplicate.* FROM Chat LEFT JOIN ChatReplicate ON ChatReplicate.chatPk = Chat.chatUid "
        stmtList +=
            " CREATE TRIGGER chat_remote_insert_ins INSTEAD OF INSERT ON Chat_ReceiveView FOR EACH ROW BEGIN REPLACE INTO Chat(chatUid, chatStartDate, chatTitle, chatGroup, chatLct) VALUES(NEW.chatUid, NEW.chatStartDate, NEW.chatTitle, NEW.chatGroup, NEW.chatLct) /*psql ON CONFLICT (chatUid) DO UPDATE SET chatStartDate = EXCLUDED.chatStartDate, chatTitle = EXCLUDED.chatTitle, chatGroup = EXCLUDED.chatGroup, chatLct = EXCLUDED.chatLct */ ; END "

        stmtList +=
            " CREATE TRIGGER ch_ins_128 AFTER INSERT ON ChatMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 128 AS chTableId, NEW.chatMemberUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 128 AND chEntityPk = NEW.chatMemberUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_128 AFTER UPDATE ON ChatMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 128 AS chTableId, NEW.chatMemberUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 128 AND chEntityPk = NEW.chatMemberUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_128 AFTER DELETE ON ChatMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 128 AS chTableId, OLD.chatMemberUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 128 AND chEntityPk = OLD.chatMemberUid); END "
        stmtList +=
            "CREATE VIEW ChatMember_ReceiveView AS  SELECT ChatMember.*, ChatMemberReplicate.* FROM ChatMember LEFT JOIN ChatMemberReplicate ON ChatMemberReplicate.chatMemberPk = ChatMember.chatMemberUid "
        stmtList +=
            " CREATE TRIGGER chatmember_remote_insert_ins INSTEAD OF INSERT ON ChatMember_ReceiveView FOR EACH ROW BEGIN REPLACE INTO ChatMember(chatMemberUid, chatMemberChatUid, chatMemberPersonUid, chatMemberJoinedDate, chatMemberLeftDate, chatMemberLct) VALUES(NEW.chatMemberUid, NEW.chatMemberChatUid, NEW.chatMemberPersonUid, NEW.chatMemberJoinedDate, NEW.chatMemberLeftDate, NEW.chatMemberLct) /*psql ON CONFLICT (chatMemberUid) DO UPDATE SET chatMemberChatUid = EXCLUDED.chatMemberChatUid, chatMemberPersonUid = EXCLUDED.chatMemberPersonUid, chatMemberJoinedDate = EXCLUDED.chatMemberJoinedDate, chatMemberLeftDate = EXCLUDED.chatMemberLeftDate, chatMemberLct = EXCLUDED.chatMemberLct */ ; END "


        stmtList +=
            " CREATE TRIGGER ch_ins_523 AFTER INSERT ON CourseAssignmentMark BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 523 AS chTableId, NEW.camUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 523 AND chEntityPk = NEW.camUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_523 AFTER UPDATE ON CourseAssignmentMark BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 523 AS chTableId, NEW.camUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 523 AND chEntityPk = NEW.camUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_523 AFTER DELETE ON CourseAssignmentMark BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 523 AS chTableId, OLD.camUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 523 AND chEntityPk = OLD.camUid); END "
        stmtList +=
            "CREATE VIEW CourseAssignmentMark_ReceiveView AS  SELECT CourseAssignmentMark.*, CourseAssignmentMarkReplicate.* FROM CourseAssignmentMark LEFT JOIN CourseAssignmentMarkReplicate ON CourseAssignmentMarkReplicate.camPk = CourseAssignmentMark.camUid "
        stmtList +=
            " CREATE TRIGGER courseassignmentmark_remote_insert_ins INSTEAD OF INSERT ON CourseAssignmentMark_ReceiveView FOR EACH ROW BEGIN REPLACE INTO CourseAssignmentMark(camUid, camAssignmentUid, camSubmitterUid, camMark, camPenalty, camLct) VALUES (NEW.camUid, NEW.camAssignmentUid, NEW.camSubmitterUid, NEW.camMark, NEW.camPenalty, NEW.camLct) /*psql ON CONFLICT (camUid) DO UPDATE SET camAssignmentUid = EXCLUDED.camAssignmentUid, camSubmitterUid = EXCLUDED.camSubmitterUid, camMark = EXCLUDED.camMark, camPenalty = EXCLUDED.camPenalty, camLct = EXCLUDED.camLct */; END "


        stmtList +=
            " CREATE TRIGGER ch_ins_522 AFTER INSERT ON CourseAssignmentSubmission BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 522 AS chTableId, NEW.casUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 522 AND chEntityPk = NEW.casUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_522 AFTER UPDATE ON CourseAssignmentSubmission BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 522 AS chTableId, NEW.casUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 522 AND chEntityPk = NEW.casUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_522 AFTER DELETE ON CourseAssignmentSubmission BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 522 AS chTableId, OLD.casUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 522 AND chEntityPk = OLD.casUid); END "
        stmtList +=
            "CREATE VIEW CourseAssignmentSubmission_ReceiveView AS  SELECT CourseAssignmentSubmission.*, CourseAssignmentSubmissionReplicate.* FROM CourseAssignmentSubmission LEFT JOIN CourseAssignmentSubmissionReplicate ON CourseAssignmentSubmissionReplicate.casPk = CourseAssignmentSubmission.casUid "
        stmtList +=
            " CREATE TRIGGER courseassignmentsubmission_remote_insert_ins INSTEAD OF INSERT ON CourseAssignmentSubmission_ReceiveView FOR EACH ROW BEGIN REPLACE INTO CourseAssignmentSubmission(casUid, casAssignmentUid, casSubmitterUid, casSubmitterPersonUid, casText, casType, casTimestamp) VALUES (NEW.casUid, NEW.casAssignmentUid, NEW.casSubmitterUid, NEW.casSubmitterPersonUid, NEW.casText, NEW.casType, NEW.casTimestamp) /*psql ON CONFLICT (casUid) DO UPDATE SET casAssignmentUid = EXCLUDED.casAssignmentUid, casSubmitterUid = EXCLUDED.casSubmitterUid, casSubmitterPersonUid = EXCLUDED.casSubmitterPersonUid, casText = EXCLUDED.casText, casType = EXCLUDED.casType, casTimestamp = EXCLUDED.casTimestamp */; END "

        stmtList +=
            " CREATE TRIGGER ch_ins_90 AFTER INSERT ON CourseAssignmentSubmissionAttachment BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 90 AS chTableId, NEW.casaUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 90 AND chEntityPk = NEW.casaUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_90 AFTER UPDATE ON CourseAssignmentSubmissionAttachment BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 90 AS chTableId, NEW.casaUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 90 AND chEntityPk = NEW.casaUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_90 AFTER DELETE ON CourseAssignmentSubmissionAttachment BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 90 AS chTableId, OLD.casaUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 90 AND chEntityPk = OLD.casaUid); END "
        stmtList +=
            "CREATE VIEW CourseAssignmentSubmissionAttachment_ReceiveView AS  SELECT CourseAssignmentSubmissionAttachment.*, CourseAssignmentSubmissionAttachmentReplicate.* FROM CourseAssignmentSubmissionAttachment LEFT JOIN CourseAssignmentSubmissionAttachmentReplicate ON CourseAssignmentSubmissionAttachmentReplicate.casaPk = CourseAssignmentSubmissionAttachment.casaUid "
        stmtList +=
            " CREATE TRIGGER courseassignmentsubmissionattachment_remote_insert_ins INSTEAD OF INSERT ON CourseAssignmentSubmissionAttachment_ReceiveView FOR EACH ROW BEGIN REPLACE INTO CourseAssignmentSubmissionAttachment(casaUid, casaSubmissionUid, casaMimeType, casaUri, casaMd5, casaSize, casaTimestamp) VALUES (NEW.casaUid, NEW.casaSubmissionUid, NEW.casaMimeType, NEW.casaUri, NEW.casaMd5, NEW.casaSize, NEW.casaTimestamp) /*psql ON CONFLICT (casaUid) DO UPDATE SET casaSubmissionUid = EXCLUDED.casaSubmissionUid, casaMimeType = EXCLUDED.casaMimeType, casaUri = EXCLUDED.casaUri, casaMd5 = EXCLUDED.casaMd5, casaSize = EXCLUDED.casaSize, casaTimestamp = EXCLUDED.casaTimestamp */; END "
        stmtList += """
        |
        |        CREATE TRIGGER ATTUPD_CourseAssignmentSubmissionAttachment
        |        AFTER UPDATE ON CourseAssignmentSubmissionAttachment FOR EACH ROW WHEN
        |        OLD.casaMd5 IS NOT NULL
        |        BEGIN
        |        
        |        INSERT INTO ZombieAttachmentData(zaUri) 
        |        SELECT OLD.casaUri AS zaUri
        |          FROM CourseAssignmentSubmissionAttachment   
        |         WHERE CourseAssignmentSubmissionAttachment.casaUid = OLD.casaUid
        |           AND (SELECT COUNT(*) 
        |                  FROM CourseAssignmentSubmissionAttachment
        |                 WHERE casaMd5 = OLD.casaMd5) = 0
        |    ; 
        |        END
        |    
        """.trimMargin()

        stmtList +=
            " CREATE TRIGGER ch_ins_124 AFTER INSERT ON CourseBlock BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 124 AS chTableId, NEW.cbUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 124 AND chEntityPk = NEW.cbUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_124 AFTER UPDATE ON CourseBlock BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 124 AS chTableId, NEW.cbUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 124 AND chEntityPk = NEW.cbUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_124 AFTER DELETE ON CourseBlock BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 124 AS chTableId, OLD.cbUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 124 AND chEntityPk = OLD.cbUid); END "
        stmtList +=
            "CREATE VIEW CourseBlock_ReceiveView AS  SELECT CourseBlock.*, CourseBlockReplicate.* FROM CourseBlock LEFT JOIN CourseBlockReplicate ON CourseBlockReplicate.cbPk = CourseBlock.cbUid "
        stmtList +=
            " CREATE TRIGGER courseblock_remote_insert_ins INSTEAD OF INSERT ON CourseBlock_ReceiveView FOR EACH ROW BEGIN REPLACE INTO CourseBlock(cbUid, cbType, cbIndentLevel, cbModuleParentBlockUid, cbTitle, cbDescription, cbCompletionCriteria, cbHideUntilDate, cbDeadlineDate, cbLateSubmissionPenalty, cbGracePeriodDate, cbMaxPoints,cbMinPoints, cbIndex, cbClazzUid, cbActive,cbHidden, cbEntityUid, cbLct) VALUES (NEW.cbUid, NEW.cbType, NEW.cbIndentLevel, NEW.cbModuleParentBlockUid, NEW.cbTitle, NEW.cbDescription, NEW.cbCompletionCriteria, NEW.cbHideUntilDate, NEW.cbDeadlineDate, NEW.cbLateSubmissionPenalty, NEW.cbGracePeriodDate, NEW.cbMaxPoints,NEW.cbMinPoints, NEW.cbIndex, NEW.cbClazzUid,NEW.cbActive, NEW.cbHidden, NEW.cbEntityUid, NEW.cbLct) /*psql ON CONFLICT (cbUid) DO UPDATE SET cbType = EXCLUDED.cbType, cbIndentLevel = EXCLUDED.cbIndentLevel, cbModuleParentBlockUid = EXCLUDED.cbModuleParentBlockUid, cbTitle = EXCLUDED.cbTitle, cbDescription = EXCLUDED.cbDescription, cbCompletionCriteria = EXCLUDED.cbCompletionCriteria, cbHideUntilDate = EXCLUDED.cbHideUntilDate,cbDeadlineDate = EXCLUDED.cbDeadlineDate, cbLateSubmissionPenalty = EXCLUDED.cbLateSubmissionPenalty, cbGracePeriodDate= EXCLUDED.cbGracePeriodDate, cbMaxPoints = EXCLUDED.cbMaxPoints, cbMinPoints = EXCLUDED.cbMinPoints, cbIndex = EXCLUDED.cbIndex,cbClazzUid = EXCLUDED.cbClazzUid, cbActive = EXCLUDED.cbActive, cbHidden = EXCLUDED.cbHidden, cbEntityUid = EXCLUDED.cbEntityUid, cbLct = EXCLUDED.cbLct */; END "


        stmtList +=
            " CREATE TRIGGER ch_ins_130 AFTER INSERT ON CourseDiscussion BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 130 AS chTableId, NEW.courseDiscussionUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 130 AND chEntityPk = NEW.courseDiscussionUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_130 AFTER UPDATE ON CourseDiscussion BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 130 AS chTableId, NEW.courseDiscussionUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 130 AND chEntityPk = NEW.courseDiscussionUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_130 AFTER DELETE ON CourseDiscussion BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 130 AS chTableId, OLD.courseDiscussionUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 130 AND chEntityPk = OLD.courseDiscussionUid); END "
        stmtList +=
            "CREATE VIEW CourseDiscussion_ReceiveView AS  SELECT CourseDiscussion.*, CourseDiscussionReplicate.* FROM CourseDiscussion LEFT JOIN CourseDiscussionReplicate ON CourseDiscussionReplicate.courseDiscussionPk = CourseDiscussion.courseDiscussionUid "
        stmtList +=
            " CREATE TRIGGER coursediscussion_remote_insert_ins INSTEAD OF INSERT ON CourseDiscussion_ReceiveView FOR EACH ROW BEGIN REPLACE INTO CourseDiscussion(courseDiscussionUid, courseDiscussionActive, courseDiscussionTitle, courseDiscussionDesc, courseDiscussionClazzUid, courseDiscussionLct) VALUES(NEW.courseDiscussionUid, NEW.courseDiscussionActive, NEW.courseDiscussionTitle, NEW.courseDiscussionDesc, NEW.courseDiscussionClazzUid, NEW.courseDiscussionLct) /*psql ON CONFLICT (courseDiscussionUid) DO UPDATE SET courseDiscussionActive = EXCLUDED.courseDiscussionActive, courseDiscussionTitle = EXCLUDED.courseDiscussionTitle, courseDiscussionDesc = EXCLUDED.courseDiscussionDesc, courseDiscussionClazzUid = EXCLUDED.courseDiscussionClazzUid, courseDiscussionLct = EXCLUDED.courseDiscussionLct */ ; END "

        stmtList +=
            " CREATE TRIGGER ch_ins_243 AFTER INSERT ON CourseGroupMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 243 AS chTableId, NEW.cgmUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 243 AND chEntityPk = NEW.cgmUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_243 AFTER UPDATE ON CourseGroupMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 243 AS chTableId, NEW.cgmUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 243 AND chEntityPk = NEW.cgmUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_243 AFTER DELETE ON CourseGroupMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 243 AS chTableId, OLD.cgmUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 243 AND chEntityPk = OLD.cgmUid); END "
        stmtList +=
            "CREATE VIEW CourseGroupMember_ReceiveView AS  SELECT CourseGroupMember.*, CourseGroupMemberReplicate.* FROM CourseGroupMember LEFT JOIN CourseGroupMemberReplicate ON CourseGroupMemberReplicate.cgmPk = CourseGroupMember.cgmUid "
        stmtList +=
            " CREATE TRIGGER coursegroupmember_remote_insert_ins INSTEAD OF INSERT ON CourseGroupMember_ReceiveView FOR EACH ROW BEGIN REPLACE INTO CourseGroupMember(cgmUid, cgmSetUid, cgmGroupNumber, cgmPersonUid, cgmLct) VALUES (NEW.cgmUid, NEW.cgmSetUid, NEW.cgmGroupNumber, NEW.cgmPersonUid, NEW.cgmLct) /*psql ON CONFLICT (cgmUid) DO UPDATE SET cgmSetUid = EXCLUDED.cgmSetUid, cgmGroupNumber = EXCLUDED.cgmGroupNumber, cgmPersonUid = EXCLUDED.cgmPersonUid, cgmLct = EXCLUDED.cgmLct */; END "

        stmtList +=
            " CREATE TRIGGER ch_ins_242 AFTER INSERT ON CourseGroupSet BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 242 AS chTableId, NEW.cgsUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 242 AND chEntityPk = NEW.cgsUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_242 AFTER UPDATE ON CourseGroupSet BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 242 AS chTableId, NEW.cgsUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 242 AND chEntityPk = NEW.cgsUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_242 AFTER DELETE ON CourseGroupSet BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 242 AS chTableId, OLD.cgsUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 242 AND chEntityPk = OLD.cgsUid); END "
        stmtList +=
            "CREATE VIEW CourseGroupSet_ReceiveView AS  SELECT CourseGroupSet.*, CourseGroupSetReplicate.* FROM CourseGroupSet LEFT JOIN CourseGroupSetReplicate ON CourseGroupSetReplicate.cgsPk = CourseGroupSet.cgsUid "
        stmtList +=
            " CREATE TRIGGER coursegroupset_remote_insert_ins INSTEAD OF INSERT ON CourseGroupSet_ReceiveView FOR EACH ROW BEGIN REPLACE INTO CourseGroupSet(cgsUid, cgsName, cgsTotalGroups, cgsActive, cgsClazzUid, cgsLct) VALUES (NEW.cgsUid, NEW.cgsName, NEW.cgsTotalGroups, NEW.cgsActive, NEW.cgsClazzUid, NEW.cgsLct) /*psql ON CONFLICT (cgsUid) DO UPDATE SET cgsName = EXCLUDED.cgsName, cgsTotalGroups = EXCLUDED.cgsTotalGroups, cgsActive = EXCLUDED.cgsActive, cgsClazzUid = EXCLUDED.cgsClazzUid, cgsLct = EXCLUDED.cgsLct */; END "

        stmtList +=
            " CREATE TRIGGER ch_ins_125 AFTER INSERT ON CoursePicture BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 125 AS chTableId, NEW.coursePictureUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 125 AND chEntityPk = NEW.coursePictureUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_125 AFTER UPDATE ON CoursePicture BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 125 AS chTableId, NEW.coursePictureUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 125 AND chEntityPk = NEW.coursePictureUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_125 AFTER DELETE ON CoursePicture BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 125 AS chTableId, OLD.coursePictureUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 125 AND chEntityPk = OLD.coursePictureUid); END "
        stmtList +=
            "CREATE VIEW CoursePicture_ReceiveView AS  SELECT CoursePicture.*, CoursePictureReplicate.* FROM CoursePicture LEFT JOIN CoursePictureReplicate ON CoursePictureReplicate.cpPk = CoursePicture.coursePictureUid "
        stmtList +=
            " CREATE TRIGGER coursepicture_remote_insert_ins INSTEAD OF INSERT ON CoursePicture_ReceiveView FOR EACH ROW BEGIN REPLACE INTO CoursePicture(coursePictureUid, coursePictureClazzUid, coursePictureMasterCsn, coursePictureLocalCsn, coursePictureLastChangedBy, coursePictureLct, coursePictureUri, coursePictureMd5, coursePictureFileSize, coursePictureTimestamp, coursePictureMimeType, coursePictureActive) VALUES (NEW.coursePictureUid, NEW.coursePictureClazzUid, NEW.coursePictureMasterCsn, NEW.coursePictureLocalCsn, NEW.coursePictureLastChangedBy, NEW.coursePictureLct, NEW.coursePictureUri, NEW.coursePictureMd5, NEW.coursePictureFileSize, NEW.coursePictureTimestamp, NEW.coursePictureMimeType, NEW.coursePictureActive) /*psql ON CONFLICT (coursePictureUid) DO UPDATE SET coursePictureClazzUid = EXCLUDED.coursePictureClazzUid, coursePictureMasterCsn = EXCLUDED.coursePictureMasterCsn, coursePictureLocalCsn = EXCLUDED.coursePictureLocalCsn, coursePictureLastChangedBy = EXCLUDED.coursePictureLastChangedBy, coursePictureLct = EXCLUDED.coursePictureLct, coursePictureUri = EXCLUDED.coursePictureUri, coursePictureMd5 = EXCLUDED.coursePictureMd5, coursePictureFileSize = EXCLUDED.coursePictureFileSize, coursePictureTimestamp = EXCLUDED.coursePictureTimestamp, coursePictureMimeType = EXCLUDED.coursePictureMimeType, coursePictureActive = EXCLUDED.coursePictureActive */; END "
        stmtList += """
        |
        |        CREATE TRIGGER ATTUPD_CoursePicture
        |        AFTER UPDATE ON CoursePicture FOR EACH ROW WHEN
        |        OLD.coursePictureMd5 IS NOT NULL
        |        BEGIN
        |        
        |        INSERT INTO ZombieAttachmentData(zaUri) 
        |        SELECT OLD.coursePictureUri AS zaUri
        |          FROM CoursePicture   
        |         WHERE CoursePicture.coursePictureUid = OLD.coursePictureUid
        |           AND (SELECT COUNT(*) 
        |                  FROM CoursePicture
        |                 WHERE coursePictureMd5 = OLD.coursePictureMd5) = 0
        |    ; 
        |        END
        |    
        """.trimMargin()

        stmtList +=
            " CREATE TRIGGER ch_ins_450 AFTER INSERT ON CourseTerminology BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 450 AS chTableId, NEW.ctUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 450 AND chEntityPk = NEW.ctUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_450 AFTER UPDATE ON CourseTerminology BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 450 AS chTableId, NEW.ctUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 450 AND chEntityPk = NEW.ctUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_450 AFTER DELETE ON CourseTerminology BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 450 AS chTableId, OLD.ctUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 450 AND chEntityPk = OLD.ctUid); END "
        stmtList +=
            "CREATE VIEW CourseTerminology_ReceiveView AS  SELECT CourseTerminology.*, CourseTerminologyReplicate.* FROM CourseTerminology LEFT JOIN CourseTerminologyReplicate ON CourseTerminologyReplicate.ctPk = CourseTerminology.ctUid "
        stmtList +=
            " CREATE TRIGGER courseterminology_remote_insert_ins INSTEAD OF INSERT ON CourseTerminology_ReceiveView FOR EACH ROW BEGIN REPLACE INTO CourseTerminology(ctUid, ctTitle, ctTerminology, ctLct) VALUES (NEW.ctUid, NEW.ctTitle, NEW.ctTerminology, NEW.ctLct) /*psql ON CONFLICT (ctUid) DO UPDATE SET ctTitle = EXCLUDED.ctTitle, ctTerminology = EXCLUDED.ctTerminology, ctLct = EXCLUDED.ctLct */; END "

        stmtList +=
            " CREATE TRIGGER ch_ins_132 AFTER INSERT ON DiscussionPost BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 132 AS chTableId, NEW.discussionPostUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 132 AND chEntityPk = NEW.discussionPostUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_132 AFTER UPDATE ON DiscussionPost BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 132 AS chTableId, NEW.discussionPostUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 132 AND chEntityPk = NEW.discussionPostUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_132 AFTER DELETE ON DiscussionPost BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 132 AS chTableId, OLD.discussionPostUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 132 AND chEntityPk = OLD.discussionPostUid); END "
        stmtList +=
            "CREATE VIEW DiscussionPost_ReceiveView AS  SELECT DiscussionPost.*, DiscussionPostReplicate.* FROM DiscussionPost LEFT JOIN DiscussionPostReplicate ON DiscussionPostReplicate.discussionPostPk = DiscussionPost.discussionPostUid "
        stmtList +=
            " CREATE TRIGGER discussionpost_remote_insert_ins INSTEAD OF INSERT ON DiscussionPost_ReceiveView FOR EACH ROW BEGIN REPLACE INTO DiscussionPost(discussionPostUid, discussionPostTitle, discussionPostMessage, discussionPostStartDate, discussionPostDiscussionTopicUid, discussionPostVisible, discussionPostArchive, discussionPostStartedPersonUid, discussionPostClazzUid, discussionPostLct) VALUES(NEW.discussionPostUid, NEW.discussionPostTitle, NEW.discussionPostMessage, NEW.discussionPostStartDate, NEW.discussionPostDiscussionTopicUid, NEW.discussionPostVisible, NEW.discussionPostArchive, NEW.discussionPostStartedPersonUid, NEW.discussionPostClazzUid, NEW.discussionPostLct) /*psql ON CONFLICT (discussionPostUid) DO UPDATE SET discussionPostTitle = EXCLUDED.discussionPostTitle , discussionPostMessage = EXCLUDED.discussionPostMessage , discussionPostStartDate = EXCLUDED.discussionPostStartDate , discussionPostDiscussionTopicUid = EXCLUDED.discussionPostDiscussionTopicUid, discussionPostVisible = EXCLUDED.discussionPostVisible , discussionPostArchive = EXCLUDED.discussionPostArchive , discussionPostStartedPersonUid = EXCLUDED.discussionPostStartedPersonUid , discussionPostClazzUid = EXCLUDED.discussionPostClazzUid, discussionPostLct = EXCLUDED.discussionPostLct */ ; END "

        stmtList +=
            " CREATE TRIGGER ch_ins_131 AFTER INSERT ON DiscussionTopic BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 131 AS chTableId, NEW.discussionTopicUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 131 AND chEntityPk = NEW.discussionTopicUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_131 AFTER UPDATE ON DiscussionTopic BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 131 AS chTableId, NEW.discussionTopicUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 131 AND chEntityPk = NEW.discussionTopicUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_131 AFTER DELETE ON DiscussionTopic BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 131 AS chTableId, OLD.discussionTopicUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 131 AND chEntityPk = OLD.discussionTopicUid); END "
        stmtList +=
            "CREATE VIEW DiscussionTopic_ReceiveView AS  SELECT DiscussionTopic.*, DiscussionTopicReplicate.* FROM DiscussionTopic LEFT JOIN DiscussionTopicReplicate ON DiscussionTopicReplicate.discussionTopicPk = DiscussionTopic.discussionTopicUid "
        stmtList +=
            " CREATE TRIGGER discussiontopic_remote_insert_ins INSTEAD OF INSERT ON DiscussionTopic_ReceiveView FOR EACH ROW BEGIN REPLACE INTO DiscussionTopic(discussionTopicUid, discussionTopicTitle, discussionTopicDesc, discussionTopicStartDate, discussionTopicCourseDiscussionUid, discussionTopicVisible, discussionTopicArchive, discussionTopicIndex, discussionTopicClazzUid, discussionTopicLct) VALUES(NEW.discussionTopicUid, NEW.discussionTopicTitle, NEW.discussionTopicDesc, NEW.discussionTopicStartDate, NEW.discussionTopicCourseDiscussionUid, NEW.discussionTopicVisible, NEW.discussionTopicArchive, NEW.discussionTopicIndex, NEW.discussionTopicClazzUid, NEW.discussionTopicLct) /*psql ON CONFLICT (discussionTopicUid) DO UPDATE SET discussionTopicTitle = EXCLUDED.discussionTopicTitle, discussionTopicDesc = EXCLUDED.discussionTopicDesc, discussionTopicStartDate = EXCLUDED.discussionTopicStartDate, discussionTopicCourseDiscussionUid = EXCLUDED.discussionTopicCourseDiscussionUid, discussionTopicVisible = EXCLUDED.discussionTopicVisible, discussionTopicArchive = EXCLUDED.discussionTopicArchive, discussionTopicIndex = EXCLUDED.discussionTopicIndex, discussionTopicClazzUid = EXCLUDED.discussionTopicClazzUid, discussionTopicLct = EXCLUDED.discussionTopicLct */ ; END "

        stmtList +=
            " CREATE TRIGGER ch_ins_126 AFTER INSERT ON Message BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 126 AS chTableId, NEW.messageUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 126 AND chEntityPk = NEW.messageUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_126 AFTER UPDATE ON Message BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 126 AS chTableId, NEW.messageUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 126 AND chEntityPk = NEW.messageUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_126 AFTER DELETE ON Message BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 126 AS chTableId, OLD.messageUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 126 AND chEntityPk = OLD.messageUid); END "
        stmtList +=
            "CREATE VIEW Message_ReceiveView AS  SELECT Message.*, MessageReplicate.* FROM Message LEFT JOIN MessageReplicate ON MessageReplicate.messagePk = Message.messageUid "
        stmtList +=
            " CREATE TRIGGER message_remote_insert_ins INSTEAD OF INSERT ON Message_ReceiveView FOR EACH ROW BEGIN REPLACE INTO Message(messageUid, messageSenderPersonUid, messageTableId, messageEntityUid, messageText, messageTimestamp, messageClazzUid, messageLct) VALUES(NEW.messageUid, NEW.messageSenderPersonUid, NEW.messageTableId, NEW.messageEntityUid, NEW.messageText, NEW.messageTimestamp, NEW.messageClazzUid, NEW.messageLct) /*psql ON CONFLICT (messageUid) DO UPDATE SET messageSenderPersonUid = EXCLUDED.messageSenderPersonUid, messageTableId = EXCLUDED.messageTableId, messageEntityUid = EXCLUDED.messageEntityUid, messageText = EXCLUDED.messageText, messageTimestamp = EXCLUDED.messageTimestamp, messageClazzUid = EXCLUDED.messageClazzUid, messageLct = EXCLUDED.messageLct */ ; END "

        stmtList +=
            " CREATE TRIGGER ch_ins_129 AFTER INSERT ON MessageRead BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 129 AS chTableId, NEW.messageReadUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 129 AND chEntityPk = NEW.messageReadUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_129 AFTER UPDATE ON MessageRead BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 129 AS chTableId, NEW.messageReadUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 129 AND chEntityPk = NEW.messageReadUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_129 AFTER DELETE ON MessageRead BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 129 AS chTableId, OLD.messageReadUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 129 AND chEntityPk = OLD.messageReadUid); END "
        stmtList +=
            "CREATE VIEW MessageRead_ReceiveView AS  SELECT MessageRead.*, MessageReadReplicate.* FROM MessageRead LEFT JOIN MessageReadReplicate ON MessageReadReplicate.messageReadPk = MessageRead.messageReadUid "
        stmtList +=
            " CREATE TRIGGER messageread_remote_insert_ins INSTEAD OF INSERT ON MessageRead_ReceiveView FOR EACH ROW BEGIN REPLACE INTO MessageRead(messageReadUid, messageReadPersonUid, messageReadMessageUid, messageReadEntityUid, messageReadLct) VALUES(NEW.messageReadUid, NEW.messageReadPersonUid, NEW.messageReadMessageUid, NEW.messageReadEntityUid, NEW.messageReadLct) /*psql ON CONFLICT (messageReadUid) DO UPDATE SET messageReadPersonUid = EXCLUDED.messageReadPersonUid, messageReadMessageUid = EXCLUDED.messageReadMessageUid, messageReadEntityUid = EXCLUDED.messageReadEntityUid, messageReadLct = EXCLUDED.messageReadLct */ ; END "

        //Triggers for entities with destructive migration
        stmtList += "DROP VIEW IF EXISTS ClazzAssignment_ReceiveView"
        stmtList += "DROP TRIGGER IF EXISTS clazzassignment_remote_insert_ins"
        stmtList += "DROP TRIGGER IF EXISTS ch_ins_521"
        stmtList += "DROP TRIGGER IF EXISTS ch_upd_521"
        stmtList += "DROP TRIGGER IF EXISTS ch_del_521"

        stmtList +=
            "CREATE VIEW ClazzAssignment_ReceiveView AS  SELECT ClazzAssignment.*, ClazzAssignmentReplicate.* FROM ClazzAssignment LEFT JOIN ClazzAssignmentReplicate ON ClazzAssignmentReplicate.caPk = ClazzAssignment.caUid "
        stmtList +=
            " CREATE TRIGGER clazzassignment_remote_insert_ins INSTEAD OF INSERT ON ClazzAssignment_ReceiveView FOR EACH ROW BEGIN REPLACE INTO ClazzAssignment(caUid, caTitle, caDescription, caGroupUid, caActive, caClassCommentEnabled, caPrivateCommentsEnabled, caRequireFileSubmission, caFileType, caSizeLimit, caNumberOfFiles, caSubmissionPolicy, caMarkingType, caRequireTextSubmission, caTextLimitType, caTextLimit, caXObjectUid, caClazzUid, caLocalChangeSeqNum, caMasterChangeSeqNum, caLastChangedBy, caLct) VALUES (NEW.caUid, NEW.caTitle, NEW.caDescription, NEW.caGroupUid, NEW.caActive, NEW.caClassCommentEnabled, NEW.caPrivateCommentsEnabled, NEW.caRequireFileSubmission, NEW.caFileType, NEW.caSizeLimit, NEW.caNumberOfFiles, NEW.caSubmissionPolicy, NEW.caMarkingType,NEW.caRequireTextSubmission, NEW.caTextLimitType, NEW.caTextLimit, NEW.caXObjectUid, NEW.caClazzUid, NEW.caLocalChangeSeqNum, NEW.caMasterChangeSeqNum, NEW.caLastChangedBy, NEW.caLct) /*psql ON CONFLICT (caUid) DO UPDATE SET caTitle = EXCLUDED.caTitle, caDescription = EXCLUDED.caDescription, caGroupUid = EXCLUDED.caGroupUid, caActive = EXCLUDED.caActive, caClassCommentEnabled = EXCLUDED.caClassCommentEnabled, caPrivateCommentsEnabled = EXCLUDED.caPrivateCommentsEnabled, caRequireFileSubmission = EXCLUDED.caRequireFileSubmission, caFileType = EXCLUDED.caFileType, caSizeLimit = EXCLUDED.caSizeLimit, caNumberOfFiles = EXCLUDED.caNumberOfFiles, caSubmissionPolicy = EXCLUDED.caSubmissionPolicy, caMarkingType = EXCLUDED.caMarkingType, caRequireTextSubmission = EXCLUDED.caRequireTextSubmission, caTextLimitType = EXCLUDED.caTextLimitType, caTextLimit = EXCLUDED.caTextLimit, caXObjectUid = EXCLUDED.caXObjectUid, caClazzUid = EXCLUDED.caClazzUid, caLocalChangeSeqNum = EXCLUDED.caLocalChangeSeqNum, caMasterChangeSeqNum = EXCLUDED.caMasterChangeSeqNum, caLastChangedBy = EXCLUDED.caLastChangedBy, caLct = EXCLUDED.caLct */; END "
        stmtList +=
            " CREATE TRIGGER ch_ins_521 AFTER INSERT ON ClazzAssignmentContentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 521 AS chTableId, NEW.cacjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 521 AND chEntityPk = NEW.cacjUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_521 AFTER UPDATE ON ClazzAssignmentContentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 521 AS chTableId, NEW.cacjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 521 AND chEntityPk = NEW.cacjUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_521 AFTER DELETE ON ClazzAssignmentContentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 521 AS chTableId, OLD.cacjUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 521 AND chEntityPk = OLD.cacjUid); END "

        stmtList += "DROP VIEW IF EXISTS ClazzAssignmentContentJoin_ReceiveView"
        stmtList += "DROP TRIGGER IF EXISTS clazzassignmentcontentjoin_remote_insert_ins"
        stmtList += "DROP TRIGGER IF EXISTS ch_ins_522"
        stmtList += "DROP TRIGGER IF EXISTS ch_upd_522"
        stmtList += "DROP TRIGGER IF EXISTS ch_del_522"


        stmtList += "DROP VIEW IF EXISTS Comments_ReceiveView"
        stmtList += "DROP TRIGGER IF EXISTS comments_remote_insert_ins"
        stmtList += "DROP TRIGGER IF EXISTS ch_ins_101"
        stmtList += "DROP TRIGGER IF EXISTS ch_upd_101"
        stmtList += "DROP TRIGGER IF EXISTS ch_del_101"
        stmtList +=
            "CREATE VIEW Comments_ReceiveView AS  SELECT Comments.*, CommentsReplicate.* FROM Comments LEFT JOIN CommentsReplicate ON CommentsReplicate.commentsPk = Comments.commentsUid "
        stmtList +=
            " CREATE TRIGGER comments_remote_insert_ins INSTEAD OF INSERT ON Comments_ReceiveView FOR EACH ROW BEGIN REPLACE INTO Comments(commentsUid, commentsText, commentsEntityType, commentsEntityUid, commentsPublic, commentsStatus, commentsPersonUid, commentsToPersonUid, commentSubmitterUid, commentsFlagged, commentsInActive, commentsDateTimeAdded, commentsDateTimeUpdated, commentsMCSN, commentsLCSN, commentsLCB, commentsLct) VALUES (NEW.commentsUid, NEW.commentsText, NEW.commentsEntityType, NEW.commentsEntityUid, NEW.commentsPublic, NEW.commentsStatus, NEW.commentsPersonUid, NEW.commentsToPersonUid, NEW.commentSubmitterUid, NEW.commentsFlagged, NEW.commentsInActive, NEW.commentsDateTimeAdded, NEW.commentsDateTimeUpdated, NEW.commentsMCSN, NEW.commentsLCSN, NEW.commentsLCB, NEW.commentsLct) /*psql ON CONFLICT (commentsUid) DO UPDATE SET commentsText = EXCLUDED.commentsText, commentsEntityType = EXCLUDED.commentsEntityType, commentsEntityUid = EXCLUDED.commentsEntityUid, commentsPublic = EXCLUDED.commentsPublic, commentsStatus = EXCLUDED.commentsStatus, commentsPersonUid = EXCLUDED.commentsPersonUid, commentsToPersonUid = EXCLUDED.commentsToPersonUid, commentSubmitterUid = EXCLUDED.commentSubmitterUid, commentsFlagged = EXCLUDED.commentsFlagged, commentsInActive = EXCLUDED.commentsInActive, commentsDateTimeAdded = EXCLUDED.commentsDateTimeAdded, commentsDateTimeUpdated = EXCLUDED.commentsDateTimeUpdated, commentsMCSN = EXCLUDED.commentsMCSN, commentsLCSN = EXCLUDED.commentsLCSN, commentsLCB = EXCLUDED.commentsLCB, commentsLct = EXCLUDED.commentsLct */; END "
        stmtList +=
            " CREATE TRIGGER ch_ins_101 AFTER INSERT ON Report BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 101 AS chTableId, NEW.reportUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 101 AND chEntityPk = NEW.reportUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_101 AFTER UPDATE ON Report BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 101 AS chTableId, NEW.reportUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 101 AND chEntityPk = NEW.reportUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_101 AFTER DELETE ON Report BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 101 AS chTableId, OLD.reportUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 101 AND chEntityPk = OLD.reportUid); END "
    }else {
        stmtList +=
            "CREATE TABLE IF NOT EXISTS Chat (  chatStartDate  BIGINT  NOT NULL , chatTitle  TEXT , chatGroup  BOOL  NOT NULL , chatLct  BIGINT  NOT NULL , chatUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS ChatMember (  chatMemberChatUid  BIGINT  NOT NULL , chatMemberPersonUid  BIGINT  NOT NULL , chatMemberJoinedDate  BIGINT  NOT NULL , chatMemberLeftDate  BIGINT  NOT NULL , chatMemberLct  BIGINT  NOT NULL , chatMemberUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS ChatReplicate (  chatPk  BIGINT  NOT NULL , chatVersionId  BIGINT  NOT NULL  DEFAULT 0 , chatDestination  BIGINT  NOT NULL , chatPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (chatPk, chatDestination) )"
        stmtList +=
            "CREATE INDEX index_ChatReplicate_chatPk_chatDestination_chatVersionId ON ChatReplicate (chatPk, chatDestination, chatVersionId)"
        stmtList +=
            "CREATE INDEX index_ChatReplicate_chatDestination_chatPending ON ChatReplicate (chatDestination, chatPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS ChatMemberReplicate (  chatMemberPk  BIGINT  NOT NULL , chatMemberVersionId  BIGINT  NOT NULL  DEFAULT 0 , chatMemberDestination  BIGINT  NOT NULL , chatMemberPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (chatMemberPk, chatMemberDestination) )"
        stmtList +=
            "CREATE INDEX index_ChatMemberReplicate_chatMemberPk_chatMemberDestination_chatMemberVersionId ON ChatMemberReplicate (chatMemberPk, chatMemberDestination, chatMemberVersionId)"
        stmtList +=
            "CREATE INDEX index_ChatMemberReplicate_chatMemberDestination_chatMemberPending ON ChatMemberReplicate (chatMemberDestination, chatMemberPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentMark (  camAssignmentUid  BIGINT  NOT NULL , camSubmitterUid  BIGINT  NOT NULL , camMark  FLOAT  NOT NULL , camPenalty  INTEGER  NOT NULL , camLct  BIGINT  NOT NULL , camUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentMarkReplicate (  camPk  BIGINT  NOT NULL , camVersionId  BIGINT  NOT NULL  DEFAULT 0 , camDestination  BIGINT  NOT NULL , camPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (camPk, camDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentMarkReplicate_camPk_camDestination_camVersionId ON CourseAssignmentMarkReplicate (camPk, camDestination, camVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentMarkReplicate_camDestination_camPending ON CourseAssignmentMarkReplicate (camDestination, camPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentSubmission (  casAssignmentUid  BIGINT  NOT NULL , casSubmitterUid  BIGINT  NOT NULL , casSubmitterPersonUid  BIGINT  NOT NULL , casText  TEXT , casType  INTEGER  NOT NULL , casTimestamp  BIGINT  NOT NULL , casUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentSubmissionAttachment (  casaSubmissionUid  BIGINT  NOT NULL , casaMimeType  TEXT , casaUri  TEXT , casaMd5  TEXT , casaSize  INTEGER  NOT NULL , casaTimestamp  BIGINT  NOT NULL , casaUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentSubmissionAttachmentReplicate (  casaPk  BIGINT  NOT NULL , casaVersionId  BIGINT  NOT NULL  DEFAULT 0 , casaDestination  BIGINT  NOT NULL , casaPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (casaPk, casaDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentSubmissionAttachmentReplicate_casaPk_casaDestination_casaVersionId ON CourseAssignmentSubmissionAttachmentReplicate (casaPk, casaDestination, casaVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentSubmissionAttachmentReplicate_casaDestination_casaPending ON CourseAssignmentSubmissionAttachmentReplicate (casaDestination, casaPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseAssignmentSubmissionReplicate (  casPk  BIGINT  NOT NULL , casVersionId  BIGINT  NOT NULL  DEFAULT 0 , casDestination  BIGINT  NOT NULL , casPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (casPk, casDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentSubmissionReplicate_casPk_casDestination_casVersionId ON CourseAssignmentSubmissionReplicate (casPk, casDestination, casVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseAssignmentSubmissionReplicate_casDestination_casPending ON CourseAssignmentSubmissionReplicate (casDestination, casPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseBlock (  cbType  INTEGER  NOT NULL , cbIndentLevel  INTEGER  NOT NULL , cbModuleParentBlockUid  BIGINT  NOT NULL , cbTitle  TEXT , cbDescription  TEXT , cbCompletionCriteria  INTEGER  NOT NULL , cbHideUntilDate  BIGINT  NOT NULL , cbDeadlineDate  BIGINT  NOT NULL , cbLateSubmissionPenalty  INTEGER  NOT NULL , cbGracePeriodDate  BIGINT  NOT NULL , cbMaxPoints  INTEGER  NOT NULL , cbMinPoints  INTEGER  NOT NULL , cbIndex  INTEGER  NOT NULL , cbClazzUid  BIGINT  NOT NULL , cbActive  BOOL  NOT NULL , cbHidden  BOOL  NOT NULL , cbEntityUid  BIGINT  NOT NULL , cbLct  BIGINT  NOT NULL , cbUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"
        stmtList += "CREATE INDEX index_CourseBlock_cbClazzUid ON CourseBlock (cbClazzUid)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseBlockReplicate (  cbPk  BIGINT  NOT NULL , cbVersionId  BIGINT  NOT NULL  DEFAULT 0 , cbDestination  BIGINT  NOT NULL , cbPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (cbPk, cbDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseBlockReplicate_cbPk_cbDestination_cbVersionId ON CourseBlockReplicate (cbPk, cbDestination, cbVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseBlockReplicate_cbDestination_cbPending ON CourseBlockReplicate (cbDestination, cbPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseDiscussion (  courseDiscussionTitle  TEXT , courseDiscussionDesc  TEXT , courseDiscussionClazzUid  BIGINT  NOT NULL , courseDiscussionActive  BOOL  NOT NULL , courseDiscussionLct  BIGINT  NOT NULL , courseDiscussionUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseDiscussionReplicate (  courseDiscussionPk  BIGINT  NOT NULL , courseDiscussionVersionId  BIGINT  NOT NULL  DEFAULT 0 , courseDiscussionDestination  BIGINT  NOT NULL , courseDiscussionPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (courseDiscussionPk, courseDiscussionDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseDiscussionReplicate_courseDiscussionPk_courseDiscussionDestination_courseDiscussionVersionId ON CourseDiscussionReplicate (courseDiscussionPk, courseDiscussionDestination, courseDiscussionVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseDiscussionReplicate_courseDiscussionDestination_courseDiscussionPending ON CourseDiscussionReplicate (courseDiscussionDestination, courseDiscussionPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseGroupMember (  cgmSetUid  BIGINT  NOT NULL , cgmGroupNumber  INTEGER  NOT NULL , cgmPersonUid  BIGINT  NOT NULL , cgmLct  BIGINT  NOT NULL , cgmUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseGroupMemberReplicate (  cgmPk  BIGINT  NOT NULL , cgmVersionId  BIGINT  NOT NULL  DEFAULT 0 , cgmDestination  BIGINT  NOT NULL , cgmPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (cgmPk, cgmDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseGroupMemberReplicate_cgmPk_cgmDestination_cgmVersionId ON CourseGroupMemberReplicate (cgmPk, cgmDestination, cgmVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseGroupMemberReplicate_cgmDestination_cgmPending ON CourseGroupMemberReplicate (cgmDestination, cgmPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseGroupSet (  cgsName  TEXT , cgsTotalGroups  INTEGER  NOT NULL , cgsActive  BOOL  NOT NULL , cgsClazzUid  BIGINT  NOT NULL , cgsLct  BIGINT  NOT NULL , cgsUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"
        stmtList += "CREATE INDEX index_CourseGroupSet_cgsClazzUid ON CourseGroupSet (cgsClazzUid)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseGroupSetReplicate (  cgsPk  BIGINT  NOT NULL , cgsVersionId  BIGINT  NOT NULL  DEFAULT 0 , cgsDestination  BIGINT  NOT NULL , cgsPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (cgsPk, cgsDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseGroupSetReplicate_cgsPk_cgsDestination_cgsVersionId ON CourseGroupSetReplicate (cgsPk, cgsDestination, cgsVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseGroupSetReplicate_cgsDestination_cgsPending ON CourseGroupSetReplicate (cgsDestination, cgsPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CoursePicture (  coursePictureClazzUid  BIGINT  NOT NULL , coursePictureMasterCsn  BIGINT  NOT NULL , coursePictureLocalCsn  BIGINT  NOT NULL , coursePictureLastChangedBy  INTEGER  NOT NULL , coursePictureLct  BIGINT  NOT NULL , coursePictureUri  TEXT , coursePictureMd5  TEXT , coursePictureFileSize  INTEGER  NOT NULL , coursePictureTimestamp  BIGINT  NOT NULL , coursePictureMimeType  TEXT , coursePictureActive  BOOL  NOT NULL , coursePictureUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS CoursePictureReplicate (  cpPk  BIGINT  NOT NULL , cpVersionId  BIGINT  NOT NULL  DEFAULT 0 , cpDestination  BIGINT  NOT NULL , cpPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (cpPk, cpDestination) )"
        stmtList +=
            "CREATE INDEX index_CoursePictureReplicate_cpPk_cpDestination_cpVersionId ON CoursePictureReplicate (cpPk, cpDestination, cpVersionId)"
        stmtList +=
            "CREATE INDEX index_CoursePictureReplicate_cpDestination_cpPending ON CoursePictureReplicate (cpDestination, cpPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseTerminology (  ctTitle  TEXT , ctTerminology  TEXT , ctLct  BIGINT  NOT NULL , ctUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS CourseTerminologyReplicate (  ctPk  BIGINT  NOT NULL , ctVersionId  BIGINT  NOT NULL  DEFAULT 0 , ctDestination  BIGINT  NOT NULL , ctPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (ctPk, ctDestination) )"
        stmtList +=
            "CREATE INDEX index_CourseTerminologyReplicate_ctPk_ctDestination_ctVersionId ON CourseTerminologyReplicate (ctPk, ctDestination, ctVersionId)"
        stmtList +=
            "CREATE INDEX index_CourseTerminologyReplicate_ctDestination_ctPending ON CourseTerminologyReplicate (ctDestination, ctPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS DiscussionPost (  discussionPostTitle  TEXT , discussionPostMessage  TEXT , discussionPostStartDate  BIGINT  NOT NULL , discussionPostDiscussionTopicUid  BIGINT  NOT NULL , discussionPostVisible  BOOL  NOT NULL , discussionPostArchive  BOOL  NOT NULL , discussionPostStartedPersonUid  BIGINT  NOT NULL , discussionPostClazzUid  BIGINT  NOT NULL , discussionPostLct  BIGINT  NOT NULL , discussionPostUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS DiscussionPostReplicate (  discussionPostPk  BIGINT  NOT NULL , discussionPostVersionId  BIGINT  NOT NULL  DEFAULT 0 , discussionPostDestination  BIGINT  NOT NULL , discussionPostPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (discussionPostPk, discussionPostDestination) )"
        stmtList +=
            "CREATE INDEX index_DiscussionPostReplicate_discussionPostPk_discussionPostDestination_discussionPostVersionId ON DiscussionPostReplicate (discussionPostPk, discussionPostDestination, discussionPostVersionId)"
        stmtList +=
            "CREATE INDEX index_DiscussionPostReplicate_discussionPostDestination_discussionPostPending ON DiscussionPostReplicate (discussionPostDestination, discussionPostPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS DiscussionTopic (  discussionTopicTitle  TEXT , discussionTopicDesc  TEXT , discussionTopicStartDate  BIGINT  NOT NULL , discussionTopicCourseDiscussionUid  BIGINT  NOT NULL , discussionTopicVisible  BOOL  NOT NULL , discussionTopicArchive  BOOL  NOT NULL , discussionTopicIndex  INTEGER  NOT NULL , discussionTopicClazzUid  BIGINT  NOT NULL , discussionTopicLct  BIGINT  NOT NULL , discussionTopicUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS DiscussionTopicReplicate (  discussionTopicPk  BIGINT  NOT NULL , discussionTopicVersionId  BIGINT  NOT NULL  DEFAULT 0 , discussionTopicDestination  BIGINT  NOT NULL , discussionTopicPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (discussionTopicPk, discussionTopicDestination) )"
        stmtList +=
            "CREATE INDEX index_DiscussionTopicReplicate_discussionTopicPk_discussionTopicDestination_discussionTopicVersionId ON DiscussionTopicReplicate (discussionTopicPk, discussionTopicDestination, discussionTopicVersionId)"
        stmtList +=
            "CREATE INDEX index_DiscussionTopicReplicate_discussionTopicDestination_discussionTopicPending ON DiscussionTopicReplicate (discussionTopicDestination, discussionTopicPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS Message (  messageSenderPersonUid  BIGINT  NOT NULL , messageTableId  INTEGER  NOT NULL , messageEntityUid  BIGINT  NOT NULL , messageText  TEXT , messageTimestamp  BIGINT  NOT NULL , messageClazzUid  BIGINT  NOT NULL , messageLct  BIGINT  NOT NULL , messageUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS MessageRead (  messageReadPersonUid  BIGINT  NOT NULL , messageReadMessageUid  BIGINT  NOT NULL , messageReadEntityUid  BIGINT  NOT NULL , messageReadLct  BIGINT  NOT NULL , messageReadUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS MessageReadReplicate (  messageReadPk  BIGINT  NOT NULL , messageReadVersionId  BIGINT  NOT NULL  DEFAULT 0 , messageReadDestination  BIGINT  NOT NULL , messageReadPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (messageReadPk, messageReadDestination) )"
        stmtList +=
            "CREATE INDEX index_MessageReadReplicate_messageReadPk_messageReadDestination_messageReadVersionId ON MessageReadReplicate (messageReadPk, messageReadDestination, messageReadVersionId)"
        stmtList +=
            "CREATE INDEX index_MessageReadReplicate_messageReadDestination_messageReadPending ON MessageReadReplicate (messageReadDestination, messageReadPending)"

        stmtList +=
            "CREATE TABLE IF NOT EXISTS MessageReplicate (  messagePk  BIGINT  NOT NULL , messageVersionId  BIGINT  NOT NULL  DEFAULT 0 , messageDestination  BIGINT  NOT NULL , messagePending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (messagePk, messageDestination) )"
        stmtList +=
            "CREATE INDEX index_MessageReplicate_messagePk_messageDestination_messageVersionId ON MessageReplicate (messagePk, messageDestination, messageVersionId)"
        stmtList +=
            "CREATE INDEX index_MessageReplicate_messageDestination_messagePending ON MessageReplicate (messageDestination, messagePending)"


        //Destructive migration
        stmtList += "DROP VIEW ClazzAssignment_ReceiveView"
        stmtList += "DROP TABLE ClazzAssignment"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS ClazzAssignment (  caTitle  TEXT , caDescription  TEXT , caGroupUid  BIGINT  NOT NULL  DEFAULT 0 , caActive  BOOL  NOT NULL , caClassCommentEnabled  BOOL  NOT NULL , caPrivateCommentsEnabled  BOOL  NOT NULL  DEFAULT true, caCompletionCriteria  INTEGER  NOT NULL  DEFAULT 100 , caRequireFileSubmission  BOOL  NOT NULL  DEFAULT true, caFileType  INTEGER  NOT NULL  DEFAULT 0 , caSizeLimit  INTEGER  NOT NULL  DEFAULT 50 , caNumberOfFiles  INTEGER  NOT NULL  DEFAULT 1 , caSubmissionPolicy  INTEGER  NOT NULL  DEFAULT 1 , caMarkingType  INTEGER  NOT NULL  DEFAULT 1 , caRequireTextSubmission  BOOL  NOT NULL  DEFAULT true, caTextLimitType  INTEGER  NOT NULL  DEFAULT 1 , caTextLimit  INTEGER  NOT NULL  DEFAULT 500 , caXObjectUid  BIGINT  NOT NULL  DEFAULT 0 , caClazzUid  BIGINT  NOT NULL , caLocalChangeSeqNum  BIGINT  NOT NULL , caMasterChangeSeqNum  BIGINT  NOT NULL , caLastChangedBy  INTEGER  NOT NULL , caLct  BIGINT  NOT NULL , caUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"
        stmtList += "CREATE INDEX index_ClazzAssignment_caClazzUid ON ClazzAssignment (caClazzUid)"

        stmtList += "DROP VIEW ClazzAssignmentContentJoin_ReceiveView"
        stmtList += "DROP TABLE ClazzAssignmentContentJoin"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoin (  cacjContentUid  BIGINT  NOT NULL , cacjAssignmentUid  BIGINT  NOT NULL , cacjActive  BOOL  NOT NULL , cacjWeight  INTEGER  NOT NULL  DEFAULT 0 , cacjMCSN  BIGINT  NOT NULL , cacjLCSN  BIGINT  NOT NULL , cacjLCB  INTEGER  NOT NULL , cacjLct  BIGINT  NOT NULL , cacjUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"


        stmtList += "DROP TABLE ClazzAssignmentRollUp"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS ClazzAssignmentRollUp (  cachePersonUid  BIGINT  NOT NULL , cacheContentEntryUid  BIGINT  NOT NULL , cacheClazzAssignmentUid  BIGINT  NOT NULL , cacheStudentScore  INTEGER  NOT NULL , cacheMaxScore  INTEGER  NOT NULL , cacheFinalWeightScoreWithPenalty  FLOAT  NOT NULL  DEFAULT 0 , cacheWeight  INTEGER  NOT NULL  DEFAULT 0 , cacheProgress  INTEGER  NOT NULL , cacheContentComplete  BOOL  NOT NULL , cacheSuccess  SMALLINT  NOT NULL , cachePenalty  INTEGER  NOT NULL , lastCsnChecked  BIGINT  NOT NULL , cacheUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"
        stmtList +=
            "CREATE UNIQUE INDEX index_ClazzAssignmentRollUp_cachePersonUid_cacheContentEntryUid_cacheClazzAssignmentUid ON ClazzAssignmentRollUp (cachePersonUid, cacheContentEntryUid, cacheClazzAssignmentUid)"

        stmtList += "DROP VIEW Comments_ReceiveView"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS Comments (  commentsText  TEXT , commentsEntityType  INTEGER  NOT NULL , commentsEntityUid  BIGINT  NOT NULL , commentsPublic  BOOL  NOT NULL , commentsStatus  INTEGER  NOT NULL , commentsPersonUid  BIGINT  NOT NULL , commentsToPersonUid  BIGINT  NOT NULL , commentSubmitterUid  BIGINT  NOT NULL , commentsFlagged  BOOL  NOT NULL , commentsInActive  BOOL  NOT NULL , commentsDateTimeAdded  BIGINT  NOT NULL , commentsDateTimeUpdated  BIGINT  NOT NULL , commentsMCSN  BIGINT  NOT NULL , commentsLCSN  BIGINT  NOT NULL , commentsLCB  INTEGER  NOT NULL , commentsLct  BIGINT  NOT NULL , commentsUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"


        //Default policy is open
        stmtList += "ALTER TABLE Clazz ADD COLUMN clazzEnrolmentPolicy INTEGER NOT NULL DEFAULT 102 "
        //Default use English terminology
        stmtList += "ALTER TABLE Clazz ADD COLUMN clazzTerminologyUid BIGINT NOT NULL DEFAULT ${('e'.code shl(8)) + 'n'.code}"

        stmtList += "ALTER TABLE XObjectEntity ADD COLUMN objectStatementRefUid INTEGER NOT NULL DEFAULT 0"


        //Triggers
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_127_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (127, NEW.chatUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_127_trig AFTER UPDATE OR INSERT ON Chat FOR EACH ROW EXECUTE PROCEDURE ch_upd_127_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_127_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (127, OLD.chatUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_127_trig AFTER DELETE ON Chat FOR EACH ROW EXECUTE PROCEDURE ch_del_127_fn(); "
        stmtList +=
            "CREATE VIEW Chat_ReceiveView AS  SELECT Chat.*, ChatReplicate.* FROM Chat LEFT JOIN ChatReplicate ON ChatReplicate.chatPk = Chat.chatUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION chat_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO Chat(chatUid, chatStartDate, chatTitle, chatGroup, chatLct) VALUES(NEW.chatUid, NEW.chatStartDate, NEW.chatTitle, NEW.chatGroup, NEW.chatLct) ON CONFLICT (chatUid) DO UPDATE SET chatStartDate = EXCLUDED.chatStartDate, chatTitle = EXCLUDED.chatTitle, chatGroup = EXCLUDED.chatGroup, chatLct = EXCLUDED.chatLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER chat_remote_insert_trig INSTEAD OF INSERT ON Chat_ReceiveView FOR EACH ROW EXECUTE PROCEDURE chat_remote_insert_fn() "

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_128_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (128, NEW.chatMemberUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_128_trig AFTER UPDATE OR INSERT ON ChatMember FOR EACH ROW EXECUTE PROCEDURE ch_upd_128_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_128_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (128, OLD.chatMemberUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_128_trig AFTER DELETE ON ChatMember FOR EACH ROW EXECUTE PROCEDURE ch_del_128_fn(); "
        stmtList +=
            "CREATE VIEW ChatMember_ReceiveView AS  SELECT ChatMember.*, ChatMemberReplicate.* FROM ChatMember LEFT JOIN ChatMemberReplicate ON ChatMemberReplicate.chatMemberPk = ChatMember.chatMemberUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION chatmember_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChatMember(chatMemberUid, chatMemberChatUid, chatMemberPersonUid, chatMemberJoinedDate, chatMemberLeftDate, chatMemberLct) VALUES(NEW.chatMemberUid, NEW.chatMemberChatUid, NEW.chatMemberPersonUid, NEW.chatMemberJoinedDate, NEW.chatMemberLeftDate, NEW.chatMemberLct) ON CONFLICT (chatMemberUid) DO UPDATE SET chatMemberChatUid = EXCLUDED.chatMemberChatUid, chatMemberPersonUid = EXCLUDED.chatMemberPersonUid, chatMemberJoinedDate = EXCLUDED.chatMemberJoinedDate, chatMemberLeftDate = EXCLUDED.chatMemberLeftDate, chatMemberLct = EXCLUDED.chatMemberLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER chatmember_remote_insert_trig INSTEAD OF INSERT ON ChatMember_ReceiveView FOR EACH ROW EXECUTE PROCEDURE chatmember_remote_insert_fn() "

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_523_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (523, NEW.camUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_523_trig AFTER UPDATE OR INSERT ON CourseAssignmentMark FOR EACH ROW EXECUTE PROCEDURE ch_upd_523_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_523_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (523, OLD.camUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_523_trig AFTER DELETE ON CourseAssignmentMark FOR EACH ROW EXECUTE PROCEDURE ch_del_523_fn(); "
        stmtList +=
            "CREATE VIEW CourseAssignmentMark_ReceiveView AS  SELECT CourseAssignmentMark.*, CourseAssignmentMarkReplicate.* FROM CourseAssignmentMark LEFT JOIN CourseAssignmentMarkReplicate ON CourseAssignmentMarkReplicate.camPk = CourseAssignmentMark.camUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION courseassignmentmark_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO CourseAssignmentMark(camUid, camAssignmentUid, camSubmitterUid, camMark, camPenalty, camLct) VALUES (NEW.camUid, NEW.camAssignmentUid, NEW.camSubmitterUid, NEW.camMark, NEW.camPenalty, NEW.camLct) ON CONFLICT (camUid) DO UPDATE SET camAssignmentUid = EXCLUDED.camAssignmentUid, camSubmitterUid = EXCLUDED.camSubmitterUid, camMark = EXCLUDED.camMark, camPenalty = EXCLUDED.camPenalty, camLct = EXCLUDED.camLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER courseassignmentmark_remote_insert_trig INSTEAD OF INSERT ON CourseAssignmentMark_ReceiveView FOR EACH ROW EXECUTE PROCEDURE courseassignmentmark_remote_insert_fn() "

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_522_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (522, NEW.casUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_522_trig AFTER UPDATE OR INSERT ON CourseAssignmentSubmission FOR EACH ROW EXECUTE PROCEDURE ch_upd_522_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_522_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (522, OLD.casUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_522_trig AFTER DELETE ON CourseAssignmentSubmission FOR EACH ROW EXECUTE PROCEDURE ch_del_522_fn(); "
        stmtList +=
            "CREATE VIEW CourseAssignmentSubmission_ReceiveView AS  SELECT CourseAssignmentSubmission.*, CourseAssignmentSubmissionReplicate.* FROM CourseAssignmentSubmission LEFT JOIN CourseAssignmentSubmissionReplicate ON CourseAssignmentSubmissionReplicate.casPk = CourseAssignmentSubmission.casUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION courseassignmentsubmission_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO CourseAssignmentSubmission(casUid, casAssignmentUid, casSubmitterUid, casSubmitterPersonUid, casText, casType, casTimestamp) VALUES (NEW.casUid, NEW.casAssignmentUid, NEW.casSubmitterUid, NEW.casSubmitterPersonUid, NEW.casText, NEW.casType, NEW.casTimestamp) ON CONFLICT (casUid) DO UPDATE SET casAssignmentUid = EXCLUDED.casAssignmentUid, casSubmitterUid = EXCLUDED.casSubmitterUid, casSubmitterPersonUid = EXCLUDED.casSubmitterPersonUid, casText = EXCLUDED.casText, casType = EXCLUDED.casType, casTimestamp = EXCLUDED.casTimestamp ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER courseassignmentsubmission_remote_insert_trig INSTEAD OF INSERT ON CourseAssignmentSubmission_ReceiveView FOR EACH ROW EXECUTE PROCEDURE courseassignmentsubmission_remote_insert_fn() "


        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_90_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (90, NEW.casaUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_90_trig AFTER UPDATE OR INSERT ON CourseAssignmentSubmissionAttachment FOR EACH ROW EXECUTE PROCEDURE ch_upd_90_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_90_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (90, OLD.casaUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_90_trig AFTER DELETE ON CourseAssignmentSubmissionAttachment FOR EACH ROW EXECUTE PROCEDURE ch_del_90_fn(); "
        stmtList +=
            "CREATE VIEW CourseAssignmentSubmissionAttachment_ReceiveView AS  SELECT CourseAssignmentSubmissionAttachment.*, CourseAssignmentSubmissionAttachmentReplicate.* FROM CourseAssignmentSubmissionAttachment LEFT JOIN CourseAssignmentSubmissionAttachmentReplicate ON CourseAssignmentSubmissionAttachmentReplicate.casaPk = CourseAssignmentSubmissionAttachment.casaUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION courseassignmentsubmissionattachment_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO CourseAssignmentSubmissionAttachment(casaUid, casaSubmissionUid, casaMimeType, casaUri, casaMd5, casaSize, casaTimestamp) VALUES (NEW.casaUid, NEW.casaSubmissionUid, NEW.casaMimeType, NEW.casaUri, NEW.casaMd5, NEW.casaSize, NEW.casaTimestamp) ON CONFLICT (casaUid) DO UPDATE SET casaSubmissionUid = EXCLUDED.casaSubmissionUid, casaMimeType = EXCLUDED.casaMimeType, casaUri = EXCLUDED.casaUri, casaMd5 = EXCLUDED.casaMd5, casaSize = EXCLUDED.casaSize, casaTimestamp = EXCLUDED.casaTimestamp ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER courseassignmentsubmissionattachment_remote_insert_trig INSTEAD OF INSERT ON CourseAssignmentSubmissionAttachment_ReceiveView FOR EACH ROW EXECUTE PROCEDURE courseassignmentsubmissionattachment_remote_insert_fn() "
        stmtList += """
        |    CREATE OR REPLACE FUNCTION attach_CourseAssignmentSubmissionAttachment_fn() RETURNS trigger AS ${'$'}${'$'}
        |    BEGIN
        |    
        |    INSERT INTO ZombieAttachmentData(zaUri) 
        |    SELECT OLD.casaUri AS zaUri
        |      FROM CourseAssignmentSubmissionAttachment   
        |     WHERE CourseAssignmentSubmissionAttachment.casaUid = OLD.casaUid
        |       AND (SELECT COUNT(*) 
        |              FROM CourseAssignmentSubmissionAttachment
        |             WHERE casaMd5 = OLD.casaMd5) = 0
        |;
        |    RETURN NEW;
        |    END ${'$'}${'$'}
        |    LANGUAGE plpgsql
        """.trimMargin()
        stmtList += """
        |CREATE TRIGGER attach_CourseAssignmentSubmissionAttachment_trig
        |AFTER UPDATE ON CourseAssignmentSubmissionAttachment
        |FOR EACH ROW WHEN (OLD.casaMd5 IS NOT NULL)
        |EXECUTE PROCEDURE attach_CourseAssignmentSubmissionAttachment_fn();
        """.trimMargin()

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_124_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (124, NEW.cbUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_124_trig AFTER UPDATE OR INSERT ON CourseBlock FOR EACH ROW EXECUTE PROCEDURE ch_upd_124_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_124_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (124, OLD.cbUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_124_trig AFTER DELETE ON CourseBlock FOR EACH ROW EXECUTE PROCEDURE ch_del_124_fn(); "
        stmtList +=
            "CREATE VIEW CourseBlock_ReceiveView AS  SELECT CourseBlock.*, CourseBlockReplicate.* FROM CourseBlock LEFT JOIN CourseBlockReplicate ON CourseBlockReplicate.cbPk = CourseBlock.cbUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION courseblock_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO CourseBlock(cbUid, cbType, cbIndentLevel, cbModuleParentBlockUid, cbTitle, cbDescription, cbCompletionCriteria, cbHideUntilDate, cbDeadlineDate, cbLateSubmissionPenalty, cbGracePeriodDate, cbMaxPoints,cbMinPoints, cbIndex, cbClazzUid, cbActive,cbHidden, cbEntityUid, cbLct) VALUES (NEW.cbUid, NEW.cbType, NEW.cbIndentLevel, NEW.cbModuleParentBlockUid, NEW.cbTitle, NEW.cbDescription, NEW.cbCompletionCriteria, NEW.cbHideUntilDate, NEW.cbDeadlineDate, NEW.cbLateSubmissionPenalty, NEW.cbGracePeriodDate, NEW.cbMaxPoints,NEW.cbMinPoints, NEW.cbIndex, NEW.cbClazzUid,NEW.cbActive, NEW.cbHidden, NEW.cbEntityUid, NEW.cbLct) ON CONFLICT (cbUid) DO UPDATE SET cbType = EXCLUDED.cbType, cbIndentLevel = EXCLUDED.cbIndentLevel, cbModuleParentBlockUid = EXCLUDED.cbModuleParentBlockUid, cbTitle = EXCLUDED.cbTitle, cbDescription = EXCLUDED.cbDescription, cbCompletionCriteria = EXCLUDED.cbCompletionCriteria, cbHideUntilDate = EXCLUDED.cbHideUntilDate,cbDeadlineDate = EXCLUDED.cbDeadlineDate, cbLateSubmissionPenalty = EXCLUDED.cbLateSubmissionPenalty, cbGracePeriodDate= EXCLUDED.cbGracePeriodDate, cbMaxPoints = EXCLUDED.cbMaxPoints, cbMinPoints = EXCLUDED.cbMinPoints, cbIndex = EXCLUDED.cbIndex,cbClazzUid = EXCLUDED.cbClazzUid, cbActive = EXCLUDED.cbActive, cbHidden = EXCLUDED.cbHidden, cbEntityUid = EXCLUDED.cbEntityUid, cbLct = EXCLUDED.cbLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER courseblock_remote_insert_trig INSTEAD OF INSERT ON CourseBlock_ReceiveView FOR EACH ROW EXECUTE PROCEDURE courseblock_remote_insert_fn() "

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_130_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (130, NEW.courseDiscussionUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_130_trig AFTER UPDATE OR INSERT ON CourseDiscussion FOR EACH ROW EXECUTE PROCEDURE ch_upd_130_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_130_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (130, OLD.courseDiscussionUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_130_trig AFTER DELETE ON CourseDiscussion FOR EACH ROW EXECUTE PROCEDURE ch_del_130_fn(); "
        stmtList +=
            "CREATE VIEW CourseDiscussion_ReceiveView AS  SELECT CourseDiscussion.*, CourseDiscussionReplicate.* FROM CourseDiscussion LEFT JOIN CourseDiscussionReplicate ON CourseDiscussionReplicate.courseDiscussionPk = CourseDiscussion.courseDiscussionUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION coursediscussion_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO CourseDiscussion(courseDiscussionUid, courseDiscussionActive, courseDiscussionTitle, courseDiscussionDesc, courseDiscussionClazzUid, courseDiscussionLct) VALUES(NEW.courseDiscussionUid, NEW.courseDiscussionActive, NEW.courseDiscussionTitle, NEW.courseDiscussionDesc, NEW.courseDiscussionClazzUid, NEW.courseDiscussionLct) ON CONFLICT (courseDiscussionUid) DO UPDATE SET courseDiscussionActive = EXCLUDED.courseDiscussionActive, courseDiscussionTitle = EXCLUDED.courseDiscussionTitle, courseDiscussionDesc = EXCLUDED.courseDiscussionDesc, courseDiscussionClazzUid = EXCLUDED.courseDiscussionClazzUid, courseDiscussionLct = EXCLUDED.courseDiscussionLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER coursediscussion_remote_insert_trig INSTEAD OF INSERT ON CourseDiscussion_ReceiveView FOR EACH ROW EXECUTE PROCEDURE coursediscussion_remote_insert_fn() "

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_243_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (243, NEW.cgmUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_243_trig AFTER UPDATE OR INSERT ON CourseGroupMember FOR EACH ROW EXECUTE PROCEDURE ch_upd_243_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_243_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (243, OLD.cgmUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_243_trig AFTER DELETE ON CourseGroupMember FOR EACH ROW EXECUTE PROCEDURE ch_del_243_fn(); "
        stmtList +=
            "CREATE VIEW CourseGroupMember_ReceiveView AS  SELECT CourseGroupMember.*, CourseGroupMemberReplicate.* FROM CourseGroupMember LEFT JOIN CourseGroupMemberReplicate ON CourseGroupMemberReplicate.cgmPk = CourseGroupMember.cgmUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION coursegroupmember_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO CourseGroupMember(cgmUid, cgmSetUid, cgmGroupNumber, cgmPersonUid, cgmLct) VALUES (NEW.cgmUid, NEW.cgmSetUid, NEW.cgmGroupNumber, NEW.cgmPersonUid, NEW.cgmLct) ON CONFLICT (cgmUid) DO UPDATE SET cgmSetUid = EXCLUDED.cgmSetUid, cgmGroupNumber = EXCLUDED.cgmGroupNumber, cgmPersonUid = EXCLUDED.cgmPersonUid, cgmLct = EXCLUDED.cgmLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER coursegroupmember_remote_insert_trig INSTEAD OF INSERT ON CourseGroupMember_ReceiveView FOR EACH ROW EXECUTE PROCEDURE coursegroupmember_remote_insert_fn() "

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_242_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (242, NEW.cgsUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_242_trig AFTER UPDATE OR INSERT ON CourseGroupSet FOR EACH ROW EXECUTE PROCEDURE ch_upd_242_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_242_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (242, OLD.cgsUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_242_trig AFTER DELETE ON CourseGroupSet FOR EACH ROW EXECUTE PROCEDURE ch_del_242_fn(); "
        stmtList +=
            "CREATE VIEW CourseGroupSet_ReceiveView AS  SELECT CourseGroupSet.*, CourseGroupSetReplicate.* FROM CourseGroupSet LEFT JOIN CourseGroupSetReplicate ON CourseGroupSetReplicate.cgsPk = CourseGroupSet.cgsUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION coursegroupset_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO CourseGroupSet(cgsUid, cgsName, cgsTotalGroups, cgsActive, cgsClazzUid, cgsLct) VALUES (NEW.cgsUid, NEW.cgsName, NEW.cgsTotalGroups, NEW.cgsActive, NEW.cgsClazzUid, NEW.cgsLct) ON CONFLICT (cgsUid) DO UPDATE SET cgsName = EXCLUDED.cgsName, cgsTotalGroups = EXCLUDED.cgsTotalGroups, cgsActive = EXCLUDED.cgsActive, cgsClazzUid = EXCLUDED.cgsClazzUid, cgsLct = EXCLUDED.cgsLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER coursegroupset_remote_insert_trig INSTEAD OF INSERT ON CourseGroupSet_ReceiveView FOR EACH ROW EXECUTE PROCEDURE coursegroupset_remote_insert_fn() "

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_125_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (125, NEW.coursePictureUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_125_trig AFTER UPDATE OR INSERT ON CoursePicture FOR EACH ROW EXECUTE PROCEDURE ch_upd_125_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_125_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (125, OLD.coursePictureUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_125_trig AFTER DELETE ON CoursePicture FOR EACH ROW EXECUTE PROCEDURE ch_del_125_fn(); "
        stmtList +=
            "CREATE VIEW CoursePicture_ReceiveView AS  SELECT CoursePicture.*, CoursePictureReplicate.* FROM CoursePicture LEFT JOIN CoursePictureReplicate ON CoursePictureReplicate.cpPk = CoursePicture.coursePictureUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION coursepicture_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO CoursePicture(coursePictureUid, coursePictureClazzUid, coursePictureMasterCsn, coursePictureLocalCsn, coursePictureLastChangedBy, coursePictureLct, coursePictureUri, coursePictureMd5, coursePictureFileSize, coursePictureTimestamp, coursePictureMimeType, coursePictureActive) VALUES (NEW.coursePictureUid, NEW.coursePictureClazzUid, NEW.coursePictureMasterCsn, NEW.coursePictureLocalCsn, NEW.coursePictureLastChangedBy, NEW.coursePictureLct, NEW.coursePictureUri, NEW.coursePictureMd5, NEW.coursePictureFileSize, NEW.coursePictureTimestamp, NEW.coursePictureMimeType, NEW.coursePictureActive) ON CONFLICT (coursePictureUid) DO UPDATE SET coursePictureClazzUid = EXCLUDED.coursePictureClazzUid, coursePictureMasterCsn = EXCLUDED.coursePictureMasterCsn, coursePictureLocalCsn = EXCLUDED.coursePictureLocalCsn, coursePictureLastChangedBy = EXCLUDED.coursePictureLastChangedBy, coursePictureLct = EXCLUDED.coursePictureLct, coursePictureUri = EXCLUDED.coursePictureUri, coursePictureMd5 = EXCLUDED.coursePictureMd5, coursePictureFileSize = EXCLUDED.coursePictureFileSize, coursePictureTimestamp = EXCLUDED.coursePictureTimestamp, coursePictureMimeType = EXCLUDED.coursePictureMimeType, coursePictureActive = EXCLUDED.coursePictureActive ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER coursepicture_remote_insert_trig INSTEAD OF INSERT ON CoursePicture_ReceiveView FOR EACH ROW EXECUTE PROCEDURE coursepicture_remote_insert_fn() "
        stmtList += """
        |    CREATE OR REPLACE FUNCTION attach_CoursePicture_fn() RETURNS trigger AS ${'$'}${'$'}
        |    BEGIN
        |    
        |    INSERT INTO ZombieAttachmentData(zaUri) 
        |    SELECT OLD.coursePictureUri AS zaUri
        |      FROM CoursePicture   
        |     WHERE CoursePicture.coursePictureUid = OLD.coursePictureUid
        |       AND (SELECT COUNT(*) 
        |              FROM CoursePicture
        |             WHERE coursePictureMd5 = OLD.coursePictureMd5) = 0
        |;
        |    RETURN NEW;
        |    END ${'$'}${'$'}
        |    LANGUAGE plpgsql
        """.trimMargin()
        stmtList += """
        |CREATE TRIGGER attach_CoursePicture_trig
        |AFTER UPDATE ON CoursePicture
        |FOR EACH ROW WHEN (OLD.coursePictureMd5 IS NOT NULL)
        |EXECUTE PROCEDURE attach_CoursePicture_fn();
        """.trimMargin()

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_450_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (450, NEW.ctUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_450_trig AFTER UPDATE OR INSERT ON CourseTerminology FOR EACH ROW EXECUTE PROCEDURE ch_upd_450_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_450_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (450, OLD.ctUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_450_trig AFTER DELETE ON CourseTerminology FOR EACH ROW EXECUTE PROCEDURE ch_del_450_fn(); "
        stmtList +=
            "CREATE VIEW CourseTerminology_ReceiveView AS  SELECT CourseTerminology.*, CourseTerminologyReplicate.* FROM CourseTerminology LEFT JOIN CourseTerminologyReplicate ON CourseTerminologyReplicate.ctPk = CourseTerminology.ctUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION courseterminology_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO CourseTerminology(ctUid, ctTitle, ctTerminology, ctLct) VALUES (NEW.ctUid, NEW.ctTitle, NEW.ctTerminology, NEW.ctLct) ON CONFLICT (ctUid) DO UPDATE SET ctTitle = EXCLUDED.ctTitle, ctTerminology = EXCLUDED.ctTerminology, ctLct = EXCLUDED.ctLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER courseterminology_remote_insert_trig INSTEAD OF INSERT ON CourseTerminology_ReceiveView FOR EACH ROW EXECUTE PROCEDURE courseterminology_remote_insert_fn() "



        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_132_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (132, NEW.discussionPostUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_132_trig AFTER UPDATE OR INSERT ON DiscussionPost FOR EACH ROW EXECUTE PROCEDURE ch_upd_132_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_132_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (132, OLD.discussionPostUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_132_trig AFTER DELETE ON DiscussionPost FOR EACH ROW EXECUTE PROCEDURE ch_del_132_fn(); "
        stmtList +=
            "CREATE VIEW DiscussionPost_ReceiveView AS  SELECT DiscussionPost.*, DiscussionPostReplicate.* FROM DiscussionPost LEFT JOIN DiscussionPostReplicate ON DiscussionPostReplicate.discussionPostPk = DiscussionPost.discussionPostUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION discussionpost_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO DiscussionPost(discussionPostUid, discussionPostTitle, discussionPostMessage, discussionPostStartDate, discussionPostDiscussionTopicUid, discussionPostVisible, discussionPostArchive, discussionPostStartedPersonUid, discussionPostClazzUid, discussionPostLct) VALUES(NEW.discussionPostUid, NEW.discussionPostTitle, NEW.discussionPostMessage, NEW.discussionPostStartDate, NEW.discussionPostDiscussionTopicUid, NEW.discussionPostVisible, NEW.discussionPostArchive, NEW.discussionPostStartedPersonUid, NEW.discussionPostClazzUid, NEW.discussionPostLct) ON CONFLICT (discussionPostUid) DO UPDATE SET discussionPostTitle = EXCLUDED.discussionPostTitle , discussionPostMessage = EXCLUDED.discussionPostMessage , discussionPostStartDate = EXCLUDED.discussionPostStartDate , discussionPostDiscussionTopicUid = EXCLUDED.discussionPostDiscussionTopicUid, discussionPostVisible = EXCLUDED.discussionPostVisible , discussionPostArchive = EXCLUDED.discussionPostArchive , discussionPostStartedPersonUid = EXCLUDED.discussionPostStartedPersonUid , discussionPostClazzUid = EXCLUDED.discussionPostClazzUid, discussionPostLct = EXCLUDED.discussionPostLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER discussionpost_remote_insert_trig INSTEAD OF INSERT ON DiscussionPost_ReceiveView FOR EACH ROW EXECUTE PROCEDURE discussionpost_remote_insert_fn() "

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_131_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (131, NEW.discussionTopicUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_131_trig AFTER UPDATE OR INSERT ON DiscussionTopic FOR EACH ROW EXECUTE PROCEDURE ch_upd_131_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_131_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (131, OLD.discussionTopicUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_131_trig AFTER DELETE ON DiscussionTopic FOR EACH ROW EXECUTE PROCEDURE ch_del_131_fn(); "
        stmtList +=
            "CREATE VIEW DiscussionTopic_ReceiveView AS  SELECT DiscussionTopic.*, DiscussionTopicReplicate.* FROM DiscussionTopic LEFT JOIN DiscussionTopicReplicate ON DiscussionTopicReplicate.discussionTopicPk = DiscussionTopic.discussionTopicUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION discussiontopic_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO DiscussionTopic(discussionTopicUid, discussionTopicTitle, discussionTopicDesc, discussionTopicStartDate, discussionTopicCourseDiscussionUid, discussionTopicVisible, discussionTopicArchive, discussionTopicIndex, discussionTopicClazzUid, discussionTopicLct) VALUES(NEW.discussionTopicUid, NEW.discussionTopicTitle, NEW.discussionTopicDesc, NEW.discussionTopicStartDate, NEW.discussionTopicCourseDiscussionUid, NEW.discussionTopicVisible, NEW.discussionTopicArchive, NEW.discussionTopicIndex, NEW.discussionTopicClazzUid, NEW.discussionTopicLct) ON CONFLICT (discussionTopicUid) DO UPDATE SET discussionTopicTitle = EXCLUDED.discussionTopicTitle, discussionTopicDesc = EXCLUDED.discussionTopicDesc, discussionTopicStartDate = EXCLUDED.discussionTopicStartDate, discussionTopicCourseDiscussionUid = EXCLUDED.discussionTopicCourseDiscussionUid, discussionTopicVisible = EXCLUDED.discussionTopicVisible, discussionTopicArchive = EXCLUDED.discussionTopicArchive, discussionTopicIndex = EXCLUDED.discussionTopicIndex, discussionTopicClazzUid = EXCLUDED.discussionTopicClazzUid, discussionTopicLct = EXCLUDED.discussionTopicLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER discussiontopic_remote_insert_trig INSTEAD OF INSERT ON DiscussionTopic_ReceiveView FOR EACH ROW EXECUTE PROCEDURE discussiontopic_remote_insert_fn() "

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_126_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (126, NEW.messageUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_126_trig AFTER UPDATE OR INSERT ON Message FOR EACH ROW EXECUTE PROCEDURE ch_upd_126_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_126_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (126, OLD.messageUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_126_trig AFTER DELETE ON Message FOR EACH ROW EXECUTE PROCEDURE ch_del_126_fn(); "
        stmtList +=
            "CREATE VIEW Message_ReceiveView AS  SELECT Message.*, MessageReplicate.* FROM Message LEFT JOIN MessageReplicate ON MessageReplicate.messagePk = Message.messageUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION message_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO Message(messageUid, messageSenderPersonUid, messageTableId, messageEntityUid, messageText, messageTimestamp, messageClazzUid, messageLct) VALUES(NEW.messageUid, NEW.messageSenderPersonUid, NEW.messageTableId, NEW.messageEntityUid, NEW.messageText, NEW.messageTimestamp, NEW.messageClazzUid, NEW.messageLct) ON CONFLICT (messageUid) DO UPDATE SET messageSenderPersonUid = EXCLUDED.messageSenderPersonUid, messageTableId = EXCLUDED.messageTableId, messageEntityUid = EXCLUDED.messageEntityUid, messageText = EXCLUDED.messageText, messageTimestamp = EXCLUDED.messageTimestamp, messageClazzUid = EXCLUDED.messageClazzUid, messageLct = EXCLUDED.messageLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER message_remote_insert_trig INSTEAD OF INSERT ON Message_ReceiveView FOR EACH ROW EXECUTE PROCEDURE message_remote_insert_fn() "


        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_129_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (129, NEW.messageReadUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_129_trig AFTER UPDATE OR INSERT ON MessageRead FOR EACH ROW EXECUTE PROCEDURE ch_upd_129_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_129_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (129, OLD.messageReadUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_129_trig AFTER DELETE ON MessageRead FOR EACH ROW EXECUTE PROCEDURE ch_del_129_fn(); "
        stmtList +=
            "CREATE VIEW MessageRead_ReceiveView AS  SELECT MessageRead.*, MessageReadReplicate.* FROM MessageRead LEFT JOIN MessageReadReplicate ON MessageReadReplicate.messageReadPk = MessageRead.messageReadUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION messageread_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO MessageRead(messageReadUid, messageReadPersonUid, messageReadMessageUid, messageReadEntityUid, messageReadLct) VALUES(NEW.messageReadUid, NEW.messageReadPersonUid, NEW.messageReadMessageUid, NEW.messageReadEntityUid, NEW.messageReadLct) ON CONFLICT (messageReadUid) DO UPDATE SET messageReadPersonUid = EXCLUDED.messageReadPersonUid, messageReadMessageUid = EXCLUDED.messageReadMessageUid, messageReadEntityUid = EXCLUDED.messageReadEntityUid, messageReadLct = EXCLUDED.messageReadLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER messageread_remote_insert_trig INSTEAD OF INSERT ON MessageRead_ReceiveView FOR EACH ROW EXECUTE PROCEDURE messageread_remote_insert_fn() "

        //Destructive migration triggers
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_520_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (520, NEW.caUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_520_trig AFTER UPDATE OR INSERT ON ClazzAssignment FOR EACH ROW EXECUTE PROCEDURE ch_upd_520_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_520_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (520, OLD.caUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_520_trig AFTER DELETE ON ClazzAssignment FOR EACH ROW EXECUTE PROCEDURE ch_del_520_fn(); "
        stmtList +=
            "CREATE VIEW ClazzAssignment_ReceiveView AS  SELECT ClazzAssignment.*, ClazzAssignmentReplicate.* FROM ClazzAssignment LEFT JOIN ClazzAssignmentReplicate ON ClazzAssignmentReplicate.caPk = ClazzAssignment.caUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION clazzassignment_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ClazzAssignment(caUid, caTitle, caDescription, caGroupUid, caActive, caClassCommentEnabled, caPrivateCommentsEnabled, caRequireFileSubmission, caFileType, caSizeLimit, caNumberOfFiles, caSubmissionPolicy, caMarkingType, caRequireTextSubmission, caTextLimitType, caTextLimit, caXObjectUid, caClazzUid, caLocalChangeSeqNum, caMasterChangeSeqNum, caLastChangedBy, caLct) VALUES (NEW.caUid, NEW.caTitle, NEW.caDescription, NEW.caGroupUid, NEW.caActive, NEW.caClassCommentEnabled, NEW.caPrivateCommentsEnabled, NEW.caRequireFileSubmission, NEW.caFileType, NEW.caSizeLimit, NEW.caNumberOfFiles, NEW.caSubmissionPolicy, NEW.caMarkingType,NEW.caRequireTextSubmission, NEW.caTextLimitType, NEW.caTextLimit, NEW.caXObjectUid, NEW.caClazzUid, NEW.caLocalChangeSeqNum, NEW.caMasterChangeSeqNum, NEW.caLastChangedBy, NEW.caLct) ON CONFLICT (caUid) DO UPDATE SET caTitle = EXCLUDED.caTitle, caDescription = EXCLUDED.caDescription, caGroupUid = EXCLUDED.caGroupUid, caActive = EXCLUDED.caActive, caClassCommentEnabled = EXCLUDED.caClassCommentEnabled, caPrivateCommentsEnabled = EXCLUDED.caPrivateCommentsEnabled, caRequireFileSubmission = EXCLUDED.caRequireFileSubmission, caFileType = EXCLUDED.caFileType, caSizeLimit = EXCLUDED.caSizeLimit, caNumberOfFiles = EXCLUDED.caNumberOfFiles, caSubmissionPolicy = EXCLUDED.caSubmissionPolicy, caMarkingType = EXCLUDED.caMarkingType, caRequireTextSubmission = EXCLUDED.caRequireTextSubmission, caTextLimitType = EXCLUDED.caTextLimitType, caTextLimit = EXCLUDED.caTextLimit, caXObjectUid = EXCLUDED.caXObjectUid, caClazzUid = EXCLUDED.caClazzUid, caLocalChangeSeqNum = EXCLUDED.caLocalChangeSeqNum, caMasterChangeSeqNum = EXCLUDED.caMasterChangeSeqNum, caLastChangedBy = EXCLUDED.caLastChangedBy, caLct = EXCLUDED.caLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER clazzassignment_remote_insert_trig INSTEAD OF INSERT ON ClazzAssignment_ReceiveView FOR EACH ROW EXECUTE PROCEDURE clazzassignment_remote_insert_fn() "


        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_521_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (521, NEW.cacjUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_521_trig AFTER UPDATE OR INSERT ON ClazzAssignmentContentJoin FOR EACH ROW EXECUTE PROCEDURE ch_upd_521_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_521_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (521, OLD.cacjUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_521_trig AFTER DELETE ON ClazzAssignmentContentJoin FOR EACH ROW EXECUTE PROCEDURE ch_del_521_fn(); "
        stmtList +=
            "CREATE VIEW ClazzAssignmentContentJoin_ReceiveView AS  SELECT ClazzAssignmentContentJoin.*, ClazzAssignmentContentJoinReplicate.* FROM ClazzAssignmentContentJoin LEFT JOIN ClazzAssignmentContentJoinReplicate ON ClazzAssignmentContentJoinReplicate.cacjPk = ClazzAssignmentContentJoin.cacjUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION clazzassignmentcontentjoin_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ClazzAssignmentContentJoin(cacjUid, cacjContentUid, cacjAssignmentUid, cacjActive,cacjWeight, cacjMCSN, cacjLCSN, cacjLCB, cacjLct) VALUES (NEW.cacjUid, NEW.cacjContentUid, NEW.cacjAssignmentUid, NEW.cacjActive, NEW.cacjWeight, NEW.cacjMCSN, NEW.cacjLCSN, NEW.cacjLCB, NEW.cacjLct) ON CONFLICT (cacjUid) DO UPDATE SET cacjContentUid = EXCLUDED.cacjContentUid, cacjAssignmentUid = EXCLUDED.cacjAssignmentUid, cacjActive = EXCLUDED.cacjActive, cacjWeight = EXCLUDED.cacjWeight, cacjMCSN = EXCLUDED.cacjMCSN, cacjLCSN = EXCLUDED.cacjLCSN, cacjLCB = EXCLUDED.cacjLCB, cacjLct = EXCLUDED.cacjLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER clazzassignmentcontentjoin_remote_insert_trig INSTEAD OF INSERT ON ClazzAssignmentContentJoin_ReceiveView FOR EACH ROW EXECUTE PROCEDURE clazzassignmentcontentjoin_remote_insert_fn() "

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_208_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (208, NEW.commentsUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList += "DROP TRIGGER IF EXISTS ch_upd_208_trig ON Comments"
        stmtList +=
            " CREATE TRIGGER ch_upd_208_trig AFTER UPDATE OR INSERT ON Comments FOR EACH ROW EXECUTE PROCEDURE ch_upd_208_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_208_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (208, OLD.commentsUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList += "DROP TRIGGER IF EXISTS ch_del_208_trig ON Comments"
        stmtList +=
            " CREATE TRIGGER ch_del_208_trig AFTER DELETE ON Comments FOR EACH ROW EXECUTE PROCEDURE ch_del_208_fn(); "
        stmtList +=
            "CREATE VIEW Comments_ReceiveView AS  SELECT Comments.*, CommentsReplicate.* FROM Comments LEFT JOIN CommentsReplicate ON CommentsReplicate.commentsPk = Comments.commentsUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION comments_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO Comments(commentsUid, commentsText, commentsEntityType, commentsEntityUid, commentsPublic, commentsStatus, commentsPersonUid, commentsToPersonUid, commentSubmitterUid, commentsFlagged, commentsInActive, commentsDateTimeAdded, commentsDateTimeUpdated, commentsMCSN, commentsLCSN, commentsLCB, commentsLct) VALUES (NEW.commentsUid, NEW.commentsText, NEW.commentsEntityType, NEW.commentsEntityUid, NEW.commentsPublic, NEW.commentsStatus, NEW.commentsPersonUid, NEW.commentsToPersonUid, NEW.commentSubmitterUid, NEW.commentsFlagged, NEW.commentsInActive, NEW.commentsDateTimeAdded, NEW.commentsDateTimeUpdated, NEW.commentsMCSN, NEW.commentsLCSN, NEW.commentsLCB, NEW.commentsLct) ON CONFLICT (commentsUid) DO UPDATE SET commentsText = EXCLUDED.commentsText, commentsEntityType = EXCLUDED.commentsEntityType, commentsEntityUid = EXCLUDED.commentsEntityUid, commentsPublic = EXCLUDED.commentsPublic, commentsStatus = EXCLUDED.commentsStatus, commentsPersonUid = EXCLUDED.commentsPersonUid, commentsToPersonUid = EXCLUDED.commentsToPersonUid, commentSubmitterUid = EXCLUDED.commentSubmitterUid, commentsFlagged = EXCLUDED.commentsFlagged, commentsInActive = EXCLUDED.commentsInActive, commentsDateTimeAdded = EXCLUDED.commentsDateTimeAdded, commentsDateTimeUpdated = EXCLUDED.commentsDateTimeUpdated, commentsMCSN = EXCLUDED.commentsMCSN, commentsLCSN = EXCLUDED.commentsLCSN, commentsLCB = EXCLUDED.commentsLCB, commentsLct = EXCLUDED.commentsLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList += "DROP TRIGGER IF EXISTS comments_remote_insert_trig ON Comments_ReceiveView"
        stmtList +=
            " CREATE TRIGGER comments_remote_insert_trig INSTEAD OF INSERT ON Comments_ReceiveView FOR EACH ROW EXECUTE PROCEDURE comments_remote_insert_fn() "
    }

    stmtList
}


/**
 * Fix the ReceiveView for tables where an alter statement was used
 */
val MIGRATION_103_104 = DoorMigrationStatementList(103, 104) { db ->
    val stmtList = mutableListOf<String>()
    if(db.dbType() == DoorDbType.SQLITE) {
        stmtList += "DROP VIEW IF EXISTS Clazz_ReceiveView"
        stmtList += "DROP TRIGGER IF EXISTS clazz_remote_insert_ins"
        stmtList +=
            "CREATE VIEW Clazz_ReceiveView AS  SELECT Clazz.*, ClazzReplicate.* FROM Clazz LEFT JOIN ClazzReplicate ON ClazzReplicate.clazzPk = Clazz.clazzUid "
        stmtList +=
            " CREATE TRIGGER clazz_remote_insert_ins INSTEAD OF INSERT ON Clazz_ReceiveView FOR EACH ROW BEGIN REPLACE INTO Clazz(clazzUid, clazzName, clazzDesc, attendanceAverage, clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid, isClazzActive, clazzLocationUid, clazzStartTime, clazzEndTime, clazzFeatures, clazzSchoolUid, clazzEnrolmentPolicy, clazzTerminologyUid, clazzMasterChangeSeqNum, clazzLocalChangeSeqNum, clazzLastChangedBy, clazzLct, clazzTimeZone, clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid, clazzCode) VALUES (NEW.clazzUid, NEW.clazzName, NEW.clazzDesc, NEW.attendanceAverage, NEW.clazzHolidayUMCalendarUid, NEW.clazzScheuleUMCalendarUid, NEW.isClazzActive, NEW.clazzLocationUid, NEW.clazzStartTime, NEW.clazzEndTime, NEW.clazzFeatures, NEW.clazzSchoolUid, NEW.clazzEnrolmentPolicy, NEW.clazzTerminologyUid, NEW.clazzMasterChangeSeqNum, NEW.clazzLocalChangeSeqNum, NEW.clazzLastChangedBy, NEW.clazzLct, NEW.clazzTimeZone, NEW.clazzStudentsPersonGroupUid, NEW.clazzTeachersPersonGroupUid, NEW.clazzPendingStudentsPersonGroupUid, NEW.clazzParentsPersonGroupUid, NEW.clazzCode) /*psql ON CONFLICT (clazzUid) DO UPDATE SET clazzName = EXCLUDED.clazzName, clazzDesc = EXCLUDED.clazzDesc, attendanceAverage = EXCLUDED.attendanceAverage, clazzHolidayUMCalendarUid = EXCLUDED.clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid = EXCLUDED.clazzScheuleUMCalendarUid, isClazzActive = EXCLUDED.isClazzActive, clazzLocationUid = EXCLUDED.clazzLocationUid, clazzStartTime = EXCLUDED.clazzStartTime, clazzEndTime = EXCLUDED.clazzEndTime, clazzFeatures = EXCLUDED.clazzFeatures, clazzSchoolUid = EXCLUDED.clazzSchoolUid, clazzEnrolmentPolicy = EXCLUDED.clazzEnrolmentPolicy, clazzTerminologyUid = EXCLUDED.clazzTerminologyUid, clazzMasterChangeSeqNum = EXCLUDED.clazzMasterChangeSeqNum, clazzLocalChangeSeqNum = EXCLUDED.clazzLocalChangeSeqNum, clazzLastChangedBy = EXCLUDED.clazzLastChangedBy, clazzLct = EXCLUDED.clazzLct, clazzTimeZone = EXCLUDED.clazzTimeZone, clazzStudentsPersonGroupUid = EXCLUDED.clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid = EXCLUDED.clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid = EXCLUDED.clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid = EXCLUDED.clazzParentsPersonGroupUid, clazzCode = EXCLUDED.clazzCode */; END "

        stmtList += "DROP VIEW IF EXISTS XObjectEntity_ReceiveView"
        stmtList += "DROP TRIGGER IF EXISTS xobjectentity_remote_insert_ins"
        stmtList +=
            "CREATE VIEW XObjectEntity_ReceiveView AS  SELECT XObjectEntity.*, XObjectEntityReplicate.* FROM XObjectEntity LEFT JOIN XObjectEntityReplicate ON XObjectEntityReplicate.xoePk = XObjectEntity.xObjectUid "
        stmtList +=
            " CREATE TRIGGER xobjectentity_remote_insert_ins INSTEAD OF INSERT ON XObjectEntity_ReceiveView FOR EACH ROW BEGIN REPLACE INTO XObjectEntity(xObjectUid, objectType, objectId, definitionType, interactionType, correctResponsePattern, objectContentEntryUid, objectStatementRefUid, xObjectMasterChangeSeqNum, xObjectocalChangeSeqNum, xObjectLastChangedBy, xObjectLct) VALUES (NEW.xObjectUid, NEW.objectType, NEW.objectId, NEW.definitionType, NEW.interactionType, NEW.correctResponsePattern, NEW.objectContentEntryUid, NEW.objectStatementRefUid, NEW.xObjectMasterChangeSeqNum, NEW.xObjectocalChangeSeqNum, NEW.xObjectLastChangedBy, NEW.xObjectLct) /*psql ON CONFLICT (xObjectUid) DO UPDATE SET objectType = EXCLUDED.objectType, objectId = EXCLUDED.objectId, definitionType = EXCLUDED.definitionType, interactionType = EXCLUDED.interactionType, correctResponsePattern = EXCLUDED.correctResponsePattern, objectContentEntryUid = EXCLUDED.objectContentEntryUid,objectStatementRefUid = EXCLUDED.objectStatementRefUid, xObjectMasterChangeSeqNum = EXCLUDED.xObjectMasterChangeSeqNum, xObjectocalChangeSeqNum = EXCLUDED.xObjectocalChangeSeqNum, xObjectLastChangedBy = EXCLUDED.xObjectLastChangedBy, xObjectLct = EXCLUDED.xObjectLct */; END "
    }else {
        stmtList += "DROP TRIGGER IF EXISTS clazz_remote_insert_trig ON Clazz_ReceiveView"
        stmtList += "DROP VIEW IF EXISTS Clazz_ReceiveView"
        stmtList +=
            "CREATE VIEW Clazz_ReceiveView AS  SELECT Clazz.*, ClazzReplicate.* FROM Clazz LEFT JOIN ClazzReplicate ON ClazzReplicate.clazzPk = Clazz.clazzUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION clazz_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO Clazz(clazzUid, clazzName, clazzDesc, attendanceAverage, clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid, isClazzActive, clazzLocationUid, clazzStartTime, clazzEndTime, clazzFeatures, clazzSchoolUid, clazzEnrolmentPolicy, clazzTerminologyUid, clazzMasterChangeSeqNum, clazzLocalChangeSeqNum, clazzLastChangedBy, clazzLct, clazzTimeZone, clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid, clazzCode) VALUES (NEW.clazzUid, NEW.clazzName, NEW.clazzDesc, NEW.attendanceAverage, NEW.clazzHolidayUMCalendarUid, NEW.clazzScheuleUMCalendarUid, NEW.isClazzActive, NEW.clazzLocationUid, NEW.clazzStartTime, NEW.clazzEndTime, NEW.clazzFeatures, NEW.clazzSchoolUid, NEW.clazzEnrolmentPolicy, NEW.clazzTerminologyUid, NEW.clazzMasterChangeSeqNum, NEW.clazzLocalChangeSeqNum, NEW.clazzLastChangedBy, NEW.clazzLct, NEW.clazzTimeZone, NEW.clazzStudentsPersonGroupUid, NEW.clazzTeachersPersonGroupUid, NEW.clazzPendingStudentsPersonGroupUid, NEW.clazzParentsPersonGroupUid, NEW.clazzCode) ON CONFLICT (clazzUid) DO UPDATE SET clazzName = EXCLUDED.clazzName, clazzDesc = EXCLUDED.clazzDesc, attendanceAverage = EXCLUDED.attendanceAverage, clazzHolidayUMCalendarUid = EXCLUDED.clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid = EXCLUDED.clazzScheuleUMCalendarUid, isClazzActive = EXCLUDED.isClazzActive, clazzLocationUid = EXCLUDED.clazzLocationUid, clazzStartTime = EXCLUDED.clazzStartTime, clazzEndTime = EXCLUDED.clazzEndTime, clazzFeatures = EXCLUDED.clazzFeatures, clazzSchoolUid = EXCLUDED.clazzSchoolUid, clazzEnrolmentPolicy = EXCLUDED.clazzEnrolmentPolicy, clazzTerminologyUid = EXCLUDED.clazzTerminologyUid, clazzMasterChangeSeqNum = EXCLUDED.clazzMasterChangeSeqNum, clazzLocalChangeSeqNum = EXCLUDED.clazzLocalChangeSeqNum, clazzLastChangedBy = EXCLUDED.clazzLastChangedBy, clazzLct = EXCLUDED.clazzLct, clazzTimeZone = EXCLUDED.clazzTimeZone, clazzStudentsPersonGroupUid = EXCLUDED.clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid = EXCLUDED.clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid = EXCLUDED.clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid = EXCLUDED.clazzParentsPersonGroupUid, clazzCode = EXCLUDED.clazzCode ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER clazz_remote_insert_trig INSTEAD OF INSERT ON Clazz_ReceiveView FOR EACH ROW EXECUTE PROCEDURE clazz_remote_insert_fn() "

        stmtList += "DROP TRIGGER IF EXISTS xobjectentity_remote_insert_trig ON XObjectEntity_ReceiveView"
        stmtList += "DROP VIEW IF EXISTS XObjectEntity_ReceiveView"
        stmtList +=
            "CREATE VIEW XObjectEntity_ReceiveView AS  SELECT XObjectEntity.*, XObjectEntityReplicate.* FROM XObjectEntity LEFT JOIN XObjectEntityReplicate ON XObjectEntityReplicate.xoePk = XObjectEntity.xObjectUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION xobjectentity_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO XObjectEntity(xObjectUid, objectType, objectId, definitionType, interactionType, correctResponsePattern, objectContentEntryUid, objectStatementRefUid, xObjectMasterChangeSeqNum, xObjectocalChangeSeqNum, xObjectLastChangedBy, xObjectLct) VALUES (NEW.xObjectUid, NEW.objectType, NEW.objectId, NEW.definitionType, NEW.interactionType, NEW.correctResponsePattern, NEW.objectContentEntryUid, NEW.objectStatementRefUid, NEW.xObjectMasterChangeSeqNum, NEW.xObjectocalChangeSeqNum, NEW.xObjectLastChangedBy, NEW.xObjectLct) ON CONFLICT (xObjectUid) DO UPDATE SET objectType = EXCLUDED.objectType, objectId = EXCLUDED.objectId, definitionType = EXCLUDED.definitionType, interactionType = EXCLUDED.interactionType, correctResponsePattern = EXCLUDED.correctResponsePattern, objectContentEntryUid = EXCLUDED.objectContentEntryUid,objectStatementRefUid = EXCLUDED.objectStatementRefUid, xObjectMasterChangeSeqNum = EXCLUDED.xObjectMasterChangeSeqNum, xObjectocalChangeSeqNum = EXCLUDED.xObjectocalChangeSeqNum, xObjectLastChangedBy = EXCLUDED.xObjectLastChangedBy, xObjectLct = EXCLUDED.xObjectLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER xobjectentity_remote_insert_trig INSTEAD OF INSERT ON XObjectEntity_ReceiveView FOR EACH ROW EXECUTE PROCEDURE xobjectentity_remote_insert_fn() "
    }



    stmtList
}

val MIGRATION_104_105 = DoorMigrationStatementList(104, 105) { db ->
    val stmtList = mutableListOf<String>()
    if (db.dbType() == DoorDbType.SQLITE) {
        stmtList += "CREATE TABLE IF NOT EXISTS ContentEntryPicture (`cepUid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cepContentEntryUid` INTEGER NOT NULL, `cepUri` TEXT, `cepMd5` TEXT, `cepFileSize` INTEGER NOT NULL, `cepTimestamp` INTEGER NOT NULL, `cepMimeType` TEXT, `cepActive` INTEGER NOT NULL)"
        stmtList += "CREATE TABLE IF NOT EXISTS ContentEntryPictureReplicate (`cepPk` INTEGER NOT NULL, `cepVersionId` INTEGER NOT NULL DEFAULT 0, `cepDestination` INTEGER NOT NULL, `cepPending` INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(`cepPk`, `cepDestination`))"
        stmtList += "CREATE INDEX IF NOT EXISTS `index_ContentEntryPictureReplicate_cepPk_cepDestination_cepVersionId` ON ContentEntryPictureReplicate (`cepPk`, `cepDestination`, `cepVersionId`)"
        stmtList += "CREATE INDEX IF NOT EXISTS `index_ContentEntryPictureReplicate_cepDestination_cepPending` ON ContentEntryPictureReplicate (`cepDestination`, `cepPending`)"

        stmtList +=
            " CREATE TRIGGER ch_ins_138 AFTER INSERT ON ContentEntryPicture BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 138 AS chTableId, NEW.cepUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 138 AND chEntityPk = NEW.cepUid); END "
        stmtList +=
            " CREATE TRIGGER ch_upd_138 AFTER UPDATE ON ContentEntryPicture BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 138 AS chTableId, NEW.cepUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 138 AND chEntityPk = NEW.cepUid); END "
        stmtList +=
            " CREATE TRIGGER ch_del_138 AFTER DELETE ON ContentEntryPicture BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 138 AS chTableId, OLD.cepUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 138 AND chEntityPk = OLD.cepUid); END "
        stmtList +=
            "CREATE VIEW ContentEntryPicture_ReceiveView AS  SELECT ContentEntryPicture.*, ContentEntryPictureReplicate.* FROM ContentEntryPicture LEFT JOIN ContentEntryPictureReplicate ON ContentEntryPictureReplicate.cepPk = ContentEntryPicture.cepUid "
        stmtList +=
            " CREATE TRIGGER ceppicture_remote_insert_ins INSTEAD OF INSERT ON ContentEntryPicture_ReceiveView FOR EACH ROW BEGIN REPLACE INTO ContentEntryPicture(cepUid, cepContentEntryUid, cepUri, cepMd5, cepFileSize, cepTimestamp, cepMimeType, cepActive) VALUES (NEW.cepUid, NEW.cepContentEntryUid, NEW.cepUri, NEW.cepMd5, NEW.cepFileSize, NEW.cepTimestamp, NEW.cepMimeType, NEW.cepActive) /*psql ON CONFLICT (cepUid) DO UPDATE SET cepContentEntryUid = EXCLUDED.cepContentEntryUid, cepUri = EXCLUDED.cepUri, cepMd5 = EXCLUDED.cepMd5, cepFileSize = EXCLUDED.cepFileSize, cepTimestamp = EXCLUDED.cepTimestamp, cepMimeType = EXCLUDED.cepMimeType, cepActive = EXCLUDED.cepActive */; END "
        stmtList += """
        |
        |        CREATE TRIGGER ATTUPD_ContentEntryPicture
        |        AFTER UPDATE ON ContentEntryPicture FOR EACH ROW WHEN
        |        OLD.cepMd5 IS NOT NULL
        |        BEGIN
        |        
        |        INSERT INTO ZombieAttachmentData(zaUri) 
        |        SELECT OLD.cepUri AS zaUri
        |          FROM ContentEntryPicture   
        |         WHERE ContentEntryPicture.cepUid = OLD.cepUid
        |           AND (SELECT COUNT(*) 
        |                  FROM ContentEntryPicture
        |                 WHERE cepMd5 = OLD.cepMd5) = 0
        |    ; 
        |        END
        |    
        """.trimMargin()


    }else {

        stmtList +=
            "CREATE TABLE IF NOT EXISTS ContentEntryPicture (  cepContentEntryUid  BIGINT  NOT NULL , cepUri  TEXT , cepMd5  TEXT , cepFileSize  INTEGER  NOT NULL , cepTimestamp  BIGINT  NOT NULL , cepMimeType  TEXT , cepActive  BOOL  NOT NULL , cepUid  BIGSERIAL  PRIMARY KEY  NOT NULL )"
        stmtList +=
            "CREATE TABLE IF NOT EXISTS ContentEntryPictureReplicate (  cepPk  BIGINT  NOT NULL , cepVersionId  BIGINT  NOT NULL  DEFAULT 0 , cepDestination  BIGINT  NOT NULL , cepPending  BOOL  NOT NULL  DEFAULT true, PRIMARY KEY (cepPk, cepDestination) )"
        stmtList +=
            "CREATE INDEX index_ContentEntryPictureReplicate_cepPk_cepDestination_cepVersionId ON ContentEntryPictureReplicate (cepPk, cepDestination, cepVersionId)"
        stmtList +=
            "CREATE INDEX index_ContentEntryPictureReplicate_cepDestination_cepPending ON ContentEntryPictureReplicate (cepDestination, cepPending)"

        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_138_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (138, NEW.cepUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_upd_138_trig AFTER UPDATE OR INSERT ON ContentEntryPicture FOR EACH ROW EXECUTE PROCEDURE ch_upd_138_fn(); "
        stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_138_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (138, OLD.cepUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        stmtList +=
            " CREATE TRIGGER ch_del_138_trig AFTER DELETE ON ContentEntryPicture FOR EACH ROW EXECUTE PROCEDURE ch_del_138_fn(); "
        stmtList +=
            "CREATE VIEW ContentEntryPicture_ReceiveView AS  SELECT ContentEntryPicture.*, ContentEntryPictureReplicate.* FROM ContentEntryPicture LEFT JOIN ContentEntryPictureReplicate ON ContentEntryPictureReplicate.cepPk = ContentEntryPicture.cepUid "
        stmtList +=
            "CREATE OR REPLACE FUNCTION ceppicture_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ContentEntryPicture(cepUid, cepContentEntryUid, cepUri, cepMd5, cepFileSize, cepTimestamp, cepMimeType, cepActive) VALUES (NEW.cepUid, NEW.cepContentEntryUid, NEW.cepUri, NEW.cepMd5, NEW.cepFileSize, NEW.cepTimestamp, NEW.cepMimeType, NEW.cepActive) ON CONFLICT (cepUid) DO UPDATE SET cepContentEntryUid = EXCLUDED.cepContentEntryUid, cepUri = EXCLUDED.cepUri, cepMd5 = EXCLUDED.cepMd5, cepFileSize = EXCLUDED.cepFileSize, cepTimestamp = EXCLUDED.cepTimestamp, cepMimeType = EXCLUDED.cepMimeType, cepActive = EXCLUDED.cepActive ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        stmtList +=
            " CREATE TRIGGER ceppicture_remote_insert_trig INSTEAD OF INSERT ON ContentEntryPicture_ReceiveView FOR EACH ROW EXECUTE PROCEDURE ceppicture_remote_insert_fn() "
        stmtList += """
        |    CREATE OR REPLACE FUNCTION attach_ContentEntryPicture_fn() RETURNS trigger AS ${'$'}${'$'}
        |    BEGIN
        |    
        |    INSERT INTO ZombieAttachmentData(zaUri) 
        |    SELECT OLD.cepUri AS zaUri
        |      FROM ContentEntryPicture   
        |     WHERE ContentEntryPicture.cepUid = OLD.cepUid
        |       AND (SELECT COUNT(*) 
        |              FROM ContentEntryPicture
        |             WHERE cepMd5 = OLD.cepMd5) = 0
        |;
        |    RETURN NEW;
        |    END ${'$'}${'$'}
        |    LANGUAGE plpgsql
        """.trimMargin()
        stmtList += """
        |CREATE TRIGGER attach_ContentEntryPicture_trig
        |AFTER UPDATE ON ContentEntryPicture
        |FOR EACH ROW WHEN (OLD.cepMd5 IS NOT NULL)
        |EXECUTE PROCEDURE attach_ContentEntryPicture_fn();
        """.trimMargin()

    }

    stmtList
}

val MIGRATION_105_106 = DoorMigrationStatementList(105, 106) { db ->
    val stmtList = mutableListOf<String>()
    stmtList += "ALTER TABLE CourseAssignmentSubmissionAttachment ADD COLUMN casaFileName TEXT"

    stmtList

}

val MIGRATION_106_107 = DoorMigrationStatementList(106, 107) {db ->
    mutableListOf<String>().apply {
        add("DROP TABLE IF EXISTS SqliteChangeSeqNums")
        add("DROP TABLE IF EXISTS UpdateNotification")
    }
}

fun migrationList() = listOf<DoorMigration>(
    UmAppDatabaseReplicationMigration91_92, MIGRATION_92_93, MIGRATION_93_94, MIGRATION_94_95,
    MIGRATION_95_96, MIGRATION_96_97, MIGRATION_97_98, MIGRATION_98_99,
    MIGRATION_99_100, MIGRATION_100_101, MIGRATION_101_102, MIGRATION_102_103,
    MIGRATION_103_104, MIGRATION_104_105, MIGRATION_105_106, MIGRATION_106_107
)


