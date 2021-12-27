package com.ustadmobile.core.db

import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.door.ext.execSqlBatch
import com.ustadmobile.door.migration.DoorMigrationSync

private fun DoorSqlDatabase.dropOldSqliteTriggers() {
    //Drop old triggers
    val db = this
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_14")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_14")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_14")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_14")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_15")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_15")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_15")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_15")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_21")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_21")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_21")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_21")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_17")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_17")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_17")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_17")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_28")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_28")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_28")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_28")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_99")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_99")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_99")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_99")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_173")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_173")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_173")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_173")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_53")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_53")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_53")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_53")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_56")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_56")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_56")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_56")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_57")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_57")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_57")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_57")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_55")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_55")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_55")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_55")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_9")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_9")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_9")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_9")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_6")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_6")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_6")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_6")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_65")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_65")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_65")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_65")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_410")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_410")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_410")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_410")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_178")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_178")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_178")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_178")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_42")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_42")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_42")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_42")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_3")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_3")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_3")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_3")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_7")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_7")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_7")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_7")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_8")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_8")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_8")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_8")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_2")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_2")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_2")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_2")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_1")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_1")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_1")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_1")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_13")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_13")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_13")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_13")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_10")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_10")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_10")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_10")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_45")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_45")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_45")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_45")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_47")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_47")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_47")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_47")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_43")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_43")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_43")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_43")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_44")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_44")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_44")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_44")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_50")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_50")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_50")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_50")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_51")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_51")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_51")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_51")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_62")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_62")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_62")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_62")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_64")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_64")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_64")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_64")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_60")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_60")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_60")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_60")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_66")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_66")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_66")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_66")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_68")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_68")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_68")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_68")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_70")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_70")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_70")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_70")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_72")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_72")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_72")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_72")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_74")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_74")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_74")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_74")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_164")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_164")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_164")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_164")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_200")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_200")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_200")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_200")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_208")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_208")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_208")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_208")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_101")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_101")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_101")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_101")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_189")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_189")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_189")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_189")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_301")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_301")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_301")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_301")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_300")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_300")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_300")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_300")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_302")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_302")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_302")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_302")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_272")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_272")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_272")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_272")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_134")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_134")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_134")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_134")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_512")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_512")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_512")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_512")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_48")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_48")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_48")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_48")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_419")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_419")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_419")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_419")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_520")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_520")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_520")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_520")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_521")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_521")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_521")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_521")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_678")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_678")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_678")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_678")
    db.execSQL("DROP TRIGGER IF EXISTS INS_LOC_679")
    db.execSQL("DROP TRIGGER IF EXISTS INS_PRI_679")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_LOC_679")
    db.execSQL("DROP TRIGGER IF EXISTS UPD_PRI_679")
}

private fun DoorSqlDatabase.dropOldPostgresTriggers() {
    /*
    select trigger_name, event_object_table, 'db.execSQL("DROP TRIGGER IF EXISTS ' || trigger_name || ' ON ' || event_object_table || '")'
    from information_schema.triggers
    WHERE trigger_name LIKE 'inc%';
     */
    val db = this
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_15_trig ON clazzlogattendancerecord")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_15_trig ON clazzlogattendancerecord")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_21_trig ON schedule")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_21_trig ON schedule")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_17_trig ON daterange")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_17_trig ON daterange")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_28_trig ON holidaycalendar")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_28_trig ON holidaycalendar")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_99_trig ON holiday")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_99_trig ON holiday")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_173_trig ON scheduledcheck")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_173_trig ON scheduledcheck")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_53_trig ON auditlog")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_53_trig ON auditlog")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_56_trig ON customfield")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_56_trig ON customfield")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_57_trig ON customfieldvalue")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_57_trig ON customfieldvalue")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_55_trig ON customfieldvalueoption")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_55_trig ON customfieldvalueoption")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_9_trig ON person")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_9_trig ON person")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_6_trig ON clazz")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_6_trig ON clazz")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_65_trig ON clazzenrolment")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_65_trig ON clazzenrolment")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_410_trig ON leavingreason")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_410_trig ON leavingreason")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_178_trig ON personcustomfieldvalue")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_178_trig ON personcustomfieldvalue")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_42_trig ON contententry")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_42_trig ON contententry")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_3_trig ON contententrycontentcategoryjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_3_trig ON contententrycontentcategoryjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_7_trig ON contententryparentchildjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_7_trig ON contententryparentchildjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_8_trig ON contententryrelatedentryjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_8_trig ON contententryrelatedentryjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_2_trig ON contentcategoryschema")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_2_trig ON contentcategoryschema")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_1_trig ON contentcategory")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_1_trig ON contentcategory")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_13_trig ON language")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_13_trig ON language")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_10_trig ON languagevariant")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_10_trig ON languagevariant")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_45_trig ON role")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_45_trig ON role")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_47_trig ON entityrole")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_47_trig ON entityrole")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_43_trig ON persongroup")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_43_trig ON persongroup")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_44_trig ON persongroupmember")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_44_trig ON persongroupmember")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_50_trig ON personpicture")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_50_trig ON personpicture")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_51_trig ON container")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_51_trig ON container")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_62_trig ON verbentity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_62_trig ON verbentity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_64_trig ON xobjectentity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_64_trig ON xobjectentity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_60_trig ON statemententity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_60_trig ON statemententity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_66_trig ON contextxobjectstatementjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_66_trig ON contextxobjectstatementjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_68_trig ON agententity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_68_trig ON agententity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_70_trig ON stateentity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_70_trig ON stateentity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_72_trig ON statecontententity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_72_trig ON statecontententity")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_74_trig ON xlangmapentry")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_74_trig ON xlangmapentry")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_164_trig ON school")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_164_trig ON school")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_200_trig ON schoolmember")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_200_trig ON schoolmember")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_208_trig ON comments")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_208_trig ON comments")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_101_trig ON report")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_101_trig ON report")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_189_trig ON site")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_189_trig ON site")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_301_trig ON learnergroup")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_301_trig ON learnergroup")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_300_trig ON learnergroupmember")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_300_trig ON learnergroupmember")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_302_trig ON grouplearningsession")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_302_trig ON grouplearningsession")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_272_trig ON siteterms")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_272_trig ON siteterms")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_134_trig ON clazzcontentjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_134_trig ON clazzcontentjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_512_trig ON personparentjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_512_trig ON personparentjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_48_trig ON scopedgrant")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_48_trig ON scopedgrant")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_419_trig ON errorreport")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_419_trig ON errorreport")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_520_trig ON clazzassignment")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_520_trig ON clazzassignment")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_521_trig ON clazzassignmentcontentjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_521_trig ON clazzassignmentcontentjoin")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_678_trig ON personauth2")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_678_trig ON personauth2")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_679_trig ON usersession")
    db.execSQL("DROP TRIGGER IF EXISTS inccsn_679_trig ON usersession")
}

private fun DoorSqlDatabase.dropOldPostgresFunctions() {
    /*
    select routine_name, 'execSQL("DROP FUNCTION IF EXISTS ' || routine_name || '")'
     from information_schema.routines where routine_name like 'inccsn%'
     */
    execSQL("DROP FUNCTION IF EXISTS inccsn_101_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_10_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_134_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_13_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_14_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_15_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_164_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_173_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_178_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_17_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_189_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_1_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_200_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_208_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_21_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_272_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_28_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_2_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_300_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_301_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_302_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_3_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_410_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_419_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_42_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_43_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_44_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_45_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_47_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_48_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_50_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_512_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_51_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_520_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_521_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_53_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_55_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_56_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_57_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_60_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_62_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_64_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_65_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_66_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_678_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_679_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_68_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_6_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_70_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_72_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_74_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_7_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_8_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_99_fn")
    execSQL("DROP FUNCTION IF EXISTS inccsn_9_fn")

}

