package com.ustadmobile.port.android.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.ustadmobile.core.db.UmAppDatabase

import org.junit.Assert
import org.junit.Rule
import org.junit.Test


class TestRoomMigration {

    private val TEST_DB = "migration-test"

    private val TEST_FROM_VERSION = 27

    @Rule @JvmField
    var helper: MigrationTestHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            UmAppDatabase::class.java.canonicalName, FrameworkSQLiteOpenHelperFactory())

//    @Test
//    fun migrate27To28() {
//        helper.createDatabase(TEST_DB, 27).apply {
//            close()
//        }
//
//        helper.runMigrationsAndValidate(TEST_DB, 28, true,
//                UmAppDatabase.MIGRATION_27_28)
//    }
//
//    @Test
//    fun migrate28to29() {
//        helper.createDatabase(TEST_DB, 28).apply {
//            close()
//        }
//
//        helper.runMigrationsAndValidate(TEST_DB, 29, true,
//                UmAppDatabase.MIGRATION_28_29)
//    }
//
//    @Test
//    fun migrate29to30() {
//        helper.createDatabase(TEST_DB, 29).apply {
//            close()
//        }
//
//        helper.runMigrationsAndValidate(TEST_DB, 30, true,
//                UmAppDatabase.MIGRATION_29_30)
//    }
//
//    @Test
//    fun migrate30to31() {
//        helper.createDatabase(TEST_DB, 30).apply {
//            close()
//        }
//
//        helper.runMigrationsAndValidate(TEST_DB, 31, true,
//                UmAppDatabase.MIGRATION_30_31)
//    }
//
//    @Test
//    fun migrate31to32() {
//        helper.createDatabase(TEST_DB, 31).apply {
//            close()
//        }
//
//        helper.runMigrationsAndValidate(TEST_DB, 32, true,
//                UmAppDatabase.MIGRATION_31_32)
//    }


//    @Test
//    fun migrate102029to102030(){
//        helper.createDatabase(TEST_DB, 102029).apply {
//            close()
//        }
//
//        helper.runMigrationsAndValidate(TEST_DB, 102030, true,
//                UmAppDatabase.MIGRATION_102029_102030)
//    }
//
//    @Test
//    fun migrate102030to102031(){
//        helper.createDatabase(TEST_DB, 102030).apply {
//            close()
//        }
//
//        helper.runMigrationsAndValidate(TEST_DB, 102031, true,
//                UmAppDatabase.MIGRATION_102030_102031)
//    }
//
//    @Test
//    fun migrate102031to102032(){
//        helper.createDatabase(TEST_DB, 102031).apply {
//            close()
//        }
//
//        helper.runMigrationsAndValidate(TEST_DB, 102032, true,
//                UmAppDatabase.MIGRATION_102031_102032)
//    }

    @Test
    fun migrate27to102032(){
        helper.createDatabase(TEST_DB, 27).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 102032, true,
                UmAppDatabase.MIGRATION_27_100028,
                UmAppDatabase.MIGRATION_100028_100029,
                UmAppDatabase.MIGRATION_100029_101029,
                UmAppDatabase.MIGRATION_101029_102029,
                UmAppDatabase.MIGRATION_102029_102030,
                UmAppDatabase.MIGRATION_102030_102031,
                UmAppDatabase.MIGRATION_102031_102032)
    }

    @Test
    fun migrate102029to102032(){
        helper.createDatabase(TEST_DB, 102029).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 102032, true,
                UmAppDatabase.MIGRATION_102029_102030,
                UmAppDatabase.MIGRATION_102030_102031,
                UmAppDatabase.MIGRATION_102031_102032)
    }

}