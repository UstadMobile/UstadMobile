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

    @Test
    fun migrate27To28() {
        helper.createDatabase(TEST_DB, 27).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 28, true,
                UmAppDatabase.MIGRATION_27_28)
    }

    @Test
    fun migrate28to29() {
        helper.createDatabase(TEST_DB, 28).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 29, true,
                UmAppDatabase.MIGRATION_28_29)
    }

}