private fun DoorSqlDatabase.addReplicationEntities() {

    val _stmtList = mutableListOf<String>()
    _stmtList += "DROP TABLE IF EXISTS PersonCustomFieldValue"

    val db = this
    if(db.dbType() == DoorDbType.SQLITE) {
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzLogReplicate ( clPk INTEGER NOT NULL, clVersionId INTEGER NOT NULL, clDestination INTEGER NOT NULL, clProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (clPk, clDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogReplicate_clDestination_clProcessed_clPk ON ClazzLogReplicate (clDestination, clProcessed, clPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_14 AFTER INSERT ON ClazzLog BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (14, NEW.clazzLogUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_14 AFTER UPDATE ON ClazzLog BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (14, NEW.clazzLogUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_14 AFTER DELETE ON ClazzLog BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (14, OLD.clazzLogUid, 2); END "
        _stmtList +=
            "CREATE VIEW ClazzLog_ReceiveView AS  SELECT ClazzLog.*, ClazzLogTracker.* FROM ClazzLog LEFT JOIN ClazzLogTracker ON ClazzLogTracker.clFk = ClazzLog.clazzLogUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecordReplicate ( clarPk INTEGER NOT NULL, clarVersionId INTEGER NOT NULL, clarDestination INTEGER NOT NULL, clarProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (clarPk, clarDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogAttendanceRecordReplicate_clarDestination_clarProcessed_clarPk ON ClazzLogAttendanceRecordReplicate (clarDestination, clarProcessed, clarPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_15 AFTER INSERT ON ClazzLogAttendanceRecord BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (15, NEW.clazzLogAttendanceRecordUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_15 AFTER UPDATE ON ClazzLogAttendanceRecord BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (15, NEW.clazzLogAttendanceRecordUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_15 AFTER DELETE ON ClazzLogAttendanceRecord BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (15, OLD.clazzLogAttendanceRecordUid, 2); END "
        _stmtList +=
            "CREATE VIEW ClazzLogAttendanceRecord_ReceiveView AS  SELECT ClazzLogAttendanceRecord.*, ClazzLogAttendanceRecordTracker.* FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLogAttendanceRecordTracker ON ClazzLogAttendanceRecordTracker.clarFk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ScheduleReplicate ( schedulePk INTEGER NOT NULL, scheduleVersionId INTEGER NOT NULL, scheduleDestination INTEGER NOT NULL, scheduleProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (schedulePk, scheduleDestination)) "
        _stmtList +=
            " CREATE INDEX index_ScheduleReplicate_scheduleDestination_scheduleProcessed_schedulePk ON ScheduleReplicate (scheduleDestination, scheduleProcessed, schedulePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_21 AFTER INSERT ON Schedule BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (21, NEW.scheduleUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_21 AFTER UPDATE ON Schedule BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (21, NEW.scheduleUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_21 AFTER DELETE ON Schedule BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (21, OLD.scheduleUid, 2); END "
        _stmtList +=
            "CREATE VIEW Schedule_ReceiveView AS  SELECT Schedule.*, ScheduleTracker.* FROM Schedule LEFT JOIN ScheduleTracker ON ScheduleTracker.scheduleFk = Schedule.scheduleUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS HolidayCalendarReplicate ( hcPk INTEGER NOT NULL, hcVersionId INTEGER NOT NULL, hcDestination INTEGER NOT NULL, hcProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (hcPk, hcDestination)) "
        _stmtList +=
            " CREATE INDEX index_HolidayCalendarReplicate_hcDestination_hcProcessed_hcPk ON HolidayCalendarReplicate (hcDestination, hcProcessed, hcPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_28 AFTER INSERT ON HolidayCalendar BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (28, NEW.umCalendarUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_28 AFTER UPDATE ON HolidayCalendar BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (28, NEW.umCalendarUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_28 AFTER DELETE ON HolidayCalendar BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (28, OLD.umCalendarUid, 2); END "
        _stmtList +=
            "CREATE VIEW HolidayCalendar_ReceiveView AS  SELECT HolidayCalendar.*, HolidayCalendarTracker.* FROM HolidayCalendar LEFT JOIN HolidayCalendarTracker ON HolidayCalendarTracker.hcFk = HolidayCalendar.umCalendarUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS HolidayReplicate ( holidayPk INTEGER NOT NULL, holidayVersionId INTEGER NOT NULL, holidayDestination INTEGER NOT NULL, holidayProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (holidayPk, holidayDestination)) "
        _stmtList +=
            " CREATE INDEX index_HolidayReplicate_holidayDestination_holidayProcessed_holidayPk ON HolidayReplicate (holidayDestination, holidayProcessed, holidayPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_99 AFTER INSERT ON Holiday BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (99, NEW.holUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_99 AFTER UPDATE ON Holiday BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (99, NEW.holUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_99 AFTER DELETE ON Holiday BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (99, OLD.holUid, 2); END "
        _stmtList +=
            "CREATE VIEW Holiday_ReceiveView AS  SELECT Holiday.*, HolidayTracker.* FROM Holiday LEFT JOIN HolidayTracker ON HolidayTracker.holidayFk = Holiday.holUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonReplicate ( personPk INTEGER NOT NULL, personVersionId INTEGER NOT NULL, personDestination INTEGER NOT NULL, personProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (personPk, personDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonReplicate_personDestination_personProcessed_personPk ON PersonReplicate (personDestination, personProcessed, personPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_9 AFTER INSERT ON Person BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (9, NEW.personUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_9 AFTER UPDATE ON Person BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (9, NEW.personUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_9 AFTER DELETE ON Person BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (9, OLD.personUid, 2); END "
        _stmtList +=
            "CREATE VIEW Person_ReceiveView AS  SELECT Person.*, PersonTracker.* FROM Person LEFT JOIN PersonTracker ON PersonTracker.personFk = Person.personUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzReplicate ( clazzPk INTEGER NOT NULL, clazzVersionId INTEGER NOT NULL, clazzDestination INTEGER NOT NULL, clazzProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (clazzPk, clazzDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzReplicate_clazzDestination_clazzProcessed_clazzPk ON ClazzReplicate (clazzDestination, clazzProcessed, clazzPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_6 AFTER INSERT ON Clazz BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (6, NEW.clazzUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_6 AFTER UPDATE ON Clazz BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (6, NEW.clazzUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_6 AFTER DELETE ON Clazz BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (6, OLD.clazzUid, 2); END "
        _stmtList +=
            "CREATE VIEW Clazz_ReceiveView AS  SELECT Clazz.*, ClazzTracker.* FROM Clazz LEFT JOIN ClazzTracker ON ClazzTracker.clazzFk = Clazz.clazzUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzEnrolmentReplicate ( cePk INTEGER NOT NULL, ceVersionId INTEGER NOT NULL, ceDestination INTEGER NOT NULL, ceProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (cePk, ceDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzEnrolmentReplicate_ceDestination_ceProcessed_cePk ON ClazzEnrolmentReplicate (ceDestination, ceProcessed, cePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_65 AFTER INSERT ON ClazzEnrolment BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (65, NEW.clazzEnrolmentUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_65 AFTER UPDATE ON ClazzEnrolment BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (65, NEW.clazzEnrolmentUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_65 AFTER DELETE ON ClazzEnrolment BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (65, OLD.clazzEnrolmentUid, 2); END "
        _stmtList +=
            "CREATE VIEW ClazzEnrolment_ReceiveView AS  SELECT ClazzEnrolment.*, ClazzEnrolmentTracker.* FROM ClazzEnrolment LEFT JOIN ClazzEnrolmentTracker ON ClazzEnrolmentTracker.ceFk = ClazzEnrolment.clazzEnrolmentUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LeavingReasonReplicate ( lrPk INTEGER NOT NULL, lrVersionId INTEGER NOT NULL, lrDestination INTEGER NOT NULL, lrProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (lrPk, lrDestination)) "
        _stmtList +=
            " CREATE INDEX index_LeavingReasonReplicate_lrDestination_lrProcessed_lrPk ON LeavingReasonReplicate (lrDestination, lrProcessed, lrPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_410 AFTER INSERT ON LeavingReason BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (410, NEW.leavingReasonUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_410 AFTER UPDATE ON LeavingReason BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (410, NEW.leavingReasonUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_410 AFTER DELETE ON LeavingReason BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (410, OLD.leavingReasonUid, 2); END "
        _stmtList +=
            "CREATE VIEW LeavingReason_ReceiveView AS  SELECT LeavingReason.*, LeavingReasonTracker.* FROM LeavingReason LEFT JOIN LeavingReasonTracker ON LeavingReasonTracker.lrFk = LeavingReason.leavingReasonUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryReplicate ( cePk INTEGER NOT NULL, ceVersionId INTEGER NOT NULL, ceDestination INTEGER NOT NULL, ceProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (cePk, ceDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryReplicate_ceDestination_ceProcessed_cePk ON ContentEntryReplicate (ceDestination, ceProcessed, cePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_42 AFTER INSERT ON ContentEntry BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (42, NEW.contentEntryUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_42 AFTER UPDATE ON ContentEntry BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (42, NEW.contentEntryUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_42 AFTER DELETE ON ContentEntry BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (42, OLD.contentEntryUid, 2); END "
        _stmtList +=
            "CREATE VIEW ContentEntry_ReceiveView AS  SELECT ContentEntry.*, ContentEntryTracker.* FROM ContentEntry LEFT JOIN ContentEntryTracker ON ContentEntryTracker.ceFk = ContentEntry.contentEntryUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryContentCategoryJoinReplicate ( ceccjPk INTEGER NOT NULL, ceccjVersionId INTEGER NOT NULL, ceccjDestination INTEGER NOT NULL, ceccjProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (ceccjPk, ceccjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryContentCategoryJoinReplicate_ceccjDestination_ceccjProcessed_ceccjPk ON ContentEntryContentCategoryJoinReplicate (ceccjDestination, ceccjProcessed, ceccjPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_3 AFTER INSERT ON ContentEntryContentCategoryJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (3, NEW.ceccjUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_3 AFTER UPDATE ON ContentEntryContentCategoryJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (3, NEW.ceccjUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_3 AFTER DELETE ON ContentEntryContentCategoryJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (3, OLD.ceccjUid, 2); END "
        _stmtList +=
            "CREATE VIEW ContentEntryContentCategoryJoin_ReceiveView AS  SELECT ContentEntryContentCategoryJoin.*, ContentEntryContentCategoryJoinTracker.* FROM ContentEntryContentCategoryJoin LEFT JOIN ContentEntryContentCategoryJoinTracker ON ContentEntryContentCategoryJoinTracker.ceccjFk = ContentEntryContentCategoryJoin.ceccjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryParentChildJoinReplicate ( cepcjPk INTEGER NOT NULL, cepcjVersionId INTEGER NOT NULL, cepcjDestination INTEGER NOT NULL, cepcjProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (cepcjPk, cepcjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryParentChildJoinReplicate_cepcjDestination_cepcjProcessed_cepcjPk ON ContentEntryParentChildJoinReplicate (cepcjDestination, cepcjProcessed, cepcjPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_7 AFTER INSERT ON ContentEntryParentChildJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (7, NEW.cepcjUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_7 AFTER UPDATE ON ContentEntryParentChildJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (7, NEW.cepcjUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_7 AFTER DELETE ON ContentEntryParentChildJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (7, OLD.cepcjUid, 2); END "
        _stmtList +=
            "CREATE VIEW ContentEntryParentChildJoin_ReceiveView AS  SELECT ContentEntryParentChildJoin.*, ContentEntryParentChildJoinTracker.* FROM ContentEntryParentChildJoin LEFT JOIN ContentEntryParentChildJoinTracker ON ContentEntryParentChildJoinTracker.cepcjFk = ContentEntryParentChildJoin.cepcjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryRelatedEntryJoinReplicate ( cerejPk INTEGER NOT NULL, cerejVersionId INTEGER NOT NULL, cerejDestination INTEGER NOT NULL, cerejProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (cerejPk, cerejDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryRelatedEntryJoinReplicate_cerejDestination_cerejProcessed_cerejPk ON ContentEntryRelatedEntryJoinReplicate (cerejDestination, cerejProcessed, cerejPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_8 AFTER INSERT ON ContentEntryRelatedEntryJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (8, NEW.cerejUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_8 AFTER UPDATE ON ContentEntryRelatedEntryJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (8, NEW.cerejUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_8 AFTER DELETE ON ContentEntryRelatedEntryJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (8, OLD.cerejUid, 2); END "
        _stmtList +=
            "CREATE VIEW ContentEntryRelatedEntryJoin_ReceiveView AS  SELECT ContentEntryRelatedEntryJoin.*, ContentEntryRelatedEntryJoinTracker.* FROM ContentEntryRelatedEntryJoin LEFT JOIN ContentEntryRelatedEntryJoinTracker ON ContentEntryRelatedEntryJoinTracker.cerejFk = ContentEntryRelatedEntryJoin.cerejUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentCategorySchemaReplicate ( ccsPk INTEGER NOT NULL, ccsVersionId INTEGER NOT NULL, ccsDestination INTEGER NOT NULL, ccsProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (ccsPk, ccsDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentCategorySchemaReplicate_ccsDestination_ccsProcessed_ccsPk ON ContentCategorySchemaReplicate (ccsDestination, ccsProcessed, ccsPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_2 AFTER INSERT ON ContentCategorySchema BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (2, NEW.contentCategorySchemaUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_2 AFTER UPDATE ON ContentCategorySchema BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (2, NEW.contentCategorySchemaUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_2 AFTER DELETE ON ContentCategorySchema BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (2, OLD.contentCategorySchemaUid, 2); END "
        _stmtList +=
            "CREATE VIEW ContentCategorySchema_ReceiveView AS  SELECT ContentCategorySchema.*, ContentCategorySchemaTracker.* FROM ContentCategorySchema LEFT JOIN ContentCategorySchemaTracker ON ContentCategorySchemaTracker.ccsFk = ContentCategorySchema.contentCategorySchemaUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentCategoryReplicate ( ccPk INTEGER NOT NULL, ccVersionId INTEGER NOT NULL, ccDestination INTEGER NOT NULL, ccProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (ccPk, ccDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentCategoryReplicate_ccDestination_ccProcessed_ccPk ON ContentCategoryReplicate (ccDestination, ccProcessed, ccPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_1 AFTER INSERT ON ContentCategory BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (1, NEW.contentCategoryUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_1 AFTER UPDATE ON ContentCategory BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (1, NEW.contentCategoryUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_1 AFTER DELETE ON ContentCategory BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (1, OLD.contentCategoryUid, 2); END "
        _stmtList +=
            "CREATE VIEW ContentCategory_ReceiveView AS  SELECT ContentCategory.*, ContentCategoryTracker.* FROM ContentCategory LEFT JOIN ContentCategoryTracker ON ContentCategoryTracker.ccFk = ContentCategory.contentCategoryUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LanguageReplicate ( languagePk INTEGER NOT NULL, languageVersionId INTEGER NOT NULL, languageDestination INTEGER NOT NULL, languageProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (languagePk, languageDestination)) "
        _stmtList +=
            " CREATE INDEX index_LanguageReplicate_languageDestination_languageProcessed_languagePk ON LanguageReplicate (languageDestination, languageProcessed, languagePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_13 AFTER INSERT ON Language BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (13, NEW.langUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_13 AFTER UPDATE ON Language BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (13, NEW.langUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_13 AFTER DELETE ON Language BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (13, OLD.langUid, 2); END "
        _stmtList +=
            "CREATE VIEW Language_ReceiveView AS  SELECT Language.*, LanguageTracker.* FROM Language LEFT JOIN LanguageTracker ON LanguageTracker.languageFk = Language.langUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LanguageVariantReplicate ( lvPk INTEGER NOT NULL, lvVersionId INTEGER NOT NULL, lvDestination INTEGER NOT NULL, lvProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (lvPk, lvDestination)) "
        _stmtList +=
            " CREATE INDEX index_LanguageVariantReplicate_lvDestination_lvProcessed_lvPk ON LanguageVariantReplicate (lvDestination, lvProcessed, lvPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_10 AFTER INSERT ON LanguageVariant BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (10, NEW.langVariantUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_10 AFTER UPDATE ON LanguageVariant BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (10, NEW.langVariantUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_10 AFTER DELETE ON LanguageVariant BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (10, OLD.langVariantUid, 2); END "
        _stmtList +=
            "CREATE VIEW LanguageVariant_ReceiveView AS  SELECT LanguageVariant.*, LanguageVariantTracker.* FROM LanguageVariant LEFT JOIN LanguageVariantTracker ON LanguageVariantTracker.lvFk = LanguageVariant.langVariantUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS RoleReplicate ( rolePk INTEGER NOT NULL, roleVersionId INTEGER NOT NULL, roleDestination INTEGER NOT NULL, roleProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (rolePk, roleDestination)) "
        _stmtList +=
            " CREATE INDEX index_RoleReplicate_roleDestination_roleProcessed_rolePk ON RoleReplicate (roleDestination, roleProcessed, rolePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_45 AFTER INSERT ON Role BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (45, NEW.roleUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_45 AFTER UPDATE ON Role BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (45, NEW.roleUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_45 AFTER DELETE ON Role BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (45, OLD.roleUid, 2); END "
        _stmtList +=
            "CREATE VIEW Role_ReceiveView AS  SELECT Role.*, RoleTracker.* FROM Role LEFT JOIN RoleTracker ON RoleTracker.roleFk = Role.roleUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonGroupReplicate ( pgPk INTEGER NOT NULL, pgVersionId INTEGER NOT NULL, pgDestination INTEGER NOT NULL, pgProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (pgPk, pgDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupReplicate_pgDestination_pgProcessed_pgPk ON PersonGroupReplicate (pgDestination, pgProcessed, pgPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_43 AFTER INSERT ON PersonGroup BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (43, NEW.groupUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_43 AFTER UPDATE ON PersonGroup BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (43, NEW.groupUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_43 AFTER DELETE ON PersonGroup BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (43, OLD.groupUid, 2); END "
        _stmtList +=
            "CREATE VIEW PersonGroup_ReceiveView AS  SELECT PersonGroup.*, PersonGroupTracker.* FROM PersonGroup LEFT JOIN PersonGroupTracker ON PersonGroupTracker.pgFk = PersonGroup.groupUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonGroupMemberReplicate ( pgmPk INTEGER NOT NULL, pgmVersionId INTEGER NOT NULL, pgmDestination INTEGER NOT NULL, pgmProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (pgmPk, pgmDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupMemberReplicate_pgmDestination_pgmProcessed_pgmPk ON PersonGroupMemberReplicate (pgmDestination, pgmProcessed, pgmPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_44 AFTER INSERT ON PersonGroupMember BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (44, NEW.groupMemberUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_44 AFTER UPDATE ON PersonGroupMember BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (44, NEW.groupMemberUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_44 AFTER DELETE ON PersonGroupMember BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (44, OLD.groupMemberUid, 2); END "
        _stmtList +=
            "CREATE VIEW PersonGroupMember_ReceiveView AS  SELECT PersonGroupMember.*, PersonGroupMemberTracker.* FROM PersonGroupMember LEFT JOIN PersonGroupMemberTracker ON PersonGroupMemberTracker.pgmFk = PersonGroupMember.groupMemberUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonPictureReplicate ( ppPk INTEGER NOT NULL, ppVersionId INTEGER NOT NULL, ppDestination INTEGER NOT NULL, ppProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (ppPk, ppDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonPictureReplicate_ppDestination_ppProcessed_ppPk ON PersonPictureReplicate (ppDestination, ppProcessed, ppPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_50 AFTER INSERT ON PersonPicture BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (50, NEW.personPictureUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_50 AFTER UPDATE ON PersonPicture BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (50, NEW.personPictureUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_50 AFTER DELETE ON PersonPicture BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (50, OLD.personPictureUid, 2); END "
        _stmtList +=
            "CREATE VIEW PersonPicture_ReceiveView AS  SELECT PersonPicture.*, PersonPictureTracker.* FROM PersonPicture LEFT JOIN PersonPictureTracker ON PersonPictureTracker.ppFk = PersonPicture.personPictureUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContainerReplicate ( containerPk INTEGER NOT NULL, containerVersionId INTEGER NOT NULL, containerDestination INTEGER NOT NULL, containerProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (containerPk, containerDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContainerReplicate_containerDestination_containerProcessed_containerPk ON ContainerReplicate (containerDestination, containerProcessed, containerPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_51 AFTER INSERT ON Container BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (51, NEW.containerUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_51 AFTER UPDATE ON Container BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (51, NEW.containerUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_51 AFTER DELETE ON Container BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (51, OLD.containerUid, 2); END "
        _stmtList +=
            "CREATE VIEW Container_ReceiveView AS  SELECT Container.*, ContainerTracker.* FROM Container LEFT JOIN ContainerTracker ON ContainerTracker.containerFk = Container.containerUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS VerbEntityReplicate ( vePk INTEGER NOT NULL, veVersionId INTEGER NOT NULL, veDestination INTEGER NOT NULL, veProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (vePk, veDestination)) "
        _stmtList +=
            " CREATE INDEX index_VerbEntityReplicate_veDestination_veProcessed_vePk ON VerbEntityReplicate (veDestination, veProcessed, vePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_62 AFTER INSERT ON VerbEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (62, NEW.verbUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_62 AFTER UPDATE ON VerbEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (62, NEW.verbUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_62 AFTER DELETE ON VerbEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (62, OLD.verbUid, 2); END "
        _stmtList +=
            "CREATE VIEW VerbEntity_ReceiveView AS  SELECT VerbEntity.*, VerbEntityTracker.* FROM VerbEntity LEFT JOIN VerbEntityTracker ON VerbEntityTracker.veFk = VerbEntity.verbUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS XObjectEntityReplicate ( xoePk INTEGER NOT NULL, xoeVersionId INTEGER NOT NULL, xoeDestination INTEGER NOT NULL, xoeProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (xoePk, xoeDestination)) "
        _stmtList +=
            " CREATE INDEX index_XObjectEntityReplicate_xoeDestination_xoeProcessed_xoePk ON XObjectEntityReplicate (xoeDestination, xoeProcessed, xoePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_64 AFTER INSERT ON XObjectEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (64, NEW.xObjectUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_64 AFTER UPDATE ON XObjectEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (64, NEW.xObjectUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_64 AFTER DELETE ON XObjectEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (64, OLD.xObjectUid, 2); END "
        _stmtList +=
            "CREATE VIEW XObjectEntity_ReceiveView AS  SELECT XObjectEntity.*, XObjectEntityTracker.* FROM XObjectEntity LEFT JOIN XObjectEntityTracker ON XObjectEntityTracker.xoeFk = XObjectEntity.xObjectUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StatementEntityReplicate ( sePk INTEGER NOT NULL, seVersionId INTEGER NOT NULL, seDestination INTEGER NOT NULL, seProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (sePk, seDestination)) "
        _stmtList +=
            " CREATE INDEX index_StatementEntityReplicate_seDestination_seProcessed_sePk ON StatementEntityReplicate (seDestination, seProcessed, sePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_60 AFTER INSERT ON StatementEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (60, NEW.statementUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_60 AFTER UPDATE ON StatementEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (60, NEW.statementUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_60 AFTER DELETE ON StatementEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (60, OLD.statementUid, 2); END "
        _stmtList +=
            "CREATE VIEW StatementEntity_ReceiveView AS  SELECT StatementEntity.*, StatementEntityTracker.* FROM StatementEntity LEFT JOIN StatementEntityTracker ON StatementEntityTracker.seFk = StatementEntity.statementUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContextXObjectStatementJoinReplicate ( cxosjPk INTEGER NOT NULL, cxosjVersionId INTEGER NOT NULL, cxosjDestination INTEGER NOT NULL, cxosjProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (cxosjPk, cxosjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContextXObjectStatementJoinReplicate_cxosjDestination_cxosjProcessed_cxosjPk ON ContextXObjectStatementJoinReplicate (cxosjDestination, cxosjProcessed, cxosjPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_66 AFTER INSERT ON ContextXObjectStatementJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (66, NEW.contextXObjectStatementJoinUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_66 AFTER UPDATE ON ContextXObjectStatementJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (66, NEW.contextXObjectStatementJoinUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_66 AFTER DELETE ON ContextXObjectStatementJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (66, OLD.contextXObjectStatementJoinUid, 2); END "
        _stmtList +=
            "CREATE VIEW ContextXObjectStatementJoin_ReceiveView AS  SELECT ContextXObjectStatementJoin.*, ContextXObjectStatementJoinTracker.* FROM ContextXObjectStatementJoin LEFT JOIN ContextXObjectStatementJoinTracker ON ContextXObjectStatementJoinTracker.cxosjFk = ContextXObjectStatementJoin.contextXObjectStatementJoinUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS AgentEntityReplicate ( aePk INTEGER NOT NULL, aeVersionId INTEGER NOT NULL, aeDestination INTEGER NOT NULL, aeProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (aePk, aeDestination)) "
        _stmtList +=
            " CREATE INDEX index_AgentEntityReplicate_aeDestination_aeProcessed_aePk ON AgentEntityReplicate (aeDestination, aeProcessed, aePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_68 AFTER INSERT ON AgentEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (68, NEW.agentUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_68 AFTER UPDATE ON AgentEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (68, NEW.agentUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_68 AFTER DELETE ON AgentEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (68, OLD.agentUid, 2); END "
        _stmtList +=
            "CREATE VIEW AgentEntity_ReceiveView AS  SELECT AgentEntity.*, AgentEntityTracker.* FROM AgentEntity LEFT JOIN AgentEntityTracker ON AgentEntityTracker.aeFk = AgentEntity.agentUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StateEntityReplicate ( sePk INTEGER NOT NULL, seVersionId INTEGER NOT NULL, seDestination INTEGER NOT NULL, seProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (sePk, seDestination)) "
        _stmtList +=
            " CREATE INDEX index_StateEntityReplicate_seDestination_seProcessed_sePk ON StateEntityReplicate (seDestination, seProcessed, sePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_70 AFTER INSERT ON StateEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (70, NEW.stateUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_70 AFTER UPDATE ON StateEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (70, NEW.stateUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_70 AFTER DELETE ON StateEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (70, OLD.stateUid, 2); END "
        _stmtList +=
            "CREATE VIEW StateEntity_ReceiveView AS  SELECT StateEntity.*, StateEntityTracker.* FROM StateEntity LEFT JOIN StateEntityTracker ON StateEntityTracker.seFk = StateEntity.stateUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StateContentEntityReplicate ( scePk INTEGER NOT NULL, sceVersionId INTEGER NOT NULL, sceDestination INTEGER NOT NULL, sceProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (scePk, sceDestination)) "
        _stmtList +=
            " CREATE INDEX index_StateContentEntityReplicate_sceDestination_sceProcessed_scePk ON StateContentEntityReplicate (sceDestination, sceProcessed, scePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_72 AFTER INSERT ON StateContentEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (72, NEW.stateContentUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_72 AFTER UPDATE ON StateContentEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (72, NEW.stateContentUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_72 AFTER DELETE ON StateContentEntity BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (72, OLD.stateContentUid, 2); END "
        _stmtList +=
            "CREATE VIEW StateContentEntity_ReceiveView AS  SELECT StateContentEntity.*, StateContentEntityTracker.* FROM StateContentEntity LEFT JOIN StateContentEntityTracker ON StateContentEntityTracker.sceFk = StateContentEntity.stateContentUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS XLangMapEntryReplicate ( xlmePk INTEGER NOT NULL, xlmeVersionId INTEGER NOT NULL, xlmeDestination INTEGER NOT NULL, xlmeProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (xlmePk, xlmeDestination)) "
        _stmtList +=
            " CREATE INDEX index_XLangMapEntryReplicate_xlmeDestination_xlmeProcessed_xlmePk ON XLangMapEntryReplicate (xlmeDestination, xlmeProcessed, xlmePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_74 AFTER INSERT ON XLangMapEntry BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (74, NEW.statementLangMapUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_74 AFTER UPDATE ON XLangMapEntry BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (74, NEW.statementLangMapUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_74 AFTER DELETE ON XLangMapEntry BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (74, OLD.statementLangMapUid, 2); END "
        _stmtList +=
            "CREATE VIEW XLangMapEntry_ReceiveView AS  SELECT XLangMapEntry.*, XLangMapEntryTracker.* FROM XLangMapEntry LEFT JOIN XLangMapEntryTracker ON XLangMapEntryTracker.xlmeFk = XLangMapEntry.statementLangMapUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SchoolReplicate ( schoolPk INTEGER NOT NULL, schoolVersionId INTEGER NOT NULL, schoolDestination INTEGER NOT NULL, schoolProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (schoolPk, schoolDestination)) "
        _stmtList +=
            " CREATE INDEX index_SchoolReplicate_schoolDestination_schoolProcessed_schoolPk ON SchoolReplicate (schoolDestination, schoolProcessed, schoolPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_164 AFTER INSERT ON School BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (164, NEW.schoolUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_164 AFTER UPDATE ON School BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (164, NEW.schoolUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_164 AFTER DELETE ON School BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (164, OLD.schoolUid, 2); END "
        _stmtList +=
            "CREATE VIEW School_ReceiveView AS  SELECT School.*, SchoolTracker.* FROM School LEFT JOIN SchoolTracker ON SchoolTracker.schoolFk = School.schoolUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SchoolMemberReplicate ( smPk INTEGER NOT NULL, smVersionId INTEGER NOT NULL, smDestination INTEGER NOT NULL, smProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (smPk, smDestination)) "
        _stmtList +=
            " CREATE INDEX index_SchoolMemberReplicate_smDestination_smProcessed_smPk ON SchoolMemberReplicate (smDestination, smProcessed, smPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_200 AFTER INSERT ON SchoolMember BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (200, NEW.schoolMemberUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_200 AFTER UPDATE ON SchoolMember BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (200, NEW.schoolMemberUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_200 AFTER DELETE ON SchoolMember BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (200, OLD.schoolMemberUid, 2); END "
        _stmtList +=
            "CREATE VIEW SchoolMember_ReceiveView AS  SELECT SchoolMember.*, SchoolMemberTracker.* FROM SchoolMember LEFT JOIN SchoolMemberTracker ON SchoolMemberTracker.smFk = SchoolMember.schoolMemberUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS CommentsReplicate ( commentsPk INTEGER NOT NULL, commentsVersionId INTEGER NOT NULL, commentsDestination INTEGER NOT NULL, commentsProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (commentsPk, commentsDestination)) "
        _stmtList +=
            " CREATE INDEX index_CommentsReplicate_commentsDestination_commentsProcessed_commentsPk ON CommentsReplicate (commentsDestination, commentsProcessed, commentsPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_208 AFTER INSERT ON Comments BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (208, NEW.commentsUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_208 AFTER UPDATE ON Comments BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (208, NEW.commentsUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_208 AFTER DELETE ON Comments BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (208, OLD.commentsUid, 2); END "
        _stmtList +=
            "CREATE VIEW Comments_ReceiveView AS  SELECT Comments.*, CommentsTracker.* FROM Comments LEFT JOIN CommentsTracker ON CommentsTracker.commentsFk = Comments.commentsUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ReportReplicate ( reportPk INTEGER NOT NULL, reportVersionId INTEGER NOT NULL, reportDestination INTEGER NOT NULL, reportProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (reportPk, reportDestination)) "
        _stmtList +=
            " CREATE INDEX index_ReportReplicate_reportDestination_reportProcessed_reportPk ON ReportReplicate (reportDestination, reportProcessed, reportPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_101 AFTER INSERT ON Report BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (101, NEW.reportUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_101 AFTER UPDATE ON Report BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (101, NEW.reportUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_101 AFTER DELETE ON Report BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (101, OLD.reportUid, 2); END "
        _stmtList +=
            "CREATE VIEW Report_ReceiveView AS  SELECT Report.*, ReportTracker.* FROM Report LEFT JOIN ReportTracker ON ReportTracker.reportFk = Report.reportUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SiteReplicate ( sitePk INTEGER NOT NULL, siteVersionId INTEGER NOT NULL, siteDestination INTEGER NOT NULL, siteProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (sitePk, siteDestination)) "
        _stmtList +=
            " CREATE INDEX index_SiteReplicate_siteDestination_siteProcessed_sitePk ON SiteReplicate (siteDestination, siteProcessed, sitePk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_189 AFTER INSERT ON Site BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (189, NEW.siteUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_189 AFTER UPDATE ON Site BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (189, NEW.siteUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_189 AFTER DELETE ON Site BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (189, OLD.siteUid, 2); END "
        _stmtList +=
            "CREATE VIEW Site_ReceiveView AS  SELECT Site.*, SiteTracker.* FROM Site LEFT JOIN SiteTracker ON SiteTracker.siteFk = Site.siteUid "
        _stmtList +=
            " CREATE TRIGGER site_remote_insert_ins INSTEAD OF INSERT ON Site_ReceiveView FOR EACH ROW BEGIN REPLACE INTO Site(siteUid, sitePcsn, siteLcsn, siteLcb, siteLct, siteName, guestLogin, registrationAllowed, authSalt) VALUES (NEW.siteUid, NEW.sitePcsn, NEW.siteLcsn, NEW.siteLcb, NEW.siteLct, NEW.siteName, NEW.guestLogin, NEW.registrationAllowed, NEW.authSalt) ; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LearnerGroupReplicate ( lgPk INTEGER NOT NULL, lgVersionId INTEGER NOT NULL, lgDestination INTEGER NOT NULL, lgProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (lgPk, lgDestination)) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupReplicate_lgDestination_lgProcessed_lgPk ON LearnerGroupReplicate (lgDestination, lgProcessed, lgPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_301 AFTER INSERT ON LearnerGroup BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (301, NEW.learnerGroupUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_301 AFTER UPDATE ON LearnerGroup BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (301, NEW.learnerGroupUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_301 AFTER DELETE ON LearnerGroup BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (301, OLD.learnerGroupUid, 2); END "
        _stmtList +=
            "CREATE VIEW LearnerGroup_ReceiveView AS  SELECT LearnerGroup.*, LearnerGroupTracker.* FROM LearnerGroup LEFT JOIN LearnerGroupTracker ON LearnerGroupTracker.lgFk = LearnerGroup.learnerGroupUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LearnerGroupMemberReplicate ( lgmPk INTEGER NOT NULL, lgmVersionId INTEGER NOT NULL, lgmDestination INTEGER NOT NULL, lgmProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (lgmPk, lgmDestination)) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupMemberReplicate_lgmDestination_lgmProcessed_lgmPk ON LearnerGroupMemberReplicate (lgmDestination, lgmProcessed, lgmPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_300 AFTER INSERT ON LearnerGroupMember BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (300, NEW.learnerGroupMemberUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_300 AFTER UPDATE ON LearnerGroupMember BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (300, NEW.learnerGroupMemberUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_300 AFTER DELETE ON LearnerGroupMember BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (300, OLD.learnerGroupMemberUid, 2); END "
        _stmtList +=
            "CREATE VIEW LearnerGroupMember_ReceiveView AS  SELECT LearnerGroupMember.*, LearnerGroupMemberTracker.* FROM LearnerGroupMember LEFT JOIN LearnerGroupMemberTracker ON LearnerGroupMemberTracker.lgmFk = LearnerGroupMember.learnerGroupMemberUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS GroupLearningSessionReplicate ( glsPk INTEGER NOT NULL, glsVersionId INTEGER NOT NULL, glsDestination INTEGER NOT NULL, glsProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (glsPk, glsDestination)) "
        _stmtList +=
            " CREATE INDEX index_GroupLearningSessionReplicate_glsDestination_glsProcessed_glsPk ON GroupLearningSessionReplicate (glsDestination, glsProcessed, glsPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_302 AFTER INSERT ON GroupLearningSession BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (302, NEW.groupLearningSessionUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_302 AFTER UPDATE ON GroupLearningSession BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (302, NEW.groupLearningSessionUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_302 AFTER DELETE ON GroupLearningSession BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (302, OLD.groupLearningSessionUid, 2); END "
        _stmtList +=
            "CREATE VIEW GroupLearningSession_ReceiveView AS  SELECT GroupLearningSession.*, GroupLearningSessionTracker.* FROM GroupLearningSession LEFT JOIN GroupLearningSessionTracker ON GroupLearningSessionTracker.glsFk = GroupLearningSession.groupLearningSessionUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SiteTermsReplicate ( stPk INTEGER NOT NULL, stVersionId INTEGER NOT NULL, stDestination INTEGER NOT NULL, stProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (stPk, stDestination)) "
        _stmtList +=
            " CREATE INDEX index_SiteTermsReplicate_stDestination_stProcessed_stPk ON SiteTermsReplicate (stDestination, stProcessed, stPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_272 AFTER INSERT ON SiteTerms BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (272, NEW.sTermsUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_272 AFTER UPDATE ON SiteTerms BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (272, NEW.sTermsUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_272 AFTER DELETE ON SiteTerms BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (272, OLD.sTermsUid, 2); END "
        _stmtList +=
            "CREATE VIEW SiteTerms_ReceiveView AS  SELECT SiteTerms.*, SiteTermsTracker.* FROM SiteTerms LEFT JOIN SiteTermsTracker ON SiteTermsTracker.stFk = SiteTerms.sTermsUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzContentJoinReplicate ( ccjPk INTEGER NOT NULL, ccjVersionId INTEGER NOT NULL, ccjDestination INTEGER NOT NULL, ccjProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (ccjPk, ccjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzContentJoinReplicate_ccjDestination_ccjProcessed_ccjPk ON ClazzContentJoinReplicate (ccjDestination, ccjProcessed, ccjPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_134 AFTER INSERT ON ClazzContentJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (134, NEW.ccjUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_134 AFTER UPDATE ON ClazzContentJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (134, NEW.ccjUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_134 AFTER DELETE ON ClazzContentJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (134, OLD.ccjUid, 2); END "
        _stmtList +=
            "CREATE VIEW ClazzContentJoin_ReceiveView AS  SELECT ClazzContentJoin.*, ClazzContentJoinTracker.* FROM ClazzContentJoin LEFT JOIN ClazzContentJoinTracker ON ClazzContentJoinTracker.ccjFk = ClazzContentJoin.ccjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonParentJoinReplicate ( ppjPk INTEGER NOT NULL, ppjVersionId INTEGER NOT NULL, ppjDestination INTEGER NOT NULL, ppjProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (ppjPk, ppjDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonParentJoinReplicate_ppjDestination_ppjProcessed_ppjPk ON PersonParentJoinReplicate (ppjDestination, ppjProcessed, ppjPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_512 AFTER INSERT ON PersonParentJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (512, NEW.ppjUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_512 AFTER UPDATE ON PersonParentJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (512, NEW.ppjUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_512 AFTER DELETE ON PersonParentJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (512, OLD.ppjUid, 2); END "
        _stmtList +=
            "CREATE VIEW PersonParentJoin_ReceiveView AS  SELECT PersonParentJoin.*, PersonParentJoinTracker.* FROM PersonParentJoin LEFT JOIN PersonParentJoinTracker ON PersonParentJoinTracker.ppjFk = PersonParentJoin.ppjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ScopedGrantReplicate ( sgPk INTEGER NOT NULL, sgVersionId INTEGER NOT NULL, sgDestination INTEGER NOT NULL, sgProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (sgPk, sgDestination)) "
        _stmtList +=
            " CREATE INDEX index_ScopedGrantReplicate_sgDestination_sgProcessed_sgPk ON ScopedGrantReplicate (sgDestination, sgProcessed, sgPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_48 AFTER INSERT ON ScopedGrant BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (48, NEW.sgUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_48 AFTER UPDATE ON ScopedGrant BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (48, NEW.sgUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_48 AFTER DELETE ON ScopedGrant BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (48, OLD.sgUid, 2); END "
        _stmtList +=
            "CREATE VIEW ScopedGrant_ReceiveView AS  SELECT ScopedGrant.*, ScopedGrantTracker.* FROM ScopedGrant LEFT JOIN ScopedGrantTracker ON ScopedGrantTracker.sgFk = ScopedGrant.sgUid "
        _stmtList +=
            " CREATE TRIGGER sg_remote_insert_ins INSTEAD OF INSERT ON ScopedGrant_ReceiveView FOR EACH ROW BEGIN REPLACE INTO ScopedGrant(sgUid, sgPcsn, sgLcsn, sgLcb, sgLct, sgTableId, sgEntityUid, sgPermissions, sgGroupUid, sgIndex, sgFlags) VALUES (NEW.sgUid, NEW.sgPcsn, NEW.sgLcsn, NEW.sgLcb, NEW.sgLct, NEW.sgTableId, NEW.sgEntityUid, NEW.sgPermissions, NEW.sgGroupUid, NEW.sgIndex, NEW.sgFlags) /*psql ON CONFLICT(sgUid) DO UPDATE SET sgLct = EXCLUDED.sgLct, sgPermissions = EXCLUDED.sgPermissions */ ; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ErrorReportReplicate ( erPk INTEGER NOT NULL, erVersionId INTEGER NOT NULL, erDestination INTEGER NOT NULL, erProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (erPk, erDestination)) "
        _stmtList +=
            " CREATE INDEX index_ErrorReportReplicate_erDestination_erProcessed_erPk ON ErrorReportReplicate (erDestination, erProcessed, erPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_419 AFTER INSERT ON ErrorReport BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (419, NEW.errUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_419 AFTER UPDATE ON ErrorReport BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (419, NEW.errUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_419 AFTER DELETE ON ErrorReport BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (419, OLD.errUid, 2); END "
        _stmtList +=
            "CREATE VIEW ErrorReport_ReceiveView AS  SELECT ErrorReport.*, ErrorReportTracker.* FROM ErrorReport LEFT JOIN ErrorReportTracker ON ErrorReportTracker.erFk = ErrorReport.errUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzAssignmentReplicate ( caPk INTEGER NOT NULL, caVersionId INTEGER NOT NULL, caDestination INTEGER NOT NULL, caProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (caPk, caDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentReplicate_caDestination_caProcessed_caPk ON ClazzAssignmentReplicate (caDestination, caProcessed, caPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_520 AFTER INSERT ON ClazzAssignment BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (520, NEW.caUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_520 AFTER UPDATE ON ClazzAssignment BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (520, NEW.caUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_520 AFTER DELETE ON ClazzAssignment BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (520, OLD.caUid, 2); END "
        _stmtList +=
            "CREATE VIEW ClazzAssignment_ReceiveView AS  SELECT ClazzAssignment.*, ClazzAssignmentTracker.* FROM ClazzAssignment LEFT JOIN ClazzAssignmentTracker ON ClazzAssignmentTracker.caFk = ClazzAssignment.caUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoinReplicate ( cacjPk INTEGER NOT NULL, cacjVersionId INTEGER NOT NULL, cacjDestination INTEGER NOT NULL, cacjProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (cacjPk, cacjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentContentJoinReplicate_cacjDestination_cacjProcessed_cacjPk ON ClazzAssignmentContentJoinReplicate (cacjDestination, cacjProcessed, cacjPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_521 AFTER INSERT ON ClazzAssignmentContentJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (521, NEW.cacjUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_521 AFTER UPDATE ON ClazzAssignmentContentJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (521, NEW.cacjUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_521 AFTER DELETE ON ClazzAssignmentContentJoin BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (521, OLD.cacjUid, 2); END "
        _stmtList +=
            "CREATE VIEW ClazzAssignmentContentJoin_ReceiveView AS  SELECT ClazzAssignmentContentJoin.*, ClazzAssignmentContentJoinTracker.* FROM ClazzAssignmentContentJoin LEFT JOIN ClazzAssignmentContentJoinTracker ON ClazzAssignmentContentJoinTracker.cacjFk = ClazzAssignmentContentJoin.cacjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonAuth2Replicate ( paPk INTEGER NOT NULL, paVersionId INTEGER NOT NULL, paDestination INTEGER NOT NULL, paProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (paPk, paDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonAuth2Replicate_paDestination_paProcessed_paPk ON PersonAuth2Replicate (paDestination, paProcessed, paPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_678 AFTER INSERT ON PersonAuth2 BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (678, NEW.pauthUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_678 AFTER UPDATE ON PersonAuth2 BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (678, NEW.pauthUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_678 AFTER DELETE ON PersonAuth2 BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (678, OLD.pauthUid, 2); END "
        _stmtList +=
            "CREATE VIEW PersonAuth2_ReceiveView AS  SELECT PersonAuth2.*, PersonAuth2Tracker.* FROM PersonAuth2 LEFT JOIN PersonAuth2Tracker ON PersonAuth2Tracker.paFk = PersonAuth2.pauthUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS UserSessionReplicate ( usPk INTEGER NOT NULL, usVersionId INTEGER NOT NULL, usDestination INTEGER NOT NULL, usProcessed INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (usPk, usDestination)) "
        _stmtList +=
            " CREATE INDEX index_UserSessionReplicate_usDestination_usProcessed_usPk ON UserSessionReplicate (usDestination, usProcessed, usPk) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_679 AFTER INSERT ON UserSession BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (679, NEW.usUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_679 AFTER UPDATE ON UserSession BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (679, NEW.usUid, 1); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_679 AFTER DELETE ON UserSession BEGIN REPLACE INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (679, OLD.usUid, 2); END "
        _stmtList +=
            "CREATE VIEW UserSession_ReceiveView AS  SELECT UserSession.*, UserSessionTracker.* FROM UserSession LEFT JOIN UserSessionTracker ON UserSessionTracker.usFk = UserSession.usUid "
        _stmtList +=
            " CREATE TRIGGER usersession_remote_ins_ins INSTEAD OF INSERT ON UserSession_ReceiveView FOR EACH ROW BEGIN REPLACE INTO UserSession(usUid, usPcsn, usLcsn, usLcb, usLct, usPersonUid, usClientNodeId, usStartTime, usEndTime, usStatus, usReason, usAuth, usSessionType) VALUES (NEW.usUid, NEW.usPcsn, NEW.usLcsn, NEW.usLcb, NEW.usLct, NEW.usPersonUid, NEW.usClientNodeId, NEW.usStartTime, NEW.usEndTime, NEW.usStatus, NEW.usReason, NEW.usAuth, NEW.usSessionType) /*postgres ON CONFLICT (usUid) DO UPDATE SET usStatus = EXCLUDED.usStatus, usEndTime = EXCLUDED.usEndTime, usReason = EXCLUDED.usReason */ ; END "
    } else {
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzLogReplicate ( clPk BIGINT NOT NULL, clVersionId BIGINT NOT NULL, clDestination BIGINT NOT NULL, clProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (clPk, clDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogReplicate_clDestination_clProcessed_clPk ON ClazzLogReplicate (clDestination, clProcessed, clPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_14_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (14, NEW.clazzLogUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_14_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_14_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_14_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (14, OLD.clazzLogUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_14_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_14_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzLog_ReceiveView AS  SELECT ClazzLog.*, ClazzLogTracker.* FROM ClazzLog LEFT JOIN ClazzLogTracker ON ClazzLogTracker.clFk = ClazzLog.clazzLogUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecordReplicate ( clarPk BIGINT NOT NULL, clarVersionId BIGINT NOT NULL, clarDestination BIGINT NOT NULL, clarProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (clarPk, clarDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogAttendanceRecordReplicate_clarDestination_clarProcessed_clarPk ON ClazzLogAttendanceRecordReplicate (clarDestination, clarProcessed, clarPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_15_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (15, NEW.clazzLogAttendanceRecordUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_15_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_15_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_15_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (15, OLD.clazzLogAttendanceRecordUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_15_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_15_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzLogAttendanceRecord_ReceiveView AS  SELECT ClazzLogAttendanceRecord.*, ClazzLogAttendanceRecordTracker.* FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLogAttendanceRecordTracker ON ClazzLogAttendanceRecordTracker.clarFk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ScheduleReplicate ( schedulePk BIGINT NOT NULL, scheduleVersionId BIGINT NOT NULL, scheduleDestination BIGINT NOT NULL, scheduleProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (schedulePk, scheduleDestination)) "
        _stmtList +=
            " CREATE INDEX index_ScheduleReplicate_scheduleDestination_scheduleProcessed_schedulePk ON ScheduleReplicate (scheduleDestination, scheduleProcessed, schedulePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_21_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (21, NEW.scheduleUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_21_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_21_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_21_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (21, OLD.scheduleUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_21_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_21_fn(); "
        _stmtList +=
            "CREATE VIEW Schedule_ReceiveView AS  SELECT Schedule.*, ScheduleTracker.* FROM Schedule LEFT JOIN ScheduleTracker ON ScheduleTracker.scheduleFk = Schedule.scheduleUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS HolidayCalendarReplicate ( hcPk BIGINT NOT NULL, hcVersionId BIGINT NOT NULL, hcDestination BIGINT NOT NULL, hcProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (hcPk, hcDestination)) "
        _stmtList +=
            " CREATE INDEX index_HolidayCalendarReplicate_hcDestination_hcProcessed_hcPk ON HolidayCalendarReplicate (hcDestination, hcProcessed, hcPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_28_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (28, NEW.umCalendarUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_28_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_28_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_28_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (28, OLD.umCalendarUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_28_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_28_fn(); "
        _stmtList +=
            "CREATE VIEW HolidayCalendar_ReceiveView AS  SELECT HolidayCalendar.*, HolidayCalendarTracker.* FROM HolidayCalendar LEFT JOIN HolidayCalendarTracker ON HolidayCalendarTracker.hcFk = HolidayCalendar.umCalendarUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS HolidayReplicate ( holidayPk BIGINT NOT NULL, holidayVersionId BIGINT NOT NULL, holidayDestination BIGINT NOT NULL, holidayProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (holidayPk, holidayDestination)) "
        _stmtList +=
            " CREATE INDEX index_HolidayReplicate_holidayDestination_holidayProcessed_holidayPk ON HolidayReplicate (holidayDestination, holidayProcessed, holidayPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_99_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (99, NEW.holUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_99_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_99_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_99_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (99, OLD.holUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_99_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_99_fn(); "
        _stmtList +=
            "CREATE VIEW Holiday_ReceiveView AS  SELECT Holiday.*, HolidayTracker.* FROM Holiday LEFT JOIN HolidayTracker ON HolidayTracker.holidayFk = Holiday.holUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonReplicate ( personPk BIGINT NOT NULL, personVersionId BIGINT NOT NULL, personDestination BIGINT NOT NULL, personProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (personPk, personDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonReplicate_personDestination_personProcessed_personPk ON PersonReplicate (personDestination, personProcessed, personPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_9_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (9, NEW.personUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_9_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_9_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_9_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (9, OLD.personUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_9_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_9_fn(); "
        _stmtList +=
            "CREATE VIEW Person_ReceiveView AS  SELECT Person.*, PersonTracker.* FROM Person LEFT JOIN PersonTracker ON PersonTracker.personFk = Person.personUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzReplicate ( clazzPk BIGINT NOT NULL, clazzVersionId BIGINT NOT NULL, clazzDestination BIGINT NOT NULL, clazzProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (clazzPk, clazzDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzReplicate_clazzDestination_clazzProcessed_clazzPk ON ClazzReplicate (clazzDestination, clazzProcessed, clazzPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_6_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (6, NEW.clazzUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_6_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_6_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_6_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (6, OLD.clazzUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_6_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_6_fn(); "
        _stmtList +=
            "CREATE VIEW Clazz_ReceiveView AS  SELECT Clazz.*, ClazzTracker.* FROM Clazz LEFT JOIN ClazzTracker ON ClazzTracker.clazzFk = Clazz.clazzUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzEnrolmentReplicate ( cePk BIGINT NOT NULL, ceVersionId BIGINT NOT NULL, ceDestination BIGINT NOT NULL, ceProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (cePk, ceDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzEnrolmentReplicate_ceDestination_ceProcessed_cePk ON ClazzEnrolmentReplicate (ceDestination, ceProcessed, cePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_65_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (65, NEW.clazzEnrolmentUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_65_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_65_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_65_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (65, OLD.clazzEnrolmentUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_65_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_65_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzEnrolment_ReceiveView AS  SELECT ClazzEnrolment.*, ClazzEnrolmentTracker.* FROM ClazzEnrolment LEFT JOIN ClazzEnrolmentTracker ON ClazzEnrolmentTracker.ceFk = ClazzEnrolment.clazzEnrolmentUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LeavingReasonReplicate ( lrPk BIGINT NOT NULL, lrVersionId BIGINT NOT NULL, lrDestination BIGINT NOT NULL, lrProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (lrPk, lrDestination)) "
        _stmtList +=
            " CREATE INDEX index_LeavingReasonReplicate_lrDestination_lrProcessed_lrPk ON LeavingReasonReplicate (lrDestination, lrProcessed, lrPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_410_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (410, NEW.leavingReasonUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_410_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_410_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_410_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (410, OLD.leavingReasonUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_410_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_410_fn(); "
        _stmtList +=
            "CREATE VIEW LeavingReason_ReceiveView AS  SELECT LeavingReason.*, LeavingReasonTracker.* FROM LeavingReason LEFT JOIN LeavingReasonTracker ON LeavingReasonTracker.lrFk = LeavingReason.leavingReasonUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryReplicate ( cePk BIGINT NOT NULL, ceVersionId BIGINT NOT NULL, ceDestination BIGINT NOT NULL, ceProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (cePk, ceDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryReplicate_ceDestination_ceProcessed_cePk ON ContentEntryReplicate (ceDestination, ceProcessed, cePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_42_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (42, NEW.contentEntryUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_42_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_42_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_42_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (42, OLD.contentEntryUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_42_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_42_fn(); "
        _stmtList +=
            "CREATE VIEW ContentEntry_ReceiveView AS  SELECT ContentEntry.*, ContentEntryTracker.* FROM ContentEntry LEFT JOIN ContentEntryTracker ON ContentEntryTracker.ceFk = ContentEntry.contentEntryUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryContentCategoryJoinReplicate ( ceccjPk BIGINT NOT NULL, ceccjVersionId BIGINT NOT NULL, ceccjDestination BIGINT NOT NULL, ceccjProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (ceccjPk, ceccjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryContentCategoryJoinReplicate_ceccjDestination_ceccjProcessed_ceccjPk ON ContentEntryContentCategoryJoinReplicate (ceccjDestination, ceccjProcessed, ceccjPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_3_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (3, NEW.ceccjUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_3_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_3_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_3_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (3, OLD.ceccjUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_3_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_3_fn(); "
        _stmtList +=
            "CREATE VIEW ContentEntryContentCategoryJoin_ReceiveView AS  SELECT ContentEntryContentCategoryJoin.*, ContentEntryContentCategoryJoinTracker.* FROM ContentEntryContentCategoryJoin LEFT JOIN ContentEntryContentCategoryJoinTracker ON ContentEntryContentCategoryJoinTracker.ceccjFk = ContentEntryContentCategoryJoin.ceccjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryParentChildJoinReplicate ( cepcjPk BIGINT NOT NULL, cepcjVersionId BIGINT NOT NULL, cepcjDestination BIGINT NOT NULL, cepcjProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (cepcjPk, cepcjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryParentChildJoinReplicate_cepcjDestination_cepcjProcessed_cepcjPk ON ContentEntryParentChildJoinReplicate (cepcjDestination, cepcjProcessed, cepcjPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_7_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (7, NEW.cepcjUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_7_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_7_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_7_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (7, OLD.cepcjUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_7_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_7_fn(); "
        _stmtList +=
            "CREATE VIEW ContentEntryParentChildJoin_ReceiveView AS  SELECT ContentEntryParentChildJoin.*, ContentEntryParentChildJoinTracker.* FROM ContentEntryParentChildJoin LEFT JOIN ContentEntryParentChildJoinTracker ON ContentEntryParentChildJoinTracker.cepcjFk = ContentEntryParentChildJoin.cepcjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryRelatedEntryJoinReplicate ( cerejPk BIGINT NOT NULL, cerejVersionId BIGINT NOT NULL, cerejDestination BIGINT NOT NULL, cerejProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (cerejPk, cerejDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryRelatedEntryJoinReplicate_cerejDestination_cerejProcessed_cerejPk ON ContentEntryRelatedEntryJoinReplicate (cerejDestination, cerejProcessed, cerejPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_8_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (8, NEW.cerejUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_8_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_8_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_8_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (8, OLD.cerejUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_8_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_8_fn(); "
        _stmtList +=
            "CREATE VIEW ContentEntryRelatedEntryJoin_ReceiveView AS  SELECT ContentEntryRelatedEntryJoin.*, ContentEntryRelatedEntryJoinTracker.* FROM ContentEntryRelatedEntryJoin LEFT JOIN ContentEntryRelatedEntryJoinTracker ON ContentEntryRelatedEntryJoinTracker.cerejFk = ContentEntryRelatedEntryJoin.cerejUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentCategorySchemaReplicate ( ccsPk BIGINT NOT NULL, ccsVersionId BIGINT NOT NULL, ccsDestination BIGINT NOT NULL, ccsProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (ccsPk, ccsDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentCategorySchemaReplicate_ccsDestination_ccsProcessed_ccsPk ON ContentCategorySchemaReplicate (ccsDestination, ccsProcessed, ccsPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_2_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (2, NEW.contentCategorySchemaUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_2_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_2_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_2_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (2, OLD.contentCategorySchemaUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_2_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_2_fn(); "
        _stmtList +=
            "CREATE VIEW ContentCategorySchema_ReceiveView AS  SELECT ContentCategorySchema.*, ContentCategorySchemaTracker.* FROM ContentCategorySchema LEFT JOIN ContentCategorySchemaTracker ON ContentCategorySchemaTracker.ccsFk = ContentCategorySchema.contentCategorySchemaUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentCategoryReplicate ( ccPk BIGINT NOT NULL, ccVersionId BIGINT NOT NULL, ccDestination BIGINT NOT NULL, ccProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (ccPk, ccDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentCategoryReplicate_ccDestination_ccProcessed_ccPk ON ContentCategoryReplicate (ccDestination, ccProcessed, ccPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_1_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (1, NEW.contentCategoryUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_1_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_1_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_1_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (1, OLD.contentCategoryUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_1_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_1_fn(); "
        _stmtList +=
            "CREATE VIEW ContentCategory_ReceiveView AS  SELECT ContentCategory.*, ContentCategoryTracker.* FROM ContentCategory LEFT JOIN ContentCategoryTracker ON ContentCategoryTracker.ccFk = ContentCategory.contentCategoryUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LanguageReplicate ( languagePk BIGINT NOT NULL, languageVersionId BIGINT NOT NULL, languageDestination BIGINT NOT NULL, languageProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (languagePk, languageDestination)) "
        _stmtList +=
            " CREATE INDEX index_LanguageReplicate_languageDestination_languageProcessed_languagePk ON LanguageReplicate (languageDestination, languageProcessed, languagePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_13_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (13, NEW.langUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_13_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_13_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_13_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (13, OLD.langUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_13_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_13_fn(); "
        _stmtList +=
            "CREATE VIEW Language_ReceiveView AS  SELECT Language.*, LanguageTracker.* FROM Language LEFT JOIN LanguageTracker ON LanguageTracker.languageFk = Language.langUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LanguageVariantReplicate ( lvPk BIGINT NOT NULL, lvVersionId BIGINT NOT NULL, lvDestination BIGINT NOT NULL, lvProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (lvPk, lvDestination)) "
        _stmtList +=
            " CREATE INDEX index_LanguageVariantReplicate_lvDestination_lvProcessed_lvPk ON LanguageVariantReplicate (lvDestination, lvProcessed, lvPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_10_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (10, NEW.langVariantUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_10_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_10_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_10_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (10, OLD.langVariantUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_10_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_10_fn(); "
        _stmtList +=
            "CREATE VIEW LanguageVariant_ReceiveView AS  SELECT LanguageVariant.*, LanguageVariantTracker.* FROM LanguageVariant LEFT JOIN LanguageVariantTracker ON LanguageVariantTracker.lvFk = LanguageVariant.langVariantUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS RoleReplicate ( rolePk BIGINT NOT NULL, roleVersionId BIGINT NOT NULL, roleDestination BIGINT NOT NULL, roleProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (rolePk, roleDestination)) "
        _stmtList +=
            " CREATE INDEX index_RoleReplicate_roleDestination_roleProcessed_rolePk ON RoleReplicate (roleDestination, roleProcessed, rolePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_45_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (45, NEW.roleUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_45_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_45_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_45_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (45, OLD.roleUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_45_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_45_fn(); "
        _stmtList +=
            "CREATE VIEW Role_ReceiveView AS  SELECT Role.*, RoleTracker.* FROM Role LEFT JOIN RoleTracker ON RoleTracker.roleFk = Role.roleUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonGroupReplicate ( pgPk BIGINT NOT NULL, pgVersionId BIGINT NOT NULL, pgDestination BIGINT NOT NULL, pgProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (pgPk, pgDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupReplicate_pgDestination_pgProcessed_pgPk ON PersonGroupReplicate (pgDestination, pgProcessed, pgPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_43_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (43, NEW.groupUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_43_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_43_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_43_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (43, OLD.groupUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_43_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_43_fn(); "
        _stmtList +=
            "CREATE VIEW PersonGroup_ReceiveView AS  SELECT PersonGroup.*, PersonGroupTracker.* FROM PersonGroup LEFT JOIN PersonGroupTracker ON PersonGroupTracker.pgFk = PersonGroup.groupUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonGroupMemberReplicate ( pgmPk BIGINT NOT NULL, pgmVersionId BIGINT NOT NULL, pgmDestination BIGINT NOT NULL, pgmProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (pgmPk, pgmDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupMemberReplicate_pgmDestination_pgmProcessed_pgmPk ON PersonGroupMemberReplicate (pgmDestination, pgmProcessed, pgmPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_44_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (44, NEW.groupMemberUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_44_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_44_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_44_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (44, OLD.groupMemberUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_44_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_44_fn(); "
        _stmtList +=
            "CREATE VIEW PersonGroupMember_ReceiveView AS  SELECT PersonGroupMember.*, PersonGroupMemberTracker.* FROM PersonGroupMember LEFT JOIN PersonGroupMemberTracker ON PersonGroupMemberTracker.pgmFk = PersonGroupMember.groupMemberUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonPictureReplicate ( ppPk BIGINT NOT NULL, ppVersionId BIGINT NOT NULL, ppDestination BIGINT NOT NULL, ppProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (ppPk, ppDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonPictureReplicate_ppDestination_ppProcessed_ppPk ON PersonPictureReplicate (ppDestination, ppProcessed, ppPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_50_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (50, NEW.personPictureUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_50_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_50_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_50_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (50, OLD.personPictureUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_50_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_50_fn(); "
        _stmtList +=
            "CREATE VIEW PersonPicture_ReceiveView AS  SELECT PersonPicture.*, PersonPictureTracker.* FROM PersonPicture LEFT JOIN PersonPictureTracker ON PersonPictureTracker.ppFk = PersonPicture.personPictureUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContainerReplicate ( containerPk BIGINT NOT NULL, containerVersionId BIGINT NOT NULL, containerDestination BIGINT NOT NULL, containerProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (containerPk, containerDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContainerReplicate_containerDestination_containerProcessed_containerPk ON ContainerReplicate (containerDestination, containerProcessed, containerPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_51_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (51, NEW.containerUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_51_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_51_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_51_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (51, OLD.containerUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_51_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_51_fn(); "
        _stmtList +=
            "CREATE VIEW Container_ReceiveView AS  SELECT Container.*, ContainerTracker.* FROM Container LEFT JOIN ContainerTracker ON ContainerTracker.containerFk = Container.containerUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS VerbEntityReplicate ( vePk BIGINT NOT NULL, veVersionId BIGINT NOT NULL, veDestination BIGINT NOT NULL, veProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (vePk, veDestination)) "
        _stmtList +=
            " CREATE INDEX index_VerbEntityReplicate_veDestination_veProcessed_vePk ON VerbEntityReplicate (veDestination, veProcessed, vePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_62_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (62, NEW.verbUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_62_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_62_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_62_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (62, OLD.verbUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_62_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_62_fn(); "
        _stmtList +=
            "CREATE VIEW VerbEntity_ReceiveView AS  SELECT VerbEntity.*, VerbEntityTracker.* FROM VerbEntity LEFT JOIN VerbEntityTracker ON VerbEntityTracker.veFk = VerbEntity.verbUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS XObjectEntityReplicate ( xoePk BIGINT NOT NULL, xoeVersionId BIGINT NOT NULL, xoeDestination BIGINT NOT NULL, xoeProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (xoePk, xoeDestination)) "
        _stmtList +=
            " CREATE INDEX index_XObjectEntityReplicate_xoeDestination_xoeProcessed_xoePk ON XObjectEntityReplicate (xoeDestination, xoeProcessed, xoePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_64_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (64, NEW.xObjectUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_64_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_64_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_64_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (64, OLD.xObjectUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_64_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_64_fn(); "
        _stmtList +=
            "CREATE VIEW XObjectEntity_ReceiveView AS  SELECT XObjectEntity.*, XObjectEntityTracker.* FROM XObjectEntity LEFT JOIN XObjectEntityTracker ON XObjectEntityTracker.xoeFk = XObjectEntity.xObjectUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StatementEntityReplicate ( sePk BIGINT NOT NULL, seVersionId BIGINT NOT NULL, seDestination BIGINT NOT NULL, seProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (sePk, seDestination)) "
        _stmtList +=
            " CREATE INDEX index_StatementEntityReplicate_seDestination_seProcessed_sePk ON StatementEntityReplicate (seDestination, seProcessed, sePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_60_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (60, NEW.statementUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_60_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_60_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_60_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (60, OLD.statementUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_60_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_60_fn(); "
        _stmtList +=
            "CREATE VIEW StatementEntity_ReceiveView AS  SELECT StatementEntity.*, StatementEntityTracker.* FROM StatementEntity LEFT JOIN StatementEntityTracker ON StatementEntityTracker.seFk = StatementEntity.statementUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContextXObjectStatementJoinReplicate ( cxosjPk BIGINT NOT NULL, cxosjVersionId BIGINT NOT NULL, cxosjDestination BIGINT NOT NULL, cxosjProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (cxosjPk, cxosjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContextXObjectStatementJoinReplicate_cxosjDestination_cxosjProcessed_cxosjPk ON ContextXObjectStatementJoinReplicate (cxosjDestination, cxosjProcessed, cxosjPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_66_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (66, NEW.contextXObjectStatementJoinUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_66_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_66_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_66_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (66, OLD.contextXObjectStatementJoinUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_66_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_66_fn(); "
        _stmtList +=
            "CREATE VIEW ContextXObjectStatementJoin_ReceiveView AS  SELECT ContextXObjectStatementJoin.*, ContextXObjectStatementJoinTracker.* FROM ContextXObjectStatementJoin LEFT JOIN ContextXObjectStatementJoinTracker ON ContextXObjectStatementJoinTracker.cxosjFk = ContextXObjectStatementJoin.contextXObjectStatementJoinUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS AgentEntityReplicate ( aePk BIGINT NOT NULL, aeVersionId BIGINT NOT NULL, aeDestination BIGINT NOT NULL, aeProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (aePk, aeDestination)) "
        _stmtList +=
            " CREATE INDEX index_AgentEntityReplicate_aeDestination_aeProcessed_aePk ON AgentEntityReplicate (aeDestination, aeProcessed, aePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_68_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (68, NEW.agentUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_68_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_68_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_68_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (68, OLD.agentUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_68_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_68_fn(); "
        _stmtList +=
            "CREATE VIEW AgentEntity_ReceiveView AS  SELECT AgentEntity.*, AgentEntityTracker.* FROM AgentEntity LEFT JOIN AgentEntityTracker ON AgentEntityTracker.aeFk = AgentEntity.agentUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StateEntityReplicate ( sePk BIGINT NOT NULL, seVersionId BIGINT NOT NULL, seDestination BIGINT NOT NULL, seProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (sePk, seDestination)) "
        _stmtList +=
            " CREATE INDEX index_StateEntityReplicate_seDestination_seProcessed_sePk ON StateEntityReplicate (seDestination, seProcessed, sePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_70_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (70, NEW.stateUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_70_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_70_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_70_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (70, OLD.stateUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_70_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_70_fn(); "
        _stmtList +=
            "CREATE VIEW StateEntity_ReceiveView AS  SELECT StateEntity.*, StateEntityTracker.* FROM StateEntity LEFT JOIN StateEntityTracker ON StateEntityTracker.seFk = StateEntity.stateUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StateContentEntityReplicate ( scePk BIGINT NOT NULL, sceVersionId BIGINT NOT NULL, sceDestination BIGINT NOT NULL, sceProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (scePk, sceDestination)) "
        _stmtList +=
            " CREATE INDEX index_StateContentEntityReplicate_sceDestination_sceProcessed_scePk ON StateContentEntityReplicate (sceDestination, sceProcessed, scePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_72_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (72, NEW.stateContentUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_72_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_72_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_72_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (72, OLD.stateContentUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_72_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_72_fn(); "
        _stmtList +=
            "CREATE VIEW StateContentEntity_ReceiveView AS  SELECT StateContentEntity.*, StateContentEntityTracker.* FROM StateContentEntity LEFT JOIN StateContentEntityTracker ON StateContentEntityTracker.sceFk = StateContentEntity.stateContentUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS XLangMapEntryReplicate ( xlmePk BIGINT NOT NULL, xlmeVersionId BIGINT NOT NULL, xlmeDestination BIGINT NOT NULL, xlmeProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (xlmePk, xlmeDestination)) "
        _stmtList +=
            " CREATE INDEX index_XLangMapEntryReplicate_xlmeDestination_xlmeProcessed_xlmePk ON XLangMapEntryReplicate (xlmeDestination, xlmeProcessed, xlmePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_74_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (74, NEW.statementLangMapUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_74_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_74_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_74_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (74, OLD.statementLangMapUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_74_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_74_fn(); "
        _stmtList +=
            "CREATE VIEW XLangMapEntry_ReceiveView AS  SELECT XLangMapEntry.*, XLangMapEntryTracker.* FROM XLangMapEntry LEFT JOIN XLangMapEntryTracker ON XLangMapEntryTracker.xlmeFk = XLangMapEntry.statementLangMapUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SchoolReplicate ( schoolPk BIGINT NOT NULL, schoolVersionId BIGINT NOT NULL, schoolDestination BIGINT NOT NULL, schoolProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (schoolPk, schoolDestination)) "
        _stmtList +=
            " CREATE INDEX index_SchoolReplicate_schoolDestination_schoolProcessed_schoolPk ON SchoolReplicate (schoolDestination, schoolProcessed, schoolPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_164_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (164, NEW.schoolUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_164_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_164_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_164_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (164, OLD.schoolUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_164_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_164_fn(); "
        _stmtList +=
            "CREATE VIEW School_ReceiveView AS  SELECT School.*, SchoolTracker.* FROM School LEFT JOIN SchoolTracker ON SchoolTracker.schoolFk = School.schoolUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SchoolMemberReplicate ( smPk BIGINT NOT NULL, smVersionId BIGINT NOT NULL, smDestination BIGINT NOT NULL, smProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (smPk, smDestination)) "
        _stmtList +=
            " CREATE INDEX index_SchoolMemberReplicate_smDestination_smProcessed_smPk ON SchoolMemberReplicate (smDestination, smProcessed, smPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_200_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (200, NEW.schoolMemberUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_200_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_200_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_200_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (200, OLD.schoolMemberUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_200_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_200_fn(); "
        _stmtList +=
            "CREATE VIEW SchoolMember_ReceiveView AS  SELECT SchoolMember.*, SchoolMemberTracker.* FROM SchoolMember LEFT JOIN SchoolMemberTracker ON SchoolMemberTracker.smFk = SchoolMember.schoolMemberUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS CommentsReplicate ( commentsPk BIGINT NOT NULL, commentsVersionId BIGINT NOT NULL, commentsDestination BIGINT NOT NULL, commentsProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (commentsPk, commentsDestination)) "
        _stmtList +=
            " CREATE INDEX index_CommentsReplicate_commentsDestination_commentsProcessed_commentsPk ON CommentsReplicate (commentsDestination, commentsProcessed, commentsPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_208_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (208, NEW.commentsUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_208_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_208_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_208_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (208, OLD.commentsUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_208_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_208_fn(); "
        _stmtList +=
            "CREATE VIEW Comments_ReceiveView AS  SELECT Comments.*, CommentsTracker.* FROM Comments LEFT JOIN CommentsTracker ON CommentsTracker.commentsFk = Comments.commentsUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ReportReplicate ( reportPk BIGINT NOT NULL, reportVersionId BIGINT NOT NULL, reportDestination BIGINT NOT NULL, reportProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (reportPk, reportDestination)) "
        _stmtList +=
            " CREATE INDEX index_ReportReplicate_reportDestination_reportProcessed_reportPk ON ReportReplicate (reportDestination, reportProcessed, reportPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_101_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (101, NEW.reportUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_101_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_101_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_101_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (101, OLD.reportUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_101_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_101_fn(); "
        _stmtList +=
            "CREATE VIEW Report_ReceiveView AS  SELECT Report.*, ReportTracker.* FROM Report LEFT JOIN ReportTracker ON ReportTracker.reportFk = Report.reportUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SiteReplicate ( sitePk BIGINT NOT NULL, siteVersionId BIGINT NOT NULL, siteDestination BIGINT NOT NULL, siteProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (sitePk, siteDestination)) "
        _stmtList +=
            " CREATE INDEX index_SiteReplicate_siteDestination_siteProcessed_sitePk ON SiteReplicate (siteDestination, siteProcessed, sitePk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_189_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (189, NEW.siteUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_189_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_189_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_189_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (189, OLD.siteUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_189_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_189_fn(); "
        _stmtList +=
            "CREATE VIEW Site_ReceiveView AS  SELECT Site.*, SiteTracker.* FROM Site LEFT JOIN SiteTracker ON SiteTracker.siteFk = Site.siteUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION site_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO Site(siteUid, sitePcsn, siteLcsn, siteLcb, siteLct, siteName, guestLogin, registrationAllowed, authSalt) VALUES (NEW.siteUid, NEW.sitePcsn, NEW.siteLcsn, NEW.siteLcb, NEW.siteLct, NEW.siteName, NEW.guestLogin, NEW.registrationAllowed, NEW.authSalt) ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER site_remote_insert_trig INSTEAD OF INSERT ON Site_ReceiveView FOR EACH ROW EXECUTE PROCEDURE site_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LearnerGroupReplicate ( lgPk BIGINT NOT NULL, lgVersionId BIGINT NOT NULL, lgDestination BIGINT NOT NULL, lgProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (lgPk, lgDestination)) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupReplicate_lgDestination_lgProcessed_lgPk ON LearnerGroupReplicate (lgDestination, lgProcessed, lgPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_301_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (301, NEW.learnerGroupUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_301_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_301_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_301_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (301, OLD.learnerGroupUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_301_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_301_fn(); "
        _stmtList +=
            "CREATE VIEW LearnerGroup_ReceiveView AS  SELECT LearnerGroup.*, LearnerGroupTracker.* FROM LearnerGroup LEFT JOIN LearnerGroupTracker ON LearnerGroupTracker.lgFk = LearnerGroup.learnerGroupUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LearnerGroupMemberReplicate ( lgmPk BIGINT NOT NULL, lgmVersionId BIGINT NOT NULL, lgmDestination BIGINT NOT NULL, lgmProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (lgmPk, lgmDestination)) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupMemberReplicate_lgmDestination_lgmProcessed_lgmPk ON LearnerGroupMemberReplicate (lgmDestination, lgmProcessed, lgmPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_300_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (300, NEW.learnerGroupMemberUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_300_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_300_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_300_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (300, OLD.learnerGroupMemberUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_300_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_300_fn(); "
        _stmtList +=
            "CREATE VIEW LearnerGroupMember_ReceiveView AS  SELECT LearnerGroupMember.*, LearnerGroupMemberTracker.* FROM LearnerGroupMember LEFT JOIN LearnerGroupMemberTracker ON LearnerGroupMemberTracker.lgmFk = LearnerGroupMember.learnerGroupMemberUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS GroupLearningSessionReplicate ( glsPk BIGINT NOT NULL, glsVersionId BIGINT NOT NULL, glsDestination BIGINT NOT NULL, glsProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (glsPk, glsDestination)) "
        _stmtList +=
            " CREATE INDEX index_GroupLearningSessionReplicate_glsDestination_glsProcessed_glsPk ON GroupLearningSessionReplicate (glsDestination, glsProcessed, glsPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_302_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (302, NEW.groupLearningSessionUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_302_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_302_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_302_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (302, OLD.groupLearningSessionUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_302_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_302_fn(); "
        _stmtList +=
            "CREATE VIEW GroupLearningSession_ReceiveView AS  SELECT GroupLearningSession.*, GroupLearningSessionTracker.* FROM GroupLearningSession LEFT JOIN GroupLearningSessionTracker ON GroupLearningSessionTracker.glsFk = GroupLearningSession.groupLearningSessionUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SiteTermsReplicate ( stPk BIGINT NOT NULL, stVersionId BIGINT NOT NULL, stDestination BIGINT NOT NULL, stProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (stPk, stDestination)) "
        _stmtList +=
            " CREATE INDEX index_SiteTermsReplicate_stDestination_stProcessed_stPk ON SiteTermsReplicate (stDestination, stProcessed, stPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_272_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (272, NEW.sTermsUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_272_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_272_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_272_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (272, OLD.sTermsUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_272_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_272_fn(); "
        _stmtList +=
            "CREATE VIEW SiteTerms_ReceiveView AS  SELECT SiteTerms.*, SiteTermsTracker.* FROM SiteTerms LEFT JOIN SiteTermsTracker ON SiteTermsTracker.stFk = SiteTerms.sTermsUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzContentJoinReplicate ( ccjPk BIGINT NOT NULL, ccjVersionId BIGINT NOT NULL, ccjDestination BIGINT NOT NULL, ccjProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (ccjPk, ccjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzContentJoinReplicate_ccjDestination_ccjProcessed_ccjPk ON ClazzContentJoinReplicate (ccjDestination, ccjProcessed, ccjPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_134_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (134, NEW.ccjUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_134_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_134_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_134_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (134, OLD.ccjUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_134_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_134_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzContentJoin_ReceiveView AS  SELECT ClazzContentJoin.*, ClazzContentJoinTracker.* FROM ClazzContentJoin LEFT JOIN ClazzContentJoinTracker ON ClazzContentJoinTracker.ccjFk = ClazzContentJoin.ccjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonParentJoinReplicate ( ppjPk BIGINT NOT NULL, ppjVersionId BIGINT NOT NULL, ppjDestination BIGINT NOT NULL, ppjProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (ppjPk, ppjDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonParentJoinReplicate_ppjDestination_ppjProcessed_ppjPk ON PersonParentJoinReplicate (ppjDestination, ppjProcessed, ppjPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_512_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (512, NEW.ppjUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_512_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_512_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_512_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (512, OLD.ppjUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_512_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_512_fn(); "
        _stmtList +=
            "CREATE VIEW PersonParentJoin_ReceiveView AS  SELECT PersonParentJoin.*, PersonParentJoinTracker.* FROM PersonParentJoin LEFT JOIN PersonParentJoinTracker ON PersonParentJoinTracker.ppjFk = PersonParentJoin.ppjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ScopedGrantReplicate ( sgPk BIGINT NOT NULL, sgVersionId BIGINT NOT NULL, sgDestination BIGINT NOT NULL, sgProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (sgPk, sgDestination)) "
        _stmtList +=
            " CREATE INDEX index_ScopedGrantReplicate_sgDestination_sgProcessed_sgPk ON ScopedGrantReplicate (sgDestination, sgProcessed, sgPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_48_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (48, NEW.sgUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_48_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_48_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_48_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (48, OLD.sgUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_48_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_48_fn(); "
        _stmtList +=
            "CREATE VIEW ScopedGrant_ReceiveView AS  SELECT ScopedGrant.*, ScopedGrantTracker.* FROM ScopedGrant LEFT JOIN ScopedGrantTracker ON ScopedGrantTracker.sgFk = ScopedGrant.sgUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION sg_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ScopedGrant(sgUid, sgPcsn, sgLcsn, sgLcb, sgLct, sgTableId, sgEntityUid, sgPermissions, sgGroupUid, sgIndex, sgFlags) VALUES (NEW.sgUid, NEW.sgPcsn, NEW.sgLcsn, NEW.sgLcb, NEW.sgLct, NEW.sgTableId, NEW.sgEntityUid, NEW.sgPermissions, NEW.sgGroupUid, NEW.sgIndex, NEW.sgFlags) ON CONFLICT(sgUid) DO UPDATE SET sgLct = EXCLUDED.sgLct, sgPermissions = EXCLUDED.sgPermissions ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER sg_remote_insert_trig INSTEAD OF INSERT ON ScopedGrant_ReceiveView FOR EACH ROW EXECUTE PROCEDURE sg_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ErrorReportReplicate ( erPk BIGINT NOT NULL, erVersionId BIGINT NOT NULL, erDestination BIGINT NOT NULL, erProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (erPk, erDestination)) "
        _stmtList +=
            " CREATE INDEX index_ErrorReportReplicate_erDestination_erProcessed_erPk ON ErrorReportReplicate (erDestination, erProcessed, erPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_419_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (419, NEW.errUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_419_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_419_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_419_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (419, OLD.errUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_419_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_419_fn(); "
        _stmtList +=
            "CREATE VIEW ErrorReport_ReceiveView AS  SELECT ErrorReport.*, ErrorReportTracker.* FROM ErrorReport LEFT JOIN ErrorReportTracker ON ErrorReportTracker.erFk = ErrorReport.errUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzAssignmentReplicate ( caPk BIGINT NOT NULL, caVersionId BIGINT NOT NULL, caDestination BIGINT NOT NULL, caProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (caPk, caDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentReplicate_caDestination_caProcessed_caPk ON ClazzAssignmentReplicate (caDestination, caProcessed, caPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_520_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (520, NEW.caUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_520_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_520_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_520_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (520, OLD.caUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_520_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_520_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzAssignment_ReceiveView AS  SELECT ClazzAssignment.*, ClazzAssignmentTracker.* FROM ClazzAssignment LEFT JOIN ClazzAssignmentTracker ON ClazzAssignmentTracker.caFk = ClazzAssignment.caUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoinReplicate ( cacjPk BIGINT NOT NULL, cacjVersionId BIGINT NOT NULL, cacjDestination BIGINT NOT NULL, cacjProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (cacjPk, cacjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentContentJoinReplicate_cacjDestination_cacjProcessed_cacjPk ON ClazzAssignmentContentJoinReplicate (cacjDestination, cacjProcessed, cacjPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_521_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (521, NEW.cacjUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_521_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_521_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_521_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (521, OLD.cacjUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_521_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_521_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzAssignmentContentJoin_ReceiveView AS  SELECT ClazzAssignmentContentJoin.*, ClazzAssignmentContentJoinTracker.* FROM ClazzAssignmentContentJoin LEFT JOIN ClazzAssignmentContentJoinTracker ON ClazzAssignmentContentJoinTracker.cacjFk = ClazzAssignmentContentJoin.cacjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonAuth2Replicate ( paPk BIGINT NOT NULL, paVersionId BIGINT NOT NULL, paDestination BIGINT NOT NULL, paProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (paPk, paDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonAuth2Replicate_paDestination_paProcessed_paPk ON PersonAuth2Replicate (paDestination, paProcessed, paPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_678_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (678, NEW.pauthUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_678_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_678_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_678_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (678, OLD.pauthUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_678_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_678_fn(); "
        _stmtList +=
            "CREATE VIEW PersonAuth2_ReceiveView AS  SELECT PersonAuth2.*, PersonAuth2Tracker.* FROM PersonAuth2 LEFT JOIN PersonAuth2Tracker ON PersonAuth2Tracker.paFk = PersonAuth2.pauthUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS UserSessionReplicate ( usPk BIGINT NOT NULL, usVersionId BIGINT NOT NULL, usDestination BIGINT NOT NULL, usProcessed BOOL NOT NULL DEFAULT false, PRIMARY KEY (usPk, usDestination)) "
        _stmtList +=
            " CREATE INDEX index_UserSessionReplicate_usDestination_usProcessed_usPk ON UserSessionReplicate (usDestination, usProcessed, usPk) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_679_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (679, NEW.usUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_679_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_679_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_679_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (679, OLD.usUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_679_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_679_fn(); "
        _stmtList +=
            "CREATE VIEW UserSession_ReceiveView AS  SELECT UserSession.*, UserSessionTracker.* FROM UserSession LEFT JOIN UserSessionTracker ON UserSessionTracker.usFk = UserSession.usUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION usersession_remote_ins_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO UserSession(usUid, usPcsn, usLcsn, usLcb, usLct, usPersonUid, usClientNodeId, usStartTime, usEndTime, usStatus, usReason, usAuth, usSessionType) VALUES (NEW.usUid, NEW.usPcsn, NEW.usLcsn, NEW.usLcb, NEW.usLct, NEW.usPersonUid, NEW.usClientNodeId, NEW.usStartTime, NEW.usEndTime, NEW.usStatus, NEW.usReason, NEW.usAuth, NEW.usSessionType) /*postgres ON CONFLICT (usUid) DO UPDATE SET usStatus = EXCLUDED.usStatus, usEndTime = EXCLUDED.usEndTime, usReason = EXCLUDED.usReason */ ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER usersession_remote_ins_trig INSTEAD OF INSERT ON UserSession_ReceiveView FOR EACH ROW EXECUTE PROCEDURE usersession_remote_ins_fn() "
    }
    db.execSqlBatch(_stmtList.toTypedArray())
}


val UmAppDatabaseReplicationMigration90_91  = DoorMigrationSync(90, 91){ db ->
    db.execSQL("ALTER TABLE DoorNode ADD COLUMN rel INTEGER NOT NULL DEFAULT 2")
    db.execSQL("ALTER TABLE Person ADD COLUMN personType INTEGER NOT NULL DEFAULT 0")
    db.execSQL("DROP TABLE ChangeLog")
    db.execSQL("DROP TABLE SyncResult")
    db.execSQL("DROP TABLE TableSyncStatus")

    //Drop old TRK
    db.execSQL("DROP TABLE IF EXISTS ClazzLog_trk")
    db.execSQL("DROP TABLE IF EXISTS ClazzLogAttendanceRecord_trk")
    db.execSQL("DROP TABLE IF EXISTS Schedule_trk")
    db.execSQL("DROP TABLE IF EXISTS DateRange_trk")
    db.execSQL("DROP TABLE IF EXISTS HolidayCalendar_trk")
    db.execSQL("DROP TABLE IF EXISTS Holiday_trk")
    db.execSQL("DROP TABLE IF EXISTS ScheduledCheck_trk")
    db.execSQL("DROP TABLE IF EXISTS AuditLog_trk")
    db.execSQL("DROP TABLE IF EXISTS CustomField_trk")
    db.execSQL("DROP TABLE IF EXISTS CustomFieldValue_trk")
    db.execSQL("DROP TABLE IF EXISTS CustomFieldValueOption_trk")
    db.execSQL("DROP TABLE IF EXISTS Person_trk")
    db.execSQL("DROP TABLE IF EXISTS Clazz_trk")
    db.execSQL("DROP TABLE IF EXISTS ClazzEnrolment_trk")
    db.execSQL("DROP TABLE IF EXISTS LeavingReason_trk")
    db.execSQL("DROP TABLE IF EXISTS PersonCustomFieldValue_trk")
    db.execSQL("DROP TABLE IF EXISTS ContentEntry_trk")
    db.execSQL("DROP TABLE IF EXISTS ContentEntryContentCategoryJoin_trk")
    db.execSQL("DROP TABLE IF EXISTS ContentEntryParentChildJoin_trk")
    db.execSQL("DROP TABLE IF EXISTS ContentEntryRelatedEntryJoin_trk")
    db.execSQL("DROP TABLE IF EXISTS ContentCategorySchema_trk")
    db.execSQL("DROP TABLE IF EXISTS ContentCategory_trk")
    db.execSQL("DROP TABLE IF EXISTS Language_trk")
    db.execSQL("DROP TABLE IF EXISTS LanguageVariant_trk")
    db.execSQL("DROP TABLE IF EXISTS Role_trk")
    db.execSQL("DROP TABLE IF EXISTS EntityRole_trk")
    db.execSQL("DROP TABLE IF EXISTS PersonGroup_trk")
    db.execSQL("DROP TABLE IF EXISTS PersonGroupMember_trk")
    db.execSQL("DROP TABLE IF EXISTS PersonPicture_trk")
    db.execSQL("DROP TABLE IF EXISTS Container_trk")
    db.execSQL("DROP TABLE IF EXISTS VerbEntity_trk")
    db.execSQL("DROP TABLE IF EXISTS XObjectEntity_trk")
    db.execSQL("DROP TABLE IF EXISTS StatementEntity_trk")
    db.execSQL("DROP TABLE IF EXISTS ContextXObjectStatementJoin_trk")
    db.execSQL("DROP TABLE IF EXISTS AgentEntity_trk")
    db.execSQL("DROP TABLE IF EXISTS StateEntity_trk")
    db.execSQL("DROP TABLE IF EXISTS StateContentEntity_trk")
    db.execSQL("DROP TABLE IF EXISTS XLangMapEntry_trk")
    db.execSQL("DROP TABLE IF EXISTS School_trk")
    db.execSQL("DROP TABLE IF EXISTS SchoolMember_trk")
    db.execSQL("DROP TABLE IF EXISTS Comments_trk")
    db.execSQL("DROP TABLE IF EXISTS Report_trk")
    db.execSQL("DROP TABLE IF EXISTS Site_trk")
    db.execSQL("DROP TABLE IF EXISTS LearnerGroup_trk")
    db.execSQL("DROP TABLE IF EXISTS LearnerGroupMember_trk")
    db.execSQL("DROP TABLE IF EXISTS GroupLearningSession_trk")
    db.execSQL("DROP TABLE IF EXISTS SiteTerms_trk")
    db.execSQL("DROP TABLE IF EXISTS ClazzContentJoin_trk")
    db.execSQL("DROP TABLE IF EXISTS PersonParentJoin_trk")
    db.execSQL("DROP TABLE IF EXISTS ScopedGrant_trk")
    db.execSQL("DROP TABLE IF EXISTS ErrorReport_trk")
    db.execSQL("DROP TABLE IF EXISTS ClazzAssignment_trk")
    db.execSQL("DROP TABLE IF EXISTS ClazzAssignmentContentJoin_trk")
    db.execSQL("DROP TABLE IF EXISTS PersonAuth2_trk")
    db.execSQL("DROP TABLE IF EXISTS UserSession_trk")



    if(db.dbType() == DoorDbType.SQLITE) {
        db.dropOldSqliteTriggers()

        //Create Replication Status
        db.execSQL( "CREATE TABLE IF NOT EXISTS ReplicationStatus (  tableId  INTEGER  NOT NULL , priority  INTEGER  NOT NULL , nodeId  INTEGER  NOT NULL , lastRemoteChangeTime  INTEGER  NOT NULL , lastFetchReplicationCompleteTime  INTEGER  NOT NULL , lastLocalChangeTime  INTEGER  NOT NULL , lastSendReplicationCompleteTime  INTEGER  NOT NULL , repStatusId  INTEGER  PRIMARY KEY  AUTOINCREMENT  NOT NULL )")
        db.execSQL("CREATE UNIQUE INDEX table_node_idx ON ReplicationStatus (tableId, nodeId)")

        //Create new changelog table
        db.execSQL("CREATE TABLE IF NOT EXISTS ChangeLog (  chTableId  INTEGER  NOT NULL , chEntityPk  INTEGER  NOT NULL , chType  INTEGER  NOT NULL , PRIMARY KEY (chTableId, chEntityPk) )")

        //trackers (temp)
        db.execSQL("CREATE TABLE IF NOT EXISTS UserSessionTrkr (  usForeignKey  INTEGER  NOT NULL , usVersionId  INTEGER  NOT NULL , usDestination  INTEGER  NOT NULL , usTrkrProcessed  INTEGER  NOT NULL  DEFAULT 0 , PRIMARY KEY (usForeignKey, usDestination) )")
        db.execSQL("CREATE TABLE IF NOT EXISTS ScopedGrantTrkr (  sgForeignKey  INTEGER  NOT NULL , sgVersionId  INTEGER  NOT NULL , sgDestination  INTEGER  NOT NULL , sgProcessed  INTEGER  NOT NULL , PRIMARY KEY (sgForeignKey, sgVersionId) )")
        db.execSQL("CREATE INDEX index_ScopedGrantTrkr_sgDestination_sgProcessed_sgForeignKey ON ScopedGrantTrkr (sgDestination, sgProcessed, sgForeignKey)")
        db.execSQL("CREATE TABLE IF NOT EXISTS AgentEntityTrkr (  aeTrkrForeignKey  INTEGER  NOT NULL , aeTrkrLastModified  INTEGER  NOT NULL , aeTrkrDestination  INTEGER  NOT NULL , aeTrkrProcessed  INTEGER  NOT NULL  DEFAULT 0 , PRIMARY KEY (aeTrkrForeignKey, aeTrkrDestination) )")
    }else {
        db.dropOldPostgresTriggers()
        db.dropOldPostgresFunctions()

        db.execSQL("CREATE TABLE IF NOT EXISTS ChangeLog (  chTableId  INTEGER  NOT NULL , chEntityPk  BIGINT  NOT NULL , chType  INTEGER  NOT NULL , PRIMARY KEY (chTableId, chEntityPk) )")

        db.execSQL("CREATE TABLE IF NOT EXISTS ReplicationStatus (  tableId  INTEGER  NOT NULL , priority  INTEGER  NOT NULL , nodeId  BIGINT  NOT NULL , lastRemoteChangeTime  BIGINT  NOT NULL , lastFetchReplicationCompleteTime  BIGINT  NOT NULL , lastLocalChangeTime  BIGINT  NOT NULL , lastSendReplicationCompleteTime  BIGINT  NOT NULL , repStatusId  SERIAL  PRIMARY KEY  NOT NULL )")
        db.execSQL("CREATE UNIQUE INDEX table_node_idx ON ReplicationStatus (tableId, nodeId)")
    }

    db.addReplicationEntities()
}