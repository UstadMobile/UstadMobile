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


class TestRoomMigration {

    @Rule @JvmField
    var helper: MigrationTestHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            UmAppDatabase::class.java.canonicalName, FrameworkSQLiteOpenHelperFactory())

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
    fun migrate83to84() {
        helper.createDatabase(TEST_DB, 83).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 84, true,
            UmAppDatabase.MIGRATION_83_84.asRoomMigration())
    }


    @Test
    fun migrate84to85() {
        helper.createDatabase(TEST_DB, 84).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 85, true,
                UmAppDatabase.MIGRATION_84_85.asRoomMigration())
    }

    @Test
    fun migrate85to86() {
        helper.createDatabase(TEST_DB, 85).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 86, true,
                UmAppDatabase.MIGRATION_85_86.asRoomMigration())
    }
    @Test
    fun migrate86to87() {
        helper.createDatabase(TEST_DB, 86).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 87, true,
                UmAppDatabase.MIGRATION_86_87.asRoomMigration())
    }

    @Test
    fun migrate87to88(){
        helper.createDatabase(TEST_DB, 87).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 88, true,
                UmAppDatabase.MIGRATION_87_88.asRoomMigration())
    }

    @Test
    fun migrate88to89(){
        helper.createDatabase(TEST_DB, 88).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 89, true,
            UmAppDatabase.MIGRATION_88_89.asRoomMigration())
    }



    @Test
    fun migrate89to90(){
        helper.createDatabase(TEST_DB, 89).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 90, true,
            UmAppDatabase.MIGRATION_89_90.asRoomMigration())
    }

    @Test
    fun migrate90to91(){
        helper.createDatabase(TEST_DB, 90).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 91, true,
                UmAppDatabase.MIGRATION_90_91.asRoomMigration())
    }

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
            UmAppDatabase.MIGRATION_92_93.asRoomMigration())
    }

    @Test
    fun migrate93to94() {
        helper.createDatabase(TEST_DB, 93).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 94, true,
            UmAppDatabase.MIGRATION_93_94.asRoomMigration())
    }

    @Test
    fun migrate94to95() {
        helper.createDatabase(TEST_DB, 94).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 95, true,
            UmAppDatabase.MIGRATION_94_95.asRoomMigration())
    }

    @Test
    fun migrate95to96() {
        helper.createDatabase(TEST_DB, 95).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 96, true,
            UmAppDatabase.MIGRATION_95_96.asRoomMigration())
    }

    @Test
    fun migrate96to97() {
        helper.createDatabase(TEST_DB, 96).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 97, true,
                UmAppDatabase.MIGRATION_96_97.asRoomMigration())
    }

    @Test
    fun migrate97to98() {
        helper.createDatabase(TEST_DB, 97).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 98, true,
            UmAppDatabase.MIGRATION_97_98.asRoomMigration())
    }

    @Test
    fun migrate98to99() {
        helper.createDatabase(TEST_DB, 98).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 99, true,
                UmAppDatabase.MIGRATION_98_99.asRoomMigration())
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
            UmAppDatabase.MIGRATION_99_100.asRoomMigration()
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
            UmAppDatabase.MIGRATION_100_101.asRoomMigration())
    }

    @Test
    fun migrate101to102() {
        helper.createDatabase(TEST_DB, 101).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 102, true,
            UmAppDatabase.MIGRATION_101_102.asRoomMigration())
    }

    @Test
    fun migrate102to103() {
        helper.createDatabase(TEST_DB, 102).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 103, true,
            UmAppDatabase.MIGRATION_102_103.asRoomMigration())
    }

    @Test
    fun migrate103to104() {
        helper.createDatabase(TEST_DB, 103).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 104, true,
            UmAppDatabase.MIGRATION_103_104.asRoomMigration())
    }

    @Test
    fun migrate104to105() {
        helper.createDatabase(TEST_DB, 104).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 105, true,
            UmAppDatabase.MIGRATION_104_105.asRoomMigration())
    }


    @Test
    fun migrate105to106() {
        helper.createDatabase(TEST_DB, 105).apply {
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 106, true,
            UmAppDatabase.MIGRATION_105_106.asRoomMigration())
    }


    companion object {
        const val TEST_DB = "migration-test"
    }
}