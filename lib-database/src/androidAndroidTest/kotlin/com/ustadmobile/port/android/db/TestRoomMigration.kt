 package com.ustadmobile.port.android.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.ustadmobile.core.db.*
import com.ustadmobile.door.asRoomMigration
import com.ustadmobile.door.util.systemTimeInMillis
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.db.ext.*
import com.ustadmobile.core.db.UmAppDatabaseReplicationMigration91_92


class TestRoomMigration {

    @Rule @JvmField
    var helper: MigrationTestHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            UmAppDatabase::class.java.canonicalName, FrameworkSQLiteOpenHelperFactory())


    @Test
    fun migrate91to92() {
        helper.createDatabase(TEST_DB, 91).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 92, true,
            UmAppDatabaseReplicationMigration91_92.asRoomMigration())
    }

    @Test
    fun migrate92to93() {
        helper.createDatabase(TEST_DB, 92).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 93, true,
            MIGRATION_92_93.asRoomMigration())
    }

    @Test
    fun migrate93to94() {
        helper.createDatabase(TEST_DB, 93).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 94, true,
            MIGRATION_93_94.asRoomMigration())
    }

    @Test
    fun migrate94to95() {
        helper.createDatabase(TEST_DB, 94).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 95, true,
            MIGRATION_94_95.asRoomMigration())
    }

    @Test
    fun migrate95to96() {
        helper.createDatabase(TEST_DB, 95).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 96, true,
            MIGRATION_95_96.asRoomMigration())
    }

    @Test
    fun migrate96to97() {
        helper.createDatabase(TEST_DB, 96).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 97, true,
                MIGRATION_96_97.asRoomMigration())
    }

    @Test
    fun migrate97to98() {
        helper.createDatabase(TEST_DB, 97).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 98, true,
            MIGRATION_97_98.asRoomMigration())
    }

    @Test
    fun migrate98to99() {
        helper.createDatabase(TEST_DB, 98).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 99, true,
                MIGRATION_98_99.asRoomMigration())
    }


    @Test
    fun migrate99to100() {
        helper.createDatabase(TEST_DB, 99).apply {
            execSQL("INSERT INTO ContainerEntryFile(cefUid, cefMd5, cefPath, ceTotalSize, ceCompressedSize, compression, lastModified) " +
                    "VALUES(1, 'abc', '/abc', 200, 100, 1, ${systemTimeInMillis()})")
            execSQL("INSERT INTO ContainerEntryFile(cefUid, cefMd5, cefPath, ceTotalSize, ceCompressedSize, compression, lastModified) " +
                    "VALUES(2, 'abc', '/abc', 200, 100, 1, ${systemTimeInMillis()})")
            execSQL("INSERT INTO ContainerEntryFile(cefUid, cefMd5, cefPath, ceTotalSize, ceCompressedSize, compression, lastModified) " +
                    "VALUES(3, 'def', '/def', 300, 100, 1, ${systemTimeInMillis()})")


            execSQL("INSERT INTO ContainerEntry(ceUid, ceContainerUid, cePath, ceCefUid) " +
                    "VALUES(1, 1, 'abc.txt', 1)")
            execSQL("INSERT INTO ContainerEntry(ceUid, ceContainerUid, cePath, ceCefUid) " +
                    "VALUES(2, 2, 'abc.txt', 2)")
            execSQL("INSERT INTO ContainerEntry(ceUid, ceContainerUid, cePath, ceCefUid)" +
                    "VALUES(3, 2, 'def', 3)")

            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 100, true,
            MIGRATION_99_100.asRoomMigration()
        ).apply {

            val cefUidChangeCursor = query("SELECT ceCefUid FROM ContainerEntry WHERE ceUid = 2")
            cefUidChangeCursor.moveToFirst()
            val cefUid = cefUidChangeCursor.getInt(0)
            Assert.assertEquals(
                "ContainerEntry 2 link was changed to the first available containerentryfile",
                1, cefUid)
            cefUidChangeCursor.close()

            val ceRemovedCursor = query("SELECT cefUid FROM ContainerEntryFile WHERE cefUid = 2")
            Assert.assertFalse("Duplicate container entry file was removed",
                ceRemovedCursor.moveToFirst())
        }
    }

    @Test
    fun migrate100to101() {
        helper.createDatabase(TEST_DB, 100).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 101, true,
            MIGRATION_100_101.asRoomMigration())
    }

    @Test
    fun migrate101to102() {
        helper.createDatabase(TEST_DB, 101).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 102, true,
            MIGRATION_101_102.asRoomMigration())
    }

    @Test
    fun migrate102to103() {
        helper.createDatabase(TEST_DB, 102).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 103, true,
            MIGRATION_102_103.asRoomMigration())
    }

    @Test
    fun migrate103to104() {
        helper.createDatabase(TEST_DB, 103).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 104, true,
            MIGRATION_103_104.asRoomMigration())
    }

    @Test
    fun migrate104to105() {
        helper.createDatabase(TEST_DB, 104).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 105, true,
            MIGRATION_104_105.asRoomMigration())
    }


    @Test
    fun migrate105to106() {
        helper.createDatabase(TEST_DB, 105).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 106, true,
            MIGRATION_105_106.asRoomMigration())
    }

    @Test
    fun migrate106To107() {
        helper.createDatabase(TEST_DB, 106).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 107, true,
            MIGRATION_106_107.asRoomMigration())
    }


    companion object {
        const val TEST_DB = "migration-test"
    }
}