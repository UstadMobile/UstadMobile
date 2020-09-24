package com.ustadmobile.port.android.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.ustadmobile.core.db.UmAppDatabase
import org.junit.Rule
import org.junit.Test


class TestRoomMigration {

    private val TEST_DB = "migration-test"

    private val TEST_FROM_VERSION = 32

    @Rule @JvmField
    var helper: MigrationTestHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            UmAppDatabase::class.java.canonicalName, FrameworkSQLiteOpenHelperFactory())


    @Test
    fun migrate32to33() {
        helper.createDatabase(TEST_DB, 32).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 33, true,
                UmAppDatabase.MIGRATION_32_33)
    }


    @Test
    fun migrate33to34() {
        helper.createDatabase(TEST_DB, 33).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 34, true,
                UmAppDatabase.MIGRATION_33_34)
    }

    @Test
    fun migrate34to35() {
        helper.createDatabase(TEST_DB, 34).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 35, true,
                UmAppDatabase.MIGRATION_34_35)
    }



    @Test
    fun migrate35to36() {
        helper.createDatabase(TEST_DB, 35).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 36, true,
                UmAppDatabase.MIGRATION_35_36)
    }

    @Test
    fun migrate36to37() {
        helper.createDatabase(TEST_DB, 36).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 37, true,
            UmAppDatabase.MIGRATION_36_37)
    }

    @Test
    fun migrate37to38() {
        helper.createDatabase(TEST_DB, 37).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 38, true,
                UmAppDatabase.MIGRATION_37_38)
    }

    @Test
    fun migrate39to40() {
        helper.createDatabase(TEST_DB, 39).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 40, true,
                UmAppDatabase.MIGRATION_37_38)
    }

}