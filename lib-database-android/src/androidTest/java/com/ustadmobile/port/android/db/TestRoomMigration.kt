package com.ustadmobile.port.android.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.asRoomMigration
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random


class TestRoomMigration {

    @Rule @JvmField
    var helper: MigrationTestHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            UmAppDatabase::class.java.canonicalName, FrameworkSQLiteOpenHelperFactory())


    @Test
    fun migrate32to33() {
        helper.createDatabase(TEST_DB, 32).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 33, true,
                UmAppDatabase.MIGRATION_32_33.asRoomMigration())
    }


    @Test
    fun migrate33to34() {
        helper.createDatabase(TEST_DB, 33).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 34, true,
                UmAppDatabase.MIGRATION_33_34.asRoomMigration())
    }

    @Test
    fun migrate34to35() {
        helper.createDatabase(TEST_DB, 34).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 35, true,
                UmAppDatabase.MIGRATION_34_35.asRoomMigration())
    }



    @Test
    fun migrate35to36() {
        helper.createDatabase(TEST_DB, 35).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 36, true,
                UmAppDatabase.MIGRATION_35_36.asRoomMigration())
    }

    @Test
    fun migrate36to37() {
        helper.createDatabase(TEST_DB, 36).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 37, true,
            UmAppDatabase.MIGRATION_36_37.asRoomMigration())
    }

    @Test
    fun migrate37to38() {
        helper.createDatabase(TEST_DB, 37).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 38, true,
                UmAppDatabase.MIGRATION_37_38.asRoomMigration())
    }

    @Test
    fun migrate38to39() {
        helper.createDatabase(TEST_DB, 38).apply {
            //The NodeClientId would always have been inserted by the onCreate callback in the previous version
            execSQL("INSERT INTO SyncNode(nodeClientId,master) VALUES (${Random.nextInt(1, Int.MAX_VALUE)}, 0)")
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 39, true,
                UmAppDatabase.MIGRATION_38_39.asRoomMigration())
    }

    @Test
    fun migrate39to40() {
        helper.createDatabase(TEST_DB, 39).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 40, true,
                UmAppDatabase.MIGRATION_39_40.asRoomMigration())
    }

    @Test
    fun migrate40to41() {
        helper.createDatabase(TEST_DB, 40).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 41, true,
                UmAppDatabase.MIGRATION_40_41.asRoomMigration())
    }


    @Test
    fun migrate41to42() {
        helper.createDatabase(TEST_DB, 41).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 42, true,
                UmAppDatabase.MIGRATION_41_42.asRoomMigration())
    }
    @Test
    fun migrate42to43() {
        helper.createDatabase(TEST_DB, 42).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 43, true,
                UmAppDatabase.MIGRATION_42_43.asRoomMigration())
    }

    @Test
    fun migrate43to44() {
        helper.createDatabase(TEST_DB, 43).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 44, true,
                UmAppDatabase.MIGRATION_43_44.asRoomMigration())
    }

    @Test
    fun migrate44to45() {
        helper.createDatabase(TEST_DB, 44).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 45, true,
                UmAppDatabase.MIGRATION_44_45.asRoomMigration())
    }

    @Test
    fun migrate45to46() {
        helper.createDatabase(TEST_DB, 45).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 46, true,
                UmAppDatabase.MIGRATION_45_46.asRoomMigration())
    }

    @Test
    fun migrate46to47() {
        helper.createDatabase(TEST_DB, 46).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 47, true,
                UmAppDatabase.MIGRATION_46_47.asRoomMigration())
    }


    @Test
    fun migrate47to48() {
        helper.createDatabase(TEST_DB, 47).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 47, true,
                UmAppDatabase.MIGRATION_47_48.asRoomMigration())
    }

    @Test
    fun migrate48to49() {
        helper.createDatabase(TEST_DB, 48).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 49, true,
                UmAppDatabase.MIGRATION_48_49.asRoomMigration())
    }


    @Test
    fun migrate49to50() {
        helper.createDatabase(TEST_DB, 49).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 50, true,
                UmAppDatabase.MIGRATION_49_50.asRoomMigration())
    }

    @Test
    fun migrate50to51() {
        helper.createDatabase(TEST_DB, 50).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 51, true,
                UmAppDatabase.MIGRATION_50_51.asRoomMigration())
    }

    @Test
    fun migrate51to52() {
        helper.createDatabase(TEST_DB, 51).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 52, true,
                UmAppDatabase.MIGRATION_51_52.asRoomMigration())
    }

    @Test
    fun migrate52to53() {
        helper.createDatabase(TEST_DB, 52).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 53, true,
                UmAppDatabase.MIGRATION_52_53.asRoomMigration())
    }

    @Test
    fun migrate53to54() {
        helper.createDatabase(TEST_DB, 53).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 54, true,
                UmAppDatabase.MIGRATION_53_54.asRoomMigration())
    }

    @Test
    fun migrate54to55() {
        helper.createDatabase(TEST_DB, 54).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 55, true,
                UmAppDatabase.MIGRATION_54_55.asRoomMigration())
    }

    @Test
    fun migrate55to56() {
        helper.createDatabase(TEST_DB, 55).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 56, true,
                UmAppDatabase.MIGRATION_55_56.asRoomMigration())
    }


    @Test
    fun migrate56to57() {
        helper.createDatabase(TEST_DB, 56).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 57, true,
                UmAppDatabase.MIGRATION_56_57.asRoomMigration())
    }




    @Test
    fun migrate57to58() {
        helper.createDatabase(TEST_DB, 57).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 58, true,
                UmAppDatabase.MIGRATION_57_58.asRoomMigration())
    }

    @Test
    fun migrate58to59() {
        helper.createDatabase(TEST_DB, 58).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 59, true,
                UmAppDatabase.MIGRATION_58_59.asRoomMigration())
    }

    @Test
    fun migrate59to60() {
        helper.createDatabase(TEST_DB, 59).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 60, true,
                UmAppDatabase.MIGRATION_59_60.asRoomMigration())
    }

    @Test
    fun migrate60to61() {
        helper.createDatabase(TEST_DB, 60).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 61, true,
                UmAppDatabase.MIGRATION_60_61.asRoomMigration())
    }

    @Test
    fun migrate61to62() {
        helper.createDatabase(TEST_DB, 61).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 62, true,
                UmAppDatabase.MIGRATION_61_62.asRoomMigration())
    }

    @Test
    fun migrate62to63() {
        helper.createDatabase(TEST_DB, 62).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 63, true,
            UmAppDatabase.MIGRATION_62_63.asRoomMigration())
    }

    @Test
    fun migrate63to64() {
        helper.createDatabase(TEST_DB, 63).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 64, true,
            UmAppDatabase.MIGRATION_63_64.asRoomMigration())
    }

    @Test
    fun migrate64to65() {
        helper.createDatabase(TEST_DB, 64).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 65, true,
                UmAppDatabase.MIGRATION_64_65.asRoomMigration())
    }

    @Test
    fun migrate65to66() {
        helper.createDatabase(TEST_DB, 65).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 66, true,
            UmAppDatabase.MIGRATION_65_66.asRoomMigration())
    }

    @Test
    fun migrate66to67() {
        helper.createDatabase(TEST_DB, 66).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 67, true,
                UmAppDatabase.MIGRATION_66_67.asRoomMigration())
    }

    @Test
    fun migrate67to68() {
        helper.createDatabase(TEST_DB, 67).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 68, true,
            com.ustadmobile.core.db.UmAppDatabase.migrate67to68(42).asRoomMigration())
    }

    @Test
    fun migrate68to69() {
        helper.createDatabase(TEST_DB, 68).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 69, true,
                UmAppDatabase.MIGRATION_68_69.asRoomMigration())
    }

    @Test
    fun migrate69to70() {
        helper.createDatabase(TEST_DB, 69).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 70, true,
            UmAppDatabase.MIGRATION_69_70.asRoomMigration())
    }

    @Test
    fun migrate70to71() {
        helper.createDatabase(TEST_DB, 70).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 71, true,
            UmAppDatabase.MIGRATION_70_71.asRoomMigration())
    }

    @Test
    fun migrate71to72() {
        helper.createDatabase(TEST_DB, 71).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 72, true,
            UmAppDatabase.MIGRATION_71_72.asRoomMigration())
    }


    @Test
    fun migrate72to73() {
        helper.createDatabase(TEST_DB, 72).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 73, true,
                UmAppDatabase.MIGRATION_72_73.asRoomMigration())
    }


    @Test
    fun migrate73to74() {
        helper.createDatabase(TEST_DB, 73).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 74, true,
                UmAppDatabase.MIGRATION_73_74.asRoomMigration())
    }

    @Test
    fun migrate74to75() {
        helper.createDatabase(TEST_DB, 74).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 75, true,
                UmAppDatabase.MIGRATION_74_75.asRoomMigration())
    }

    @Test
    fun migrate75to76() {
        helper.createDatabase(TEST_DB, 75).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 76, true,
                UmAppDatabase.MIGRATION_75_76.asRoomMigration())
    }

    @Test
    fun migrate76to77() {
        helper.createDatabase(TEST_DB, 76).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 77, true,
            UmAppDatabase.MIGRATION_76_77.asRoomMigration())
    }

    @Test
    fun migrate77to78() {
        helper.createDatabase(TEST_DB, 77).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 78, true,
            UmAppDatabase.MIGRATION_77_78.asRoomMigration())
    }

    @Test
    fun migrate78to79() {
        helper.createDatabase(TEST_DB, 78).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 79, true,
            UmAppDatabase.MIGRATION_78_79.asRoomMigration())
    }

    // 7/August/2021
    @Test
    fun migrate79to80() {
        helper.createDatabase(TEST_DB, 79).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 80, true,
            UmAppDatabase.MIGRATION_79_80.asRoomMigration())
    }

    // 2/Sept/2021
    @Test
    fun migrate80to81() {
        helper.createDatabase(TEST_DB, 80).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 81, true,
            UmAppDatabase.MIGRATION_80_81.asRoomMigration())
    }

    @Test
    fun migrate81to82() {
        helper.createDatabase(TEST_DB, 81).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 82, true,
            UmAppDatabase.MIGRATION_81_82.asRoomMigration())
    }

    @Test
    fun migrate82to83() {
        helper.createDatabase(TEST_DB, 82).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 83, true,
            UmAppDatabase.MIGRATION_82_83.asRoomMigration())
    }

    @Test
    fun migrate84to85() {
        helper.createDatabase(TEST_DB, 84).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 85, true,
                UmAppDatabase.MIGRATION_84_85.asRoomMigration())
    }


    companion object {
        const val TEST_DB = "migration-test"
    }
}