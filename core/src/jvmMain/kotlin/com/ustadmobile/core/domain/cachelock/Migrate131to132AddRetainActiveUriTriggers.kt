package com.ustadmobile.core.domain.cachelock

import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.migration.DoorMigrationStatementList

/**
 * Add triggers for retention of PersonPicture and CoursePicture data on server side
 */
val Migrate131to132AddRetainActiveUriTriggers = DoorMigrationStatementList(131, 132) { db ->
    //This is hardcoded to preserve the integrity of the migration
    buildList {
        when (db.dbType()) {
            DoorDbType.SQLITE -> {
                add("""
                        CREATE TRIGGER IF NOT EXISTS Retain_PersonPicture_Ins_personPictureUri
                        AFTER INSERT ON PersonPicture
                        FOR EACH ROW WHEN NEW.personPictureUri IS NOT NULL
                        BEGIN
                        INSERT OR REPLACE INTO CacheLockJoin(cljTableId, cljEntityUid, cljUrl, cljLockId, cljStatus, cljType)
                        VALUES(50, NEW.personPictureUid, NEW.personPictureUri, 0, 1, 1);
                        END
                    ;""")

                add("""
                        CREATE TRIGGER IF NOT EXISTS Retain_PersonPicture_Ins_personPictureThumbnailUri
                        AFTER INSERT ON PersonPicture
                        FOR EACH ROW WHEN NEW.personPictureThumbnailUri IS NOT NULL
                        BEGIN
                        INSERT OR REPLACE INTO CacheLockJoin(cljTableId, cljEntityUid, cljUrl, cljLockId, cljStatus, cljType)
                        VALUES(50, NEW.personPictureUid, NEW.personPictureThumbnailUri, 0, 1, 1);
                        END
                    ;""")

                add("""
                    CREATE TRIGGER IF NOT EXISTS Retain_PersonPicture_Upd_personPictureUri_New
                    AFTER UPDATE ON PersonPicture
                    FOR EACH ROW WHEN NEW.personPictureUri != OLD.personPictureUri AND NEW.personPictureUri IS NOT NULL
                    BEGIN
                        INSERT OR REPLACE INTO CacheLockJoin(cljTableId, cljEntityUid, cljUrl, cljLockId, cljStatus, cljType)
                        VALUES(50, NEW.personPictureUid, NEW.personPictureUri, 0, 1, 1);
                    END   
                ;""")

                add("""CREATE TRIGGER IF NOT EXISTS Retain_PersonPicture_Upd_personPictureUri_Old
AFTER UPDATE ON PersonPicture
FOR EACH ROW WHEN NEW.personPictureUri != OLD.personPictureUri AND OLD.personPictureUri IS NOT NULL
BEGIN
    UPDATE CacheLockJoin 
       SET cljStatus = 3
     WHERE cljTableId = 50
       AND cljEntityUid = OLD.personPictureUid
       AND cljUrl = OLD.personPictureUri;
END        ;""")

                add("""
                    CREATE TRIGGER IF NOT EXISTS Retain_PersonPicture_Upd_personPictureThumbnailUri_New
                    AFTER UPDATE ON PersonPicture
                    FOR EACH ROW WHEN NEW.personPictureThumbnailUri != OLD.personPictureThumbnailUri AND NEW.personPictureThumbnailUri IS NOT NULL
                    BEGIN
                        INSERT OR REPLACE INTO CacheLockJoin(cljTableId, cljEntityUid, cljUrl, cljLockId, cljStatus, cljType)
                        VALUES(50, NEW.personPictureUid, NEW.personPictureThumbnailUri, 0, 1, 1);
                    END   
                ;""")

                add("""CREATE TRIGGER IF NOT EXISTS Retain_PersonPicture_Upd_personPictureThumbnailUri_Old
AFTER UPDATE ON PersonPicture
FOR EACH ROW WHEN NEW.personPictureThumbnailUri != OLD.personPictureThumbnailUri AND OLD.personPictureThumbnailUri IS NOT NULL
BEGIN
    UPDATE CacheLockJoin 
       SET cljStatus = 3
     WHERE cljTableId = 50
       AND cljEntityUid = OLD.personPictureUid
       AND cljUrl = OLD.personPictureThumbnailUri;
END        ;""")

                add("""CREATE TRIGGER IF NOT EXISTS Retain_PersonPicture_Del_personPictureUri
AFTER DELETE ON PersonPicture
FOR EACH ROW WHEN OLD.personPictureUri IS NOT NULL
BEGIN
    UPDATE CacheLockJoin 
       SET cljStatus = 3
     WHERE cljTableId = 50
       AND cljEntityUid = OLD.personPictureUid
       AND cljUrl = OLD.personPictureUri;
END       ;""")

                add("""CREATE TRIGGER IF NOT EXISTS Retain_PersonPicture_Del_personPictureThumbnailUri
AFTER DELETE ON PersonPicture
FOR EACH ROW WHEN OLD.personPictureThumbnailUri IS NOT NULL
BEGIN
    UPDATE CacheLockJoin 
       SET cljStatus = 3
     WHERE cljTableId = 50
       AND cljEntityUid = OLD.personPictureUid
       AND cljUrl = OLD.personPictureThumbnailUri;
END       ;""")

                add("""
                        CREATE TRIGGER IF NOT EXISTS Retain_CoursePicture_Ins_coursePictureUri
                        AFTER INSERT ON CoursePicture
                        FOR EACH ROW WHEN NEW.coursePictureUri IS NOT NULL
                        BEGIN
                        INSERT OR REPLACE INTO CacheLockJoin(cljTableId, cljEntityUid, cljUrl, cljLockId, cljStatus, cljType)
                        VALUES(125, NEW.coursePictureUid, NEW.coursePictureUri, 0, 1, 1);
                        END
                    ;""")

                add("""
                        CREATE TRIGGER IF NOT EXISTS Retain_CoursePicture_Ins_coursePictureThumbnailUri
                        AFTER INSERT ON CoursePicture
                        FOR EACH ROW WHEN NEW.coursePictureThumbnailUri IS NOT NULL
                        BEGIN
                        INSERT OR REPLACE INTO CacheLockJoin(cljTableId, cljEntityUid, cljUrl, cljLockId, cljStatus, cljType)
                        VALUES(125, NEW.coursePictureUid, NEW.coursePictureThumbnailUri, 0, 1, 1);
                        END
                    ;""")

                add("""
                    CREATE TRIGGER IF NOT EXISTS Retain_CoursePicture_Upd_coursePictureUri_New
                    AFTER UPDATE ON CoursePicture
                    FOR EACH ROW WHEN NEW.coursePictureUri != OLD.coursePictureUri AND NEW.coursePictureUri IS NOT NULL
                    BEGIN
                        INSERT OR REPLACE INTO CacheLockJoin(cljTableId, cljEntityUid, cljUrl, cljLockId, cljStatus, cljType)
                        VALUES(125, NEW.coursePictureUid, NEW.coursePictureUri, 0, 1, 1);
                    END   
                ;""")

                add("""CREATE TRIGGER IF NOT EXISTS Retain_CoursePicture_Upd_coursePictureUri_Old
AFTER UPDATE ON CoursePicture
FOR EACH ROW WHEN NEW.coursePictureUri != OLD.coursePictureUri AND OLD.coursePictureUri IS NOT NULL
BEGIN
    UPDATE CacheLockJoin 
       SET cljStatus = 3
     WHERE cljTableId = 125
       AND cljEntityUid = OLD.coursePictureUid
       AND cljUrl = OLD.coursePictureUri;
END        ;""")

                add("""
                    CREATE TRIGGER IF NOT EXISTS Retain_CoursePicture_Upd_coursePictureThumbnailUri_New
                    AFTER UPDATE ON CoursePicture
                    FOR EACH ROW WHEN NEW.coursePictureThumbnailUri != OLD.coursePictureThumbnailUri AND NEW.coursePictureThumbnailUri IS NOT NULL
                    BEGIN
                        INSERT OR REPLACE INTO CacheLockJoin(cljTableId, cljEntityUid, cljUrl, cljLockId, cljStatus, cljType)
                        VALUES(125, NEW.coursePictureUid, NEW.coursePictureThumbnailUri, 0, 1, 1);
                    END   
                ;""")

                add("""CREATE TRIGGER IF NOT EXISTS Retain_CoursePicture_Upd_coursePictureThumbnailUri_Old
AFTER UPDATE ON CoursePicture
FOR EACH ROW WHEN NEW.coursePictureThumbnailUri != OLD.coursePictureThumbnailUri AND OLD.coursePictureThumbnailUri IS NOT NULL
BEGIN
    UPDATE CacheLockJoin 
       SET cljStatus = 3
     WHERE cljTableId = 125
       AND cljEntityUid = OLD.coursePictureUid
       AND cljUrl = OLD.coursePictureThumbnailUri;
END        ;""")

                add("""CREATE TRIGGER IF NOT EXISTS Retain_CoursePicture_Del_coursePictureUri
AFTER DELETE ON CoursePicture
FOR EACH ROW WHEN OLD.coursePictureUri IS NOT NULL
BEGIN
    UPDATE CacheLockJoin 
       SET cljStatus = 3
     WHERE cljTableId = 125
       AND cljEntityUid = OLD.coursePictureUid
       AND cljUrl = OLD.coursePictureUri;
END       ;""")

                add("""CREATE TRIGGER IF NOT EXISTS Retain_CoursePicture_Del_coursePictureThumbnailUri
AFTER DELETE ON CoursePicture
FOR EACH ROW WHEN OLD.coursePictureThumbnailUri IS NOT NULL
BEGIN
    UPDATE CacheLockJoin 
       SET cljStatus = 3
     WHERE cljTableId = 125
       AND cljEntityUid = OLD.coursePictureUid
       AND cljUrl = OLD.coursePictureThumbnailUri;
END       ;""")
            }
        }
    }
}