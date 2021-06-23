package com.ustadmobile.port.android.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.ustadmobile.core.db.UmAppDatabase
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random


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
    fun migrate38to39() {
        helper.createDatabase(TEST_DB, 38).apply {
            //The NodeClientId would always have been inserted by the onCreate callback in the previous version
            execSQL("INSERT INTO SyncNode(nodeClientId,master) VALUES (${Random.nextInt(1, Int.MAX_VALUE)}, 0)")
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 39, true,
                UmAppDatabase.MIGRATION_38_39)
    }

    @Test
    fun migrate39to40() {
        helper.createDatabase(TEST_DB, 39).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 40, true,
                UmAppDatabase.MIGRATION_39_40)
    }

    @Test
    fun migrate40to41() {
        helper.createDatabase(TEST_DB, 40).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 41, true,
                UmAppDatabase.MIGRATION_40_41)
    }


    @Test
    fun migrate41to42() {
        helper.createDatabase(TEST_DB, 41).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 42, true,
                UmAppDatabase.MIGRATION_41_42)
    }
    @Test
    fun migrate42to43() {
        helper.createDatabase(TEST_DB, 42).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 43, true,
                UmAppDatabase.MIGRATION_42_43)
    }

    @Test
    fun migrate43to44() {
        helper.createDatabase(TEST_DB, 43).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 44, true,
                UmAppDatabase.MIGRATION_43_44)
    }

    @Test
    fun migrate44to45() {
        helper.createDatabase(TEST_DB, 44).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 45, true,
                UmAppDatabase.MIGRATION_44_45)
    }

    @Test
    fun migrate45to46() {
        helper.createDatabase(TEST_DB, 45).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 46, true,
                UmAppDatabase.MIGRATION_45_46)
    }

    @Test
    fun migrate46to47() {
        helper.createDatabase(TEST_DB, 46).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 47, true,
                UmAppDatabase.MIGRATION_46_47)
    }


    @Test
    fun migrate47to48() {
        helper.createDatabase(TEST_DB, 47).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 47, true,
                UmAppDatabase.MIGRATION_47_48)
    }

    @Test
    fun migrate48to49() {
        helper.createDatabase(TEST_DB, 48).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 49, true,
                UmAppDatabase.MIGRATION_48_49)
    }


    @Test
    fun migrate49to50() {
        helper.createDatabase(TEST_DB, 49).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 50, true,
                UmAppDatabase.MIGRATION_49_50)
    }

    @Test
    fun migrate50to51() {
        helper.createDatabase(TEST_DB, 50).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 51, true,
                UmAppDatabase.MIGRATION_50_51)
    }

    @Test
    fun migrate51to52() {
        helper.createDatabase(TEST_DB, 51).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 52, true,
                UmAppDatabase.MIGRATION_51_52)
    }

    @Test
    fun migrate52to53() {
        helper.createDatabase(TEST_DB, 52).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 53, true,
                UmAppDatabase.MIGRATION_52_53)
    }

    @Test
    fun migrate53to54() {
        helper.createDatabase(TEST_DB, 53).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 54, true,
                UmAppDatabase.MIGRATION_53_54)
    }

    @Test
    fun migrate54to55() {
        helper.createDatabase(TEST_DB, 54).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 55, true,
                UmAppDatabase.MIGRATION_54_55)
    }

    @Test
    fun migrate55to56() {
        helper.createDatabase(TEST_DB, 55).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 56, true,
                UmAppDatabase.MIGRATION_55_56)
    }


    @Test
    fun migrate56to57() {
        helper.createDatabase(TEST_DB, 56).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 57, true,
                UmAppDatabase.MIGRATION_56_57)
    }




    @Test
    fun migrate57to58() {
        helper.createDatabase(TEST_DB, 57).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 58, true,
                UmAppDatabase.MIGRATION_57_58)
    }

    @Test
    fun migrate58to59() {
        helper.createDatabase(TEST_DB, 58).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 59, true,
                UmAppDatabase.MIGRATION_58_59)
    }

    @Test
    fun migrate59to60() {
        helper.createDatabase(TEST_DB, 59).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 60, true,
                UmAppDatabase.MIGRATION_59_60)
    }

    @Test
    fun migrate60to61() {
        helper.createDatabase(TEST_DB, 60).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 61, true,
                UmAppDatabase.MIGRATION_60_61)
    }

    @Test
    fun migrate61to62() {
        helper.createDatabase(TEST_DB, 61).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 62, true,
                UmAppDatabase.MIGRATION_61_62)
    }

    @Test
    fun migrate62to63() {
        helper.createDatabase(TEST_DB, 62).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 63, true,
            UmAppDatabase.MIGRATION_62_63)
    }

    @Test
    fun migrate63to64() {
        helper.createDatabase(TEST_DB, 63).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 64, true,
            UmAppDatabase.MIGRATION_63_64)
    }

    @Test
    fun migrate64to65() {
        helper.createDatabase(TEST_DB, 64).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 65, true,
                UmAppDatabase.MIGRATION_64_65)
    }

    @Test
    fun migrate65to66() {
        helper.createDatabase(TEST_DB, 65).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 66, true,
            UmAppDatabase.MIGRATION_65_66)
    }

    @Test
    fun migrate66to67() {
        helper.createDatabase(TEST_DB, 66).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 67, true,
                UmAppDatabase.MIGRATION_66_67)
    }

}