 package com.ustadmobile.port.android.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.ustadmobile.core.db.*
import com.ustadmobile.door.asRoomMigration
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.db.ext.*


class TestRoomMigration {

    @Rule @JvmField
    var helper: MigrationTestHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            UmAppDatabase::class.java, listOf(), FrameworkSQLiteOpenHelperFactory())

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

    @Test
    fun migrate107To108() {
        helper.createDatabase(TEST_DB, 107).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 108, true,
            MIGRATION_107_108.asRoomMigration())
    }

    @Test
    fun migrate108To109() {
        helper.createDatabase(TEST_DB, 108).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 109, true,
            MIGRATION_108_109.asRoomMigration())
    }

    @Test
    fun migrate120to121() {
        helper.createDatabase(TEST_DB, 120).apply {
            close()
        }


        helper.runMigrationsAndValidate(TEST_DB, 121, true,
            MIGRATION_120_121.asRoomMigration())
    }


    companion object {
        const val TEST_DB = "migration-test"
    }
}