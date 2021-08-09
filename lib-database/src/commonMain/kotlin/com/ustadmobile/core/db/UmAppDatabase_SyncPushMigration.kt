package com.ustadmobile.core.db

import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.migration.DoorMigrationSync
import com.ustadmobile.door.util.systemTimeInMillis

val UmAppDatabase_SyncPushMigration = DoorMigrationSync(42, 43) { database ->

    if(database.dbType() == DoorDbType.SQLITE) {
        database.execSQL("CREATE TABLE IF NOT EXISTS ChangeLog (  chTableId  INTEGER  NOT NULL , chEntityPk  INTEGER  NOT NULL , dispatched  INTEGER  NOT NULL , chTime  INTEGER  NOT NULL , changeLogUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
        database.execSQL("CREATE TABLE IF NOT EXISTS SqliteChangeSeqNums (  sCsnTableId  INTEGER  PRIMARY KEY  NOT NULL , sCsnNextLocal  INTEGER  NOT NULL , sCsnNextPrimary  INTEGER  NOT NULL )")
        database.execSQL("""
  |CREATE 
  | INDEX index_SqliteChangeSeqNums_sCsnNextLocal 
  |ON SqliteChangeSeqNums (sCsnNextLocal)
  """.trimMargin())
        database.execSQL("""
  |CREATE 
  | INDEX index_SqliteChangeSeqNums_sCsnNextPrimary 
  |ON SqliteChangeSeqNums (sCsnNextPrimary)
  """.trimMargin())
        database.execSQL("CREATE TABLE IF NOT EXISTS TableSyncStatus (  tsTableId  INTEGER  PRIMARY KEY  NOT NULL , tsLastChanged  INTEGER  NOT NULL , tsLastSynced  INTEGER  NOT NULL )")
        database.execSQL("CREATE TABLE IF NOT EXISTS UpdateNotification (  pnDeviceId  INTEGER  NOT NULL , pnTableId  INTEGER  NOT NULL , pnTimestamp  INTEGER  NOT NULL , pnUid  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
        database.execSQL("""
  |CREATE 
  |UNIQUE INDEX index_UpdateNotification_pnDeviceId_pnTableId 
  |ON UpdateNotification (pnDeviceId, pnTableId)
  """.trimMargin())
        database.execSQL("""
  |CREATE 
  | INDEX index_UpdateNotification_pnDeviceId_pnTimestamp 
  |ON UpdateNotification (pnDeviceId, pnTimestamp)
  """.trimMargin())
        database.execSQL("DROP TRIGGER IF EXISTS INS_14")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_14")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_14
  |AFTER INSERT ON ClazzLog
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.clazzLogLCSN = 0)
  |BEGIN
  |    UPDATE ClazzLog
  |    SET clazzLogMSQN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 14)
  |    WHERE clazzLogUid = NEW.clazzLogUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 14;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_14
  |            AFTER INSERT ON ClazzLog
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.clazzLogMSQN = 0)
  |            BEGIN
  |                UPDATE ClazzLog
  |                SET clazzLogMSQN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 14)
  |                WHERE clazzLogUid = NEW.clazzLogUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 14;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 14, NEW.clazzLogUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_14
  |AFTER UPDATE ON ClazzLog
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.clazzLogLCSN == OLD.clazzLogLCSN OR
  |        NEW.clazzLogLCSN == 0))
  |BEGIN
  |    UPDATE ClazzLog
  |    SET clazzLogLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 14) 
  |    WHERE clazzLogUid = NEW.clazzLogUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 14;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_14
  |            AFTER UPDATE ON ClazzLog
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.clazzLogMSQN == OLD.clazzLogMSQN OR
  |                    NEW.clazzLogMSQN == 0))
  |            BEGIN
  |                UPDATE ClazzLog
  |                SET clazzLogMSQN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 14)
  |                WHERE clazzLogUid = NEW.clazzLogUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 14;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 14, NEW.clazzLogUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(14, (SELECT COALESCE((SELECT MAX(clazzLogLCSN) FROM ClazzLog), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(14, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ClazzLog_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzLog_trk_epk_clientId_tmp ON ClazzLog_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzLog_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzLog_trk_nest.pk FROM ClazzLog_trk ClazzLog_trk_nest 
  |  WHERE ClazzLog_trk_nest.clientId = ClazzLog_trk.clientId AND
  |  ClazzLog_trk_nest.epk = ClazzLog_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzLog_trk_clientId_epk_csn  ON ClazzLog_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzLog_trk_epk_clientId ON ClazzLog_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzLog_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_15")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_15")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_15
  |AFTER INSERT ON ClazzLogAttendanceRecord
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.clazzLogAttendanceRecordLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ClazzLogAttendanceRecord
  |    SET clazzLogAttendanceRecordMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 15)
  |    WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 15;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_15
  |            AFTER INSERT ON ClazzLogAttendanceRecord
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.clazzLogAttendanceRecordMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ClazzLogAttendanceRecord
  |                SET clazzLogAttendanceRecordMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 15)
  |                WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 15;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 15, NEW.clazzLogAttendanceRecordUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_15
  |AFTER UPDATE ON ClazzLogAttendanceRecord
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.clazzLogAttendanceRecordLocalChangeSeqNum == OLD.clazzLogAttendanceRecordLocalChangeSeqNum OR
  |        NEW.clazzLogAttendanceRecordLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ClazzLogAttendanceRecord
  |    SET clazzLogAttendanceRecordLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 15) 
  |    WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 15;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_15
  |            AFTER UPDATE ON ClazzLogAttendanceRecord
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.clazzLogAttendanceRecordMasterChangeSeqNum == OLD.clazzLogAttendanceRecordMasterChangeSeqNum OR
  |                    NEW.clazzLogAttendanceRecordMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ClazzLogAttendanceRecord
  |                SET clazzLogAttendanceRecordMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 15)
  |                WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 15;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 15, NEW.clazzLogAttendanceRecordUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(15, (SELECT COALESCE((SELECT MAX(clazzLogAttendanceRecordLocalChangeSeqNum) FROM ClazzLogAttendanceRecord), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(15, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ClazzLogAttendanceRecord_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzLogAttendanceRecord_trk_epk_clientId_tmp ON ClazzLogAttendanceRecord_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzLogAttendanceRecord_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzLogAttendanceRecord_trk_nest.pk FROM ClazzLogAttendanceRecord_trk ClazzLogAttendanceRecord_trk_nest 
  |  WHERE ClazzLogAttendanceRecord_trk_nest.clientId = ClazzLogAttendanceRecord_trk.clientId AND
  |  ClazzLogAttendanceRecord_trk_nest.epk = ClazzLogAttendanceRecord_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzLogAttendanceRecord_trk_clientId_epk_csn  ON ClazzLogAttendanceRecord_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzLogAttendanceRecord_trk_epk_clientId ON ClazzLogAttendanceRecord_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzLogAttendanceRecord_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_21")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_21")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_21
  |AFTER INSERT ON Schedule
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.scheduleLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE Schedule
  |    SET scheduleMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 21)
  |    WHERE scheduleUid = NEW.scheduleUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 21;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_21
  |            AFTER INSERT ON Schedule
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.scheduleMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE Schedule
  |                SET scheduleMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 21)
  |                WHERE scheduleUid = NEW.scheduleUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 21;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 21, NEW.scheduleUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_21
  |AFTER UPDATE ON Schedule
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.scheduleLocalChangeSeqNum == OLD.scheduleLocalChangeSeqNum OR
  |        NEW.scheduleLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE Schedule
  |    SET scheduleLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 21) 
  |    WHERE scheduleUid = NEW.scheduleUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 21;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_21
  |            AFTER UPDATE ON Schedule
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.scheduleMasterChangeSeqNum == OLD.scheduleMasterChangeSeqNum OR
  |                    NEW.scheduleMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE Schedule
  |                SET scheduleMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 21)
  |                WHERE scheduleUid = NEW.scheduleUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 21;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 21, NEW.scheduleUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(21, (SELECT COALESCE((SELECT MAX(scheduleLocalChangeSeqNum) FROM Schedule), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(21, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_Schedule_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Schedule_trk_epk_clientId_tmp ON Schedule_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Schedule_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Schedule_trk_nest.pk FROM Schedule_trk Schedule_trk_nest 
  |  WHERE Schedule_trk_nest.clientId = Schedule_trk.clientId AND
  |  Schedule_trk_nest.epk = Schedule_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Schedule_trk_clientId_epk_csn  ON Schedule_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Schedule_trk_epk_clientId ON Schedule_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Schedule_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_17")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_17")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_17
  |AFTER INSERT ON DateRange
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.dateRangeLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE DateRange
  |    SET dateRangeMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 17)
  |    WHERE dateRangeUid = NEW.dateRangeUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 17;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_17
  |            AFTER INSERT ON DateRange
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.dateRangeMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE DateRange
  |                SET dateRangeMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 17)
  |                WHERE dateRangeUid = NEW.dateRangeUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 17;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 17, NEW.dateRangeUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_17
  |AFTER UPDATE ON DateRange
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.dateRangeLocalChangeSeqNum == OLD.dateRangeLocalChangeSeqNum OR
  |        NEW.dateRangeLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE DateRange
  |    SET dateRangeLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 17) 
  |    WHERE dateRangeUid = NEW.dateRangeUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 17;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_17
  |            AFTER UPDATE ON DateRange
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.dateRangeMasterChangeSeqNum == OLD.dateRangeMasterChangeSeqNum OR
  |                    NEW.dateRangeMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE DateRange
  |                SET dateRangeMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 17)
  |                WHERE dateRangeUid = NEW.dateRangeUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 17;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 17, NEW.dateRangeUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(17, (SELECT COALESCE((SELECT MAX(dateRangeLocalChangeSeqNum) FROM DateRange), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(17, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_DateRange_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_DateRange_trk_epk_clientId_tmp ON DateRange_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM DateRange_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT DateRange_trk_nest.pk FROM DateRange_trk DateRange_trk_nest 
  |  WHERE DateRange_trk_nest.clientId = DateRange_trk.clientId AND
  |  DateRange_trk_nest.epk = DateRange_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_DateRange_trk_clientId_epk_csn  ON DateRange_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_DateRange_trk_epk_clientId ON DateRange_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_DateRange_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_28")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_28")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_28
  |AFTER INSERT ON HolidayCalendar
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.umCalendarLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE HolidayCalendar
  |    SET umCalendarMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 28)
  |    WHERE umCalendarUid = NEW.umCalendarUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 28;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_28
  |            AFTER INSERT ON HolidayCalendar
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.umCalendarMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE HolidayCalendar
  |                SET umCalendarMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 28)
  |                WHERE umCalendarUid = NEW.umCalendarUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 28;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 28, NEW.umCalendarUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_28
  |AFTER UPDATE ON HolidayCalendar
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.umCalendarLocalChangeSeqNum == OLD.umCalendarLocalChangeSeqNum OR
  |        NEW.umCalendarLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE HolidayCalendar
  |    SET umCalendarLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 28) 
  |    WHERE umCalendarUid = NEW.umCalendarUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 28;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_28
  |            AFTER UPDATE ON HolidayCalendar
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.umCalendarMasterChangeSeqNum == OLD.umCalendarMasterChangeSeqNum OR
  |                    NEW.umCalendarMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE HolidayCalendar
  |                SET umCalendarMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 28)
  |                WHERE umCalendarUid = NEW.umCalendarUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 28;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 28, NEW.umCalendarUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(28, (SELECT COALESCE((SELECT MAX(umCalendarLocalChangeSeqNum) FROM HolidayCalendar), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(28, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_HolidayCalendar_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_HolidayCalendar_trk_epk_clientId_tmp ON HolidayCalendar_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM HolidayCalendar_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT HolidayCalendar_trk_nest.pk FROM HolidayCalendar_trk HolidayCalendar_trk_nest 
  |  WHERE HolidayCalendar_trk_nest.clientId = HolidayCalendar_trk.clientId AND
  |  HolidayCalendar_trk_nest.epk = HolidayCalendar_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_HolidayCalendar_trk_clientId_epk_csn  ON HolidayCalendar_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_HolidayCalendar_trk_epk_clientId ON HolidayCalendar_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_HolidayCalendar_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_99")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_99")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_99
  |AFTER INSERT ON Holiday
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.holLocalCsn = 0)
  |BEGIN
  |    UPDATE Holiday
  |    SET holMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 99)
  |    WHERE holUid = NEW.holUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 99;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_99
  |            AFTER INSERT ON Holiday
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.holMasterCsn = 0)
  |            BEGIN
  |                UPDATE Holiday
  |                SET holMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 99)
  |                WHERE holUid = NEW.holUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 99;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 99, NEW.holUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_99
  |AFTER UPDATE ON Holiday
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.holLocalCsn == OLD.holLocalCsn OR
  |        NEW.holLocalCsn == 0))
  |BEGIN
  |    UPDATE Holiday
  |    SET holLocalCsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 99) 
  |    WHERE holUid = NEW.holUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 99;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_99
  |            AFTER UPDATE ON Holiday
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.holMasterCsn == OLD.holMasterCsn OR
  |                    NEW.holMasterCsn == 0))
  |            BEGIN
  |                UPDATE Holiday
  |                SET holMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 99)
  |                WHERE holUid = NEW.holUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 99;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 99, NEW.holUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(99, (SELECT COALESCE((SELECT MAX(holLocalCsn) FROM Holiday), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(99, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_Holiday_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Holiday_trk_epk_clientId_tmp ON Holiday_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Holiday_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Holiday_trk_nest.pk FROM Holiday_trk Holiday_trk_nest 
  |  WHERE Holiday_trk_nest.clientId = Holiday_trk.clientId AND
  |  Holiday_trk_nest.epk = Holiday_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Holiday_trk_clientId_epk_csn  ON Holiday_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Holiday_trk_epk_clientId ON Holiday_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Holiday_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_173")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_173")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_173
  |AFTER INSERT ON ScheduledCheck
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.scheduledCheckLocalCsn = 0)
  |BEGIN
  |    UPDATE ScheduledCheck
  |    SET scheduledCheckMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 173)
  |    WHERE scheduledCheckUid = NEW.scheduledCheckUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 173;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_173
  |            AFTER INSERT ON ScheduledCheck
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.scheduledCheckMasterCsn = 0)
  |            BEGIN
  |                UPDATE ScheduledCheck
  |                SET scheduledCheckMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 173)
  |                WHERE scheduledCheckUid = NEW.scheduledCheckUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 173;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 173, NEW.scheduledCheckUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_173
  |AFTER UPDATE ON ScheduledCheck
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.scheduledCheckLocalCsn == OLD.scheduledCheckLocalCsn OR
  |        NEW.scheduledCheckLocalCsn == 0))
  |BEGIN
  |    UPDATE ScheduledCheck
  |    SET scheduledCheckLocalCsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 173) 
  |    WHERE scheduledCheckUid = NEW.scheduledCheckUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 173;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_173
  |            AFTER UPDATE ON ScheduledCheck
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.scheduledCheckMasterCsn == OLD.scheduledCheckMasterCsn OR
  |                    NEW.scheduledCheckMasterCsn == 0))
  |            BEGIN
  |                UPDATE ScheduledCheck
  |                SET scheduledCheckMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 173)
  |                WHERE scheduledCheckUid = NEW.scheduledCheckUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 173;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 173, NEW.scheduledCheckUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(173, (SELECT COALESCE((SELECT MAX(scheduledCheckLocalCsn) FROM ScheduledCheck), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(173, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ScheduledCheck_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ScheduledCheck_trk_epk_clientId_tmp ON ScheduledCheck_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ScheduledCheck_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ScheduledCheck_trk_nest.pk FROM ScheduledCheck_trk ScheduledCheck_trk_nest 
  |  WHERE ScheduledCheck_trk_nest.clientId = ScheduledCheck_trk.clientId AND
  |  ScheduledCheck_trk_nest.epk = ScheduledCheck_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ScheduledCheck_trk_clientId_epk_csn  ON ScheduledCheck_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ScheduledCheck_trk_epk_clientId ON ScheduledCheck_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ScheduledCheck_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_53")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_53")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_53
  |AFTER INSERT ON AuditLog
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.auditLogLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE AuditLog
  |    SET auditLogMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 53)
  |    WHERE auditLogUid = NEW.auditLogUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 53;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_53
  |            AFTER INSERT ON AuditLog
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.auditLogMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE AuditLog
  |                SET auditLogMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 53)
  |                WHERE auditLogUid = NEW.auditLogUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 53;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 53, NEW.auditLogUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_53
  |AFTER UPDATE ON AuditLog
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.auditLogLocalChangeSeqNum == OLD.auditLogLocalChangeSeqNum OR
  |        NEW.auditLogLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE AuditLog
  |    SET auditLogLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 53) 
  |    WHERE auditLogUid = NEW.auditLogUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 53;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_53
  |            AFTER UPDATE ON AuditLog
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.auditLogMasterChangeSeqNum == OLD.auditLogMasterChangeSeqNum OR
  |                    NEW.auditLogMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE AuditLog
  |                SET auditLogMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 53)
  |                WHERE auditLogUid = NEW.auditLogUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 53;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 53, NEW.auditLogUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(53, (SELECT COALESCE((SELECT MAX(auditLogLocalChangeSeqNum) FROM AuditLog), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(53, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_AuditLog_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_AuditLog_trk_epk_clientId_tmp ON AuditLog_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM AuditLog_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT AuditLog_trk_nest.pk FROM AuditLog_trk AuditLog_trk_nest 
  |  WHERE AuditLog_trk_nest.clientId = AuditLog_trk.clientId AND
  |  AuditLog_trk_nest.epk = AuditLog_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_AuditLog_trk_clientId_epk_csn  ON AuditLog_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_AuditLog_trk_epk_clientId ON AuditLog_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_AuditLog_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_56")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_56")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_56
  |AFTER INSERT ON CustomField
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.customFieldLCSN = 0)
  |BEGIN
  |    UPDATE CustomField
  |    SET customFieldMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 56)
  |    WHERE customFieldUid = NEW.customFieldUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 56;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_56
  |            AFTER INSERT ON CustomField
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.customFieldMCSN = 0)
  |            BEGIN
  |                UPDATE CustomField
  |                SET customFieldMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 56)
  |                WHERE customFieldUid = NEW.customFieldUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 56;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 56, NEW.customFieldUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_56
  |AFTER UPDATE ON CustomField
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.customFieldLCSN == OLD.customFieldLCSN OR
  |        NEW.customFieldLCSN == 0))
  |BEGIN
  |    UPDATE CustomField
  |    SET customFieldLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 56) 
  |    WHERE customFieldUid = NEW.customFieldUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 56;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_56
  |            AFTER UPDATE ON CustomField
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.customFieldMCSN == OLD.customFieldMCSN OR
  |                    NEW.customFieldMCSN == 0))
  |            BEGIN
  |                UPDATE CustomField
  |                SET customFieldMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 56)
  |                WHERE customFieldUid = NEW.customFieldUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 56;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 56, NEW.customFieldUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(56, (SELECT COALESCE((SELECT MAX(customFieldLCSN) FROM CustomField), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(56, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_CustomField_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_CustomField_trk_epk_clientId_tmp ON CustomField_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM CustomField_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT CustomField_trk_nest.pk FROM CustomField_trk CustomField_trk_nest 
  |  WHERE CustomField_trk_nest.clientId = CustomField_trk.clientId AND
  |  CustomField_trk_nest.epk = CustomField_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_CustomField_trk_clientId_epk_csn  ON CustomField_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_CustomField_trk_epk_clientId ON CustomField_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_CustomField_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_57")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_57")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_57
  |AFTER INSERT ON CustomFieldValue
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.customFieldValueLCSN = 0)
  |BEGIN
  |    UPDATE CustomFieldValue
  |    SET customFieldValueMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 57)
  |    WHERE customFieldValueUid = NEW.customFieldValueUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 57;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_57
  |            AFTER INSERT ON CustomFieldValue
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.customFieldValueMCSN = 0)
  |            BEGIN
  |                UPDATE CustomFieldValue
  |                SET customFieldValueMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 57)
  |                WHERE customFieldValueUid = NEW.customFieldValueUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 57;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 57, NEW.customFieldValueUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_57
  |AFTER UPDATE ON CustomFieldValue
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.customFieldValueLCSN == OLD.customFieldValueLCSN OR
  |        NEW.customFieldValueLCSN == 0))
  |BEGIN
  |    UPDATE CustomFieldValue
  |    SET customFieldValueLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 57) 
  |    WHERE customFieldValueUid = NEW.customFieldValueUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 57;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_57
  |            AFTER UPDATE ON CustomFieldValue
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.customFieldValueMCSN == OLD.customFieldValueMCSN OR
  |                    NEW.customFieldValueMCSN == 0))
  |            BEGIN
  |                UPDATE CustomFieldValue
  |                SET customFieldValueMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 57)
  |                WHERE customFieldValueUid = NEW.customFieldValueUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 57;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 57, NEW.customFieldValueUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(57, (SELECT COALESCE((SELECT MAX(customFieldValueLCSN) FROM CustomFieldValue), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(57, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_CustomFieldValue_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_CustomFieldValue_trk_epk_clientId_tmp ON CustomFieldValue_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM CustomFieldValue_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT CustomFieldValue_trk_nest.pk FROM CustomFieldValue_trk CustomFieldValue_trk_nest 
  |  WHERE CustomFieldValue_trk_nest.clientId = CustomFieldValue_trk.clientId AND
  |  CustomFieldValue_trk_nest.epk = CustomFieldValue_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_CustomFieldValue_trk_clientId_epk_csn  ON CustomFieldValue_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_CustomFieldValue_trk_epk_clientId ON CustomFieldValue_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_CustomFieldValue_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_55")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_55")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_55
  |AFTER INSERT ON CustomFieldValueOption
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.customFieldValueOptionLCSN = 0)
  |BEGIN
  |    UPDATE CustomFieldValueOption
  |    SET customFieldValueOptionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 55)
  |    WHERE customFieldValueOptionUid = NEW.customFieldValueOptionUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 55;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_55
  |            AFTER INSERT ON CustomFieldValueOption
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.customFieldValueOptionMCSN = 0)
  |            BEGIN
  |                UPDATE CustomFieldValueOption
  |                SET customFieldValueOptionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 55)
  |                WHERE customFieldValueOptionUid = NEW.customFieldValueOptionUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 55;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 55, NEW.customFieldValueOptionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_55
  |AFTER UPDATE ON CustomFieldValueOption
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.customFieldValueOptionLCSN == OLD.customFieldValueOptionLCSN OR
  |        NEW.customFieldValueOptionLCSN == 0))
  |BEGIN
  |    UPDATE CustomFieldValueOption
  |    SET customFieldValueOptionLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 55) 
  |    WHERE customFieldValueOptionUid = NEW.customFieldValueOptionUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 55;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_55
  |            AFTER UPDATE ON CustomFieldValueOption
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.customFieldValueOptionMCSN == OLD.customFieldValueOptionMCSN OR
  |                    NEW.customFieldValueOptionMCSN == 0))
  |            BEGIN
  |                UPDATE CustomFieldValueOption
  |                SET customFieldValueOptionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 55)
  |                WHERE customFieldValueOptionUid = NEW.customFieldValueOptionUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 55;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 55, NEW.customFieldValueOptionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(55, (SELECT COALESCE((SELECT MAX(customFieldValueOptionLCSN) FROM CustomFieldValueOption), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(55, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_CustomFieldValueOption_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_CustomFieldValueOption_trk_epk_clientId_tmp ON CustomFieldValueOption_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM CustomFieldValueOption_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT CustomFieldValueOption_trk_nest.pk FROM CustomFieldValueOption_trk CustomFieldValueOption_trk_nest 
  |  WHERE CustomFieldValueOption_trk_nest.clientId = CustomFieldValueOption_trk.clientId AND
  |  CustomFieldValueOption_trk_nest.epk = CustomFieldValueOption_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_CustomFieldValueOption_trk_clientId_epk_csn  ON CustomFieldValueOption_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_CustomFieldValueOption_trk_epk_clientId ON CustomFieldValueOption_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_CustomFieldValueOption_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_9")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_9")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_9
  |AFTER INSERT ON Person
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.personLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE Person
  |    SET personMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 9)
  |    WHERE personUid = NEW.personUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 9;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_9
  |            AFTER INSERT ON Person
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.personMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE Person
  |                SET personMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 9)
  |                WHERE personUid = NEW.personUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 9;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 9, NEW.personUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_9
  |AFTER UPDATE ON Person
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.personLocalChangeSeqNum == OLD.personLocalChangeSeqNum OR
  |        NEW.personLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE Person
  |    SET personLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 9) 
  |    WHERE personUid = NEW.personUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 9;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_9
  |            AFTER UPDATE ON Person
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.personMasterChangeSeqNum == OLD.personMasterChangeSeqNum OR
  |                    NEW.personMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE Person
  |                SET personMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 9)
  |                WHERE personUid = NEW.personUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 9;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 9, NEW.personUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(9, (SELECT COALESCE((SELECT MAX(personLocalChangeSeqNum) FROM Person), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(9, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_Person_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Person_trk_epk_clientId_tmp ON Person_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Person_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Person_trk_nest.pk FROM Person_trk Person_trk_nest 
  |  WHERE Person_trk_nest.clientId = Person_trk.clientId AND
  |  Person_trk_nest.epk = Person_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Person_trk_clientId_epk_csn  ON Person_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Person_trk_epk_clientId ON Person_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Person_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_6")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_6")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_6
  |AFTER INSERT ON Clazz
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.clazzLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE Clazz
  |    SET clazzMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 6)
  |    WHERE clazzUid = NEW.clazzUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 6;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_6
  |            AFTER INSERT ON Clazz
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.clazzMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE Clazz
  |                SET clazzMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 6)
  |                WHERE clazzUid = NEW.clazzUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 6;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 6, NEW.clazzUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_6
  |AFTER UPDATE ON Clazz
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.clazzLocalChangeSeqNum == OLD.clazzLocalChangeSeqNum OR
  |        NEW.clazzLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE Clazz
  |    SET clazzLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 6) 
  |    WHERE clazzUid = NEW.clazzUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 6;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_6
  |            AFTER UPDATE ON Clazz
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.clazzMasterChangeSeqNum == OLD.clazzMasterChangeSeqNum OR
  |                    NEW.clazzMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE Clazz
  |                SET clazzMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 6)
  |                WHERE clazzUid = NEW.clazzUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 6;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 6, NEW.clazzUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(6, (SELECT COALESCE((SELECT MAX(clazzLocalChangeSeqNum) FROM Clazz), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(6, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_Clazz_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Clazz_trk_epk_clientId_tmp ON Clazz_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Clazz_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Clazz_trk_nest.pk FROM Clazz_trk Clazz_trk_nest 
  |  WHERE Clazz_trk_nest.clientId = Clazz_trk.clientId AND
  |  Clazz_trk_nest.epk = Clazz_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Clazz_trk_clientId_epk_csn  ON Clazz_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Clazz_trk_epk_clientId ON Clazz_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Clazz_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_65")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_65")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_65
  |AFTER INSERT ON ClazzMember
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.clazzMemberLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ClazzMember
  |    SET clazzMemberMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 65)
  |    WHERE clazzMemberUid = NEW.clazzMemberUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 65;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_65
  |            AFTER INSERT ON ClazzMember
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.clazzMemberMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ClazzMember
  |                SET clazzMemberMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 65)
  |                WHERE clazzMemberUid = NEW.clazzMemberUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 65;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 65, NEW.clazzMemberUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_65
  |AFTER UPDATE ON ClazzMember
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.clazzMemberLocalChangeSeqNum == OLD.clazzMemberLocalChangeSeqNum OR
  |        NEW.clazzMemberLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ClazzMember
  |    SET clazzMemberLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 65) 
  |    WHERE clazzMemberUid = NEW.clazzMemberUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 65;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_65
  |            AFTER UPDATE ON ClazzMember
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.clazzMemberMasterChangeSeqNum == OLD.clazzMemberMasterChangeSeqNum OR
  |                    NEW.clazzMemberMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ClazzMember
  |                SET clazzMemberMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 65)
  |                WHERE clazzMemberUid = NEW.clazzMemberUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 65;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 65, NEW.clazzMemberUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(65, (SELECT COALESCE((SELECT MAX(clazzMemberLocalChangeSeqNum) FROM ClazzMember), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(65, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ClazzMember_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzMember_trk_epk_clientId_tmp ON ClazzMember_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzMember_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzMember_trk_nest.pk FROM ClazzMember_trk ClazzMember_trk_nest 
  |  WHERE ClazzMember_trk_nest.clientId = ClazzMember_trk.clientId AND
  |  ClazzMember_trk_nest.epk = ClazzMember_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzMember_trk_clientId_epk_csn  ON ClazzMember_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzMember_trk_epk_clientId ON ClazzMember_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzMember_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_178")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_178")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_178
  |AFTER INSERT ON PersonCustomFieldValue
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.personCustomFieldValueLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE PersonCustomFieldValue
  |    SET personCustomFieldValueMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 178)
  |    WHERE personCustomFieldValueUid = NEW.personCustomFieldValueUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 178;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_178
  |            AFTER INSERT ON PersonCustomFieldValue
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.personCustomFieldValueMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE PersonCustomFieldValue
  |                SET personCustomFieldValueMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 178)
  |                WHERE personCustomFieldValueUid = NEW.personCustomFieldValueUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 178;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 178, NEW.personCustomFieldValueUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_178
  |AFTER UPDATE ON PersonCustomFieldValue
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.personCustomFieldValueLocalChangeSeqNum == OLD.personCustomFieldValueLocalChangeSeqNum OR
  |        NEW.personCustomFieldValueLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE PersonCustomFieldValue
  |    SET personCustomFieldValueLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 178) 
  |    WHERE personCustomFieldValueUid = NEW.personCustomFieldValueUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 178;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_178
  |            AFTER UPDATE ON PersonCustomFieldValue
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.personCustomFieldValueMasterChangeSeqNum == OLD.personCustomFieldValueMasterChangeSeqNum OR
  |                    NEW.personCustomFieldValueMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE PersonCustomFieldValue
  |                SET personCustomFieldValueMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 178)
  |                WHERE personCustomFieldValueUid = NEW.personCustomFieldValueUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 178;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 178, NEW.personCustomFieldValueUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(178, (SELECT COALESCE((SELECT MAX(personCustomFieldValueLocalChangeSeqNum) FROM PersonCustomFieldValue), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(178, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_PersonCustomFieldValue_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_PersonCustomFieldValue_trk_epk_clientId_tmp ON PersonCustomFieldValue_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM PersonCustomFieldValue_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT PersonCustomFieldValue_trk_nest.pk FROM PersonCustomFieldValue_trk PersonCustomFieldValue_trk_nest 
  |  WHERE PersonCustomFieldValue_trk_nest.clientId = PersonCustomFieldValue_trk.clientId AND
  |  PersonCustomFieldValue_trk_nest.epk = PersonCustomFieldValue_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_PersonCustomFieldValue_trk_clientId_epk_csn  ON PersonCustomFieldValue_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_PersonCustomFieldValue_trk_epk_clientId ON PersonCustomFieldValue_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_PersonCustomFieldValue_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_42")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_42")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_42
  |AFTER INSERT ON ContentEntry
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.contentEntryLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ContentEntry
  |    SET contentEntryMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 42)
  |    WHERE contentEntryUid = NEW.contentEntryUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 42;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_42
  |            AFTER INSERT ON ContentEntry
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.contentEntryMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ContentEntry
  |                SET contentEntryMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 42)
  |                WHERE contentEntryUid = NEW.contentEntryUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 42;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 42, NEW.contentEntryUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_42
  |AFTER UPDATE ON ContentEntry
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.contentEntryLocalChangeSeqNum == OLD.contentEntryLocalChangeSeqNum OR
  |        NEW.contentEntryLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ContentEntry
  |    SET contentEntryLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 42) 
  |    WHERE contentEntryUid = NEW.contentEntryUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 42;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_42
  |            AFTER UPDATE ON ContentEntry
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.contentEntryMasterChangeSeqNum == OLD.contentEntryMasterChangeSeqNum OR
  |                    NEW.contentEntryMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ContentEntry
  |                SET contentEntryMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 42)
  |                WHERE contentEntryUid = NEW.contentEntryUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 42;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 42, NEW.contentEntryUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(42, (SELECT COALESCE((SELECT MAX(contentEntryLocalChangeSeqNum) FROM ContentEntry), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(42, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ContentEntry_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentEntry_trk_epk_clientId_tmp ON ContentEntry_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentEntry_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentEntry_trk_nest.pk FROM ContentEntry_trk ContentEntry_trk_nest 
  |  WHERE ContentEntry_trk_nest.clientId = ContentEntry_trk.clientId AND
  |  ContentEntry_trk_nest.epk = ContentEntry_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentEntry_trk_clientId_epk_csn  ON ContentEntry_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentEntry_trk_epk_clientId ON ContentEntry_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentEntry_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_3")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_3")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_3
  |AFTER INSERT ON ContentEntryContentCategoryJoin
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.ceccjLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ContentEntryContentCategoryJoin
  |    SET ceccjMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 3)
  |    WHERE ceccjUid = NEW.ceccjUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 3;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_3
  |            AFTER INSERT ON ContentEntryContentCategoryJoin
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.ceccjMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ContentEntryContentCategoryJoin
  |                SET ceccjMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 3)
  |                WHERE ceccjUid = NEW.ceccjUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 3;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 3, NEW.ceccjUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_3
  |AFTER UPDATE ON ContentEntryContentCategoryJoin
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.ceccjLocalChangeSeqNum == OLD.ceccjLocalChangeSeqNum OR
  |        NEW.ceccjLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ContentEntryContentCategoryJoin
  |    SET ceccjLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 3) 
  |    WHERE ceccjUid = NEW.ceccjUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 3;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_3
  |            AFTER UPDATE ON ContentEntryContentCategoryJoin
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.ceccjMasterChangeSeqNum == OLD.ceccjMasterChangeSeqNum OR
  |                    NEW.ceccjMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ContentEntryContentCategoryJoin
  |                SET ceccjMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 3)
  |                WHERE ceccjUid = NEW.ceccjUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 3;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 3, NEW.ceccjUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(3, (SELECT COALESCE((SELECT MAX(ceccjLocalChangeSeqNum) FROM ContentEntryContentCategoryJoin), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(3, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ContentEntryContentCategoryJoin_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentEntryContentCategoryJoin_trk_epk_clientId_tmp ON ContentEntryContentCategoryJoin_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentEntryContentCategoryJoin_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentEntryContentCategoryJoin_trk_nest.pk FROM ContentEntryContentCategoryJoin_trk ContentEntryContentCategoryJoin_trk_nest 
  |  WHERE ContentEntryContentCategoryJoin_trk_nest.clientId = ContentEntryContentCategoryJoin_trk.clientId AND
  |  ContentEntryContentCategoryJoin_trk_nest.epk = ContentEntryContentCategoryJoin_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentEntryContentCategoryJoin_trk_clientId_epk_csn  ON ContentEntryContentCategoryJoin_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentEntryContentCategoryJoin_trk_epk_clientId ON ContentEntryContentCategoryJoin_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentEntryContentCategoryJoin_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_7")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_7")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_7
  |AFTER INSERT ON ContentEntryParentChildJoin
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.cepcjLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ContentEntryParentChildJoin
  |    SET cepcjMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 7)
  |    WHERE cepcjUid = NEW.cepcjUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 7;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_7
  |            AFTER INSERT ON ContentEntryParentChildJoin
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.cepcjMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ContentEntryParentChildJoin
  |                SET cepcjMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 7)
  |                WHERE cepcjUid = NEW.cepcjUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 7;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 7, NEW.cepcjUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_7
  |AFTER UPDATE ON ContentEntryParentChildJoin
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.cepcjLocalChangeSeqNum == OLD.cepcjLocalChangeSeqNum OR
  |        NEW.cepcjLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ContentEntryParentChildJoin
  |    SET cepcjLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 7) 
  |    WHERE cepcjUid = NEW.cepcjUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 7;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_7
  |            AFTER UPDATE ON ContentEntryParentChildJoin
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.cepcjMasterChangeSeqNum == OLD.cepcjMasterChangeSeqNum OR
  |                    NEW.cepcjMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ContentEntryParentChildJoin
  |                SET cepcjMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 7)
  |                WHERE cepcjUid = NEW.cepcjUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 7;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 7, NEW.cepcjUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(7, (SELECT COALESCE((SELECT MAX(cepcjLocalChangeSeqNum) FROM ContentEntryParentChildJoin), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(7, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ContentEntryParentChildJoin_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentEntryParentChildJoin_trk_epk_clientId_tmp ON ContentEntryParentChildJoin_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentEntryParentChildJoin_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentEntryParentChildJoin_trk_nest.pk FROM ContentEntryParentChildJoin_trk ContentEntryParentChildJoin_trk_nest 
  |  WHERE ContentEntryParentChildJoin_trk_nest.clientId = ContentEntryParentChildJoin_trk.clientId AND
  |  ContentEntryParentChildJoin_trk_nest.epk = ContentEntryParentChildJoin_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentEntryParentChildJoin_trk_clientId_epk_csn  ON ContentEntryParentChildJoin_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentEntryParentChildJoin_trk_epk_clientId ON ContentEntryParentChildJoin_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentEntryParentChildJoin_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_8")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_8")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_8
  |AFTER INSERT ON ContentEntryRelatedEntryJoin
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.cerejLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ContentEntryRelatedEntryJoin
  |    SET cerejMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 8)
  |    WHERE cerejUid = NEW.cerejUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 8;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_8
  |            AFTER INSERT ON ContentEntryRelatedEntryJoin
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.cerejMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ContentEntryRelatedEntryJoin
  |                SET cerejMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 8)
  |                WHERE cerejUid = NEW.cerejUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 8;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 8, NEW.cerejUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_8
  |AFTER UPDATE ON ContentEntryRelatedEntryJoin
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.cerejLocalChangeSeqNum == OLD.cerejLocalChangeSeqNum OR
  |        NEW.cerejLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ContentEntryRelatedEntryJoin
  |    SET cerejLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 8) 
  |    WHERE cerejUid = NEW.cerejUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 8;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_8
  |            AFTER UPDATE ON ContentEntryRelatedEntryJoin
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.cerejMasterChangeSeqNum == OLD.cerejMasterChangeSeqNum OR
  |                    NEW.cerejMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ContentEntryRelatedEntryJoin
  |                SET cerejMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 8)
  |                WHERE cerejUid = NEW.cerejUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 8;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 8, NEW.cerejUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(8, (SELECT COALESCE((SELECT MAX(cerejLocalChangeSeqNum) FROM ContentEntryRelatedEntryJoin), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(8, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ContentEntryRelatedEntryJoin_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentEntryRelatedEntryJoin_trk_epk_clientId_tmp ON ContentEntryRelatedEntryJoin_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentEntryRelatedEntryJoin_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentEntryRelatedEntryJoin_trk_nest.pk FROM ContentEntryRelatedEntryJoin_trk ContentEntryRelatedEntryJoin_trk_nest 
  |  WHERE ContentEntryRelatedEntryJoin_trk_nest.clientId = ContentEntryRelatedEntryJoin_trk.clientId AND
  |  ContentEntryRelatedEntryJoin_trk_nest.epk = ContentEntryRelatedEntryJoin_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentEntryRelatedEntryJoin_trk_clientId_epk_csn  ON ContentEntryRelatedEntryJoin_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentEntryRelatedEntryJoin_trk_epk_clientId ON ContentEntryRelatedEntryJoin_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentEntryRelatedEntryJoin_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_2")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_2")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_2
  |AFTER INSERT ON ContentCategorySchema
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.contentCategorySchemaLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ContentCategorySchema
  |    SET contentCategorySchemaMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 2)
  |    WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 2;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_2
  |            AFTER INSERT ON ContentCategorySchema
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.contentCategorySchemaMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ContentCategorySchema
  |                SET contentCategorySchemaMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 2)
  |                WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 2;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 2, NEW.contentCategorySchemaUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_2
  |AFTER UPDATE ON ContentCategorySchema
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.contentCategorySchemaLocalChangeSeqNum == OLD.contentCategorySchemaLocalChangeSeqNum OR
  |        NEW.contentCategorySchemaLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ContentCategorySchema
  |    SET contentCategorySchemaLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 2) 
  |    WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 2;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_2
  |            AFTER UPDATE ON ContentCategorySchema
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.contentCategorySchemaMasterChangeSeqNum == OLD.contentCategorySchemaMasterChangeSeqNum OR
  |                    NEW.contentCategorySchemaMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ContentCategorySchema
  |                SET contentCategorySchemaMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 2)
  |                WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 2;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 2, NEW.contentCategorySchemaUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(2, (SELECT COALESCE((SELECT MAX(contentCategorySchemaLocalChangeSeqNum) FROM ContentCategorySchema), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(2, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ContentCategorySchema_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentCategorySchema_trk_epk_clientId_tmp ON ContentCategorySchema_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentCategorySchema_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentCategorySchema_trk_nest.pk FROM ContentCategorySchema_trk ContentCategorySchema_trk_nest 
  |  WHERE ContentCategorySchema_trk_nest.clientId = ContentCategorySchema_trk.clientId AND
  |  ContentCategorySchema_trk_nest.epk = ContentCategorySchema_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentCategorySchema_trk_clientId_epk_csn  ON ContentCategorySchema_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentCategorySchema_trk_epk_clientId ON ContentCategorySchema_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentCategorySchema_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_1")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_1")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_1
  |AFTER INSERT ON ContentCategory
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.contentCategoryLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ContentCategory
  |    SET contentCategoryMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 1)
  |    WHERE contentCategoryUid = NEW.contentCategoryUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 1;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_1
  |            AFTER INSERT ON ContentCategory
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.contentCategoryMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ContentCategory
  |                SET contentCategoryMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 1)
  |                WHERE contentCategoryUid = NEW.contentCategoryUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 1;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 1, NEW.contentCategoryUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_1
  |AFTER UPDATE ON ContentCategory
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.contentCategoryLocalChangeSeqNum == OLD.contentCategoryLocalChangeSeqNum OR
  |        NEW.contentCategoryLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ContentCategory
  |    SET contentCategoryLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 1) 
  |    WHERE contentCategoryUid = NEW.contentCategoryUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 1;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_1
  |            AFTER UPDATE ON ContentCategory
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.contentCategoryMasterChangeSeqNum == OLD.contentCategoryMasterChangeSeqNum OR
  |                    NEW.contentCategoryMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ContentCategory
  |                SET contentCategoryMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 1)
  |                WHERE contentCategoryUid = NEW.contentCategoryUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 1;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 1, NEW.contentCategoryUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(1, (SELECT COALESCE((SELECT MAX(contentCategoryLocalChangeSeqNum) FROM ContentCategory), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(1, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ContentCategory_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentCategory_trk_epk_clientId_tmp ON ContentCategory_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentCategory_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentCategory_trk_nest.pk FROM ContentCategory_trk ContentCategory_trk_nest 
  |  WHERE ContentCategory_trk_nest.clientId = ContentCategory_trk.clientId AND
  |  ContentCategory_trk_nest.epk = ContentCategory_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentCategory_trk_clientId_epk_csn  ON ContentCategory_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentCategory_trk_epk_clientId ON ContentCategory_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentCategory_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_13")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_13")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_13
  |AFTER INSERT ON Language
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.langLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE Language
  |    SET langMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 13)
  |    WHERE langUid = NEW.langUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 13;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_13
  |            AFTER INSERT ON Language
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.langMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE Language
  |                SET langMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 13)
  |                WHERE langUid = NEW.langUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 13;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 13, NEW.langUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_13
  |AFTER UPDATE ON Language
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.langLocalChangeSeqNum == OLD.langLocalChangeSeqNum OR
  |        NEW.langLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE Language
  |    SET langLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 13) 
  |    WHERE langUid = NEW.langUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 13;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_13
  |            AFTER UPDATE ON Language
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.langMasterChangeSeqNum == OLD.langMasterChangeSeqNum OR
  |                    NEW.langMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE Language
  |                SET langMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 13)
  |                WHERE langUid = NEW.langUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 13;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 13, NEW.langUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(13, (SELECT COALESCE((SELECT MAX(langLocalChangeSeqNum) FROM Language), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(13, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_Language_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Language_trk_epk_clientId_tmp ON Language_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Language_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Language_trk_nest.pk FROM Language_trk Language_trk_nest 
  |  WHERE Language_trk_nest.clientId = Language_trk.clientId AND
  |  Language_trk_nest.epk = Language_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Language_trk_clientId_epk_csn  ON Language_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Language_trk_epk_clientId ON Language_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Language_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_10")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_10")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_10
  |AFTER INSERT ON LanguageVariant
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.langVariantLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE LanguageVariant
  |    SET langVariantMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 10)
  |    WHERE langVariantUid = NEW.langVariantUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 10;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_10
  |            AFTER INSERT ON LanguageVariant
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.langVariantMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE LanguageVariant
  |                SET langVariantMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 10)
  |                WHERE langVariantUid = NEW.langVariantUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 10;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 10, NEW.langVariantUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_10
  |AFTER UPDATE ON LanguageVariant
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.langVariantLocalChangeSeqNum == OLD.langVariantLocalChangeSeqNum OR
  |        NEW.langVariantLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE LanguageVariant
  |    SET langVariantLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 10) 
  |    WHERE langVariantUid = NEW.langVariantUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 10;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_10
  |            AFTER UPDATE ON LanguageVariant
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.langVariantMasterChangeSeqNum == OLD.langVariantMasterChangeSeqNum OR
  |                    NEW.langVariantMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE LanguageVariant
  |                SET langVariantMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 10)
  |                WHERE langVariantUid = NEW.langVariantUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 10;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 10, NEW.langVariantUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(10, (SELECT COALESCE((SELECT MAX(langVariantLocalChangeSeqNum) FROM LanguageVariant), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(10, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_LanguageVariant_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_LanguageVariant_trk_epk_clientId_tmp ON LanguageVariant_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM LanguageVariant_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT LanguageVariant_trk_nest.pk FROM LanguageVariant_trk LanguageVariant_trk_nest 
  |  WHERE LanguageVariant_trk_nest.clientId = LanguageVariant_trk.clientId AND
  |  LanguageVariant_trk_nest.epk = LanguageVariant_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_LanguageVariant_trk_clientId_epk_csn  ON LanguageVariant_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_LanguageVariant_trk_epk_clientId ON LanguageVariant_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_LanguageVariant_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_45")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_45")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_45
  |AFTER INSERT ON Role
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.roleLocalCsn = 0)
  |BEGIN
  |    UPDATE Role
  |    SET roleMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 45)
  |    WHERE roleUid = NEW.roleUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 45;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_45
  |            AFTER INSERT ON Role
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.roleMasterCsn = 0)
  |            BEGIN
  |                UPDATE Role
  |                SET roleMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 45)
  |                WHERE roleUid = NEW.roleUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 45;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 45, NEW.roleUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_45
  |AFTER UPDATE ON Role
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.roleLocalCsn == OLD.roleLocalCsn OR
  |        NEW.roleLocalCsn == 0))
  |BEGIN
  |    UPDATE Role
  |    SET roleLocalCsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 45) 
  |    WHERE roleUid = NEW.roleUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 45;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_45
  |            AFTER UPDATE ON Role
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.roleMasterCsn == OLD.roleMasterCsn OR
  |                    NEW.roleMasterCsn == 0))
  |            BEGIN
  |                UPDATE Role
  |                SET roleMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 45)
  |                WHERE roleUid = NEW.roleUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 45;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 45, NEW.roleUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(45, (SELECT COALESCE((SELECT MAX(roleLocalCsn) FROM Role), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(45, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_Role_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Role_trk_epk_clientId_tmp ON Role_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Role_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Role_trk_nest.pk FROM Role_trk Role_trk_nest 
  |  WHERE Role_trk_nest.clientId = Role_trk.clientId AND
  |  Role_trk_nest.epk = Role_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Role_trk_clientId_epk_csn  ON Role_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Role_trk_epk_clientId ON Role_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Role_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_47")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_47")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_47
  |AFTER INSERT ON EntityRole
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.erLocalCsn = 0)
  |BEGIN
  |    UPDATE EntityRole
  |    SET erMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 47)
  |    WHERE erUid = NEW.erUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 47;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_47
  |            AFTER INSERT ON EntityRole
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.erMasterCsn = 0)
  |            BEGIN
  |                UPDATE EntityRole
  |                SET erMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 47)
  |                WHERE erUid = NEW.erUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 47;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 47, NEW.erUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_47
  |AFTER UPDATE ON EntityRole
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.erLocalCsn == OLD.erLocalCsn OR
  |        NEW.erLocalCsn == 0))
  |BEGIN
  |    UPDATE EntityRole
  |    SET erLocalCsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 47) 
  |    WHERE erUid = NEW.erUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 47;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_47
  |            AFTER UPDATE ON EntityRole
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.erMasterCsn == OLD.erMasterCsn OR
  |                    NEW.erMasterCsn == 0))
  |            BEGIN
  |                UPDATE EntityRole
  |                SET erMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 47)
  |                WHERE erUid = NEW.erUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 47;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 47, NEW.erUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(47, (SELECT COALESCE((SELECT MAX(erLocalCsn) FROM EntityRole), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(47, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_EntityRole_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_EntityRole_trk_epk_clientId_tmp ON EntityRole_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM EntityRole_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT EntityRole_trk_nest.pk FROM EntityRole_trk EntityRole_trk_nest 
  |  WHERE EntityRole_trk_nest.clientId = EntityRole_trk.clientId AND
  |  EntityRole_trk_nest.epk = EntityRole_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_EntityRole_trk_clientId_epk_csn  ON EntityRole_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_EntityRole_trk_epk_clientId ON EntityRole_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_EntityRole_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_43")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_43")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_43
  |AFTER INSERT ON PersonGroup
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.groupLocalCsn = 0)
  |BEGIN
  |    UPDATE PersonGroup
  |    SET groupMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 43)
  |    WHERE groupUid = NEW.groupUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 43;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_43
  |            AFTER INSERT ON PersonGroup
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.groupMasterCsn = 0)
  |            BEGIN
  |                UPDATE PersonGroup
  |                SET groupMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 43)
  |                WHERE groupUid = NEW.groupUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 43;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 43, NEW.groupUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_43
  |AFTER UPDATE ON PersonGroup
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.groupLocalCsn == OLD.groupLocalCsn OR
  |        NEW.groupLocalCsn == 0))
  |BEGIN
  |    UPDATE PersonGroup
  |    SET groupLocalCsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 43) 
  |    WHERE groupUid = NEW.groupUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 43;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_43
  |            AFTER UPDATE ON PersonGroup
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.groupMasterCsn == OLD.groupMasterCsn OR
  |                    NEW.groupMasterCsn == 0))
  |            BEGIN
  |                UPDATE PersonGroup
  |                SET groupMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 43)
  |                WHERE groupUid = NEW.groupUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 43;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 43, NEW.groupUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(43, (SELECT COALESCE((SELECT MAX(groupLocalCsn) FROM PersonGroup), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(43, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_PersonGroup_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_PersonGroup_trk_epk_clientId_tmp ON PersonGroup_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM PersonGroup_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT PersonGroup_trk_nest.pk FROM PersonGroup_trk PersonGroup_trk_nest 
  |  WHERE PersonGroup_trk_nest.clientId = PersonGroup_trk.clientId AND
  |  PersonGroup_trk_nest.epk = PersonGroup_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_PersonGroup_trk_clientId_epk_csn  ON PersonGroup_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_PersonGroup_trk_epk_clientId ON PersonGroup_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_PersonGroup_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_44")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_44")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_44
  |AFTER INSERT ON PersonGroupMember
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.groupMemberLocalCsn = 0)
  |BEGIN
  |    UPDATE PersonGroupMember
  |    SET groupMemberMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 44)
  |    WHERE groupMemberUid = NEW.groupMemberUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 44;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_44
  |            AFTER INSERT ON PersonGroupMember
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.groupMemberMasterCsn = 0)
  |            BEGIN
  |                UPDATE PersonGroupMember
  |                SET groupMemberMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 44)
  |                WHERE groupMemberUid = NEW.groupMemberUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 44;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 44, NEW.groupMemberUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_44
  |AFTER UPDATE ON PersonGroupMember
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.groupMemberLocalCsn == OLD.groupMemberLocalCsn OR
  |        NEW.groupMemberLocalCsn == 0))
  |BEGIN
  |    UPDATE PersonGroupMember
  |    SET groupMemberLocalCsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 44) 
  |    WHERE groupMemberUid = NEW.groupMemberUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 44;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_44
  |            AFTER UPDATE ON PersonGroupMember
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.groupMemberMasterCsn == OLD.groupMemberMasterCsn OR
  |                    NEW.groupMemberMasterCsn == 0))
  |            BEGIN
  |                UPDATE PersonGroupMember
  |                SET groupMemberMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 44)
  |                WHERE groupMemberUid = NEW.groupMemberUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 44;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 44, NEW.groupMemberUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(44, (SELECT COALESCE((SELECT MAX(groupMemberLocalCsn) FROM PersonGroupMember), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(44, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_PersonGroupMember_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_PersonGroupMember_trk_epk_clientId_tmp ON PersonGroupMember_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM PersonGroupMember_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT PersonGroupMember_trk_nest.pk FROM PersonGroupMember_trk PersonGroupMember_trk_nest 
  |  WHERE PersonGroupMember_trk_nest.clientId = PersonGroupMember_trk.clientId AND
  |  PersonGroupMember_trk_nest.epk = PersonGroupMember_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_PersonGroupMember_trk_clientId_epk_csn  ON PersonGroupMember_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_PersonGroupMember_trk_epk_clientId ON PersonGroupMember_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_PersonGroupMember_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_50")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_50")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_50
  |AFTER INSERT ON PersonPicture
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.personPictureLocalCsn = 0)
  |BEGIN
  |    UPDATE PersonPicture
  |    SET personPictureMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 50)
  |    WHERE personPictureUid = NEW.personPictureUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 50;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_50
  |            AFTER INSERT ON PersonPicture
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.personPictureMasterCsn = 0)
  |            BEGIN
  |                UPDATE PersonPicture
  |                SET personPictureMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 50)
  |                WHERE personPictureUid = NEW.personPictureUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 50;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 50, NEW.personPictureUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_50
  |AFTER UPDATE ON PersonPicture
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.personPictureLocalCsn == OLD.personPictureLocalCsn OR
  |        NEW.personPictureLocalCsn == 0))
  |BEGIN
  |    UPDATE PersonPicture
  |    SET personPictureLocalCsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 50) 
  |    WHERE personPictureUid = NEW.personPictureUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 50;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_50
  |            AFTER UPDATE ON PersonPicture
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.personPictureMasterCsn == OLD.personPictureMasterCsn OR
  |                    NEW.personPictureMasterCsn == 0))
  |            BEGIN
  |                UPDATE PersonPicture
  |                SET personPictureMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 50)
  |                WHERE personPictureUid = NEW.personPictureUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 50;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 50, NEW.personPictureUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(50, (SELECT COALESCE((SELECT MAX(personPictureLocalCsn) FROM PersonPicture), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(50, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_PersonPicture_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_PersonPicture_trk_epk_clientId_tmp ON PersonPicture_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM PersonPicture_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT PersonPicture_trk_nest.pk FROM PersonPicture_trk PersonPicture_trk_nest 
  |  WHERE PersonPicture_trk_nest.clientId = PersonPicture_trk.clientId AND
  |  PersonPicture_trk_nest.epk = PersonPicture_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_PersonPicture_trk_clientId_epk_csn  ON PersonPicture_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_PersonPicture_trk_epk_clientId ON PersonPicture_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_PersonPicture_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_51")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_51")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_51
  |AFTER INSERT ON Container
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.cntLocalCsn = 0)
  |BEGIN
  |    UPDATE Container
  |    SET cntMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 51)
  |    WHERE containerUid = NEW.containerUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 51;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_51
  |            AFTER INSERT ON Container
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.cntMasterCsn = 0)
  |            BEGIN
  |                UPDATE Container
  |                SET cntMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 51)
  |                WHERE containerUid = NEW.containerUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 51;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 51, NEW.containerUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_51
  |AFTER UPDATE ON Container
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.cntLocalCsn == OLD.cntLocalCsn OR
  |        NEW.cntLocalCsn == 0))
  |BEGIN
  |    UPDATE Container
  |    SET cntLocalCsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 51) 
  |    WHERE containerUid = NEW.containerUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 51;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_51
  |            AFTER UPDATE ON Container
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.cntMasterCsn == OLD.cntMasterCsn OR
  |                    NEW.cntMasterCsn == 0))
  |            BEGIN
  |                UPDATE Container
  |                SET cntMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 51)
  |                WHERE containerUid = NEW.containerUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 51;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 51, NEW.containerUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(51, (SELECT COALESCE((SELECT MAX(cntLocalCsn) FROM Container), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(51, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_Container_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Container_trk_epk_clientId_tmp ON Container_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Container_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Container_trk_nest.pk FROM Container_trk Container_trk_nest 
  |  WHERE Container_trk_nest.clientId = Container_trk.clientId AND
  |  Container_trk_nest.epk = Container_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Container_trk_clientId_epk_csn  ON Container_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Container_trk_epk_clientId ON Container_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Container_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_62")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_62")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_62
  |AFTER INSERT ON VerbEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.verbLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE VerbEntity
  |    SET verbMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 62)
  |    WHERE verbUid = NEW.verbUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 62;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_62
  |            AFTER INSERT ON VerbEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.verbMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE VerbEntity
  |                SET verbMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 62)
  |                WHERE verbUid = NEW.verbUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 62;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 62, NEW.verbUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_62
  |AFTER UPDATE ON VerbEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.verbLocalChangeSeqNum == OLD.verbLocalChangeSeqNum OR
  |        NEW.verbLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE VerbEntity
  |    SET verbLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 62) 
  |    WHERE verbUid = NEW.verbUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 62;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_62
  |            AFTER UPDATE ON VerbEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.verbMasterChangeSeqNum == OLD.verbMasterChangeSeqNum OR
  |                    NEW.verbMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE VerbEntity
  |                SET verbMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 62)
  |                WHERE verbUid = NEW.verbUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 62;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 62, NEW.verbUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(62, (SELECT COALESCE((SELECT MAX(verbLocalChangeSeqNum) FROM VerbEntity), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(62, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_VerbEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_VerbEntity_trk_epk_clientId_tmp ON VerbEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM VerbEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT VerbEntity_trk_nest.pk FROM VerbEntity_trk VerbEntity_trk_nest 
  |  WHERE VerbEntity_trk_nest.clientId = VerbEntity_trk.clientId AND
  |  VerbEntity_trk_nest.epk = VerbEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_VerbEntity_trk_clientId_epk_csn  ON VerbEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_VerbEntity_trk_epk_clientId ON VerbEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_VerbEntity_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_64")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_64")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_64
  |AFTER INSERT ON XObjectEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.xObjectocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE XObjectEntity
  |    SET xObjectMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 64)
  |    WHERE xObjectUid = NEW.xObjectUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 64;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_64
  |            AFTER INSERT ON XObjectEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.xObjectMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE XObjectEntity
  |                SET xObjectMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 64)
  |                WHERE xObjectUid = NEW.xObjectUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 64;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 64, NEW.xObjectUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_64
  |AFTER UPDATE ON XObjectEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.xObjectocalChangeSeqNum == OLD.xObjectocalChangeSeqNum OR
  |        NEW.xObjectocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE XObjectEntity
  |    SET xObjectocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 64) 
  |    WHERE xObjectUid = NEW.xObjectUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 64;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_64
  |            AFTER UPDATE ON XObjectEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.xObjectMasterChangeSeqNum == OLD.xObjectMasterChangeSeqNum OR
  |                    NEW.xObjectMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE XObjectEntity
  |                SET xObjectMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 64)
  |                WHERE xObjectUid = NEW.xObjectUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 64;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 64, NEW.xObjectUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(64, (SELECT COALESCE((SELECT MAX(xObjectocalChangeSeqNum) FROM XObjectEntity), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(64, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_XObjectEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_XObjectEntity_trk_epk_clientId_tmp ON XObjectEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM XObjectEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT XObjectEntity_trk_nest.pk FROM XObjectEntity_trk XObjectEntity_trk_nest 
  |  WHERE XObjectEntity_trk_nest.clientId = XObjectEntity_trk.clientId AND
  |  XObjectEntity_trk_nest.epk = XObjectEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_XObjectEntity_trk_clientId_epk_csn  ON XObjectEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_XObjectEntity_trk_epk_clientId ON XObjectEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_XObjectEntity_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_60")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_60")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_60
  |AFTER INSERT ON StatementEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.statementLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE StatementEntity
  |    SET statementMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 60)
  |    WHERE statementUid = NEW.statementUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 60;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_60
  |            AFTER INSERT ON StatementEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.statementMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE StatementEntity
  |                SET statementMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 60)
  |                WHERE statementUid = NEW.statementUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 60;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 60, NEW.statementUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_60
  |AFTER UPDATE ON StatementEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.statementLocalChangeSeqNum == OLD.statementLocalChangeSeqNum OR
  |        NEW.statementLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE StatementEntity
  |    SET statementLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 60) 
  |    WHERE statementUid = NEW.statementUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 60;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_60
  |            AFTER UPDATE ON StatementEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.statementMasterChangeSeqNum == OLD.statementMasterChangeSeqNum OR
  |                    NEW.statementMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE StatementEntity
  |                SET statementMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 60)
  |                WHERE statementUid = NEW.statementUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 60;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 60, NEW.statementUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(60, (SELECT COALESCE((SELECT MAX(statementLocalChangeSeqNum) FROM StatementEntity), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(60, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_StatementEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_StatementEntity_trk_epk_clientId_tmp ON StatementEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM StatementEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT StatementEntity_trk_nest.pk FROM StatementEntity_trk StatementEntity_trk_nest 
  |  WHERE StatementEntity_trk_nest.clientId = StatementEntity_trk.clientId AND
  |  StatementEntity_trk_nest.epk = StatementEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_StatementEntity_trk_clientId_epk_csn  ON StatementEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_StatementEntity_trk_epk_clientId ON StatementEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_StatementEntity_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_66")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_66")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_66
  |AFTER INSERT ON ContextXObjectStatementJoin
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.verbLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ContextXObjectStatementJoin
  |    SET verbMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 66)
  |    WHERE contextXObjectStatementJoinUid = NEW.contextXObjectStatementJoinUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 66;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_66
  |            AFTER INSERT ON ContextXObjectStatementJoin
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.verbMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ContextXObjectStatementJoin
  |                SET verbMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 66)
  |                WHERE contextXObjectStatementJoinUid = NEW.contextXObjectStatementJoinUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 66;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 66, NEW.contextXObjectStatementJoinUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_66
  |AFTER UPDATE ON ContextXObjectStatementJoin
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.verbLocalChangeSeqNum == OLD.verbLocalChangeSeqNum OR
  |        NEW.verbLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ContextXObjectStatementJoin
  |    SET verbLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 66) 
  |    WHERE contextXObjectStatementJoinUid = NEW.contextXObjectStatementJoinUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 66;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_66
  |            AFTER UPDATE ON ContextXObjectStatementJoin
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.verbMasterChangeSeqNum == OLD.verbMasterChangeSeqNum OR
  |                    NEW.verbMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ContextXObjectStatementJoin
  |                SET verbMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 66)
  |                WHERE contextXObjectStatementJoinUid = NEW.contextXObjectStatementJoinUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 66;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 66, NEW.contextXObjectStatementJoinUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(66, (SELECT COALESCE((SELECT MAX(verbLocalChangeSeqNum) FROM ContextXObjectStatementJoin), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(66, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ContextXObjectStatementJoin_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContextXObjectStatementJoin_trk_epk_clientId_tmp ON ContextXObjectStatementJoin_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContextXObjectStatementJoin_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContextXObjectStatementJoin_trk_nest.pk FROM ContextXObjectStatementJoin_trk ContextXObjectStatementJoin_trk_nest 
  |  WHERE ContextXObjectStatementJoin_trk_nest.clientId = ContextXObjectStatementJoin_trk.clientId AND
  |  ContextXObjectStatementJoin_trk_nest.epk = ContextXObjectStatementJoin_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContextXObjectStatementJoin_trk_clientId_epk_csn  ON ContextXObjectStatementJoin_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContextXObjectStatementJoin_trk_epk_clientId ON ContextXObjectStatementJoin_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContextXObjectStatementJoin_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_68")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_68")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_68
  |AFTER INSERT ON AgentEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.statementLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE AgentEntity
  |    SET statementMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 68)
  |    WHERE agentUid = NEW.agentUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 68;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_68
  |            AFTER INSERT ON AgentEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.statementMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE AgentEntity
  |                SET statementMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 68)
  |                WHERE agentUid = NEW.agentUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 68;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 68, NEW.agentUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_68
  |AFTER UPDATE ON AgentEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.statementLocalChangeSeqNum == OLD.statementLocalChangeSeqNum OR
  |        NEW.statementLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE AgentEntity
  |    SET statementLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 68) 
  |    WHERE agentUid = NEW.agentUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 68;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_68
  |            AFTER UPDATE ON AgentEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.statementMasterChangeSeqNum == OLD.statementMasterChangeSeqNum OR
  |                    NEW.statementMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE AgentEntity
  |                SET statementMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 68)
  |                WHERE agentUid = NEW.agentUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 68;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 68, NEW.agentUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(68, (SELECT COALESCE((SELECT MAX(statementLocalChangeSeqNum) FROM AgentEntity), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(68, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_AgentEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_AgentEntity_trk_epk_clientId_tmp ON AgentEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM AgentEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT AgentEntity_trk_nest.pk FROM AgentEntity_trk AgentEntity_trk_nest 
  |  WHERE AgentEntity_trk_nest.clientId = AgentEntity_trk.clientId AND
  |  AgentEntity_trk_nest.epk = AgentEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_AgentEntity_trk_clientId_epk_csn  ON AgentEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_AgentEntity_trk_epk_clientId ON AgentEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_AgentEntity_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_70")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_70")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_70
  |AFTER INSERT ON StateEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.stateLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE StateEntity
  |    SET stateMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 70)
  |    WHERE stateUid = NEW.stateUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 70;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_70
  |            AFTER INSERT ON StateEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.stateMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE StateEntity
  |                SET stateMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 70)
  |                WHERE stateUid = NEW.stateUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 70;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 70, NEW.stateUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_70
  |AFTER UPDATE ON StateEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.stateLocalChangeSeqNum == OLD.stateLocalChangeSeqNum OR
  |        NEW.stateLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE StateEntity
  |    SET stateLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 70) 
  |    WHERE stateUid = NEW.stateUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 70;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_70
  |            AFTER UPDATE ON StateEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.stateMasterChangeSeqNum == OLD.stateMasterChangeSeqNum OR
  |                    NEW.stateMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE StateEntity
  |                SET stateMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 70)
  |                WHERE stateUid = NEW.stateUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 70;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 70, NEW.stateUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(70, (SELECT COALESCE((SELECT MAX(stateLocalChangeSeqNum) FROM StateEntity), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(70, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_StateEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_StateEntity_trk_epk_clientId_tmp ON StateEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM StateEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT StateEntity_trk_nest.pk FROM StateEntity_trk StateEntity_trk_nest 
  |  WHERE StateEntity_trk_nest.clientId = StateEntity_trk.clientId AND
  |  StateEntity_trk_nest.epk = StateEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_StateEntity_trk_clientId_epk_csn  ON StateEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_StateEntity_trk_epk_clientId ON StateEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_StateEntity_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_72")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_72")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_72
  |AFTER INSERT ON StateContentEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.stateContentLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE StateContentEntity
  |    SET stateContentMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 72)
  |    WHERE stateContentUid = NEW.stateContentUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 72;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_72
  |            AFTER INSERT ON StateContentEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.stateContentMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE StateContentEntity
  |                SET stateContentMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 72)
  |                WHERE stateContentUid = NEW.stateContentUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 72;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 72, NEW.stateContentUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_72
  |AFTER UPDATE ON StateContentEntity
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.stateContentLocalChangeSeqNum == OLD.stateContentLocalChangeSeqNum OR
  |        NEW.stateContentLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE StateContentEntity
  |    SET stateContentLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 72) 
  |    WHERE stateContentUid = NEW.stateContentUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 72;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_72
  |            AFTER UPDATE ON StateContentEntity
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.stateContentMasterChangeSeqNum == OLD.stateContentMasterChangeSeqNum OR
  |                    NEW.stateContentMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE StateContentEntity
  |                SET stateContentMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 72)
  |                WHERE stateContentUid = NEW.stateContentUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 72;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 72, NEW.stateContentUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(72, (SELECT COALESCE((SELECT MAX(stateContentLocalChangeSeqNum) FROM StateContentEntity), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(72, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_StateContentEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_StateContentEntity_trk_epk_clientId_tmp ON StateContentEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM StateContentEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT StateContentEntity_trk_nest.pk FROM StateContentEntity_trk StateContentEntity_trk_nest 
  |  WHERE StateContentEntity_trk_nest.clientId = StateContentEntity_trk.clientId AND
  |  StateContentEntity_trk_nest.epk = StateContentEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_StateContentEntity_trk_clientId_epk_csn  ON StateContentEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_StateContentEntity_trk_epk_clientId ON StateContentEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_StateContentEntity_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_74")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_74")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_74
  |AFTER INSERT ON XLangMapEntry
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.statementLangMapLocalCsn = 0)
  |BEGIN
  |    UPDATE XLangMapEntry
  |    SET statementLangMapMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 74)
  |    WHERE statementLangMapUid = NEW.statementLangMapUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 74;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_74
  |            AFTER INSERT ON XLangMapEntry
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.statementLangMapMasterCsn = 0)
  |            BEGIN
  |                UPDATE XLangMapEntry
  |                SET statementLangMapMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 74)
  |                WHERE statementLangMapUid = NEW.statementLangMapUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 74;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 74, NEW.statementLangMapUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_74
  |AFTER UPDATE ON XLangMapEntry
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.statementLangMapLocalCsn == OLD.statementLangMapLocalCsn OR
  |        NEW.statementLangMapLocalCsn == 0))
  |BEGIN
  |    UPDATE XLangMapEntry
  |    SET statementLangMapLocalCsn = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 74) 
  |    WHERE statementLangMapUid = NEW.statementLangMapUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 74;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_74
  |            AFTER UPDATE ON XLangMapEntry
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.statementLangMapMasterCsn == OLD.statementLangMapMasterCsn OR
  |                    NEW.statementLangMapMasterCsn == 0))
  |            BEGIN
  |                UPDATE XLangMapEntry
  |                SET statementLangMapMasterCsn = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 74)
  |                WHERE statementLangMapUid = NEW.statementLangMapUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 74;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 74, NEW.statementLangMapUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(74, (SELECT COALESCE((SELECT MAX(statementLangMapLocalCsn) FROM XLangMapEntry), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(74, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_XLangMapEntry_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_XLangMapEntry_trk_epk_clientId_tmp ON XLangMapEntry_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM XLangMapEntry_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT XLangMapEntry_trk_nest.pk FROM XLangMapEntry_trk XLangMapEntry_trk_nest 
  |  WHERE XLangMapEntry_trk_nest.clientId = XLangMapEntry_trk.clientId AND
  |  XLangMapEntry_trk_nest.epk = XLangMapEntry_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_XLangMapEntry_trk_clientId_epk_csn  ON XLangMapEntry_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_XLangMapEntry_trk_epk_clientId ON XLangMapEntry_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_XLangMapEntry_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_164")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_164")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_164
  |AFTER INSERT ON School
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.schoolLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE School
  |    SET schoolMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 164)
  |    WHERE schoolUid = NEW.schoolUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 164;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_164
  |            AFTER INSERT ON School
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.schoolMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE School
  |                SET schoolMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 164)
  |                WHERE schoolUid = NEW.schoolUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 164;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 164, NEW.schoolUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_164
  |AFTER UPDATE ON School
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.schoolLocalChangeSeqNum == OLD.schoolLocalChangeSeqNum OR
  |        NEW.schoolLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE School
  |    SET schoolLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 164) 
  |    WHERE schoolUid = NEW.schoolUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 164;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_164
  |            AFTER UPDATE ON School
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.schoolMasterChangeSeqNum == OLD.schoolMasterChangeSeqNum OR
  |                    NEW.schoolMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE School
  |                SET schoolMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 164)
  |                WHERE schoolUid = NEW.schoolUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 164;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 164, NEW.schoolUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(164, (SELECT COALESCE((SELECT MAX(schoolLocalChangeSeqNum) FROM School), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(164, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_School_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_School_trk_epk_clientId_tmp ON School_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM School_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT School_trk_nest.pk FROM School_trk School_trk_nest 
  |  WHERE School_trk_nest.clientId = School_trk.clientId AND
  |  School_trk_nest.epk = School_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_School_trk_clientId_epk_csn  ON School_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_School_trk_epk_clientId ON School_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_School_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_200")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_200")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_200
  |AFTER INSERT ON SchoolMember
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.schoolMemberLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE SchoolMember
  |    SET schoolMemberMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 200)
  |    WHERE schoolMemberUid = NEW.schoolMemberUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 200;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_200
  |            AFTER INSERT ON SchoolMember
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.schoolMemberMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE SchoolMember
  |                SET schoolMemberMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 200)
  |                WHERE schoolMemberUid = NEW.schoolMemberUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 200;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 200, NEW.schoolMemberUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_200
  |AFTER UPDATE ON SchoolMember
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.schoolMemberLocalChangeSeqNum == OLD.schoolMemberLocalChangeSeqNum OR
  |        NEW.schoolMemberLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE SchoolMember
  |    SET schoolMemberLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 200) 
  |    WHERE schoolMemberUid = NEW.schoolMemberUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 200;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_200
  |            AFTER UPDATE ON SchoolMember
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.schoolMemberMasterChangeSeqNum == OLD.schoolMemberMasterChangeSeqNum OR
  |                    NEW.schoolMemberMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE SchoolMember
  |                SET schoolMemberMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 200)
  |                WHERE schoolMemberUid = NEW.schoolMemberUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 200;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 200, NEW.schoolMemberUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(200, (SELECT COALESCE((SELECT MAX(schoolMemberLocalChangeSeqNum) FROM SchoolMember), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(200, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_SchoolMember_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_SchoolMember_trk_epk_clientId_tmp ON SchoolMember_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM SchoolMember_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT SchoolMember_trk_nest.pk FROM SchoolMember_trk SchoolMember_trk_nest 
  |  WHERE SchoolMember_trk_nest.clientId = SchoolMember_trk.clientId AND
  |  SchoolMember_trk_nest.epk = SchoolMember_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_SchoolMember_trk_clientId_epk_csn  ON SchoolMember_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_SchoolMember_trk_epk_clientId ON SchoolMember_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_SchoolMember_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_201")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_201")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_201
  |AFTER INSERT ON ClazzWork
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.clazzWorkLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ClazzWork
  |    SET clazzWorkMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 201)
  |    WHERE clazzWorkUid = NEW.clazzWorkUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 201;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_201
  |            AFTER INSERT ON ClazzWork
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.clazzWorkMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ClazzWork
  |                SET clazzWorkMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 201)
  |                WHERE clazzWorkUid = NEW.clazzWorkUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 201;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 201, NEW.clazzWorkUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_201
  |AFTER UPDATE ON ClazzWork
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.clazzWorkLocalChangeSeqNum == OLD.clazzWorkLocalChangeSeqNum OR
  |        NEW.clazzWorkLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ClazzWork
  |    SET clazzWorkLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 201) 
  |    WHERE clazzWorkUid = NEW.clazzWorkUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 201;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_201
  |            AFTER UPDATE ON ClazzWork
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.clazzWorkMasterChangeSeqNum == OLD.clazzWorkMasterChangeSeqNum OR
  |                    NEW.clazzWorkMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ClazzWork
  |                SET clazzWorkMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 201)
  |                WHERE clazzWorkUid = NEW.clazzWorkUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 201;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 201, NEW.clazzWorkUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(201, (SELECT COALESCE((SELECT MAX(clazzWorkLocalChangeSeqNum) FROM ClazzWork), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(201, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWork_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWork_trk_epk_clientId_tmp ON ClazzWork_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWork_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWork_trk_nest.pk FROM ClazzWork_trk ClazzWork_trk_nest 
  |  WHERE ClazzWork_trk_nest.clientId = ClazzWork_trk.clientId AND
  |  ClazzWork_trk_nest.epk = ClazzWork_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWork_trk_clientId_epk_csn  ON ClazzWork_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWork_trk_epk_clientId ON ClazzWork_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWork_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_204")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_204")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_204
  |AFTER INSERT ON ClazzWorkContentJoin
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.clazzWorkContentJoinLCSN = 0)
  |BEGIN
  |    UPDATE ClazzWorkContentJoin
  |    SET clazzWorkContentJoinMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 204)
  |    WHERE clazzWorkContentJoinUid = NEW.clazzWorkContentJoinUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 204;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_204
  |            AFTER INSERT ON ClazzWorkContentJoin
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.clazzWorkContentJoinMCSN = 0)
  |            BEGIN
  |                UPDATE ClazzWorkContentJoin
  |                SET clazzWorkContentJoinMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 204)
  |                WHERE clazzWorkContentJoinUid = NEW.clazzWorkContentJoinUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 204;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 204, NEW.clazzWorkContentJoinUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_204
  |AFTER UPDATE ON ClazzWorkContentJoin
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.clazzWorkContentJoinLCSN == OLD.clazzWorkContentJoinLCSN OR
  |        NEW.clazzWorkContentJoinLCSN == 0))
  |BEGIN
  |    UPDATE ClazzWorkContentJoin
  |    SET clazzWorkContentJoinLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 204) 
  |    WHERE clazzWorkContentJoinUid = NEW.clazzWorkContentJoinUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 204;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_204
  |            AFTER UPDATE ON ClazzWorkContentJoin
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.clazzWorkContentJoinMCSN == OLD.clazzWorkContentJoinMCSN OR
  |                    NEW.clazzWorkContentJoinMCSN == 0))
  |            BEGIN
  |                UPDATE ClazzWorkContentJoin
  |                SET clazzWorkContentJoinMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 204)
  |                WHERE clazzWorkContentJoinUid = NEW.clazzWorkContentJoinUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 204;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 204, NEW.clazzWorkContentJoinUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(204, (SELECT COALESCE((SELECT MAX(clazzWorkContentJoinLCSN) FROM ClazzWorkContentJoin), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(204, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWorkContentJoin_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWorkContentJoin_trk_epk_clientId_tmp ON ClazzWorkContentJoin_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWorkContentJoin_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWorkContentJoin_trk_nest.pk FROM ClazzWorkContentJoin_trk ClazzWorkContentJoin_trk_nest 
  |  WHERE ClazzWorkContentJoin_trk_nest.clientId = ClazzWorkContentJoin_trk.clientId AND
  |  ClazzWorkContentJoin_trk_nest.epk = ClazzWorkContentJoin_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWorkContentJoin_trk_clientId_epk_csn  ON ClazzWorkContentJoin_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWorkContentJoin_trk_epk_clientId ON ClazzWorkContentJoin_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWorkContentJoin_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_208")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_208")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_208
  |AFTER INSERT ON Comments
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.commentsLCSN = 0)
  |BEGIN
  |    UPDATE Comments
  |    SET commentsMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 208)
  |    WHERE commentsUid = NEW.commentsUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 208;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_208
  |            AFTER INSERT ON Comments
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.commentsMCSN = 0)
  |            BEGIN
  |                UPDATE Comments
  |                SET commentsMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 208)
  |                WHERE commentsUid = NEW.commentsUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 208;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 208, NEW.commentsUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_208
  |AFTER UPDATE ON Comments
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.commentsLCSN == OLD.commentsLCSN OR
  |        NEW.commentsLCSN == 0))
  |BEGIN
  |    UPDATE Comments
  |    SET commentsLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 208) 
  |    WHERE commentsUid = NEW.commentsUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 208;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_208
  |            AFTER UPDATE ON Comments
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.commentsMCSN == OLD.commentsMCSN OR
  |                    NEW.commentsMCSN == 0))
  |            BEGIN
  |                UPDATE Comments
  |                SET commentsMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 208)
  |                WHERE commentsUid = NEW.commentsUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 208;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 208, NEW.commentsUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(208, (SELECT COALESCE((SELECT MAX(commentsLCSN) FROM Comments), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(208, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_Comments_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Comments_trk_epk_clientId_tmp ON Comments_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Comments_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Comments_trk_nest.pk FROM Comments_trk Comments_trk_nest 
  |  WHERE Comments_trk_nest.clientId = Comments_trk.clientId AND
  |  Comments_trk_nest.epk = Comments_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Comments_trk_clientId_epk_csn  ON Comments_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Comments_trk_epk_clientId ON Comments_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Comments_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_202")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_202")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_202
  |AFTER INSERT ON ClazzWorkQuestion
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.clazzWorkQuestionLCSN = 0)
  |BEGIN
  |    UPDATE ClazzWorkQuestion
  |    SET clazzWorkQuestionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 202)
  |    WHERE clazzWorkQuestionUid = NEW.clazzWorkQuestionUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 202;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_202
  |            AFTER INSERT ON ClazzWorkQuestion
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.clazzWorkQuestionMCSN = 0)
  |            BEGIN
  |                UPDATE ClazzWorkQuestion
  |                SET clazzWorkQuestionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 202)
  |                WHERE clazzWorkQuestionUid = NEW.clazzWorkQuestionUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 202;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 202, NEW.clazzWorkQuestionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_202
  |AFTER UPDATE ON ClazzWorkQuestion
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.clazzWorkQuestionLCSN == OLD.clazzWorkQuestionLCSN OR
  |        NEW.clazzWorkQuestionLCSN == 0))
  |BEGIN
  |    UPDATE ClazzWorkQuestion
  |    SET clazzWorkQuestionLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 202) 
  |    WHERE clazzWorkQuestionUid = NEW.clazzWorkQuestionUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 202;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_202
  |            AFTER UPDATE ON ClazzWorkQuestion
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.clazzWorkQuestionMCSN == OLD.clazzWorkQuestionMCSN OR
  |                    NEW.clazzWorkQuestionMCSN == 0))
  |            BEGIN
  |                UPDATE ClazzWorkQuestion
  |                SET clazzWorkQuestionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 202)
  |                WHERE clazzWorkQuestionUid = NEW.clazzWorkQuestionUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 202;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 202, NEW.clazzWorkQuestionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(202, (SELECT COALESCE((SELECT MAX(clazzWorkQuestionLCSN) FROM ClazzWorkQuestion), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(202, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWorkQuestion_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWorkQuestion_trk_epk_clientId_tmp ON ClazzWorkQuestion_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWorkQuestion_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWorkQuestion_trk_nest.pk FROM ClazzWorkQuestion_trk ClazzWorkQuestion_trk_nest 
  |  WHERE ClazzWorkQuestion_trk_nest.clientId = ClazzWorkQuestion_trk.clientId AND
  |  ClazzWorkQuestion_trk_nest.epk = ClazzWorkQuestion_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWorkQuestion_trk_clientId_epk_csn  ON ClazzWorkQuestion_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWorkQuestion_trk_epk_clientId ON ClazzWorkQuestion_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWorkQuestion_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_203")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_203")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_203
  |AFTER INSERT ON ClazzWorkQuestionOption
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.clazzWorkQuestionOptionLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ClazzWorkQuestionOption
  |    SET clazzWorkQuestionOptionMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 203)
  |    WHERE clazzWorkQuestionOptionUid = NEW.clazzWorkQuestionOptionUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 203;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_203
  |            AFTER INSERT ON ClazzWorkQuestionOption
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.clazzWorkQuestionOptionMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ClazzWorkQuestionOption
  |                SET clazzWorkQuestionOptionMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 203)
  |                WHERE clazzWorkQuestionOptionUid = NEW.clazzWorkQuestionOptionUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 203;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 203, NEW.clazzWorkQuestionOptionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_203
  |AFTER UPDATE ON ClazzWorkQuestionOption
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.clazzWorkQuestionOptionLocalChangeSeqNum == OLD.clazzWorkQuestionOptionLocalChangeSeqNum OR
  |        NEW.clazzWorkQuestionOptionLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ClazzWorkQuestionOption
  |    SET clazzWorkQuestionOptionLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 203) 
  |    WHERE clazzWorkQuestionOptionUid = NEW.clazzWorkQuestionOptionUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 203;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_203
  |            AFTER UPDATE ON ClazzWorkQuestionOption
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.clazzWorkQuestionOptionMasterChangeSeqNum == OLD.clazzWorkQuestionOptionMasterChangeSeqNum OR
  |                    NEW.clazzWorkQuestionOptionMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ClazzWorkQuestionOption
  |                SET clazzWorkQuestionOptionMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 203)
  |                WHERE clazzWorkQuestionOptionUid = NEW.clazzWorkQuestionOptionUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 203;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 203, NEW.clazzWorkQuestionOptionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(203, (SELECT COALESCE((SELECT MAX(clazzWorkQuestionOptionLocalChangeSeqNum) FROM ClazzWorkQuestionOption), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(203, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWorkQuestionOption_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWorkQuestionOption_trk_epk_clientId_tmp ON ClazzWorkQuestionOption_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWorkQuestionOption_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWorkQuestionOption_trk_nest.pk FROM ClazzWorkQuestionOption_trk ClazzWorkQuestionOption_trk_nest 
  |  WHERE ClazzWorkQuestionOption_trk_nest.clientId = ClazzWorkQuestionOption_trk.clientId AND
  |  ClazzWorkQuestionOption_trk_nest.epk = ClazzWorkQuestionOption_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWorkQuestionOption_trk_clientId_epk_csn  ON ClazzWorkQuestionOption_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWorkQuestionOption_trk_epk_clientId ON ClazzWorkQuestionOption_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWorkQuestionOption_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_206")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_206")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_206
  |AFTER INSERT ON ClazzWorkSubmission
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.clazzWorkSubmissionLCSN = 0)
  |BEGIN
  |    UPDATE ClazzWorkSubmission
  |    SET clazzWorkSubmissionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 206)
  |    WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 206;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_206
  |            AFTER INSERT ON ClazzWorkSubmission
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.clazzWorkSubmissionMCSN = 0)
  |            BEGIN
  |                UPDATE ClazzWorkSubmission
  |                SET clazzWorkSubmissionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 206)
  |                WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 206;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 206, NEW.clazzWorkSubmissionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_206
  |AFTER UPDATE ON ClazzWorkSubmission
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.clazzWorkSubmissionLCSN == OLD.clazzWorkSubmissionLCSN OR
  |        NEW.clazzWorkSubmissionLCSN == 0))
  |BEGIN
  |    UPDATE ClazzWorkSubmission
  |    SET clazzWorkSubmissionLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 206) 
  |    WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 206;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_206
  |            AFTER UPDATE ON ClazzWorkSubmission
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.clazzWorkSubmissionMCSN == OLD.clazzWorkSubmissionMCSN OR
  |                    NEW.clazzWorkSubmissionMCSN == 0))
  |            BEGIN
  |                UPDATE ClazzWorkSubmission
  |                SET clazzWorkSubmissionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 206)
  |                WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 206;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 206, NEW.clazzWorkSubmissionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(206, (SELECT COALESCE((SELECT MAX(clazzWorkSubmissionLCSN) FROM ClazzWorkSubmission), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(206, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWorkSubmission_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWorkSubmission_trk_epk_clientId_tmp ON ClazzWorkSubmission_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWorkSubmission_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWorkSubmission_trk_nest.pk FROM ClazzWorkSubmission_trk ClazzWorkSubmission_trk_nest 
  |  WHERE ClazzWorkSubmission_trk_nest.clientId = ClazzWorkSubmission_trk.clientId AND
  |  ClazzWorkSubmission_trk_nest.epk = ClazzWorkSubmission_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWorkSubmission_trk_clientId_epk_csn  ON ClazzWorkSubmission_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWorkSubmission_trk_epk_clientId ON ClazzWorkSubmission_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWorkSubmission_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_209")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_209")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_209
  |AFTER INSERT ON ClazzWorkQuestionResponse
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.clazzWorkQuestionResponseLCSN = 0)
  |BEGIN
  |    UPDATE ClazzWorkQuestionResponse
  |    SET clazzWorkQuestionResponseMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 209)
  |    WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 209;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_209
  |            AFTER INSERT ON ClazzWorkQuestionResponse
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.clazzWorkQuestionResponseMCSN = 0)
  |            BEGIN
  |                UPDATE ClazzWorkQuestionResponse
  |                SET clazzWorkQuestionResponseMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 209)
  |                WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 209;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 209, NEW.clazzWorkQuestionResponseUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_209
  |AFTER UPDATE ON ClazzWorkQuestionResponse
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.clazzWorkQuestionResponseLCSN == OLD.clazzWorkQuestionResponseLCSN OR
  |        NEW.clazzWorkQuestionResponseLCSN == 0))
  |BEGIN
  |    UPDATE ClazzWorkQuestionResponse
  |    SET clazzWorkQuestionResponseLCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 209) 
  |    WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 209;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_209
  |            AFTER UPDATE ON ClazzWorkQuestionResponse
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.clazzWorkQuestionResponseMCSN == OLD.clazzWorkQuestionResponseMCSN OR
  |                    NEW.clazzWorkQuestionResponseMCSN == 0))
  |            BEGIN
  |                UPDATE ClazzWorkQuestionResponse
  |                SET clazzWorkQuestionResponseMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 209)
  |                WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 209;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 209, NEW.clazzWorkQuestionResponseUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(209, (SELECT COALESCE((SELECT MAX(clazzWorkQuestionResponseLCSN) FROM ClazzWorkQuestionResponse), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(209, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWorkQuestionResponse_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWorkQuestionResponse_trk_epk_clientId_tmp ON ClazzWorkQuestionResponse_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWorkQuestionResponse_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWorkQuestionResponse_trk_nest.pk FROM ClazzWorkQuestionResponse_trk ClazzWorkQuestionResponse_trk_nest 
  |  WHERE ClazzWorkQuestionResponse_trk_nest.clientId = ClazzWorkQuestionResponse_trk.clientId AND
  |  ClazzWorkQuestionResponse_trk_nest.epk = ClazzWorkQuestionResponse_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWorkQuestionResponse_trk_clientId_epk_csn  ON ClazzWorkQuestionResponse_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWorkQuestionResponse_trk_epk_clientId ON ClazzWorkQuestionResponse_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWorkQuestionResponse_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_210")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_210")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_210
  |AFTER INSERT ON ContentEntryProgress
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.contentEntryProgressLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ContentEntryProgress
  |    SET contentEntryProgressMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 210)
  |    WHERE contentEntryProgressUid = NEW.contentEntryProgressUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 210;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_210
  |            AFTER INSERT ON ContentEntryProgress
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.contentEntryProgressMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ContentEntryProgress
  |                SET contentEntryProgressMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 210)
  |                WHERE contentEntryProgressUid = NEW.contentEntryProgressUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 210;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 210, NEW.contentEntryProgressUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_210
  |AFTER UPDATE ON ContentEntryProgress
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.contentEntryProgressLocalChangeSeqNum == OLD.contentEntryProgressLocalChangeSeqNum OR
  |        NEW.contentEntryProgressLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ContentEntryProgress
  |    SET contentEntryProgressLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 210) 
  |    WHERE contentEntryProgressUid = NEW.contentEntryProgressUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 210;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_210
  |            AFTER UPDATE ON ContentEntryProgress
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.contentEntryProgressMasterChangeSeqNum == OLD.contentEntryProgressMasterChangeSeqNum OR
  |                    NEW.contentEntryProgressMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ContentEntryProgress
  |                SET contentEntryProgressMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 210)
  |                WHERE contentEntryProgressUid = NEW.contentEntryProgressUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 210;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 210, NEW.contentEntryProgressUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(210, (SELECT COALESCE((SELECT MAX(contentEntryProgressLocalChangeSeqNum) FROM ContentEntryProgress), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(210, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ContentEntryProgress_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentEntryProgress_trk_epk_clientId_tmp ON ContentEntryProgress_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentEntryProgress_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentEntryProgress_trk_nest.pk FROM ContentEntryProgress_trk ContentEntryProgress_trk_nest 
  |  WHERE ContentEntryProgress_trk_nest.clientId = ContentEntryProgress_trk.clientId AND
  |  ContentEntryProgress_trk_nest.epk = ContentEntryProgress_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentEntryProgress_trk_clientId_epk_csn  ON ContentEntryProgress_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentEntryProgress_trk_epk_clientId ON ContentEntryProgress_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentEntryProgress_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_101")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_101")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_101
  |AFTER INSERT ON Report
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.reportLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE Report
  |    SET reportMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 101)
  |    WHERE reportUid = NEW.reportUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 101;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_101
  |            AFTER INSERT ON Report
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.reportMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE Report
  |                SET reportMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 101)
  |                WHERE reportUid = NEW.reportUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 101;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 101, NEW.reportUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_101
  |AFTER UPDATE ON Report
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.reportLocalChangeSeqNum == OLD.reportLocalChangeSeqNum OR
  |        NEW.reportLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE Report
  |    SET reportLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 101) 
  |    WHERE reportUid = NEW.reportUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 101;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_101
  |            AFTER UPDATE ON Report
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.reportMasterChangeSeqNum == OLD.reportMasterChangeSeqNum OR
  |                    NEW.reportMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE Report
  |                SET reportMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 101)
  |                WHERE reportUid = NEW.reportUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 101;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 101, NEW.reportUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(101, (SELECT COALESCE((SELECT MAX(reportLocalChangeSeqNum) FROM Report), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(101, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_Report_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Report_trk_epk_clientId_tmp ON Report_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Report_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Report_trk_nest.pk FROM Report_trk Report_trk_nest 
  |  WHERE Report_trk_nest.clientId = Report_trk.clientId AND
  |  Report_trk_nest.epk = Report_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Report_trk_clientId_epk_csn  ON Report_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Report_trk_epk_clientId ON Report_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Report_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_102")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_102")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_102
  |AFTER INSERT ON ReportFilter
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.reportFilterLocalChangeSeqNum = 0)
  |BEGIN
  |    UPDATE ReportFilter
  |    SET reportFilterMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 102)
  |    WHERE reportFilterUid = NEW.reportFilterUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 102;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_102
  |            AFTER INSERT ON ReportFilter
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.reportFilterMasterChangeSeqNum = 0)
  |            BEGIN
  |                UPDATE ReportFilter
  |                SET reportFilterMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 102)
  |                WHERE reportFilterUid = NEW.reportFilterUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 102;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 102, NEW.reportFilterUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_102
  |AFTER UPDATE ON ReportFilter
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.reportFilterLocalChangeSeqNum == OLD.reportFilterLocalChangeSeqNum OR
  |        NEW.reportFilterLocalChangeSeqNum == 0))
  |BEGIN
  |    UPDATE ReportFilter
  |    SET reportFilterLocalChangeSeqNum = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 102) 
  |    WHERE reportFilterUid = NEW.reportFilterUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 102;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_102
  |            AFTER UPDATE ON ReportFilter
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.reportFilterMasterChangeSeqNum == OLD.reportFilterMasterChangeSeqNum OR
  |                    NEW.reportFilterMasterChangeSeqNum == 0))
  |            BEGIN
  |                UPDATE ReportFilter
  |                SET reportFilterMasterChangeSeqNum = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 102)
  |                WHERE reportFilterUid = NEW.reportFilterUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 102;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 102, NEW.reportFilterUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(102, (SELECT COALESCE((SELECT MAX(reportFilterLocalChangeSeqNum) FROM ReportFilter), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(102, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_ReportFilter_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ReportFilter_trk_epk_clientId_tmp ON ReportFilter_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ReportFilter_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ReportFilter_trk_nest.pk FROM ReportFilter_trk ReportFilter_trk_nest 
  |  WHERE ReportFilter_trk_nest.clientId = ReportFilter_trk.clientId AND
  |  ReportFilter_trk_nest.epk = ReportFilter_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ReportFilter_trk_clientId_epk_csn  ON ReportFilter_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ReportFilter_trk_epk_clientId ON ReportFilter_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ReportFilter_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_301")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_301")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_301
  |AFTER INSERT ON LearnerGroup
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.learnerGroupCSN = 0)
  |BEGIN
  |    UPDATE LearnerGroup
  |    SET learnerGroupMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 301)
  |    WHERE learnerGroupUid = NEW.learnerGroupUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 301;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_301
  |            AFTER INSERT ON LearnerGroup
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.learnerGroupMCSN = 0)
  |            BEGIN
  |                UPDATE LearnerGroup
  |                SET learnerGroupMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 301)
  |                WHERE learnerGroupUid = NEW.learnerGroupUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 301;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 301, NEW.learnerGroupUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_301
  |AFTER UPDATE ON LearnerGroup
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.learnerGroupCSN == OLD.learnerGroupCSN OR
  |        NEW.learnerGroupCSN == 0))
  |BEGIN
  |    UPDATE LearnerGroup
  |    SET learnerGroupCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 301) 
  |    WHERE learnerGroupUid = NEW.learnerGroupUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 301;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_301
  |            AFTER UPDATE ON LearnerGroup
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.learnerGroupMCSN == OLD.learnerGroupMCSN OR
  |                    NEW.learnerGroupMCSN == 0))
  |            BEGIN
  |                UPDATE LearnerGroup
  |                SET learnerGroupMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 301)
  |                WHERE learnerGroupUid = NEW.learnerGroupUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 301;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 301, NEW.learnerGroupUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(301, (SELECT COALESCE((SELECT MAX(learnerGroupCSN) FROM LearnerGroup), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(301, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_LearnerGroup_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_LearnerGroup_trk_epk_clientId_tmp ON LearnerGroup_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM LearnerGroup_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT LearnerGroup_trk_nest.pk FROM LearnerGroup_trk LearnerGroup_trk_nest 
  |  WHERE LearnerGroup_trk_nest.clientId = LearnerGroup_trk.clientId AND
  |  LearnerGroup_trk_nest.epk = LearnerGroup_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_LearnerGroup_trk_clientId_epk_csn  ON LearnerGroup_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_LearnerGroup_trk_epk_clientId ON LearnerGroup_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_LearnerGroup_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_300")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_300")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_300
  |AFTER INSERT ON LearnerGroupMember
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.learnerGroupMemberCSN = 0)
  |BEGIN
  |    UPDATE LearnerGroupMember
  |    SET learnerGroupMemberMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 300)
  |    WHERE learnerGroupMemberUid = NEW.learnerGroupMemberUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 300;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_300
  |            AFTER INSERT ON LearnerGroupMember
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.learnerGroupMemberMCSN = 0)
  |            BEGIN
  |                UPDATE LearnerGroupMember
  |                SET learnerGroupMemberMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 300)
  |                WHERE learnerGroupMemberUid = NEW.learnerGroupMemberUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 300;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 300, NEW.learnerGroupMemberUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_300
  |AFTER UPDATE ON LearnerGroupMember
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.learnerGroupMemberCSN == OLD.learnerGroupMemberCSN OR
  |        NEW.learnerGroupMemberCSN == 0))
  |BEGIN
  |    UPDATE LearnerGroupMember
  |    SET learnerGroupMemberCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 300) 
  |    WHERE learnerGroupMemberUid = NEW.learnerGroupMemberUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 300;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_300
  |            AFTER UPDATE ON LearnerGroupMember
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.learnerGroupMemberMCSN == OLD.learnerGroupMemberMCSN OR
  |                    NEW.learnerGroupMemberMCSN == 0))
  |            BEGIN
  |                UPDATE LearnerGroupMember
  |                SET learnerGroupMemberMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 300)
  |                WHERE learnerGroupMemberUid = NEW.learnerGroupMemberUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 300;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 300, NEW.learnerGroupMemberUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(300, (SELECT COALESCE((SELECT MAX(learnerGroupMemberCSN) FROM LearnerGroupMember), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(300, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_LearnerGroupMember_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_LearnerGroupMember_trk_epk_clientId_tmp ON LearnerGroupMember_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM LearnerGroupMember_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT LearnerGroupMember_trk_nest.pk FROM LearnerGroupMember_trk LearnerGroupMember_trk_nest 
  |  WHERE LearnerGroupMember_trk_nest.clientId = LearnerGroupMember_trk.clientId AND
  |  LearnerGroupMember_trk_nest.epk = LearnerGroupMember_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_LearnerGroupMember_trk_clientId_epk_csn  ON LearnerGroupMember_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_LearnerGroupMember_trk_epk_clientId ON LearnerGroupMember_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_LearnerGroupMember_trk_epk_clientId_tmp")
        database.execSQL("DROP TRIGGER IF EXISTS INS_302")
        database.execSQL("DROP TRIGGER IF EXISTS UPD_302")
        database.execSQL("""
  |CREATE TRIGGER INS_LOC_302
  |AFTER INSERT ON GroupLearningSession
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0) AND
  |    NEW.groupLearningSessionCSN = 0)
  |BEGIN
  |    UPDATE GroupLearningSession
  |    SET groupLearningSessionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 302)
  |    WHERE groupLearningSessionUid = NEW.groupLearningSessionUid;
  |    
  |    UPDATE SqliteChangeSeqNums
  |    SET sCsnNextPrimary = sCsnNextPrimary + 1
  |    WHERE sCsnTableId = 302;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER INS_PRI_302
  |            AFTER INSERT ON GroupLearningSession
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1) AND
  |                NEW.groupLearningSessionMCSN = 0)
  |            BEGIN
  |                UPDATE GroupLearningSession
  |                SET groupLearningSessionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 302)
  |                WHERE groupLearningSessionUid = NEW.groupLearningSessionUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 302;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 302, NEW.groupLearningSessionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("""
  |CREATE TRIGGER UPD_LOC_302
  |AFTER UPDATE ON GroupLearningSession
  |FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 0)
  |    AND (NEW.groupLearningSessionCSN == OLD.groupLearningSessionCSN OR
  |        NEW.groupLearningSessionCSN == 0))
  |BEGIN
  |    UPDATE GroupLearningSession
  |    SET groupLearningSessionCSN = (SELECT sCsnNextLocal FROM SqliteChangeSeqNums WHERE sCsnTableId = 302) 
  |    WHERE groupLearningSessionUid = NEW.groupLearningSessionUid;
  |    
  |    UPDATE SqliteChangeSeqNums 
  |    SET sCsnNextLocal = sCsnNextLocal + 1
  |    WHERE sCsnTableId = 302;
  |END
  """.trimMargin())
        database.execSQL("""
  |            CREATE TRIGGER UPD_PRI_302
  |            AFTER UPDATE ON GroupLearningSession
  |            FOR EACH ROW WHEN (((SELECT CAST(master AS INTEGER) FROM SyncNode) = 1)
  |                AND (NEW.groupLearningSessionMCSN == OLD.groupLearningSessionMCSN OR
  |                    NEW.groupLearningSessionMCSN == 0))
  |            BEGIN
  |                UPDATE GroupLearningSession
  |                SET groupLearningSessionMCSN = (SELECT sCsnNextPrimary FROM SqliteChangeSeqNums WHERE sCsnTableId = 302)
  |                WHERE groupLearningSessionUid = NEW.groupLearningSessionUid;
  |                
  |                UPDATE SqliteChangeSeqNums
  |                SET sCsnNextPrimary = sCsnNextPrimary + 1
  |                WHERE sCsnTableId = 302;
  |                
  |                INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  |SELECT 302, NEW.groupLearningSessionUid, 0, (strftime('%s','now') * 1000) + ((strftime('%f','now') * 1000) % 1000);
  |            END
  """.trimMargin())
        database.execSQL("REPLACE INTO SqliteChangeSeqNums(sCsnTableId, sCsnNextLocal, sCsnNextPrimary) VALUES(302, (SELECT COALESCE((SELECT MAX(groupLearningSessionCSN) FROM GroupLearningSession), 0) + 1), 1)")
        database.execSQL("INSERT INTO TableSyncStatus(tsTableId, tsLastChanged, tsLastSynced) VALUES(302, ${systemTimeInMillis()}, 0)")
        database.execSQL("DROP INDEX IF EXISTS index_GroupLearningSession_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_GroupLearningSession_trk_epk_clientId_tmp ON GroupLearningSession_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM GroupLearningSession_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT GroupLearningSession_trk_nest.pk FROM GroupLearningSession_trk GroupLearningSession_trk_nest 
  |  WHERE GroupLearningSession_trk_nest.clientId = GroupLearningSession_trk.clientId AND
  |  GroupLearningSession_trk_nest.epk = GroupLearningSession_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_GroupLearningSession_trk_clientId_epk_csn  ON GroupLearningSession_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_GroupLearningSession_trk_epk_clientId ON GroupLearningSession_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_GroupLearningSession_trk_epk_clientId_tmp")
    } else {
        database.execSQL("CREATE TABLE IF NOT EXISTS ChangeLog (  chTableId  INTEGER  NOT NULL , chEntityPk  BIGINT  NOT NULL , dispatched  BOOL  NOT NULL , chTime  BIGINT  NOT NULL , changeLogUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
        database.execSQL("CREATE TABLE IF NOT EXISTS SqliteChangeSeqNums (  sCsnTableId  INTEGER  PRIMARY KEY  NOT NULL , sCsnNextLocal  INTEGER  NOT NULL , sCsnNextPrimary  INTEGER  NOT NULL )")
        database.execSQL("""
  |CREATE 
  | INDEX index_SqliteChangeSeqNums_sCsnNextLocal 
  |ON SqliteChangeSeqNums (sCsnNextLocal)
  """.trimMargin())
        database.execSQL("""
  |CREATE 
  | INDEX index_SqliteChangeSeqNums_sCsnNextPrimary 
  |ON SqliteChangeSeqNums (sCsnNextPrimary)
  """.trimMargin())
        database.execSQL("CREATE TABLE IF NOT EXISTS TableSyncStatus (  tsTableId  INTEGER  PRIMARY KEY  NOT NULL , tsLastChanged  BIGINT  NOT NULL , tsLastSynced  BIGINT  NOT NULL )")
        database.execSQL("CREATE TABLE IF NOT EXISTS UpdateNotification (  pnDeviceId  INTEGER  NOT NULL , pnTableId  INTEGER  NOT NULL , pnTimestamp  BIGINT  NOT NULL , pnUid  BIGSERIAL  PRIMARY KEY  NOT NULL )")
        database.execSQL("""
  |CREATE 
  |UNIQUE INDEX index_UpdateNotification_pnDeviceId_pnTableId 
  |ON UpdateNotification (pnDeviceId, pnTableId)
  """.trimMargin())
        database.execSQL("""
  |CREATE 
  | INDEX index_UpdateNotification_pnDeviceId_pnTimestamp 
  |ON UpdateNotification (pnDeviceId, pnTimestamp)
  """.trimMargin())
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_14_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ClazzLog SET clazzLogLCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogLCSN 
  | ELSE NEXTVAL('ClazzLog_lcsn_seq') END),
  | clazzLogMSQN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ClazzLog_mcsn_seq') 
  | ELSE NEW.clazzLogMSQN END)
  | WHERE clazzLogUid = NEW.clazzLogUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 14, NEW.clazzLogUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ClazzLog_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzLog_trk_epk_clientId_tmp ON ClazzLog_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzLog_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzLog_trk_nest.pk FROM ClazzLog_trk ClazzLog_trk_nest 
  |  WHERE ClazzLog_trk_nest.clientId = ClazzLog_trk.clientId AND
  |  ClazzLog_trk_nest.epk = ClazzLog_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzLog_trk_clientId_epk_csn  ON ClazzLog_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzLog_trk_epk_clientId ON ClazzLog_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzLog_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_15_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ClazzLogAttendanceRecord SET clazzLogAttendanceRecordLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLogAttendanceRecordLocalChangeSeqNum 
  | ELSE NEXTVAL('ClazzLogAttendanceRecord_lcsn_seq') END),
  | clazzLogAttendanceRecordMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ClazzLogAttendanceRecord_mcsn_seq') 
  | ELSE NEW.clazzLogAttendanceRecordMasterChangeSeqNum END)
  | WHERE clazzLogAttendanceRecordUid = NEW.clazzLogAttendanceRecordUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 15, NEW.clazzLogAttendanceRecordUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ClazzLogAttendanceRecord_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzLogAttendanceRecord_trk_epk_clientId_tmp ON ClazzLogAttendanceRecord_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzLogAttendanceRecord_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzLogAttendanceRecord_trk_nest.pk FROM ClazzLogAttendanceRecord_trk ClazzLogAttendanceRecord_trk_nest 
  |  WHERE ClazzLogAttendanceRecord_trk_nest.clientId = ClazzLogAttendanceRecord_trk.clientId AND
  |  ClazzLogAttendanceRecord_trk_nest.epk = ClazzLogAttendanceRecord_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzLogAttendanceRecord_trk_clientId_epk_csn  ON ClazzLogAttendanceRecord_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzLogAttendanceRecord_trk_epk_clientId ON ClazzLogAttendanceRecord_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzLogAttendanceRecord_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_21_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE Schedule SET scheduleLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.scheduleLocalChangeSeqNum 
  | ELSE NEXTVAL('Schedule_lcsn_seq') END),
  | scheduleMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('Schedule_mcsn_seq') 
  | ELSE NEW.scheduleMasterChangeSeqNum END)
  | WHERE scheduleUid = NEW.scheduleUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 21, NEW.scheduleUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_Schedule_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Schedule_trk_epk_clientId_tmp ON Schedule_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Schedule_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Schedule_trk_nest.pk FROM Schedule_trk Schedule_trk_nest 
  |  WHERE Schedule_trk_nest.clientId = Schedule_trk.clientId AND
  |  Schedule_trk_nest.epk = Schedule_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Schedule_trk_clientId_epk_csn  ON Schedule_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Schedule_trk_epk_clientId ON Schedule_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Schedule_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_17_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE DateRange SET dateRangeLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.dateRangeLocalChangeSeqNum 
  | ELSE NEXTVAL('DateRange_lcsn_seq') END),
  | dateRangeMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('DateRange_mcsn_seq') 
  | ELSE NEW.dateRangeMasterChangeSeqNum END)
  | WHERE dateRangeUid = NEW.dateRangeUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 17, NEW.dateRangeUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_DateRange_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_DateRange_trk_epk_clientId_tmp ON DateRange_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM DateRange_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT DateRange_trk_nest.pk FROM DateRange_trk DateRange_trk_nest 
  |  WHERE DateRange_trk_nest.clientId = DateRange_trk.clientId AND
  |  DateRange_trk_nest.epk = DateRange_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_DateRange_trk_clientId_epk_csn  ON DateRange_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_DateRange_trk_epk_clientId ON DateRange_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_DateRange_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_28_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE HolidayCalendar SET umCalendarLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.umCalendarLocalChangeSeqNum 
  | ELSE NEXTVAL('HolidayCalendar_lcsn_seq') END),
  | umCalendarMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('HolidayCalendar_mcsn_seq') 
  | ELSE NEW.umCalendarMasterChangeSeqNum END)
  | WHERE umCalendarUid = NEW.umCalendarUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 28, NEW.umCalendarUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_HolidayCalendar_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_HolidayCalendar_trk_epk_clientId_tmp ON HolidayCalendar_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM HolidayCalendar_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT HolidayCalendar_trk_nest.pk FROM HolidayCalendar_trk HolidayCalendar_trk_nest 
  |  WHERE HolidayCalendar_trk_nest.clientId = HolidayCalendar_trk.clientId AND
  |  HolidayCalendar_trk_nest.epk = HolidayCalendar_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_HolidayCalendar_trk_clientId_epk_csn  ON HolidayCalendar_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_HolidayCalendar_trk_epk_clientId ON HolidayCalendar_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_HolidayCalendar_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_99_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE Holiday SET holLocalCsn =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.holLocalCsn 
  | ELSE NEXTVAL('Holiday_lcsn_seq') END),
  | holMasterCsn = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('Holiday_mcsn_seq') 
  | ELSE NEW.holMasterCsn END)
  | WHERE holUid = NEW.holUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 99, NEW.holUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_Holiday_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Holiday_trk_epk_clientId_tmp ON Holiday_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Holiday_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Holiday_trk_nest.pk FROM Holiday_trk Holiday_trk_nest 
  |  WHERE Holiday_trk_nest.clientId = Holiday_trk.clientId AND
  |  Holiday_trk_nest.epk = Holiday_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Holiday_trk_clientId_epk_csn  ON Holiday_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Holiday_trk_epk_clientId ON Holiday_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Holiday_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_173_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ScheduledCheck SET scheduledCheckLocalCsn =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.scheduledCheckLocalCsn 
  | ELSE NEXTVAL('ScheduledCheck_lcsn_seq') END),
  | scheduledCheckMasterCsn = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ScheduledCheck_mcsn_seq') 
  | ELSE NEW.scheduledCheckMasterCsn END)
  | WHERE scheduledCheckUid = NEW.scheduledCheckUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 173, NEW.scheduledCheckUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ScheduledCheck_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ScheduledCheck_trk_epk_clientId_tmp ON ScheduledCheck_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ScheduledCheck_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ScheduledCheck_trk_nest.pk FROM ScheduledCheck_trk ScheduledCheck_trk_nest 
  |  WHERE ScheduledCheck_trk_nest.clientId = ScheduledCheck_trk.clientId AND
  |  ScheduledCheck_trk_nest.epk = ScheduledCheck_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ScheduledCheck_trk_clientId_epk_csn  ON ScheduledCheck_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ScheduledCheck_trk_epk_clientId ON ScheduledCheck_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ScheduledCheck_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_53_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE AuditLog SET auditLogLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.auditLogLocalChangeSeqNum 
  | ELSE NEXTVAL('AuditLog_lcsn_seq') END),
  | auditLogMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('AuditLog_mcsn_seq') 
  | ELSE NEW.auditLogMasterChangeSeqNum END)
  | WHERE auditLogUid = NEW.auditLogUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 53, NEW.auditLogUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_AuditLog_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_AuditLog_trk_epk_clientId_tmp ON AuditLog_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM AuditLog_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT AuditLog_trk_nest.pk FROM AuditLog_trk AuditLog_trk_nest 
  |  WHERE AuditLog_trk_nest.clientId = AuditLog_trk.clientId AND
  |  AuditLog_trk_nest.epk = AuditLog_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_AuditLog_trk_clientId_epk_csn  ON AuditLog_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_AuditLog_trk_epk_clientId ON AuditLog_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_AuditLog_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_56_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE CustomField SET customFieldLCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldLCSN 
  | ELSE NEXTVAL('CustomField_lcsn_seq') END),
  | customFieldMCSN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('CustomField_mcsn_seq') 
  | ELSE NEW.customFieldMCSN END)
  | WHERE customFieldUid = NEW.customFieldUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 56, NEW.customFieldUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_CustomField_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_CustomField_trk_epk_clientId_tmp ON CustomField_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM CustomField_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT CustomField_trk_nest.pk FROM CustomField_trk CustomField_trk_nest 
  |  WHERE CustomField_trk_nest.clientId = CustomField_trk.clientId AND
  |  CustomField_trk_nest.epk = CustomField_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_CustomField_trk_clientId_epk_csn  ON CustomField_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_CustomField_trk_epk_clientId ON CustomField_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_CustomField_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_57_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE CustomFieldValue SET customFieldValueLCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldValueLCSN 
  | ELSE NEXTVAL('CustomFieldValue_lcsn_seq') END),
  | customFieldValueMCSN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('CustomFieldValue_mcsn_seq') 
  | ELSE NEW.customFieldValueMCSN END)
  | WHERE customFieldValueUid = NEW.customFieldValueUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 57, NEW.customFieldValueUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_CustomFieldValue_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_CustomFieldValue_trk_epk_clientId_tmp ON CustomFieldValue_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM CustomFieldValue_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT CustomFieldValue_trk_nest.pk FROM CustomFieldValue_trk CustomFieldValue_trk_nest 
  |  WHERE CustomFieldValue_trk_nest.clientId = CustomFieldValue_trk.clientId AND
  |  CustomFieldValue_trk_nest.epk = CustomFieldValue_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_CustomFieldValue_trk_clientId_epk_csn  ON CustomFieldValue_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_CustomFieldValue_trk_epk_clientId ON CustomFieldValue_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_CustomFieldValue_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_55_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE CustomFieldValueOption SET customFieldValueOptionLCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.customFieldValueOptionLCSN 
  | ELSE NEXTVAL('CustomFieldValueOption_lcsn_seq') END),
  | customFieldValueOptionMCSN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('CustomFieldValueOption_mcsn_seq') 
  | ELSE NEW.customFieldValueOptionMCSN END)
  | WHERE customFieldValueOptionUid = NEW.customFieldValueOptionUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 55, NEW.customFieldValueOptionUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_CustomFieldValueOption_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_CustomFieldValueOption_trk_epk_clientId_tmp ON CustomFieldValueOption_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM CustomFieldValueOption_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT CustomFieldValueOption_trk_nest.pk FROM CustomFieldValueOption_trk CustomFieldValueOption_trk_nest 
  |  WHERE CustomFieldValueOption_trk_nest.clientId = CustomFieldValueOption_trk.clientId AND
  |  CustomFieldValueOption_trk_nest.epk = CustomFieldValueOption_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_CustomFieldValueOption_trk_clientId_epk_csn  ON CustomFieldValueOption_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_CustomFieldValueOption_trk_epk_clientId ON CustomFieldValueOption_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_CustomFieldValueOption_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_9_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE Person SET personLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personLocalChangeSeqNum 
  | ELSE NEXTVAL('Person_lcsn_seq') END),
  | personMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('Person_mcsn_seq') 
  | ELSE NEW.personMasterChangeSeqNum END)
  | WHERE personUid = NEW.personUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 9, NEW.personUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_Person_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Person_trk_epk_clientId_tmp ON Person_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Person_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Person_trk_nest.pk FROM Person_trk Person_trk_nest 
  |  WHERE Person_trk_nest.clientId = Person_trk.clientId AND
  |  Person_trk_nest.epk = Person_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Person_trk_clientId_epk_csn  ON Person_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Person_trk_epk_clientId ON Person_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Person_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_6_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE Clazz SET clazzLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzLocalChangeSeqNum 
  | ELSE NEXTVAL('Clazz_lcsn_seq') END),
  | clazzMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('Clazz_mcsn_seq') 
  | ELSE NEW.clazzMasterChangeSeqNum END)
  | WHERE clazzUid = NEW.clazzUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 6, NEW.clazzUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_Clazz_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Clazz_trk_epk_clientId_tmp ON Clazz_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Clazz_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Clazz_trk_nest.pk FROM Clazz_trk Clazz_trk_nest 
  |  WHERE Clazz_trk_nest.clientId = Clazz_trk.clientId AND
  |  Clazz_trk_nest.epk = Clazz_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Clazz_trk_clientId_epk_csn  ON Clazz_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Clazz_trk_epk_clientId ON Clazz_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Clazz_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_65_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ClazzMember SET clazzMemberLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzMemberLocalChangeSeqNum 
  | ELSE NEXTVAL('ClazzMember_lcsn_seq') END),
  | clazzMemberMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ClazzMember_mcsn_seq') 
  | ELSE NEW.clazzMemberMasterChangeSeqNum END)
  | WHERE clazzMemberUid = NEW.clazzMemberUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 65, NEW.clazzMemberUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ClazzMember_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzMember_trk_epk_clientId_tmp ON ClazzMember_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzMember_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzMember_trk_nest.pk FROM ClazzMember_trk ClazzMember_trk_nest 
  |  WHERE ClazzMember_trk_nest.clientId = ClazzMember_trk.clientId AND
  |  ClazzMember_trk_nest.epk = ClazzMember_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzMember_trk_clientId_epk_csn  ON ClazzMember_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzMember_trk_epk_clientId ON ClazzMember_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzMember_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_178_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE PersonCustomFieldValue SET personCustomFieldValueLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personCustomFieldValueLocalChangeSeqNum 
  | ELSE NEXTVAL('PersonCustomFieldValue_lcsn_seq') END),
  | personCustomFieldValueMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('PersonCustomFieldValue_mcsn_seq') 
  | ELSE NEW.personCustomFieldValueMasterChangeSeqNum END)
  | WHERE personCustomFieldValueUid = NEW.personCustomFieldValueUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 178, NEW.personCustomFieldValueUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_PersonCustomFieldValue_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_PersonCustomFieldValue_trk_epk_clientId_tmp ON PersonCustomFieldValue_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM PersonCustomFieldValue_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT PersonCustomFieldValue_trk_nest.pk FROM PersonCustomFieldValue_trk PersonCustomFieldValue_trk_nest 
  |  WHERE PersonCustomFieldValue_trk_nest.clientId = PersonCustomFieldValue_trk.clientId AND
  |  PersonCustomFieldValue_trk_nest.epk = PersonCustomFieldValue_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_PersonCustomFieldValue_trk_clientId_epk_csn  ON PersonCustomFieldValue_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_PersonCustomFieldValue_trk_epk_clientId ON PersonCustomFieldValue_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_PersonCustomFieldValue_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_42_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ContentEntry SET contentEntryLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentEntryLocalChangeSeqNum 
  | ELSE NEXTVAL('ContentEntry_lcsn_seq') END),
  | contentEntryMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ContentEntry_mcsn_seq') 
  | ELSE NEW.contentEntryMasterChangeSeqNum END)
  | WHERE contentEntryUid = NEW.contentEntryUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 42, NEW.contentEntryUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ContentEntry_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentEntry_trk_epk_clientId_tmp ON ContentEntry_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentEntry_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentEntry_trk_nest.pk FROM ContentEntry_trk ContentEntry_trk_nest 
  |  WHERE ContentEntry_trk_nest.clientId = ContentEntry_trk.clientId AND
  |  ContentEntry_trk_nest.epk = ContentEntry_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentEntry_trk_clientId_epk_csn  ON ContentEntry_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentEntry_trk_epk_clientId ON ContentEntry_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentEntry_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_3_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ContentEntryContentCategoryJoin SET ceccjLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.ceccjLocalChangeSeqNum 
  | ELSE NEXTVAL('ContentEntryContentCategoryJoin_lcsn_seq') END),
  | ceccjMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ContentEntryContentCategoryJoin_mcsn_seq') 
  | ELSE NEW.ceccjMasterChangeSeqNum END)
  | WHERE ceccjUid = NEW.ceccjUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 3, NEW.ceccjUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ContentEntryContentCategoryJoin_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentEntryContentCategoryJoin_trk_epk_clientId_tmp ON ContentEntryContentCategoryJoin_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentEntryContentCategoryJoin_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentEntryContentCategoryJoin_trk_nest.pk FROM ContentEntryContentCategoryJoin_trk ContentEntryContentCategoryJoin_trk_nest 
  |  WHERE ContentEntryContentCategoryJoin_trk_nest.clientId = ContentEntryContentCategoryJoin_trk.clientId AND
  |  ContentEntryContentCategoryJoin_trk_nest.epk = ContentEntryContentCategoryJoin_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentEntryContentCategoryJoin_trk_clientId_epk_csn  ON ContentEntryContentCategoryJoin_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentEntryContentCategoryJoin_trk_epk_clientId ON ContentEntryContentCategoryJoin_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentEntryContentCategoryJoin_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_7_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ContentEntryParentChildJoin SET cepcjLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cepcjLocalChangeSeqNum 
  | ELSE NEXTVAL('ContentEntryParentChildJoin_lcsn_seq') END),
  | cepcjMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ContentEntryParentChildJoin_mcsn_seq') 
  | ELSE NEW.cepcjMasterChangeSeqNum END)
  | WHERE cepcjUid = NEW.cepcjUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 7, NEW.cepcjUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ContentEntryParentChildJoin_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentEntryParentChildJoin_trk_epk_clientId_tmp ON ContentEntryParentChildJoin_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentEntryParentChildJoin_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentEntryParentChildJoin_trk_nest.pk FROM ContentEntryParentChildJoin_trk ContentEntryParentChildJoin_trk_nest 
  |  WHERE ContentEntryParentChildJoin_trk_nest.clientId = ContentEntryParentChildJoin_trk.clientId AND
  |  ContentEntryParentChildJoin_trk_nest.epk = ContentEntryParentChildJoin_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentEntryParentChildJoin_trk_clientId_epk_csn  ON ContentEntryParentChildJoin_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentEntryParentChildJoin_trk_epk_clientId ON ContentEntryParentChildJoin_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentEntryParentChildJoin_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_8_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ContentEntryRelatedEntryJoin SET cerejLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cerejLocalChangeSeqNum 
  | ELSE NEXTVAL('ContentEntryRelatedEntryJoin_lcsn_seq') END),
  | cerejMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ContentEntryRelatedEntryJoin_mcsn_seq') 
  | ELSE NEW.cerejMasterChangeSeqNum END)
  | WHERE cerejUid = NEW.cerejUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 8, NEW.cerejUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ContentEntryRelatedEntryJoin_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentEntryRelatedEntryJoin_trk_epk_clientId_tmp ON ContentEntryRelatedEntryJoin_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentEntryRelatedEntryJoin_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentEntryRelatedEntryJoin_trk_nest.pk FROM ContentEntryRelatedEntryJoin_trk ContentEntryRelatedEntryJoin_trk_nest 
  |  WHERE ContentEntryRelatedEntryJoin_trk_nest.clientId = ContentEntryRelatedEntryJoin_trk.clientId AND
  |  ContentEntryRelatedEntryJoin_trk_nest.epk = ContentEntryRelatedEntryJoin_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentEntryRelatedEntryJoin_trk_clientId_epk_csn  ON ContentEntryRelatedEntryJoin_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentEntryRelatedEntryJoin_trk_epk_clientId ON ContentEntryRelatedEntryJoin_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentEntryRelatedEntryJoin_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_2_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ContentCategorySchema SET contentCategorySchemaLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentCategorySchemaLocalChangeSeqNum 
  | ELSE NEXTVAL('ContentCategorySchema_lcsn_seq') END),
  | contentCategorySchemaMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ContentCategorySchema_mcsn_seq') 
  | ELSE NEW.contentCategorySchemaMasterChangeSeqNum END)
  | WHERE contentCategorySchemaUid = NEW.contentCategorySchemaUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 2, NEW.contentCategorySchemaUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ContentCategorySchema_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentCategorySchema_trk_epk_clientId_tmp ON ContentCategorySchema_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentCategorySchema_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentCategorySchema_trk_nest.pk FROM ContentCategorySchema_trk ContentCategorySchema_trk_nest 
  |  WHERE ContentCategorySchema_trk_nest.clientId = ContentCategorySchema_trk.clientId AND
  |  ContentCategorySchema_trk_nest.epk = ContentCategorySchema_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentCategorySchema_trk_clientId_epk_csn  ON ContentCategorySchema_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentCategorySchema_trk_epk_clientId ON ContentCategorySchema_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentCategorySchema_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_1_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ContentCategory SET contentCategoryLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentCategoryLocalChangeSeqNum 
  | ELSE NEXTVAL('ContentCategory_lcsn_seq') END),
  | contentCategoryMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ContentCategory_mcsn_seq') 
  | ELSE NEW.contentCategoryMasterChangeSeqNum END)
  | WHERE contentCategoryUid = NEW.contentCategoryUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 1, NEW.contentCategoryUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ContentCategory_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentCategory_trk_epk_clientId_tmp ON ContentCategory_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentCategory_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentCategory_trk_nest.pk FROM ContentCategory_trk ContentCategory_trk_nest 
  |  WHERE ContentCategory_trk_nest.clientId = ContentCategory_trk.clientId AND
  |  ContentCategory_trk_nest.epk = ContentCategory_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentCategory_trk_clientId_epk_csn  ON ContentCategory_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentCategory_trk_epk_clientId ON ContentCategory_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentCategory_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_13_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE Language SET langLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.langLocalChangeSeqNum 
  | ELSE NEXTVAL('Language_lcsn_seq') END),
  | langMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('Language_mcsn_seq') 
  | ELSE NEW.langMasterChangeSeqNum END)
  | WHERE langUid = NEW.langUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 13, NEW.langUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_Language_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Language_trk_epk_clientId_tmp ON Language_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Language_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Language_trk_nest.pk FROM Language_trk Language_trk_nest 
  |  WHERE Language_trk_nest.clientId = Language_trk.clientId AND
  |  Language_trk_nest.epk = Language_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Language_trk_clientId_epk_csn  ON Language_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Language_trk_epk_clientId ON Language_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Language_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_10_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE LanguageVariant SET langVariantLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.langVariantLocalChangeSeqNum 
  | ELSE NEXTVAL('LanguageVariant_lcsn_seq') END),
  | langVariantMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('LanguageVariant_mcsn_seq') 
  | ELSE NEW.langVariantMasterChangeSeqNum END)
  | WHERE langVariantUid = NEW.langVariantUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 10, NEW.langVariantUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_LanguageVariant_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_LanguageVariant_trk_epk_clientId_tmp ON LanguageVariant_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM LanguageVariant_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT LanguageVariant_trk_nest.pk FROM LanguageVariant_trk LanguageVariant_trk_nest 
  |  WHERE LanguageVariant_trk_nest.clientId = LanguageVariant_trk.clientId AND
  |  LanguageVariant_trk_nest.epk = LanguageVariant_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_LanguageVariant_trk_clientId_epk_csn  ON LanguageVariant_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_LanguageVariant_trk_epk_clientId ON LanguageVariant_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_LanguageVariant_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_45_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE Role SET roleLocalCsn =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.roleLocalCsn 
  | ELSE NEXTVAL('Role_lcsn_seq') END),
  | roleMasterCsn = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('Role_mcsn_seq') 
  | ELSE NEW.roleMasterCsn END)
  | WHERE roleUid = NEW.roleUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 45, NEW.roleUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_Role_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Role_trk_epk_clientId_tmp ON Role_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Role_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Role_trk_nest.pk FROM Role_trk Role_trk_nest 
  |  WHERE Role_trk_nest.clientId = Role_trk.clientId AND
  |  Role_trk_nest.epk = Role_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Role_trk_clientId_epk_csn  ON Role_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Role_trk_epk_clientId ON Role_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Role_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_47_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE EntityRole SET erLocalCsn =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.erLocalCsn 
  | ELSE NEXTVAL('EntityRole_lcsn_seq') END),
  | erMasterCsn = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('EntityRole_mcsn_seq') 
  | ELSE NEW.erMasterCsn END)
  | WHERE erUid = NEW.erUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 47, NEW.erUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_EntityRole_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_EntityRole_trk_epk_clientId_tmp ON EntityRole_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM EntityRole_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT EntityRole_trk_nest.pk FROM EntityRole_trk EntityRole_trk_nest 
  |  WHERE EntityRole_trk_nest.clientId = EntityRole_trk.clientId AND
  |  EntityRole_trk_nest.epk = EntityRole_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_EntityRole_trk_clientId_epk_csn  ON EntityRole_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_EntityRole_trk_epk_clientId ON EntityRole_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_EntityRole_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_43_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE PersonGroup SET groupLocalCsn =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.groupLocalCsn 
  | ELSE NEXTVAL('PersonGroup_lcsn_seq') END),
  | groupMasterCsn = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('PersonGroup_mcsn_seq') 
  | ELSE NEW.groupMasterCsn END)
  | WHERE groupUid = NEW.groupUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 43, NEW.groupUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_PersonGroup_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_PersonGroup_trk_epk_clientId_tmp ON PersonGroup_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM PersonGroup_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT PersonGroup_trk_nest.pk FROM PersonGroup_trk PersonGroup_trk_nest 
  |  WHERE PersonGroup_trk_nest.clientId = PersonGroup_trk.clientId AND
  |  PersonGroup_trk_nest.epk = PersonGroup_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_PersonGroup_trk_clientId_epk_csn  ON PersonGroup_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_PersonGroup_trk_epk_clientId ON PersonGroup_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_PersonGroup_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_44_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE PersonGroupMember SET groupMemberLocalCsn =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.groupMemberLocalCsn 
  | ELSE NEXTVAL('PersonGroupMember_lcsn_seq') END),
  | groupMemberMasterCsn = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('PersonGroupMember_mcsn_seq') 
  | ELSE NEW.groupMemberMasterCsn END)
  | WHERE groupMemberUid = NEW.groupMemberUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 44, NEW.groupMemberUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_PersonGroupMember_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_PersonGroupMember_trk_epk_clientId_tmp ON PersonGroupMember_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM PersonGroupMember_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT PersonGroupMember_trk_nest.pk FROM PersonGroupMember_trk PersonGroupMember_trk_nest 
  |  WHERE PersonGroupMember_trk_nest.clientId = PersonGroupMember_trk.clientId AND
  |  PersonGroupMember_trk_nest.epk = PersonGroupMember_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_PersonGroupMember_trk_clientId_epk_csn  ON PersonGroupMember_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_PersonGroupMember_trk_epk_clientId ON PersonGroupMember_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_PersonGroupMember_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_50_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE PersonPicture SET personPictureLocalCsn =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.personPictureLocalCsn 
  | ELSE NEXTVAL('PersonPicture_lcsn_seq') END),
  | personPictureMasterCsn = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('PersonPicture_mcsn_seq') 
  | ELSE NEW.personPictureMasterCsn END)
  | WHERE personPictureUid = NEW.personPictureUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 50, NEW.personPictureUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_PersonPicture_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_PersonPicture_trk_epk_clientId_tmp ON PersonPicture_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM PersonPicture_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT PersonPicture_trk_nest.pk FROM PersonPicture_trk PersonPicture_trk_nest 
  |  WHERE PersonPicture_trk_nest.clientId = PersonPicture_trk.clientId AND
  |  PersonPicture_trk_nest.epk = PersonPicture_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_PersonPicture_trk_clientId_epk_csn  ON PersonPicture_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_PersonPicture_trk_epk_clientId ON PersonPicture_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_PersonPicture_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_51_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE Container SET cntLocalCsn =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.cntLocalCsn 
  | ELSE NEXTVAL('Container_lcsn_seq') END),
  | cntMasterCsn = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('Container_mcsn_seq') 
  | ELSE NEW.cntMasterCsn END)
  | WHERE containerUid = NEW.containerUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 51, NEW.containerUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_Container_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Container_trk_epk_clientId_tmp ON Container_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Container_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Container_trk_nest.pk FROM Container_trk Container_trk_nest 
  |  WHERE Container_trk_nest.clientId = Container_trk.clientId AND
  |  Container_trk_nest.epk = Container_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Container_trk_clientId_epk_csn  ON Container_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Container_trk_epk_clientId ON Container_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Container_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_62_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE VerbEntity SET verbLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.verbLocalChangeSeqNum 
  | ELSE NEXTVAL('VerbEntity_lcsn_seq') END),
  | verbMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('VerbEntity_mcsn_seq') 
  | ELSE NEW.verbMasterChangeSeqNum END)
  | WHERE verbUid = NEW.verbUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 62, NEW.verbUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_VerbEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_VerbEntity_trk_epk_clientId_tmp ON VerbEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM VerbEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT VerbEntity_trk_nest.pk FROM VerbEntity_trk VerbEntity_trk_nest 
  |  WHERE VerbEntity_trk_nest.clientId = VerbEntity_trk.clientId AND
  |  VerbEntity_trk_nest.epk = VerbEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_VerbEntity_trk_clientId_epk_csn  ON VerbEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_VerbEntity_trk_epk_clientId ON VerbEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_VerbEntity_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_64_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE XObjectEntity SET xObjectocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.xObjectocalChangeSeqNum 
  | ELSE NEXTVAL('XObjectEntity_lcsn_seq') END),
  | xObjectMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('XObjectEntity_mcsn_seq') 
  | ELSE NEW.xObjectMasterChangeSeqNum END)
  | WHERE xObjectUid = NEW.xObjectUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 64, NEW.xObjectUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_XObjectEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_XObjectEntity_trk_epk_clientId_tmp ON XObjectEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM XObjectEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT XObjectEntity_trk_nest.pk FROM XObjectEntity_trk XObjectEntity_trk_nest 
  |  WHERE XObjectEntity_trk_nest.clientId = XObjectEntity_trk.clientId AND
  |  XObjectEntity_trk_nest.epk = XObjectEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_XObjectEntity_trk_clientId_epk_csn  ON XObjectEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_XObjectEntity_trk_epk_clientId ON XObjectEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_XObjectEntity_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_60_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE StatementEntity SET statementLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLocalChangeSeqNum 
  | ELSE NEXTVAL('StatementEntity_lcsn_seq') END),
  | statementMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('StatementEntity_mcsn_seq') 
  | ELSE NEW.statementMasterChangeSeqNum END)
  | WHERE statementUid = NEW.statementUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 60, NEW.statementUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_StatementEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_StatementEntity_trk_epk_clientId_tmp ON StatementEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM StatementEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT StatementEntity_trk_nest.pk FROM StatementEntity_trk StatementEntity_trk_nest 
  |  WHERE StatementEntity_trk_nest.clientId = StatementEntity_trk.clientId AND
  |  StatementEntity_trk_nest.epk = StatementEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_StatementEntity_trk_clientId_epk_csn  ON StatementEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_StatementEntity_trk_epk_clientId ON StatementEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_StatementEntity_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_66_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ContextXObjectStatementJoin SET verbLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.verbLocalChangeSeqNum 
  | ELSE NEXTVAL('ContextXObjectStatementJoin_lcsn_seq') END),
  | verbMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ContextXObjectStatementJoin_mcsn_seq') 
  | ELSE NEW.verbMasterChangeSeqNum END)
  | WHERE contextXObjectStatementJoinUid = NEW.contextXObjectStatementJoinUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 66, NEW.contextXObjectStatementJoinUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ContextXObjectStatementJoin_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContextXObjectStatementJoin_trk_epk_clientId_tmp ON ContextXObjectStatementJoin_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContextXObjectStatementJoin_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContextXObjectStatementJoin_trk_nest.pk FROM ContextXObjectStatementJoin_trk ContextXObjectStatementJoin_trk_nest 
  |  WHERE ContextXObjectStatementJoin_trk_nest.clientId = ContextXObjectStatementJoin_trk.clientId AND
  |  ContextXObjectStatementJoin_trk_nest.epk = ContextXObjectStatementJoin_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContextXObjectStatementJoin_trk_clientId_epk_csn  ON ContextXObjectStatementJoin_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContextXObjectStatementJoin_trk_epk_clientId ON ContextXObjectStatementJoin_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContextXObjectStatementJoin_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_68_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE AgentEntity SET statementLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLocalChangeSeqNum 
  | ELSE NEXTVAL('AgentEntity_lcsn_seq') END),
  | statementMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('AgentEntity_mcsn_seq') 
  | ELSE NEW.statementMasterChangeSeqNum END)
  | WHERE agentUid = NEW.agentUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 68, NEW.agentUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_AgentEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_AgentEntity_trk_epk_clientId_tmp ON AgentEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM AgentEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT AgentEntity_trk_nest.pk FROM AgentEntity_trk AgentEntity_trk_nest 
  |  WHERE AgentEntity_trk_nest.clientId = AgentEntity_trk.clientId AND
  |  AgentEntity_trk_nest.epk = AgentEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_AgentEntity_trk_clientId_epk_csn  ON AgentEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_AgentEntity_trk_epk_clientId ON AgentEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_AgentEntity_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_70_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE StateEntity SET stateLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.stateLocalChangeSeqNum 
  | ELSE NEXTVAL('StateEntity_lcsn_seq') END),
  | stateMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('StateEntity_mcsn_seq') 
  | ELSE NEW.stateMasterChangeSeqNum END)
  | WHERE stateUid = NEW.stateUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 70, NEW.stateUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_StateEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_StateEntity_trk_epk_clientId_tmp ON StateEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM StateEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT StateEntity_trk_nest.pk FROM StateEntity_trk StateEntity_trk_nest 
  |  WHERE StateEntity_trk_nest.clientId = StateEntity_trk.clientId AND
  |  StateEntity_trk_nest.epk = StateEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_StateEntity_trk_clientId_epk_csn  ON StateEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_StateEntity_trk_epk_clientId ON StateEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_StateEntity_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_72_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE StateContentEntity SET stateContentLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.stateContentLocalChangeSeqNum 
  | ELSE NEXTVAL('StateContentEntity_lcsn_seq') END),
  | stateContentMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('StateContentEntity_mcsn_seq') 
  | ELSE NEW.stateContentMasterChangeSeqNum END)
  | WHERE stateContentUid = NEW.stateContentUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 72, NEW.stateContentUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_StateContentEntity_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_StateContentEntity_trk_epk_clientId_tmp ON StateContentEntity_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM StateContentEntity_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT StateContentEntity_trk_nest.pk FROM StateContentEntity_trk StateContentEntity_trk_nest 
  |  WHERE StateContentEntity_trk_nest.clientId = StateContentEntity_trk.clientId AND
  |  StateContentEntity_trk_nest.epk = StateContentEntity_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_StateContentEntity_trk_clientId_epk_csn  ON StateContentEntity_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_StateContentEntity_trk_epk_clientId ON StateContentEntity_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_StateContentEntity_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_74_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE XLangMapEntry SET statementLangMapLocalCsn =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.statementLangMapLocalCsn 
  | ELSE NEXTVAL('XLangMapEntry_lcsn_seq') END),
  | statementLangMapMasterCsn = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('XLangMapEntry_mcsn_seq') 
  | ELSE NEW.statementLangMapMasterCsn END)
  | WHERE statementLangMapUid = NEW.statementLangMapUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 74, NEW.statementLangMapUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_XLangMapEntry_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_XLangMapEntry_trk_epk_clientId_tmp ON XLangMapEntry_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM XLangMapEntry_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT XLangMapEntry_trk_nest.pk FROM XLangMapEntry_trk XLangMapEntry_trk_nest 
  |  WHERE XLangMapEntry_trk_nest.clientId = XLangMapEntry_trk.clientId AND
  |  XLangMapEntry_trk_nest.epk = XLangMapEntry_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_XLangMapEntry_trk_clientId_epk_csn  ON XLangMapEntry_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_XLangMapEntry_trk_epk_clientId ON XLangMapEntry_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_XLangMapEntry_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_164_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE School SET schoolLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.schoolLocalChangeSeqNum 
  | ELSE NEXTVAL('School_lcsn_seq') END),
  | schoolMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('School_mcsn_seq') 
  | ELSE NEW.schoolMasterChangeSeqNum END)
  | WHERE schoolUid = NEW.schoolUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 164, NEW.schoolUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_School_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_School_trk_epk_clientId_tmp ON School_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM School_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT School_trk_nest.pk FROM School_trk School_trk_nest 
  |  WHERE School_trk_nest.clientId = School_trk.clientId AND
  |  School_trk_nest.epk = School_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_School_trk_clientId_epk_csn  ON School_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_School_trk_epk_clientId ON School_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_School_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_200_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE SchoolMember SET schoolMemberLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.schoolMemberLocalChangeSeqNum 
  | ELSE NEXTVAL('SchoolMember_lcsn_seq') END),
  | schoolMemberMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('SchoolMember_mcsn_seq') 
  | ELSE NEW.schoolMemberMasterChangeSeqNum END)
  | WHERE schoolMemberUid = NEW.schoolMemberUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 200, NEW.schoolMemberUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_SchoolMember_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_SchoolMember_trk_epk_clientId_tmp ON SchoolMember_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM SchoolMember_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT SchoolMember_trk_nest.pk FROM SchoolMember_trk SchoolMember_trk_nest 
  |  WHERE SchoolMember_trk_nest.clientId = SchoolMember_trk.clientId AND
  |  SchoolMember_trk_nest.epk = SchoolMember_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_SchoolMember_trk_clientId_epk_csn  ON SchoolMember_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_SchoolMember_trk_epk_clientId ON SchoolMember_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_SchoolMember_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_201_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ClazzWork SET clazzWorkLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkLocalChangeSeqNum 
  | ELSE NEXTVAL('ClazzWork_lcsn_seq') END),
  | clazzWorkMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ClazzWork_mcsn_seq') 
  | ELSE NEW.clazzWorkMasterChangeSeqNum END)
  | WHERE clazzWorkUid = NEW.clazzWorkUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 201, NEW.clazzWorkUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWork_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWork_trk_epk_clientId_tmp ON ClazzWork_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWork_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWork_trk_nest.pk FROM ClazzWork_trk ClazzWork_trk_nest 
  |  WHERE ClazzWork_trk_nest.clientId = ClazzWork_trk.clientId AND
  |  ClazzWork_trk_nest.epk = ClazzWork_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWork_trk_clientId_epk_csn  ON ClazzWork_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWork_trk_epk_clientId ON ClazzWork_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWork_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_204_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ClazzWorkContentJoin SET clazzWorkContentJoinLCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkContentJoinLCSN 
  | ELSE NEXTVAL('ClazzWorkContentJoin_lcsn_seq') END),
  | clazzWorkContentJoinMCSN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ClazzWorkContentJoin_mcsn_seq') 
  | ELSE NEW.clazzWorkContentJoinMCSN END)
  | WHERE clazzWorkContentJoinUid = NEW.clazzWorkContentJoinUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 204, NEW.clazzWorkContentJoinUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWorkContentJoin_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWorkContentJoin_trk_epk_clientId_tmp ON ClazzWorkContentJoin_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWorkContentJoin_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWorkContentJoin_trk_nest.pk FROM ClazzWorkContentJoin_trk ClazzWorkContentJoin_trk_nest 
  |  WHERE ClazzWorkContentJoin_trk_nest.clientId = ClazzWorkContentJoin_trk.clientId AND
  |  ClazzWorkContentJoin_trk_nest.epk = ClazzWorkContentJoin_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWorkContentJoin_trk_clientId_epk_csn  ON ClazzWorkContentJoin_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWorkContentJoin_trk_epk_clientId ON ClazzWorkContentJoin_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWorkContentJoin_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_208_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE Comments SET commentsLCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.commentsLCSN 
  | ELSE NEXTVAL('Comments_lcsn_seq') END),
  | commentsMCSN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('Comments_mcsn_seq') 
  | ELSE NEW.commentsMCSN END)
  | WHERE commentsUid = NEW.commentsUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 208, NEW.commentsUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_Comments_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Comments_trk_epk_clientId_tmp ON Comments_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Comments_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Comments_trk_nest.pk FROM Comments_trk Comments_trk_nest 
  |  WHERE Comments_trk_nest.clientId = Comments_trk.clientId AND
  |  Comments_trk_nest.epk = Comments_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Comments_trk_clientId_epk_csn  ON Comments_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Comments_trk_epk_clientId ON Comments_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Comments_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_202_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ClazzWorkQuestion SET clazzWorkQuestionLCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionLCSN 
  | ELSE NEXTVAL('ClazzWorkQuestion_lcsn_seq') END),
  | clazzWorkQuestionMCSN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ClazzWorkQuestion_mcsn_seq') 
  | ELSE NEW.clazzWorkQuestionMCSN END)
  | WHERE clazzWorkQuestionUid = NEW.clazzWorkQuestionUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 202, NEW.clazzWorkQuestionUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWorkQuestion_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWorkQuestion_trk_epk_clientId_tmp ON ClazzWorkQuestion_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWorkQuestion_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWorkQuestion_trk_nest.pk FROM ClazzWorkQuestion_trk ClazzWorkQuestion_trk_nest 
  |  WHERE ClazzWorkQuestion_trk_nest.clientId = ClazzWorkQuestion_trk.clientId AND
  |  ClazzWorkQuestion_trk_nest.epk = ClazzWorkQuestion_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWorkQuestion_trk_clientId_epk_csn  ON ClazzWorkQuestion_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWorkQuestion_trk_epk_clientId ON ClazzWorkQuestion_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWorkQuestion_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_203_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ClazzWorkQuestionOption SET clazzWorkQuestionOptionLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionOptionLocalChangeSeqNum 
  | ELSE NEXTVAL('ClazzWorkQuestionOption_lcsn_seq') END),
  | clazzWorkQuestionOptionMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ClazzWorkQuestionOption_mcsn_seq') 
  | ELSE NEW.clazzWorkQuestionOptionMasterChangeSeqNum END)
  | WHERE clazzWorkQuestionOptionUid = NEW.clazzWorkQuestionOptionUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 203, NEW.clazzWorkQuestionOptionUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWorkQuestionOption_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWorkQuestionOption_trk_epk_clientId_tmp ON ClazzWorkQuestionOption_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWorkQuestionOption_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWorkQuestionOption_trk_nest.pk FROM ClazzWorkQuestionOption_trk ClazzWorkQuestionOption_trk_nest 
  |  WHERE ClazzWorkQuestionOption_trk_nest.clientId = ClazzWorkQuestionOption_trk.clientId AND
  |  ClazzWorkQuestionOption_trk_nest.epk = ClazzWorkQuestionOption_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWorkQuestionOption_trk_clientId_epk_csn  ON ClazzWorkQuestionOption_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWorkQuestionOption_trk_epk_clientId ON ClazzWorkQuestionOption_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWorkQuestionOption_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_206_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ClazzWorkSubmission SET clazzWorkSubmissionLCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkSubmissionLCSN 
  | ELSE NEXTVAL('ClazzWorkSubmission_lcsn_seq') END),
  | clazzWorkSubmissionMCSN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ClazzWorkSubmission_mcsn_seq') 
  | ELSE NEW.clazzWorkSubmissionMCSN END)
  | WHERE clazzWorkSubmissionUid = NEW.clazzWorkSubmissionUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 206, NEW.clazzWorkSubmissionUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWorkSubmission_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWorkSubmission_trk_epk_clientId_tmp ON ClazzWorkSubmission_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWorkSubmission_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWorkSubmission_trk_nest.pk FROM ClazzWorkSubmission_trk ClazzWorkSubmission_trk_nest 
  |  WHERE ClazzWorkSubmission_trk_nest.clientId = ClazzWorkSubmission_trk.clientId AND
  |  ClazzWorkSubmission_trk_nest.epk = ClazzWorkSubmission_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWorkSubmission_trk_clientId_epk_csn  ON ClazzWorkSubmission_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWorkSubmission_trk_epk_clientId ON ClazzWorkSubmission_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWorkSubmission_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_209_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ClazzWorkQuestionResponse SET clazzWorkQuestionResponseLCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.clazzWorkQuestionResponseLCSN 
  | ELSE NEXTVAL('ClazzWorkQuestionResponse_lcsn_seq') END),
  | clazzWorkQuestionResponseMCSN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ClazzWorkQuestionResponse_mcsn_seq') 
  | ELSE NEW.clazzWorkQuestionResponseMCSN END)
  | WHERE clazzWorkQuestionResponseUid = NEW.clazzWorkQuestionResponseUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 209, NEW.clazzWorkQuestionResponseUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ClazzWorkQuestionResponse_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ClazzWorkQuestionResponse_trk_epk_clientId_tmp ON ClazzWorkQuestionResponse_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ClazzWorkQuestionResponse_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ClazzWorkQuestionResponse_trk_nest.pk FROM ClazzWorkQuestionResponse_trk ClazzWorkQuestionResponse_trk_nest 
  |  WHERE ClazzWorkQuestionResponse_trk_nest.clientId = ClazzWorkQuestionResponse_trk.clientId AND
  |  ClazzWorkQuestionResponse_trk_nest.epk = ClazzWorkQuestionResponse_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ClazzWorkQuestionResponse_trk_clientId_epk_csn  ON ClazzWorkQuestionResponse_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ClazzWorkQuestionResponse_trk_epk_clientId ON ClazzWorkQuestionResponse_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ClazzWorkQuestionResponse_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_210_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ContentEntryProgress SET contentEntryProgressLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.contentEntryProgressLocalChangeSeqNum 
  | ELSE NEXTVAL('ContentEntryProgress_lcsn_seq') END),
  | contentEntryProgressMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ContentEntryProgress_mcsn_seq') 
  | ELSE NEW.contentEntryProgressMasterChangeSeqNum END)
  | WHERE contentEntryProgressUid = NEW.contentEntryProgressUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 210, NEW.contentEntryProgressUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ContentEntryProgress_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ContentEntryProgress_trk_epk_clientId_tmp ON ContentEntryProgress_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ContentEntryProgress_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ContentEntryProgress_trk_nest.pk FROM ContentEntryProgress_trk ContentEntryProgress_trk_nest 
  |  WHERE ContentEntryProgress_trk_nest.clientId = ContentEntryProgress_trk.clientId AND
  |  ContentEntryProgress_trk_nest.epk = ContentEntryProgress_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ContentEntryProgress_trk_clientId_epk_csn  ON ContentEntryProgress_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ContentEntryProgress_trk_epk_clientId ON ContentEntryProgress_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ContentEntryProgress_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_101_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE Report SET reportLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.reportLocalChangeSeqNum 
  | ELSE NEXTVAL('Report_lcsn_seq') END),
  | reportMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('Report_mcsn_seq') 
  | ELSE NEW.reportMasterChangeSeqNum END)
  | WHERE reportUid = NEW.reportUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 101, NEW.reportUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_Report_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_Report_trk_epk_clientId_tmp ON Report_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM Report_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT Report_trk_nest.pk FROM Report_trk Report_trk_nest 
  |  WHERE Report_trk_nest.clientId = Report_trk.clientId AND
  |  Report_trk_nest.epk = Report_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_Report_trk_clientId_epk_csn  ON Report_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_Report_trk_epk_clientId ON Report_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_Report_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_102_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE ReportFilter SET reportFilterLocalChangeSeqNum =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.reportFilterLocalChangeSeqNum 
  | ELSE NEXTVAL('ReportFilter_lcsn_seq') END),
  | reportFilterMasterChangeSeqNum = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('ReportFilter_mcsn_seq') 
  | ELSE NEW.reportFilterMasterChangeSeqNum END)
  | WHERE reportFilterUid = NEW.reportFilterUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 102, NEW.reportFilterUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_ReportFilter_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_ReportFilter_trk_epk_clientId_tmp ON ReportFilter_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM ReportFilter_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT ReportFilter_trk_nest.pk FROM ReportFilter_trk ReportFilter_trk_nest 
  |  WHERE ReportFilter_trk_nest.clientId = ReportFilter_trk.clientId AND
  |  ReportFilter_trk_nest.epk = ReportFilter_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_ReportFilter_trk_clientId_epk_csn  ON ReportFilter_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_ReportFilter_trk_epk_clientId ON ReportFilter_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_ReportFilter_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_301_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE LearnerGroup SET learnerGroupCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.learnerGroupCSN 
  | ELSE NEXTVAL('LearnerGroup_lcsn_seq') END),
  | learnerGroupMCSN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('LearnerGroup_mcsn_seq') 
  | ELSE NEW.learnerGroupMCSN END)
  | WHERE learnerGroupUid = NEW.learnerGroupUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 301, NEW.learnerGroupUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_LearnerGroup_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_LearnerGroup_trk_epk_clientId_tmp ON LearnerGroup_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM LearnerGroup_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT LearnerGroup_trk_nest.pk FROM LearnerGroup_trk LearnerGroup_trk_nest 
  |  WHERE LearnerGroup_trk_nest.clientId = LearnerGroup_trk.clientId AND
  |  LearnerGroup_trk_nest.epk = LearnerGroup_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_LearnerGroup_trk_clientId_epk_csn  ON LearnerGroup_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_LearnerGroup_trk_epk_clientId ON LearnerGroup_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_LearnerGroup_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_300_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE LearnerGroupMember SET learnerGroupMemberCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.learnerGroupMemberCSN 
  | ELSE NEXTVAL('LearnerGroupMember_lcsn_seq') END),
  | learnerGroupMemberMCSN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('LearnerGroupMember_mcsn_seq') 
  | ELSE NEW.learnerGroupMemberMCSN END)
  | WHERE learnerGroupMemberUid = NEW.learnerGroupMemberUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 300, NEW.learnerGroupMemberUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_LearnerGroupMember_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_LearnerGroupMember_trk_epk_clientId_tmp ON LearnerGroupMember_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM LearnerGroupMember_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT LearnerGroupMember_trk_nest.pk FROM LearnerGroupMember_trk LearnerGroupMember_trk_nest 
  |  WHERE LearnerGroupMember_trk_nest.clientId = LearnerGroupMember_trk.clientId AND
  |  LearnerGroupMember_trk_nest.epk = LearnerGroupMember_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_LearnerGroupMember_trk_clientId_epk_csn  ON LearnerGroupMember_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_LearnerGroupMember_trk_epk_clientId ON LearnerGroupMember_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_LearnerGroupMember_trk_epk_clientId_tmp")
        database.execSQL("""
  |CREATE OR REPLACE FUNCTION 
  | inccsn_302_fn() RETURNS trigger AS ${'$'}${'$'}
  | BEGIN  
  | UPDATE GroupLearningSession SET groupLearningSessionCSN =
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.groupLearningSessionCSN 
  | ELSE NEXTVAL('GroupLearningSession_lcsn_seq') END),
  | groupLearningSessionMCSN = 
  | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
  | THEN NEXTVAL('GroupLearningSession_mcsn_seq') 
  | ELSE NEW.groupLearningSessionMCSN END)
  | WHERE groupLearningSessionUid = NEW.groupLearningSessionUid;
  | INSERT INTO ChangeLog(chTableId, chEntityPk, dispatched, chTime) 
  | SELECT 302, NEW.groupLearningSessionUid, false, cast(extract(epoch from now()) * 1000 AS BIGINT)
  | WHERE COALESCE((SELECT master From SyncNode LIMIT 1), false);
  | RETURN null;
  | END ${'$'}${'$'}
  | LANGUAGE plpgsql
  """.trimMargin())
        database.execSQL("DROP INDEX IF EXISTS index_GroupLearningSession_trk_clientId_epk_rx_csn")
        database.execSQL("CREATE INDEX index_GroupLearningSession_trk_epk_clientId_tmp ON GroupLearningSession_trk (epk, clientId)")
        database.execSQL("""
  |DELETE FROM GroupLearningSession_trk 
  |  WHERE 
  |  pk != 
  |  (SELECT GroupLearningSession_trk_nest.pk FROM GroupLearningSession_trk GroupLearningSession_trk_nest 
  |  WHERE GroupLearningSession_trk_nest.clientId = GroupLearningSession_trk.clientId AND
  |  GroupLearningSession_trk_nest.epk = GroupLearningSession_trk.epk ORDER BY CSN DESC LIMIT 1) 
  """.trimMargin())
        database.execSQL("CREATE INDEX index_GroupLearningSession_trk_clientId_epk_csn  ON GroupLearningSession_trk (clientId, epk, csn)")
        database.execSQL("CREATE UNIQUE INDEX index_GroupLearningSession_trk_epk_clientId ON GroupLearningSession_trk (epk, clientId)")
        database.execSQL("DROP INDEX index_GroupLearningSession_trk_epk_clientId_tmp")
    }

}
