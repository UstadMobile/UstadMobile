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
            " CREATE TABLE IF NOT EXISTS ClazzLogReplicate ( clPk INTEGER NOT NULL, clVersionId INTEGER NOT NULL DEFAULT 0, clDestination INTEGER NOT NULL, clPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (clPk, clDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogReplicate_clPk_clDestination_clVersionId ON ClazzLogReplicate (clPk, clDestination, clVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogReplicate_clDestination_clPending ON ClazzLogReplicate (clDestination, clPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_14 AFTER INSERT ON ClazzLog BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 14 AS chTableId, NEW.clazzLogUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 14 AND chEntityPk = NEW.clazzLogUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_14 AFTER UPDATE ON ClazzLog BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 14 AS chTableId, NEW.clazzLogUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 14 AND chEntityPk = NEW.clazzLogUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_14 AFTER DELETE ON ClazzLog BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 14 AS chTableId, OLD.clazzLogUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 14 AND chEntityPk = OLD.clazzLogUid); END "
        _stmtList +=
            "CREATE VIEW ClazzLog_ReceiveView AS  SELECT ClazzLog.*, ClazzLogReplicate.* FROM ClazzLog LEFT JOIN ClazzLogReplicate ON ClazzLogReplicate.clPk = ClazzLog.clazzLogUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecordReplicate ( clarPk INTEGER NOT NULL, clarVersionId INTEGER NOT NULL DEFAULT 0, clarDestination INTEGER NOT NULL, clarPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (clarPk, clarDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogAttendanceRecordReplicate_clarPk_clarDestination_clarVersionId ON ClazzLogAttendanceRecordReplicate (clarPk, clarDestination, clarVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogAttendanceRecordReplicate_clarDestination_clarPending ON ClazzLogAttendanceRecordReplicate (clarDestination, clarPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_15 AFTER INSERT ON ClazzLogAttendanceRecord BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 15 AS chTableId, NEW.clazzLogAttendanceRecordUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 15 AND chEntityPk = NEW.clazzLogAttendanceRecordUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_15 AFTER UPDATE ON ClazzLogAttendanceRecord BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 15 AS chTableId, NEW.clazzLogAttendanceRecordUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 15 AND chEntityPk = NEW.clazzLogAttendanceRecordUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_15 AFTER DELETE ON ClazzLogAttendanceRecord BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 15 AS chTableId, OLD.clazzLogAttendanceRecordUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 15 AND chEntityPk = OLD.clazzLogAttendanceRecordUid); END "
        _stmtList +=
            "CREATE VIEW ClazzLogAttendanceRecord_ReceiveView AS  SELECT ClazzLogAttendanceRecord.*, ClazzLogAttendanceRecordReplicate.* FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLogAttendanceRecordReplicate ON ClazzLogAttendanceRecordReplicate.clarPk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ScheduleReplicate ( schedulePk INTEGER NOT NULL, scheduleVersionId INTEGER NOT NULL DEFAULT 0, scheduleDestination INTEGER NOT NULL, schedulePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (schedulePk, scheduleDestination)) "
        _stmtList +=
            " CREATE INDEX index_ScheduleReplicate_schedulePk_scheduleDestination_scheduleVersionId ON ScheduleReplicate (schedulePk, scheduleDestination, scheduleVersionId) "
        _stmtList +=
            " CREATE INDEX index_ScheduleReplicate_scheduleDestination_schedulePending ON ScheduleReplicate (scheduleDestination, schedulePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_21 AFTER INSERT ON Schedule BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 21 AS chTableId, NEW.scheduleUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 21 AND chEntityPk = NEW.scheduleUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_21 AFTER UPDATE ON Schedule BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 21 AS chTableId, NEW.scheduleUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 21 AND chEntityPk = NEW.scheduleUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_21 AFTER DELETE ON Schedule BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 21 AS chTableId, OLD.scheduleUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 21 AND chEntityPk = OLD.scheduleUid); END "
        _stmtList +=
            "CREATE VIEW Schedule_ReceiveView AS  SELECT Schedule.*, ScheduleReplicate.* FROM Schedule LEFT JOIN ScheduleReplicate ON ScheduleReplicate.schedulePk = Schedule.scheduleUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS HolidayCalendarReplicate ( hcPk INTEGER NOT NULL, hcVersionId INTEGER NOT NULL DEFAULT 0, hcDestination INTEGER NOT NULL, hcPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (hcPk, hcDestination)) "
        _stmtList +=
            " CREATE INDEX index_HolidayCalendarReplicate_hcPk_hcDestination_hcVersionId ON HolidayCalendarReplicate (hcPk, hcDestination, hcVersionId) "
        _stmtList +=
            " CREATE INDEX index_HolidayCalendarReplicate_hcDestination_hcPending ON HolidayCalendarReplicate (hcDestination, hcPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_28 AFTER INSERT ON HolidayCalendar BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 28 AS chTableId, NEW.umCalendarUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 28 AND chEntityPk = NEW.umCalendarUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_28 AFTER UPDATE ON HolidayCalendar BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 28 AS chTableId, NEW.umCalendarUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 28 AND chEntityPk = NEW.umCalendarUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_28 AFTER DELETE ON HolidayCalendar BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 28 AS chTableId, OLD.umCalendarUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 28 AND chEntityPk = OLD.umCalendarUid); END "
        _stmtList +=
            "CREATE VIEW HolidayCalendar_ReceiveView AS  SELECT HolidayCalendar.*, HolidayCalendarReplicate.* FROM HolidayCalendar LEFT JOIN HolidayCalendarReplicate ON HolidayCalendarReplicate.hcPk = HolidayCalendar.umCalendarUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS HolidayReplicate ( holidayPk INTEGER NOT NULL, holidayVersionId INTEGER NOT NULL DEFAULT 0, holidayDestination INTEGER NOT NULL, holidayPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (holidayPk, holidayDestination)) "
        _stmtList +=
            " CREATE INDEX index_HolidayReplicate_holidayPk_holidayDestination_holidayVersionId ON HolidayReplicate (holidayPk, holidayDestination, holidayVersionId) "
        _stmtList +=
            " CREATE INDEX index_HolidayReplicate_holidayDestination_holidayPending ON HolidayReplicate (holidayDestination, holidayPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_99 AFTER INSERT ON Holiday BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 99 AS chTableId, NEW.holUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 99 AND chEntityPk = NEW.holUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_99 AFTER UPDATE ON Holiday BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 99 AS chTableId, NEW.holUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 99 AND chEntityPk = NEW.holUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_99 AFTER DELETE ON Holiday BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 99 AS chTableId, OLD.holUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 99 AND chEntityPk = OLD.holUid); END "
        _stmtList +=
            "CREATE VIEW Holiday_ReceiveView AS  SELECT Holiday.*, HolidayReplicate.* FROM Holiday LEFT JOIN HolidayReplicate ON HolidayReplicate.holidayPk = Holiday.holUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonReplicate ( personPk INTEGER NOT NULL, personVersionId INTEGER NOT NULL DEFAULT 0, personDestination INTEGER NOT NULL, personPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (personPk, personDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonReplicate_personPk_personDestination_personVersionId ON PersonReplicate (personPk, personDestination, personVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonReplicate_personDestination_personPending ON PersonReplicate (personDestination, personPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_9 AFTER INSERT ON Person BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 9 AS chTableId, NEW.personUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 9 AND chEntityPk = NEW.personUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_9 AFTER UPDATE ON Person BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 9 AS chTableId, NEW.personUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 9 AND chEntityPk = NEW.personUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_9 AFTER DELETE ON Person BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 9 AS chTableId, OLD.personUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 9 AND chEntityPk = OLD.personUid); END "
        _stmtList +=
            "CREATE VIEW Person_ReceiveView AS  SELECT Person.*, PersonReplicate.* FROM Person LEFT JOIN PersonReplicate ON PersonReplicate.personPk = Person.personUid "
        _stmtList +=
            " CREATE TRIGGER person_remote_insert_ins INSTEAD OF INSERT ON Person_ReceiveView FOR EACH ROW BEGIN REPLACE INTO Person(personUid, username, firstNames, lastName, emailAddr, phoneNum, gender, active, admin, personNotes, fatherName, fatherNumber, motherName, motherNum, dateOfBirth, personAddress, personOrgId, personGroupUid, personMasterChangeSeqNum, personLocalChangeSeqNum, personLastChangedBy, personLct, personCountry, personType) VALUES (NEW.personUid, NEW.username, NEW.firstNames, NEW.lastName, NEW.emailAddr, NEW.phoneNum, NEW.gender, NEW.active, NEW.admin, NEW.personNotes, NEW.fatherName, NEW.fatherNumber, NEW.motherName, NEW.motherNum, NEW.dateOfBirth, NEW.personAddress, NEW.personOrgId, NEW.personGroupUid, NEW.personMasterChangeSeqNum, NEW.personLocalChangeSeqNum, NEW.personLastChangedBy, NEW.personLct, NEW.personCountry, NEW.personType) /*psql ON CONFLICT (personUid) DO UPDATE SET username = EXCLUDED.username, firstNames = EXCLUDED.firstNames, lastName = EXCLUDED.lastName, emailAddr = EXCLUDED.emailAddr, phoneNum = EXCLUDED.phoneNum, gender = EXCLUDED.gender, active = EXCLUDED.active, admin = EXCLUDED.admin, personNotes = EXCLUDED.personNotes, fatherName = EXCLUDED.fatherName, fatherNumber = EXCLUDED.fatherNumber, motherName = EXCLUDED.motherName, motherNum = EXCLUDED.motherNum, dateOfBirth = EXCLUDED.dateOfBirth, personAddress = EXCLUDED.personAddress, personOrgId = EXCLUDED.personOrgId, personGroupUid = EXCLUDED.personGroupUid, personMasterChangeSeqNum = EXCLUDED.personMasterChangeSeqNum, personLocalChangeSeqNum = EXCLUDED.personLocalChangeSeqNum, personLastChangedBy = EXCLUDED.personLastChangedBy, personLct = EXCLUDED.personLct, personCountry = EXCLUDED.personCountry, personType = EXCLUDED.personType */; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzReplicate ( clazzPk INTEGER NOT NULL, clazzVersionId INTEGER NOT NULL DEFAULT 0, clazzDestination INTEGER NOT NULL, clazzPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (clazzPk, clazzDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzReplicate_clazzPk_clazzDestination_clazzVersionId ON ClazzReplicate (clazzPk, clazzDestination, clazzVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzReplicate_clazzDestination_clazzPending ON ClazzReplicate (clazzDestination, clazzPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_6 AFTER INSERT ON Clazz BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 6 AS chTableId, NEW.clazzUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 6 AND chEntityPk = NEW.clazzUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_6 AFTER UPDATE ON Clazz BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 6 AS chTableId, NEW.clazzUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 6 AND chEntityPk = NEW.clazzUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_6 AFTER DELETE ON Clazz BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 6 AS chTableId, OLD.clazzUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 6 AND chEntityPk = OLD.clazzUid); END "
        _stmtList +=
            "CREATE VIEW Clazz_ReceiveView AS  SELECT Clazz.*, ClazzReplicate.* FROM Clazz LEFT JOIN ClazzReplicate ON ClazzReplicate.clazzPk = Clazz.clazzUid "
        _stmtList +=
            " CREATE TRIGGER clazz_remote_insert_ins INSTEAD OF INSERT ON Clazz_ReceiveView FOR EACH ROW BEGIN REPLACE INTO Clazz(clazzUid, clazzName, clazzDesc, attendanceAverage, clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid, isClazzActive, clazzLocationUid, clazzStartTime, clazzEndTime, clazzFeatures, clazzSchoolUid, clazzMasterChangeSeqNum, clazzLocalChangeSeqNum, clazzLastChangedBy, clazzLct, clazzTimeZone, clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid, clazzCode) VALUES (NEW.clazzUid, NEW.clazzName, NEW.clazzDesc, NEW.attendanceAverage, NEW.clazzHolidayUMCalendarUid, NEW.clazzScheuleUMCalendarUid, NEW.isClazzActive, NEW.clazzLocationUid, NEW.clazzStartTime, NEW.clazzEndTime, NEW.clazzFeatures, NEW.clazzSchoolUid, NEW.clazzMasterChangeSeqNum, NEW.clazzLocalChangeSeqNum, NEW.clazzLastChangedBy, NEW.clazzLct, NEW.clazzTimeZone, NEW.clazzStudentsPersonGroupUid, NEW.clazzTeachersPersonGroupUid, NEW.clazzPendingStudentsPersonGroupUid, NEW.clazzParentsPersonGroupUid, NEW.clazzCode) /*psql ON CONFLICT (clazzUid) DO UPDATE SET clazzName = EXCLUDED.clazzName, clazzDesc = EXCLUDED.clazzDesc, attendanceAverage = EXCLUDED.attendanceAverage, clazzHolidayUMCalendarUid = EXCLUDED.clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid = EXCLUDED.clazzScheuleUMCalendarUid, isClazzActive = EXCLUDED.isClazzActive, clazzLocationUid = EXCLUDED.clazzLocationUid, clazzStartTime = EXCLUDED.clazzStartTime, clazzEndTime = EXCLUDED.clazzEndTime, clazzFeatures = EXCLUDED.clazzFeatures, clazzSchoolUid = EXCLUDED.clazzSchoolUid, clazzMasterChangeSeqNum = EXCLUDED.clazzMasterChangeSeqNum, clazzLocalChangeSeqNum = EXCLUDED.clazzLocalChangeSeqNum, clazzLastChangedBy = EXCLUDED.clazzLastChangedBy, clazzLct = EXCLUDED.clazzLct, clazzTimeZone = EXCLUDED.clazzTimeZone, clazzStudentsPersonGroupUid = EXCLUDED.clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid = EXCLUDED.clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid = EXCLUDED.clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid = EXCLUDED.clazzParentsPersonGroupUid, clazzCode = EXCLUDED.clazzCode */; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzEnrolmentReplicate ( cePk INTEGER NOT NULL, ceVersionId INTEGER NOT NULL DEFAULT 0, ceDestination INTEGER NOT NULL, cePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (cePk, ceDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzEnrolmentReplicate_cePk_ceDestination_ceVersionId ON ClazzEnrolmentReplicate (cePk, ceDestination, ceVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzEnrolmentReplicate_ceDestination_cePending ON ClazzEnrolmentReplicate (ceDestination, cePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_65 AFTER INSERT ON ClazzEnrolment BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 65 AS chTableId, NEW.clazzEnrolmentUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 65 AND chEntityPk = NEW.clazzEnrolmentUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_65 AFTER UPDATE ON ClazzEnrolment BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 65 AS chTableId, NEW.clazzEnrolmentUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 65 AND chEntityPk = NEW.clazzEnrolmentUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_65 AFTER DELETE ON ClazzEnrolment BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 65 AS chTableId, OLD.clazzEnrolmentUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 65 AND chEntityPk = OLD.clazzEnrolmentUid); END "
        _stmtList +=
            "CREATE VIEW ClazzEnrolment_ReceiveView AS  SELECT ClazzEnrolment.*, ClazzEnrolmentReplicate.* FROM ClazzEnrolment LEFT JOIN ClazzEnrolmentReplicate ON ClazzEnrolmentReplicate.cePk = ClazzEnrolment.clazzEnrolmentUid "
        _stmtList +=
            " CREATE TRIGGER clazzenrolment_remote_insert_ins INSTEAD OF INSERT ON ClazzEnrolment_ReceiveView FOR EACH ROW BEGIN REPLACE INTO ClazzEnrolment(clazzEnrolmentUid, clazzEnrolmentPersonUid, clazzEnrolmentClazzUid, clazzEnrolmentDateJoined, clazzEnrolmentDateLeft, clazzEnrolmentRole, clazzEnrolmentAttendancePercentage, clazzEnrolmentActive, clazzEnrolmentLeavingReasonUid, clazzEnrolmentOutcome, clazzEnrolmentLocalChangeSeqNum, clazzEnrolmentMasterChangeSeqNum, clazzEnrolmentLastChangedBy, clazzEnrolmentLct) VALUES (NEW.clazzEnrolmentUid, NEW.clazzEnrolmentPersonUid, NEW.clazzEnrolmentClazzUid, NEW.clazzEnrolmentDateJoined, NEW.clazzEnrolmentDateLeft, NEW.clazzEnrolmentRole, NEW.clazzEnrolmentAttendancePercentage, NEW.clazzEnrolmentActive, NEW.clazzEnrolmentLeavingReasonUid, NEW.clazzEnrolmentOutcome, NEW.clazzEnrolmentLocalChangeSeqNum, NEW.clazzEnrolmentMasterChangeSeqNum, NEW.clazzEnrolmentLastChangedBy, NEW.clazzEnrolmentLct) /*psql ON CONFLICT (clazzEnrolmentUid) DO UPDATE SET clazzEnrolmentPersonUid = EXCLUDED.clazzEnrolmentPersonUid, clazzEnrolmentClazzUid = EXCLUDED.clazzEnrolmentClazzUid, clazzEnrolmentDateJoined = EXCLUDED.clazzEnrolmentDateJoined, clazzEnrolmentDateLeft = EXCLUDED.clazzEnrolmentDateLeft, clazzEnrolmentRole = EXCLUDED.clazzEnrolmentRole, clazzEnrolmentAttendancePercentage = EXCLUDED.clazzEnrolmentAttendancePercentage, clazzEnrolmentActive = EXCLUDED.clazzEnrolmentActive, clazzEnrolmentLeavingReasonUid = EXCLUDED.clazzEnrolmentLeavingReasonUid, clazzEnrolmentOutcome = EXCLUDED.clazzEnrolmentOutcome, clazzEnrolmentLocalChangeSeqNum = EXCLUDED.clazzEnrolmentLocalChangeSeqNum, clazzEnrolmentMasterChangeSeqNum = EXCLUDED.clazzEnrolmentMasterChangeSeqNum, clazzEnrolmentLastChangedBy = EXCLUDED.clazzEnrolmentLastChangedBy, clazzEnrolmentLct = EXCLUDED.clazzEnrolmentLct */; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LeavingReasonReplicate ( lrPk INTEGER NOT NULL, lrVersionId INTEGER NOT NULL DEFAULT 0, lrDestination INTEGER NOT NULL, lrPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (lrPk, lrDestination)) "
        _stmtList +=
            " CREATE INDEX index_LeavingReasonReplicate_lrPk_lrDestination_lrVersionId ON LeavingReasonReplicate (lrPk, lrDestination, lrVersionId) "
        _stmtList +=
            " CREATE INDEX index_LeavingReasonReplicate_lrDestination_lrPending ON LeavingReasonReplicate (lrDestination, lrPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_410 AFTER INSERT ON LeavingReason BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 410 AS chTableId, NEW.leavingReasonUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 410 AND chEntityPk = NEW.leavingReasonUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_410 AFTER UPDATE ON LeavingReason BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 410 AS chTableId, NEW.leavingReasonUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 410 AND chEntityPk = NEW.leavingReasonUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_410 AFTER DELETE ON LeavingReason BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 410 AS chTableId, OLD.leavingReasonUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 410 AND chEntityPk = OLD.leavingReasonUid); END "
        _stmtList +=
            "CREATE VIEW LeavingReason_ReceiveView AS  SELECT LeavingReason.*, LeavingReasonReplicate.* FROM LeavingReason LEFT JOIN LeavingReasonReplicate ON LeavingReasonReplicate.lrPk = LeavingReason.leavingReasonUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryReplicate ( cePk INTEGER NOT NULL, ceVersionId INTEGER NOT NULL DEFAULT 0, ceDestination INTEGER NOT NULL, cePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (cePk, ceDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryReplicate_cePk_ceDestination_ceVersionId ON ContentEntryReplicate (cePk, ceDestination, ceVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryReplicate_ceDestination_cePending ON ContentEntryReplicate (ceDestination, cePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_42 AFTER INSERT ON ContentEntry BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 42 AS chTableId, NEW.contentEntryUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 42 AND chEntityPk = NEW.contentEntryUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_42 AFTER UPDATE ON ContentEntry BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 42 AS chTableId, NEW.contentEntryUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 42 AND chEntityPk = NEW.contentEntryUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_42 AFTER DELETE ON ContentEntry BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 42 AS chTableId, OLD.contentEntryUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 42 AND chEntityPk = OLD.contentEntryUid); END "
        _stmtList +=
            "CREATE VIEW ContentEntry_ReceiveView AS  SELECT ContentEntry.*, ContentEntryReplicate.* FROM ContentEntry LEFT JOIN ContentEntryReplicate ON ContentEntryReplicate.cePk = ContentEntry.contentEntryUid "
        _stmtList +=
            " CREATE TRIGGER contententry_remote_insert_ins INSTEAD OF INSERT ON ContentEntry_ReceiveView FOR EACH ROW BEGIN REPLACE INTO ContentEntry(contentEntryUid, title, description, entryId, author, publisher, licenseType, licenseName, licenseUrl, sourceUrl, thumbnailUrl, lastModified, primaryLanguageUid, languageVariantUid, contentFlags, leaf, publik, ceInactive, completionCriteria, minScore, contentTypeFlag, contentOwner, contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum, contentEntryLastChangedBy, contentEntryLct) VALUES (NEW.contentEntryUid, NEW.title, NEW.description, NEW.entryId, NEW.author, NEW.publisher, NEW.licenseType, NEW.licenseName, NEW.licenseUrl, NEW.sourceUrl, NEW.thumbnailUrl, NEW.lastModified, NEW.primaryLanguageUid, NEW.languageVariantUid, NEW.contentFlags, NEW.leaf, NEW.publik, NEW.ceInactive, NEW.completionCriteria, NEW.minScore, NEW.contentTypeFlag, NEW.contentOwner, NEW.contentEntryLocalChangeSeqNum, NEW.contentEntryMasterChangeSeqNum, NEW.contentEntryLastChangedBy, NEW.contentEntryLct) /*psql ON CONFLICT (contentEntryUid) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description, entryId = EXCLUDED.entryId, author = EXCLUDED.author, publisher = EXCLUDED.publisher, licenseType = EXCLUDED.licenseType, licenseName = EXCLUDED.licenseName, licenseUrl = EXCLUDED.licenseUrl, sourceUrl = EXCLUDED.sourceUrl, thumbnailUrl = EXCLUDED.thumbnailUrl, lastModified = EXCLUDED.lastModified, primaryLanguageUid = EXCLUDED.primaryLanguageUid, languageVariantUid = EXCLUDED.languageVariantUid, contentFlags = EXCLUDED.contentFlags, leaf = EXCLUDED.leaf, publik = EXCLUDED.publik, ceInactive = EXCLUDED.ceInactive, completionCriteria = EXCLUDED.completionCriteria, minScore = EXCLUDED.minScore, contentTypeFlag = EXCLUDED.contentTypeFlag, contentOwner = EXCLUDED.contentOwner, contentEntryLocalChangeSeqNum = EXCLUDED.contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum = EXCLUDED.contentEntryMasterChangeSeqNum, contentEntryLastChangedBy = EXCLUDED.contentEntryLastChangedBy, contentEntryLct = EXCLUDED.contentEntryLct*/; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryContentCategoryJoinReplicate ( ceccjPk INTEGER NOT NULL, ceccjVersionId INTEGER NOT NULL DEFAULT 0, ceccjDestination INTEGER NOT NULL, ceccjPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (ceccjPk, ceccjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryContentCategoryJoinReplicate_ceccjPk_ceccjDestination_ceccjVersionId ON ContentEntryContentCategoryJoinReplicate (ceccjPk, ceccjDestination, ceccjVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryContentCategoryJoinReplicate_ceccjDestination_ceccjPending ON ContentEntryContentCategoryJoinReplicate (ceccjDestination, ceccjPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_3 AFTER INSERT ON ContentEntryContentCategoryJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 3 AS chTableId, NEW.ceccjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 3 AND chEntityPk = NEW.ceccjUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_3 AFTER UPDATE ON ContentEntryContentCategoryJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 3 AS chTableId, NEW.ceccjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 3 AND chEntityPk = NEW.ceccjUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_3 AFTER DELETE ON ContentEntryContentCategoryJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 3 AS chTableId, OLD.ceccjUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 3 AND chEntityPk = OLD.ceccjUid); END "
        _stmtList +=
            "CREATE VIEW ContentEntryContentCategoryJoin_ReceiveView AS  SELECT ContentEntryContentCategoryJoin.*, ContentEntryContentCategoryJoinReplicate.* FROM ContentEntryContentCategoryJoin LEFT JOIN ContentEntryContentCategoryJoinReplicate ON ContentEntryContentCategoryJoinReplicate.ceccjPk = ContentEntryContentCategoryJoin.ceccjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryParentChildJoinReplicate ( cepcjPk INTEGER NOT NULL, cepcjVersionId INTEGER NOT NULL DEFAULT 0, cepcjDestination INTEGER NOT NULL, cepcjPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (cepcjPk, cepcjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryParentChildJoinReplicate_cepcjPk_cepcjDestination_cepcjVersionId ON ContentEntryParentChildJoinReplicate (cepcjPk, cepcjDestination, cepcjVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryParentChildJoinReplicate_cepcjDestination_cepcjPending ON ContentEntryParentChildJoinReplicate (cepcjDestination, cepcjPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_7 AFTER INSERT ON ContentEntryParentChildJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 7 AS chTableId, NEW.cepcjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 7 AND chEntityPk = NEW.cepcjUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_7 AFTER UPDATE ON ContentEntryParentChildJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 7 AS chTableId, NEW.cepcjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 7 AND chEntityPk = NEW.cepcjUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_7 AFTER DELETE ON ContentEntryParentChildJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 7 AS chTableId, OLD.cepcjUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 7 AND chEntityPk = OLD.cepcjUid); END "
        _stmtList +=
            "CREATE VIEW ContentEntryParentChildJoin_ReceiveView AS  SELECT ContentEntryParentChildJoin.*, ContentEntryParentChildJoinReplicate.* FROM ContentEntryParentChildJoin LEFT JOIN ContentEntryParentChildJoinReplicate ON ContentEntryParentChildJoinReplicate.cepcjPk = ContentEntryParentChildJoin.cepcjUid "
        _stmtList +=
            " CREATE TRIGGER contententryparentchildjoin_remote_insert_ins INSTEAD OF INSERT ON ContentEntryParentChildJoin_ReceiveView FOR EACH ROW BEGIN REPLACE INTO ContentEntryParentChildJoin(cepcjParentContentEntryUid, cepcjChildContentEntryUid, childIndex, cepcjUid, cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum, cepcjLastChangedBy, cepcjLct) VALUES (NEW.cepcjParentContentEntryUid, NEW.cepcjChildContentEntryUid, NEW.childIndex, NEW.cepcjUid, NEW.cepcjLocalChangeSeqNum, NEW.cepcjMasterChangeSeqNum, NEW.cepcjLastChangedBy, NEW.cepcjLct) /*psql ON CONFLICT (cepcjUid) DO UPDATE SET cepcjParentContentEntryUid = EXCLUDED.cepcjParentContentEntryUid, cepcjChildContentEntryUid = EXCLUDED.cepcjChildContentEntryUid, childIndex = EXCLUDED.childIndex, cepcjLocalChangeSeqNum = EXCLUDED.cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum = EXCLUDED.cepcjMasterChangeSeqNum, cepcjLastChangedBy = EXCLUDED.cepcjLastChangedBy, cepcjLct = EXCLUDED.cepcjLct */; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryRelatedEntryJoinReplicate ( cerejPk INTEGER NOT NULL, cerejVersionId INTEGER NOT NULL DEFAULT 0, cerejDestination INTEGER NOT NULL, cerejPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (cerejPk, cerejDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryRelatedEntryJoinReplicate_cerejPk_cerejDestination_cerejVersionId ON ContentEntryRelatedEntryJoinReplicate (cerejPk, cerejDestination, cerejVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryRelatedEntryJoinReplicate_cerejDestination_cerejPending ON ContentEntryRelatedEntryJoinReplicate (cerejDestination, cerejPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_8 AFTER INSERT ON ContentEntryRelatedEntryJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 8 AS chTableId, NEW.cerejUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 8 AND chEntityPk = NEW.cerejUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_8 AFTER UPDATE ON ContentEntryRelatedEntryJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 8 AS chTableId, NEW.cerejUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 8 AND chEntityPk = NEW.cerejUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_8 AFTER DELETE ON ContentEntryRelatedEntryJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 8 AS chTableId, OLD.cerejUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 8 AND chEntityPk = OLD.cerejUid); END "
        _stmtList +=
            "CREATE VIEW ContentEntryRelatedEntryJoin_ReceiveView AS  SELECT ContentEntryRelatedEntryJoin.*, ContentEntryRelatedEntryJoinReplicate.* FROM ContentEntryRelatedEntryJoin LEFT JOIN ContentEntryRelatedEntryJoinReplicate ON ContentEntryRelatedEntryJoinReplicate.cerejPk = ContentEntryRelatedEntryJoin.cerejUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentCategorySchemaReplicate ( ccsPk INTEGER NOT NULL, ccsVersionId INTEGER NOT NULL DEFAULT 0, ccsDestination INTEGER NOT NULL, ccsPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (ccsPk, ccsDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentCategorySchemaReplicate_ccsPk_ccsDestination_ccsVersionId ON ContentCategorySchemaReplicate (ccsPk, ccsDestination, ccsVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentCategorySchemaReplicate_ccsDestination_ccsPending ON ContentCategorySchemaReplicate (ccsDestination, ccsPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_2 AFTER INSERT ON ContentCategorySchema BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 2 AS chTableId, NEW.contentCategorySchemaUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 2 AND chEntityPk = NEW.contentCategorySchemaUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_2 AFTER UPDATE ON ContentCategorySchema BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 2 AS chTableId, NEW.contentCategorySchemaUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 2 AND chEntityPk = NEW.contentCategorySchemaUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_2 AFTER DELETE ON ContentCategorySchema BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 2 AS chTableId, OLD.contentCategorySchemaUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 2 AND chEntityPk = OLD.contentCategorySchemaUid); END "
        _stmtList +=
            "CREATE VIEW ContentCategorySchema_ReceiveView AS  SELECT ContentCategorySchema.*, ContentCategorySchemaReplicate.* FROM ContentCategorySchema LEFT JOIN ContentCategorySchemaReplicate ON ContentCategorySchemaReplicate.ccsPk = ContentCategorySchema.contentCategorySchemaUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentCategoryReplicate ( ccPk INTEGER NOT NULL, ccVersionId INTEGER NOT NULL DEFAULT 0, ccDestination INTEGER NOT NULL, ccPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (ccPk, ccDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentCategoryReplicate_ccPk_ccDestination_ccVersionId ON ContentCategoryReplicate (ccPk, ccDestination, ccVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentCategoryReplicate_ccDestination_ccPending ON ContentCategoryReplicate (ccDestination, ccPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_1 AFTER INSERT ON ContentCategory BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 1 AS chTableId, NEW.contentCategoryUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 1 AND chEntityPk = NEW.contentCategoryUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_1 AFTER UPDATE ON ContentCategory BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 1 AS chTableId, NEW.contentCategoryUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 1 AND chEntityPk = NEW.contentCategoryUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_1 AFTER DELETE ON ContentCategory BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 1 AS chTableId, OLD.contentCategoryUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 1 AND chEntityPk = OLD.contentCategoryUid); END "
        _stmtList +=
            "CREATE VIEW ContentCategory_ReceiveView AS  SELECT ContentCategory.*, ContentCategoryReplicate.* FROM ContentCategory LEFT JOIN ContentCategoryReplicate ON ContentCategoryReplicate.ccPk = ContentCategory.contentCategoryUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LanguageReplicate ( languagePk INTEGER NOT NULL, languageVersionId INTEGER NOT NULL DEFAULT 0, languageDestination INTEGER NOT NULL, languagePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (languagePk, languageDestination)) "
        _stmtList +=
            " CREATE INDEX index_LanguageReplicate_languagePk_languageDestination_languageVersionId ON LanguageReplicate (languagePk, languageDestination, languageVersionId) "
        _stmtList +=
            " CREATE INDEX index_LanguageReplicate_languageDestination_languagePending ON LanguageReplicate (languageDestination, languagePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_13 AFTER INSERT ON Language BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 13 AS chTableId, NEW.langUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 13 AND chEntityPk = NEW.langUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_13 AFTER UPDATE ON Language BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 13 AS chTableId, NEW.langUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 13 AND chEntityPk = NEW.langUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_13 AFTER DELETE ON Language BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 13 AS chTableId, OLD.langUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 13 AND chEntityPk = OLD.langUid); END "
        _stmtList +=
            "CREATE VIEW Language_ReceiveView AS  SELECT Language.*, LanguageReplicate.* FROM Language LEFT JOIN LanguageReplicate ON LanguageReplicate.languagePk = Language.langUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LanguageVariantReplicate ( lvPk INTEGER NOT NULL, lvVersionId INTEGER NOT NULL DEFAULT 0, lvDestination INTEGER NOT NULL, lvPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (lvPk, lvDestination)) "
        _stmtList +=
            " CREATE INDEX index_LanguageVariantReplicate_lvPk_lvDestination_lvVersionId ON LanguageVariantReplicate (lvPk, lvDestination, lvVersionId) "
        _stmtList +=
            " CREATE INDEX index_LanguageVariantReplicate_lvDestination_lvPending ON LanguageVariantReplicate (lvDestination, lvPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_10 AFTER INSERT ON LanguageVariant BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 10 AS chTableId, NEW.langVariantUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 10 AND chEntityPk = NEW.langVariantUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_10 AFTER UPDATE ON LanguageVariant BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 10 AS chTableId, NEW.langVariantUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 10 AND chEntityPk = NEW.langVariantUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_10 AFTER DELETE ON LanguageVariant BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 10 AS chTableId, OLD.langVariantUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 10 AND chEntityPk = OLD.langVariantUid); END "
        _stmtList +=
            "CREATE VIEW LanguageVariant_ReceiveView AS  SELECT LanguageVariant.*, LanguageVariantReplicate.* FROM LanguageVariant LEFT JOIN LanguageVariantReplicate ON LanguageVariantReplicate.lvPk = LanguageVariant.langVariantUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonGroupReplicate ( pgPk INTEGER NOT NULL, pgVersionId INTEGER NOT NULL DEFAULT 0, pgDestination INTEGER NOT NULL, pgPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (pgPk, pgDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupReplicate_pgPk_pgDestination_pgVersionId ON PersonGroupReplicate (pgPk, pgDestination, pgVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupReplicate_pgDestination_pgPending ON PersonGroupReplicate (pgDestination, pgPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_43 AFTER INSERT ON PersonGroup BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 43 AS chTableId, NEW.groupUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 43 AND chEntityPk = NEW.groupUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_43 AFTER UPDATE ON PersonGroup BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 43 AS chTableId, NEW.groupUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 43 AND chEntityPk = NEW.groupUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_43 AFTER DELETE ON PersonGroup BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 43 AS chTableId, OLD.groupUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 43 AND chEntityPk = OLD.groupUid); END "
        _stmtList +=
            "CREATE VIEW PersonGroup_ReceiveView AS  SELECT PersonGroup.*, PersonGroupReplicate.* FROM PersonGroup LEFT JOIN PersonGroupReplicate ON PersonGroupReplicate.pgPk = PersonGroup.groupUid "
        _stmtList +=
            " CREATE TRIGGER persongroup_remote_insert_ins INSTEAD OF INSERT ON PersonGroup_ReceiveView FOR EACH ROW BEGIN REPLACE INTO PersonGroup(groupUid, groupMasterCsn, groupLocalCsn, groupLastChangedBy, groupLct, groupName, groupActive, personGroupFlag) VALUES (NEW.groupUid, NEW.groupMasterCsn, NEW.groupLocalCsn, NEW.groupLastChangedBy, NEW.groupLct, NEW.groupName, NEW.groupActive, NEW.personGroupFlag) /*psql ON CONFLICT (groupUid) DO UPDATE SET groupMasterCsn = EXCLUDED.groupMasterCsn, groupLocalCsn = EXCLUDED.groupLocalCsn, groupLastChangedBy = EXCLUDED.groupLastChangedBy, groupLct = EXCLUDED.groupLct, groupName = EXCLUDED.groupName, groupActive = EXCLUDED.groupActive, personGroupFlag = EXCLUDED.personGroupFlag */; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonGroupMemberReplicate ( pgmPk INTEGER NOT NULL, pgmVersionId INTEGER NOT NULL DEFAULT 0, pgmDestination INTEGER NOT NULL, pgmPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (pgmPk, pgmDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupMemberReplicate_pgmPk_pgmDestination_pgmVersionId ON PersonGroupMemberReplicate (pgmPk, pgmDestination, pgmVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupMemberReplicate_pgmDestination_pgmPending ON PersonGroupMemberReplicate (pgmDestination, pgmPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_44 AFTER INSERT ON PersonGroupMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 44 AS chTableId, NEW.groupMemberUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 44 AND chEntityPk = NEW.groupMemberUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_44 AFTER UPDATE ON PersonGroupMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 44 AS chTableId, NEW.groupMemberUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 44 AND chEntityPk = NEW.groupMemberUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_44 AFTER DELETE ON PersonGroupMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 44 AS chTableId, OLD.groupMemberUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 44 AND chEntityPk = OLD.groupMemberUid); END "
        _stmtList +=
            "CREATE VIEW PersonGroupMember_ReceiveView AS  SELECT PersonGroupMember.*, PersonGroupMemberReplicate.* FROM PersonGroupMember LEFT JOIN PersonGroupMemberReplicate ON PersonGroupMemberReplicate.pgmPk = PersonGroupMember.groupMemberUid "
        _stmtList +=
            " CREATE TRIGGER persongroupmember_remote_insert_ins INSTEAD OF INSERT ON PersonGroupMember_ReceiveView FOR EACH ROW BEGIN REPLACE INTO PersonGroupMember(groupMemberUid, groupMemberActive, groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, groupMemberLastChangedBy, groupMemberLct) VALUES (NEW.groupMemberUid, NEW.groupMemberActive, NEW.groupMemberPersonUid, NEW.groupMemberGroupUid, NEW.groupMemberMasterCsn, NEW.groupMemberLocalCsn, NEW.groupMemberLastChangedBy, NEW.groupMemberLct) /*psql ON CONFLICT (groupMemberUid) DO UPDATE SET groupMemberActive = EXCLUDED.groupMemberActive, groupMemberPersonUid = EXCLUDED.groupMemberPersonUid, groupMemberGroupUid = EXCLUDED.groupMemberGroupUid, groupMemberMasterCsn = EXCLUDED.groupMemberMasterCsn, groupMemberLocalCsn = EXCLUDED.groupMemberLocalCsn, groupMemberLastChangedBy = EXCLUDED.groupMemberLastChangedBy, groupMemberLct = EXCLUDED.groupMemberLct */; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonPictureReplicate ( ppPk INTEGER NOT NULL, ppVersionId INTEGER NOT NULL DEFAULT 0, ppDestination INTEGER NOT NULL, ppPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (ppPk, ppDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonPictureReplicate_ppPk_ppDestination_ppVersionId ON PersonPictureReplicate (ppPk, ppDestination, ppVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonPictureReplicate_ppDestination_ppPending ON PersonPictureReplicate (ppDestination, ppPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_50 AFTER INSERT ON PersonPicture BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 50 AS chTableId, NEW.personPictureUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 50 AND chEntityPk = NEW.personPictureUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_50 AFTER UPDATE ON PersonPicture BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 50 AS chTableId, NEW.personPictureUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 50 AND chEntityPk = NEW.personPictureUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_50 AFTER DELETE ON PersonPicture BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 50 AS chTableId, OLD.personPictureUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 50 AND chEntityPk = OLD.personPictureUid); END "
        _stmtList +=
            "CREATE VIEW PersonPicture_ReceiveView AS  SELECT PersonPicture.*, PersonPictureReplicate.* FROM PersonPicture LEFT JOIN PersonPictureReplicate ON PersonPictureReplicate.ppPk = PersonPicture.personPictureUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContainerReplicate ( containerPk INTEGER NOT NULL, containerVersionId INTEGER NOT NULL DEFAULT 0, containerDestination INTEGER NOT NULL, containerPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (containerPk, containerDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContainerReplicate_containerPk_containerDestination_containerVersionId ON ContainerReplicate (containerPk, containerDestination, containerVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContainerReplicate_containerDestination_containerPending ON ContainerReplicate (containerDestination, containerPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_51 AFTER INSERT ON Container BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 51 AS chTableId, NEW.containerUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 51 AND chEntityPk = NEW.containerUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_51 AFTER UPDATE ON Container BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 51 AS chTableId, NEW.containerUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 51 AND chEntityPk = NEW.containerUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_51 AFTER DELETE ON Container BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 51 AS chTableId, OLD.containerUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 51 AND chEntityPk = OLD.containerUid); END "
        _stmtList +=
            "CREATE VIEW Container_ReceiveView AS  SELECT Container.*, ContainerReplicate.* FROM Container LEFT JOIN ContainerReplicate ON ContainerReplicate.containerPk = Container.containerUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS VerbEntityReplicate ( vePk INTEGER NOT NULL, veVersionId INTEGER NOT NULL DEFAULT 0, veDestination INTEGER NOT NULL, vePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (vePk, veDestination)) "
        _stmtList +=
            " CREATE INDEX index_VerbEntityReplicate_vePk_veDestination_veVersionId ON VerbEntityReplicate (vePk, veDestination, veVersionId) "
        _stmtList +=
            " CREATE INDEX index_VerbEntityReplicate_veDestination_vePending ON VerbEntityReplicate (veDestination, vePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_62 AFTER INSERT ON VerbEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 62 AS chTableId, NEW.verbUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 62 AND chEntityPk = NEW.verbUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_62 AFTER UPDATE ON VerbEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 62 AS chTableId, NEW.verbUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 62 AND chEntityPk = NEW.verbUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_62 AFTER DELETE ON VerbEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 62 AS chTableId, OLD.verbUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 62 AND chEntityPk = OLD.verbUid); END "
        _stmtList +=
            "CREATE VIEW VerbEntity_ReceiveView AS  SELECT VerbEntity.*, VerbEntityReplicate.* FROM VerbEntity LEFT JOIN VerbEntityReplicate ON VerbEntityReplicate.vePk = VerbEntity.verbUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS XObjectEntityReplicate ( xoePk INTEGER NOT NULL, xoeVersionId INTEGER NOT NULL DEFAULT 0, xoeDestination INTEGER NOT NULL, xoePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (xoePk, xoeDestination)) "
        _stmtList +=
            " CREATE INDEX index_XObjectEntityReplicate_xoePk_xoeDestination_xoeVersionId ON XObjectEntityReplicate (xoePk, xoeDestination, xoeVersionId) "
        _stmtList +=
            " CREATE INDEX index_XObjectEntityReplicate_xoeDestination_xoePending ON XObjectEntityReplicate (xoeDestination, xoePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_64 AFTER INSERT ON XObjectEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 64 AS chTableId, NEW.xObjectUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 64 AND chEntityPk = NEW.xObjectUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_64 AFTER UPDATE ON XObjectEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 64 AS chTableId, NEW.xObjectUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 64 AND chEntityPk = NEW.xObjectUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_64 AFTER DELETE ON XObjectEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 64 AS chTableId, OLD.xObjectUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 64 AND chEntityPk = OLD.xObjectUid); END "
        _stmtList +=
            "CREATE VIEW XObjectEntity_ReceiveView AS  SELECT XObjectEntity.*, XObjectEntityReplicate.* FROM XObjectEntity LEFT JOIN XObjectEntityReplicate ON XObjectEntityReplicate.xoePk = XObjectEntity.xObjectUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StatementEntityReplicate ( sePk INTEGER NOT NULL, seVersionId INTEGER NOT NULL DEFAULT 0, seDestination INTEGER NOT NULL, sePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (sePk, seDestination)) "
        _stmtList +=
            " CREATE INDEX index_StatementEntityReplicate_sePk_seDestination_seVersionId ON StatementEntityReplicate (sePk, seDestination, seVersionId) "
        _stmtList +=
            " CREATE INDEX index_StatementEntityReplicate_seDestination_sePending ON StatementEntityReplicate (seDestination, sePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_60 AFTER INSERT ON StatementEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 60 AS chTableId, NEW.statementUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 60 AND chEntityPk = NEW.statementUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_60 AFTER UPDATE ON StatementEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 60 AS chTableId, NEW.statementUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 60 AND chEntityPk = NEW.statementUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_60 AFTER DELETE ON StatementEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 60 AS chTableId, OLD.statementUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 60 AND chEntityPk = OLD.statementUid); END "
        _stmtList +=
            "CREATE VIEW StatementEntity_ReceiveView AS  SELECT StatementEntity.*, StatementEntityReplicate.* FROM StatementEntity LEFT JOIN StatementEntityReplicate ON StatementEntityReplicate.sePk = StatementEntity.statementUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContextXObjectStatementJoinReplicate ( cxosjPk INTEGER NOT NULL, cxosjVersionId INTEGER NOT NULL DEFAULT 0, cxosjDestination INTEGER NOT NULL, cxosjPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (cxosjPk, cxosjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContextXObjectStatementJoinReplicate_cxosjPk_cxosjDestination_cxosjVersionId ON ContextXObjectStatementJoinReplicate (cxosjPk, cxosjDestination, cxosjVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContextXObjectStatementJoinReplicate_cxosjDestination_cxosjPending ON ContextXObjectStatementJoinReplicate (cxosjDestination, cxosjPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_66 AFTER INSERT ON ContextXObjectStatementJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 66 AS chTableId, NEW.contextXObjectStatementJoinUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 66 AND chEntityPk = NEW.contextXObjectStatementJoinUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_66 AFTER UPDATE ON ContextXObjectStatementJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 66 AS chTableId, NEW.contextXObjectStatementJoinUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 66 AND chEntityPk = NEW.contextXObjectStatementJoinUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_66 AFTER DELETE ON ContextXObjectStatementJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 66 AS chTableId, OLD.contextXObjectStatementJoinUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 66 AND chEntityPk = OLD.contextXObjectStatementJoinUid); END "
        _stmtList +=
            "CREATE VIEW ContextXObjectStatementJoin_ReceiveView AS  SELECT ContextXObjectStatementJoin.*, ContextXObjectStatementJoinReplicate.* FROM ContextXObjectStatementJoin LEFT JOIN ContextXObjectStatementJoinReplicate ON ContextXObjectStatementJoinReplicate.cxosjPk = ContextXObjectStatementJoin.contextXObjectStatementJoinUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS AgentEntityReplicate ( aePk INTEGER NOT NULL, aeVersionId INTEGER NOT NULL DEFAULT 0, aeDestination INTEGER NOT NULL, aePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (aePk, aeDestination)) "
        _stmtList +=
            " CREATE INDEX index_AgentEntityReplicate_aePk_aeDestination_aeVersionId ON AgentEntityReplicate (aePk, aeDestination, aeVersionId) "
        _stmtList +=
            " CREATE INDEX index_AgentEntityReplicate_aeDestination_aePending ON AgentEntityReplicate (aeDestination, aePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_68 AFTER INSERT ON AgentEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 68 AS chTableId, NEW.agentUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 68 AND chEntityPk = NEW.agentUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_68 AFTER UPDATE ON AgentEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 68 AS chTableId, NEW.agentUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 68 AND chEntityPk = NEW.agentUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_68 AFTER DELETE ON AgentEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 68 AS chTableId, OLD.agentUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 68 AND chEntityPk = OLD.agentUid); END "
        _stmtList +=
            "CREATE VIEW AgentEntity_ReceiveView AS  SELECT AgentEntity.*, AgentEntityReplicate.* FROM AgentEntity LEFT JOIN AgentEntityReplicate ON AgentEntityReplicate.aePk = AgentEntity.agentUid "
        _stmtList +=
            " CREATE TRIGGER agententity_remote_insert_ins INSTEAD OF INSERT ON AgentEntity_ReceiveView FOR EACH ROW BEGIN REPLACE INTO AgentEntity(agentUid, agentMbox, agentMbox_sha1sum, agentOpenid, agentAccountName, agentHomePage, agentPersonUid, statementMasterChangeSeqNum, statementLocalChangeSeqNum, statementLastChangedBy, agentLct) VALUES (NEW.agentUid, NEW.agentMbox, NEW.agentMbox_sha1sum, NEW.agentOpenid, NEW.agentAccountName, NEW.agentHomePage, NEW.agentPersonUid, NEW.statementMasterChangeSeqNum, NEW.statementLocalChangeSeqNum, NEW.statementLastChangedBy, NEW.agentLct) /*psql ON CONFLICT (agentUid) DO UPDATE SET agentMbox = EXCLUDED.agentMbox, agentMbox_sha1sum = EXCLUDED.agentMbox_sha1sum, agentOpenid = EXCLUDED.agentOpenid, agentAccountName = EXCLUDED.agentAccountName, agentHomePage = EXCLUDED.agentHomePage, agentPersonUid = EXCLUDED.agentPersonUid, statementMasterChangeSeqNum = EXCLUDED.statementMasterChangeSeqNum, statementLocalChangeSeqNum = EXCLUDED.statementLocalChangeSeqNum, statementLastChangedBy = EXCLUDED.statementLastChangedBy, agentLct = EXCLUDED.agentLct*/; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StateEntityReplicate ( sePk INTEGER NOT NULL, seVersionId INTEGER NOT NULL DEFAULT 0, seDestination INTEGER NOT NULL, sePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (sePk, seDestination)) "
        _stmtList +=
            " CREATE INDEX index_StateEntityReplicate_sePk_seDestination_seVersionId ON StateEntityReplicate (sePk, seDestination, seVersionId) "
        _stmtList +=
            " CREATE INDEX index_StateEntityReplicate_seDestination_sePending ON StateEntityReplicate (seDestination, sePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_70 AFTER INSERT ON StateEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 70 AS chTableId, NEW.stateUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 70 AND chEntityPk = NEW.stateUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_70 AFTER UPDATE ON StateEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 70 AS chTableId, NEW.stateUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 70 AND chEntityPk = NEW.stateUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_70 AFTER DELETE ON StateEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 70 AS chTableId, OLD.stateUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 70 AND chEntityPk = OLD.stateUid); END "
        _stmtList +=
            "CREATE VIEW StateEntity_ReceiveView AS  SELECT StateEntity.*, StateEntityReplicate.* FROM StateEntity LEFT JOIN StateEntityReplicate ON StateEntityReplicate.sePk = StateEntity.stateUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StateContentEntityReplicate ( scePk INTEGER NOT NULL, sceVersionId INTEGER NOT NULL DEFAULT 0, sceDestination INTEGER NOT NULL, scePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (scePk, sceDestination)) "
        _stmtList +=
            " CREATE INDEX index_StateContentEntityReplicate_scePk_sceDestination_sceVersionId ON StateContentEntityReplicate (scePk, sceDestination, sceVersionId) "
        _stmtList +=
            " CREATE INDEX index_StateContentEntityReplicate_sceDestination_scePending ON StateContentEntityReplicate (sceDestination, scePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_72 AFTER INSERT ON StateContentEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 72 AS chTableId, NEW.stateContentUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 72 AND chEntityPk = NEW.stateContentUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_72 AFTER UPDATE ON StateContentEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 72 AS chTableId, NEW.stateContentUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 72 AND chEntityPk = NEW.stateContentUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_72 AFTER DELETE ON StateContentEntity BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 72 AS chTableId, OLD.stateContentUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 72 AND chEntityPk = OLD.stateContentUid); END "
        _stmtList +=
            "CREATE VIEW StateContentEntity_ReceiveView AS  SELECT StateContentEntity.*, StateContentEntityReplicate.* FROM StateContentEntity LEFT JOIN StateContentEntityReplicate ON StateContentEntityReplicate.scePk = StateContentEntity.stateContentUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS XLangMapEntryReplicate ( xlmePk INTEGER NOT NULL, xlmeVersionId INTEGER NOT NULL DEFAULT 0, xlmeDestination INTEGER NOT NULL, xlmePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (xlmePk, xlmeDestination)) "
        _stmtList +=
            " CREATE INDEX index_XLangMapEntryReplicate_xlmePk_xlmeDestination_xlmeVersionId ON XLangMapEntryReplicate (xlmePk, xlmeDestination, xlmeVersionId) "
        _stmtList +=
            " CREATE INDEX index_XLangMapEntryReplicate_xlmeDestination_xlmePending ON XLangMapEntryReplicate (xlmeDestination, xlmePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_74 AFTER INSERT ON XLangMapEntry BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 74 AS chTableId, NEW.statementLangMapUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 74 AND chEntityPk = NEW.statementLangMapUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_74 AFTER UPDATE ON XLangMapEntry BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 74 AS chTableId, NEW.statementLangMapUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 74 AND chEntityPk = NEW.statementLangMapUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_74 AFTER DELETE ON XLangMapEntry BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 74 AS chTableId, OLD.statementLangMapUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 74 AND chEntityPk = OLD.statementLangMapUid); END "
        _stmtList +=
            "CREATE VIEW XLangMapEntry_ReceiveView AS  SELECT XLangMapEntry.*, XLangMapEntryReplicate.* FROM XLangMapEntry LEFT JOIN XLangMapEntryReplicate ON XLangMapEntryReplicate.xlmePk = XLangMapEntry.statementLangMapUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SchoolReplicate ( schoolPk INTEGER NOT NULL, schoolVersionId INTEGER NOT NULL DEFAULT 0, schoolDestination INTEGER NOT NULL, schoolPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (schoolPk, schoolDestination)) "
        _stmtList +=
            " CREATE INDEX index_SchoolReplicate_schoolPk_schoolDestination_schoolVersionId ON SchoolReplicate (schoolPk, schoolDestination, schoolVersionId) "
        _stmtList +=
            " CREATE INDEX index_SchoolReplicate_schoolDestination_schoolPending ON SchoolReplicate (schoolDestination, schoolPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_164 AFTER INSERT ON School BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 164 AS chTableId, NEW.schoolUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 164 AND chEntityPk = NEW.schoolUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_164 AFTER UPDATE ON School BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 164 AS chTableId, NEW.schoolUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 164 AND chEntityPk = NEW.schoolUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_164 AFTER DELETE ON School BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 164 AS chTableId, OLD.schoolUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 164 AND chEntityPk = OLD.schoolUid); END "
        _stmtList +=
            "CREATE VIEW School_ReceiveView AS  SELECT School.*, SchoolReplicate.* FROM School LEFT JOIN SchoolReplicate ON SchoolReplicate.schoolPk = School.schoolUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SchoolMemberReplicate ( smPk INTEGER NOT NULL, smVersionId INTEGER NOT NULL DEFAULT 0, smDestination INTEGER NOT NULL, smPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (smPk, smDestination)) "
        _stmtList +=
            " CREATE INDEX index_SchoolMemberReplicate_smPk_smDestination_smVersionId ON SchoolMemberReplicate (smPk, smDestination, smVersionId) "
        _stmtList +=
            " CREATE INDEX index_SchoolMemberReplicate_smDestination_smPending ON SchoolMemberReplicate (smDestination, smPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_200 AFTER INSERT ON SchoolMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 200 AS chTableId, NEW.schoolMemberUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 200 AND chEntityPk = NEW.schoolMemberUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_200 AFTER UPDATE ON SchoolMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 200 AS chTableId, NEW.schoolMemberUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 200 AND chEntityPk = NEW.schoolMemberUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_200 AFTER DELETE ON SchoolMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 200 AS chTableId, OLD.schoolMemberUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 200 AND chEntityPk = OLD.schoolMemberUid); END "
        _stmtList +=
            "CREATE VIEW SchoolMember_ReceiveView AS  SELECT SchoolMember.*, SchoolMemberReplicate.* FROM SchoolMember LEFT JOIN SchoolMemberReplicate ON SchoolMemberReplicate.smPk = SchoolMember.schoolMemberUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS CommentsReplicate ( commentsPk INTEGER NOT NULL, commentsVersionId INTEGER NOT NULL DEFAULT 0, commentsDestination INTEGER NOT NULL, commentsPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (commentsPk, commentsDestination)) "
        _stmtList +=
            " CREATE INDEX index_CommentsReplicate_commentsPk_commentsDestination_commentsVersionId ON CommentsReplicate (commentsPk, commentsDestination, commentsVersionId) "
        _stmtList +=
            " CREATE INDEX index_CommentsReplicate_commentsDestination_commentsPending ON CommentsReplicate (commentsDestination, commentsPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_208 AFTER INSERT ON Comments BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 208 AS chTableId, NEW.commentsUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 208 AND chEntityPk = NEW.commentsUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_208 AFTER UPDATE ON Comments BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 208 AS chTableId, NEW.commentsUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 208 AND chEntityPk = NEW.commentsUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_208 AFTER DELETE ON Comments BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 208 AS chTableId, OLD.commentsUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 208 AND chEntityPk = OLD.commentsUid); END "
        _stmtList +=
            "CREATE VIEW Comments_ReceiveView AS  SELECT Comments.*, CommentsReplicate.* FROM Comments LEFT JOIN CommentsReplicate ON CommentsReplicate.commentsPk = Comments.commentsUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ReportReplicate ( reportPk INTEGER NOT NULL, reportVersionId INTEGER NOT NULL DEFAULT 0, reportDestination INTEGER NOT NULL, reportPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (reportPk, reportDestination)) "
        _stmtList +=
            " CREATE INDEX index_ReportReplicate_reportPk_reportDestination_reportVersionId ON ReportReplicate (reportPk, reportDestination, reportVersionId) "
        _stmtList +=
            " CREATE INDEX index_ReportReplicate_reportDestination_reportPending ON ReportReplicate (reportDestination, reportPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_101 AFTER INSERT ON Report BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 101 AS chTableId, NEW.reportUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 101 AND chEntityPk = NEW.reportUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_101 AFTER UPDATE ON Report BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 101 AS chTableId, NEW.reportUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 101 AND chEntityPk = NEW.reportUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_101 AFTER DELETE ON Report BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 101 AS chTableId, OLD.reportUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 101 AND chEntityPk = OLD.reportUid); END "
        _stmtList +=
            "CREATE VIEW Report_ReceiveView AS  SELECT Report.*, ReportReplicate.* FROM Report LEFT JOIN ReportReplicate ON ReportReplicate.reportPk = Report.reportUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SiteReplicate ( sitePk INTEGER NOT NULL, siteVersionId INTEGER NOT NULL DEFAULT 0, siteDestination INTEGER NOT NULL, sitePending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (sitePk, siteDestination)) "
        _stmtList +=
            " CREATE INDEX index_SiteReplicate_sitePk_siteDestination_siteVersionId ON SiteReplicate (sitePk, siteDestination, siteVersionId) "
        _stmtList +=
            " CREATE INDEX index_SiteReplicate_siteDestination_sitePending ON SiteReplicate (siteDestination, sitePending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_189 AFTER INSERT ON Site BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 189 AS chTableId, NEW.siteUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 189 AND chEntityPk = NEW.siteUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_189 AFTER UPDATE ON Site BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 189 AS chTableId, NEW.siteUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 189 AND chEntityPk = NEW.siteUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_189 AFTER DELETE ON Site BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 189 AS chTableId, OLD.siteUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 189 AND chEntityPk = OLD.siteUid); END "
        _stmtList +=
            "CREATE VIEW Site_ReceiveView AS  SELECT Site.*, SiteReplicate.* FROM Site LEFT JOIN SiteReplicate ON SiteReplicate.sitePk = Site.siteUid "
        _stmtList +=
            " CREATE TRIGGER site_remote_insert_ins INSTEAD OF INSERT ON Site_ReceiveView FOR EACH ROW BEGIN REPLACE INTO Site(siteUid, sitePcsn, siteLcsn, siteLcb, siteLct, siteName, guestLogin, registrationAllowed, authSalt) VALUES (NEW.siteUid, NEW.sitePcsn, NEW.siteLcsn, NEW.siteLcb, NEW.siteLct, NEW.siteName, NEW.guestLogin, NEW.registrationAllowed, NEW.authSalt) /*psql ON CONFLICT (siteUid) DO UPDATE SET sitePcsn = EXCLUDED.sitePcsn, siteLcsn = EXCLUDED.siteLcsn, siteLcb = EXCLUDED.siteLcb, siteLct = EXCLUDED.siteLct, siteName = EXCLUDED.siteName, guestLogin = EXCLUDED.guestLogin, registrationAllowed = EXCLUDED.registrationAllowed, authSalt = EXCLUDED.authSalt*/; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LearnerGroupReplicate ( lgPk INTEGER NOT NULL, lgVersionId INTEGER NOT NULL DEFAULT 0, lgDestination INTEGER NOT NULL, lgPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (lgPk, lgDestination)) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupReplicate_lgPk_lgDestination_lgVersionId ON LearnerGroupReplicate (lgPk, lgDestination, lgVersionId) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupReplicate_lgDestination_lgPending ON LearnerGroupReplicate (lgDestination, lgPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_301 AFTER INSERT ON LearnerGroup BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 301 AS chTableId, NEW.learnerGroupUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 301 AND chEntityPk = NEW.learnerGroupUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_301 AFTER UPDATE ON LearnerGroup BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 301 AS chTableId, NEW.learnerGroupUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 301 AND chEntityPk = NEW.learnerGroupUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_301 AFTER DELETE ON LearnerGroup BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 301 AS chTableId, OLD.learnerGroupUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 301 AND chEntityPk = OLD.learnerGroupUid); END "
        _stmtList +=
            "CREATE VIEW LearnerGroup_ReceiveView AS  SELECT LearnerGroup.*, LearnerGroupReplicate.* FROM LearnerGroup LEFT JOIN LearnerGroupReplicate ON LearnerGroupReplicate.lgPk = LearnerGroup.learnerGroupUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LearnerGroupMemberReplicate ( lgmPk INTEGER NOT NULL, lgmVersionId INTEGER NOT NULL DEFAULT 0, lgmDestination INTEGER NOT NULL, lgmPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (lgmPk, lgmDestination)) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupMemberReplicate_lgmPk_lgmDestination_lgmVersionId ON LearnerGroupMemberReplicate (lgmPk, lgmDestination, lgmVersionId) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupMemberReplicate_lgmDestination_lgmPending ON LearnerGroupMemberReplicate (lgmDestination, lgmPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_300 AFTER INSERT ON LearnerGroupMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 300 AS chTableId, NEW.learnerGroupMemberUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 300 AND chEntityPk = NEW.learnerGroupMemberUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_300 AFTER UPDATE ON LearnerGroupMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 300 AS chTableId, NEW.learnerGroupMemberUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 300 AND chEntityPk = NEW.learnerGroupMemberUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_300 AFTER DELETE ON LearnerGroupMember BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 300 AS chTableId, OLD.learnerGroupMemberUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 300 AND chEntityPk = OLD.learnerGroupMemberUid); END "
        _stmtList +=
            "CREATE VIEW LearnerGroupMember_ReceiveView AS  SELECT LearnerGroupMember.*, LearnerGroupMemberReplicate.* FROM LearnerGroupMember LEFT JOIN LearnerGroupMemberReplicate ON LearnerGroupMemberReplicate.lgmPk = LearnerGroupMember.learnerGroupMemberUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS GroupLearningSessionReplicate ( glsPk INTEGER NOT NULL, glsVersionId INTEGER NOT NULL DEFAULT 0, glsDestination INTEGER NOT NULL, glsPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (glsPk, glsDestination)) "
        _stmtList +=
            " CREATE INDEX index_GroupLearningSessionReplicate_glsPk_glsDestination_glsVersionId ON GroupLearningSessionReplicate (glsPk, glsDestination, glsVersionId) "
        _stmtList +=
            " CREATE INDEX index_GroupLearningSessionReplicate_glsDestination_glsPending ON GroupLearningSessionReplicate (glsDestination, glsPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_302 AFTER INSERT ON GroupLearningSession BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 302 AS chTableId, NEW.groupLearningSessionUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 302 AND chEntityPk = NEW.groupLearningSessionUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_302 AFTER UPDATE ON GroupLearningSession BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 302 AS chTableId, NEW.groupLearningSessionUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 302 AND chEntityPk = NEW.groupLearningSessionUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_302 AFTER DELETE ON GroupLearningSession BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 302 AS chTableId, OLD.groupLearningSessionUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 302 AND chEntityPk = OLD.groupLearningSessionUid); END "
        _stmtList +=
            "CREATE VIEW GroupLearningSession_ReceiveView AS  SELECT GroupLearningSession.*, GroupLearningSessionReplicate.* FROM GroupLearningSession LEFT JOIN GroupLearningSessionReplicate ON GroupLearningSessionReplicate.glsPk = GroupLearningSession.groupLearningSessionUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SiteTermsReplicate ( stPk INTEGER NOT NULL, stVersionId INTEGER NOT NULL DEFAULT 0, stDestination INTEGER NOT NULL, stPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (stPk, stDestination)) "
        _stmtList +=
            " CREATE INDEX index_SiteTermsReplicate_stPk_stDestination_stVersionId ON SiteTermsReplicate (stPk, stDestination, stVersionId) "
        _stmtList +=
            " CREATE INDEX index_SiteTermsReplicate_stDestination_stPending ON SiteTermsReplicate (stDestination, stPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_272 AFTER INSERT ON SiteTerms BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 272 AS chTableId, NEW.sTermsUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 272 AND chEntityPk = NEW.sTermsUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_272 AFTER UPDATE ON SiteTerms BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 272 AS chTableId, NEW.sTermsUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 272 AND chEntityPk = NEW.sTermsUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_272 AFTER DELETE ON SiteTerms BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 272 AS chTableId, OLD.sTermsUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 272 AND chEntityPk = OLD.sTermsUid); END "
        _stmtList +=
            "CREATE VIEW SiteTerms_ReceiveView AS  SELECT SiteTerms.*, SiteTermsReplicate.* FROM SiteTerms LEFT JOIN SiteTermsReplicate ON SiteTermsReplicate.stPk = SiteTerms.sTermsUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzContentJoinReplicate ( ccjPk INTEGER NOT NULL, ccjVersionId INTEGER NOT NULL DEFAULT 0, ccjDestination INTEGER NOT NULL, ccjPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (ccjPk, ccjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzContentJoinReplicate_ccjPk_ccjDestination_ccjVersionId ON ClazzContentJoinReplicate (ccjPk, ccjDestination, ccjVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzContentJoinReplicate_ccjDestination_ccjPending ON ClazzContentJoinReplicate (ccjDestination, ccjPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_134 AFTER INSERT ON ClazzContentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 134 AS chTableId, NEW.ccjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 134 AND chEntityPk = NEW.ccjUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_134 AFTER UPDATE ON ClazzContentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 134 AS chTableId, NEW.ccjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 134 AND chEntityPk = NEW.ccjUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_134 AFTER DELETE ON ClazzContentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 134 AS chTableId, OLD.ccjUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 134 AND chEntityPk = OLD.ccjUid); END "
        _stmtList +=
            "CREATE VIEW ClazzContentJoin_ReceiveView AS  SELECT ClazzContentJoin.*, ClazzContentJoinReplicate.* FROM ClazzContentJoin LEFT JOIN ClazzContentJoinReplicate ON ClazzContentJoinReplicate.ccjPk = ClazzContentJoin.ccjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonParentJoinReplicate ( ppjPk INTEGER NOT NULL, ppjVersionId INTEGER NOT NULL DEFAULT 0, ppjDestination INTEGER NOT NULL, ppjPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (ppjPk, ppjDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonParentJoinReplicate_ppjPk_ppjDestination_ppjVersionId ON PersonParentJoinReplicate (ppjPk, ppjDestination, ppjVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonParentJoinReplicate_ppjDestination_ppjPending ON PersonParentJoinReplicate (ppjDestination, ppjPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_512 AFTER INSERT ON PersonParentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 512 AS chTableId, NEW.ppjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 512 AND chEntityPk = NEW.ppjUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_512 AFTER UPDATE ON PersonParentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 512 AS chTableId, NEW.ppjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 512 AND chEntityPk = NEW.ppjUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_512 AFTER DELETE ON PersonParentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 512 AS chTableId, OLD.ppjUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 512 AND chEntityPk = OLD.ppjUid); END "
        _stmtList +=
            "CREATE VIEW PersonParentJoin_ReceiveView AS  SELECT PersonParentJoin.*, PersonParentJoinReplicate.* FROM PersonParentJoin LEFT JOIN PersonParentJoinReplicate ON PersonParentJoinReplicate.ppjPk = PersonParentJoin.ppjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ScopedGrantReplicate ( sgPk INTEGER NOT NULL, sgVersionId INTEGER NOT NULL DEFAULT 0, sgDestination INTEGER NOT NULL, sgPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (sgPk, sgDestination)) "
        _stmtList +=
            " CREATE INDEX index_ScopedGrantReplicate_sgPk_sgDestination_sgVersionId ON ScopedGrantReplicate (sgPk, sgDestination, sgVersionId) "
        _stmtList +=
            " CREATE INDEX index_ScopedGrantReplicate_sgDestination_sgPending ON ScopedGrantReplicate (sgDestination, sgPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_48 AFTER INSERT ON ScopedGrant BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 48 AS chTableId, NEW.sgUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 48 AND chEntityPk = NEW.sgUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_48 AFTER UPDATE ON ScopedGrant BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 48 AS chTableId, NEW.sgUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 48 AND chEntityPk = NEW.sgUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_48 AFTER DELETE ON ScopedGrant BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 48 AS chTableId, OLD.sgUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 48 AND chEntityPk = OLD.sgUid); END "
        _stmtList +=
            "CREATE VIEW ScopedGrant_ReceiveView AS  SELECT ScopedGrant.*, ScopedGrantReplicate.* FROM ScopedGrant LEFT JOIN ScopedGrantReplicate ON ScopedGrantReplicate.sgPk = ScopedGrant.sgUid "
        _stmtList +=
            " CREATE TRIGGER sg_remote_insert_ins INSTEAD OF INSERT ON ScopedGrant_ReceiveView FOR EACH ROW BEGIN REPLACE INTO ScopedGrant(sgUid, sgPcsn, sgLcsn, sgLcb, sgLct, sgTableId, sgEntityUid, sgPermissions, sgGroupUid, sgIndex, sgFlags) VALUES (NEW.sgUid, NEW.sgPcsn, NEW.sgLcsn, NEW.sgLcb, NEW.sgLct, NEW.sgTableId, NEW.sgEntityUid, NEW.sgPermissions, NEW.sgGroupUid, NEW.sgIndex, NEW.sgFlags) /*psql ON CONFLICT(sgUid) DO UPDATE SET sgLct = EXCLUDED.sgLct, sgPermissions = EXCLUDED.sgPermissions */ ; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ErrorReportReplicate ( erPk INTEGER NOT NULL, erVersionId INTEGER NOT NULL DEFAULT 0, erDestination INTEGER NOT NULL, erPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (erPk, erDestination)) "
        _stmtList +=
            " CREATE INDEX index_ErrorReportReplicate_erPk_erDestination_erVersionId ON ErrorReportReplicate (erPk, erDestination, erVersionId) "
        _stmtList +=
            " CREATE INDEX index_ErrorReportReplicate_erDestination_erPending ON ErrorReportReplicate (erDestination, erPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_419 AFTER INSERT ON ErrorReport BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 419 AS chTableId, NEW.errUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 419 AND chEntityPk = NEW.errUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_419 AFTER UPDATE ON ErrorReport BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 419 AS chTableId, NEW.errUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 419 AND chEntityPk = NEW.errUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_419 AFTER DELETE ON ErrorReport BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 419 AS chTableId, OLD.errUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 419 AND chEntityPk = OLD.errUid); END "
        _stmtList +=
            "CREATE VIEW ErrorReport_ReceiveView AS  SELECT ErrorReport.*, ErrorReportReplicate.* FROM ErrorReport LEFT JOIN ErrorReportReplicate ON ErrorReportReplicate.erPk = ErrorReport.errUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzAssignmentReplicate ( caPk INTEGER NOT NULL, caVersionId INTEGER NOT NULL DEFAULT 0, caDestination INTEGER NOT NULL, caPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (caPk, caDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentReplicate_caPk_caDestination_caVersionId ON ClazzAssignmentReplicate (caPk, caDestination, caVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentReplicate_caDestination_caPending ON ClazzAssignmentReplicate (caDestination, caPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_520 AFTER INSERT ON ClazzAssignment BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 520 AS chTableId, NEW.caUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 520 AND chEntityPk = NEW.caUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_520 AFTER UPDATE ON ClazzAssignment BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 520 AS chTableId, NEW.caUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 520 AND chEntityPk = NEW.caUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_520 AFTER DELETE ON ClazzAssignment BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 520 AS chTableId, OLD.caUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 520 AND chEntityPk = OLD.caUid); END "
        _stmtList +=
            "CREATE VIEW ClazzAssignment_ReceiveView AS  SELECT ClazzAssignment.*, ClazzAssignmentReplicate.* FROM ClazzAssignment LEFT JOIN ClazzAssignmentReplicate ON ClazzAssignmentReplicate.caPk = ClazzAssignment.caUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoinReplicate ( cacjPk INTEGER NOT NULL, cacjVersionId INTEGER NOT NULL DEFAULT 0, cacjDestination INTEGER NOT NULL, cacjPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (cacjPk, cacjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentContentJoinReplicate_cacjPk_cacjDestination_cacjVersionId ON ClazzAssignmentContentJoinReplicate (cacjPk, cacjDestination, cacjVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentContentJoinReplicate_cacjDestination_cacjPending ON ClazzAssignmentContentJoinReplicate (cacjDestination, cacjPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_521 AFTER INSERT ON ClazzAssignmentContentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 521 AS chTableId, NEW.cacjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 521 AND chEntityPk = NEW.cacjUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_521 AFTER UPDATE ON ClazzAssignmentContentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 521 AS chTableId, NEW.cacjUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 521 AND chEntityPk = NEW.cacjUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_521 AFTER DELETE ON ClazzAssignmentContentJoin BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 521 AS chTableId, OLD.cacjUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 521 AND chEntityPk = OLD.cacjUid); END "
        _stmtList +=
            "CREATE VIEW ClazzAssignmentContentJoin_ReceiveView AS  SELECT ClazzAssignmentContentJoin.*, ClazzAssignmentContentJoinReplicate.* FROM ClazzAssignmentContentJoin LEFT JOIN ClazzAssignmentContentJoinReplicate ON ClazzAssignmentContentJoinReplicate.cacjPk = ClazzAssignmentContentJoin.cacjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonAuth2Replicate ( paPk INTEGER NOT NULL, paVersionId INTEGER NOT NULL DEFAULT 0, paDestination INTEGER NOT NULL, paPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (paPk, paDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonAuth2Replicate_paPk_paDestination_paVersionId ON PersonAuth2Replicate (paPk, paDestination, paVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonAuth2Replicate_paDestination_paPending ON PersonAuth2Replicate (paDestination, paPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_678 AFTER INSERT ON PersonAuth2 BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 678 AS chTableId, NEW.pauthUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 678 AND chEntityPk = NEW.pauthUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_678 AFTER UPDATE ON PersonAuth2 BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 678 AS chTableId, NEW.pauthUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 678 AND chEntityPk = NEW.pauthUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_678 AFTER DELETE ON PersonAuth2 BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 678 AS chTableId, OLD.pauthUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 678 AND chEntityPk = OLD.pauthUid); END "
        _stmtList +=
            "CREATE VIEW PersonAuth2_ReceiveView AS  SELECT PersonAuth2.*, PersonAuth2Replicate.* FROM PersonAuth2 LEFT JOIN PersonAuth2Replicate ON PersonAuth2Replicate.paPk = PersonAuth2.pauthUid "
        _stmtList +=
            " CREATE TRIGGER personauth2_remote_insert_ins INSTEAD OF INSERT ON PersonAuth2_ReceiveView FOR EACH ROW BEGIN REPLACE INTO PersonAuth2(pauthUid, pauthMechanism, pauthAuth, pauthLcsn, pauthPcsn, pauthLcb, pauthLct) VALUES (NEW.pauthUid, NEW.pauthMechanism, NEW.pauthAuth, NEW.pauthLcsn, NEW.pauthPcsn, NEW.pauthLcb, NEW.pauthLct) /*psql ON CONFLICT (pauthUid) DO UPDATE SET pauthMechanism = EXCLUDED.pauthMechanism, pauthAuth = EXCLUDED.pauthAuth, pauthLcsn = EXCLUDED.pauthLcsn, pauthPcsn = EXCLUDED.pauthPcsn, pauthLcb = EXCLUDED.pauthLcb, pauthLct = EXCLUDED.pauthLct */; END "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS UserSessionReplicate ( usPk INTEGER NOT NULL, usVersionId INTEGER NOT NULL DEFAULT 0, usDestination INTEGER NOT NULL, usPending INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (usPk, usDestination)) "
        _stmtList +=
            " CREATE INDEX index_UserSessionReplicate_usPk_usDestination_usVersionId ON UserSessionReplicate (usPk, usDestination, usVersionId) "
        _stmtList +=
            " CREATE INDEX index_UserSessionReplicate_usDestination_usPending ON UserSessionReplicate (usDestination, usPending) "
        _stmtList +=
            " CREATE TRIGGER ch_ins_679 AFTER INSERT ON UserSession BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 679 AS chTableId, NEW.usUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 679 AND chEntityPk = NEW.usUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_upd_679 AFTER UPDATE ON UserSession BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 679 AS chTableId, NEW.usUid AS chEntityPk, 1 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 679 AND chEntityPk = NEW.usUid); END "
        _stmtList +=
            " CREATE TRIGGER ch_del_679 AFTER DELETE ON UserSession BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) SELECT 679 AS chTableId, OLD.usUid AS chEntityPk, 2 AS chType WHERE NOT EXISTS( SELECT chTableId FROM ChangeLog WHERE chTableId = 679 AND chEntityPk = OLD.usUid); END "
        _stmtList +=
            "CREATE VIEW UserSession_ReceiveView AS  SELECT UserSession.*, UserSessionReplicate.* FROM UserSession LEFT JOIN UserSessionReplicate ON UserSessionReplicate.usPk = UserSession.usUid "
        _stmtList +=
            " CREATE TRIGGER usersession_remote_ins_ins INSTEAD OF INSERT ON UserSession_ReceiveView FOR EACH ROW BEGIN REPLACE INTO UserSession(usUid, usPcsn, usLcsn, usLcb, usLct, usPersonUid, usClientNodeId, usStartTime, usEndTime, usStatus, usReason, usAuth, usSessionType) VALUES (NEW.usUid, NEW.usPcsn, NEW.usLcsn, NEW.usLcb, NEW.usLct, NEW.usPersonUid, NEW.usClientNodeId, NEW.usStartTime, NEW.usEndTime, NEW.usStatus, NEW.usReason, NEW.usAuth, NEW.usSessionType) /*postgres ON CONFLICT (usUid) DO UPDATE SET usStatus = EXCLUDED.usStatus, usEndTime = EXCLUDED.usEndTime, usReason = EXCLUDED.usReason */ ; END "
    } else {
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzLogReplicate ( clPk BIGINT NOT NULL, clVersionId BIGINT NOT NULL DEFAULT 0, clDestination BIGINT NOT NULL, clPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (clPk, clDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogReplicate_clPk_clDestination_clVersionId ON ClazzLogReplicate (clPk, clDestination, clVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogReplicate_clDestination_clPending ON ClazzLogReplicate (clDestination, clPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_14_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (14, NEW.clazzLogUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_14_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_14_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_14_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (14, OLD.clazzLogUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_14_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_14_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzLog_ReceiveView AS  SELECT ClazzLog.*, ClazzLogReplicate.* FROM ClazzLog LEFT JOIN ClazzLogReplicate ON ClazzLogReplicate.clPk = ClazzLog.clazzLogUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzLogAttendanceRecordReplicate ( clarPk BIGINT NOT NULL, clarVersionId BIGINT NOT NULL DEFAULT 0, clarDestination BIGINT NOT NULL, clarPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (clarPk, clarDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogAttendanceRecordReplicate_clarPk_clarDestination_clarVersionId ON ClazzLogAttendanceRecordReplicate (clarPk, clarDestination, clarVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzLogAttendanceRecordReplicate_clarDestination_clarPending ON ClazzLogAttendanceRecordReplicate (clarDestination, clarPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_15_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (15, NEW.clazzLogAttendanceRecordUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_15_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_15_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_15_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (15, OLD.clazzLogAttendanceRecordUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_15_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_15_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzLogAttendanceRecord_ReceiveView AS  SELECT ClazzLogAttendanceRecord.*, ClazzLogAttendanceRecordReplicate.* FROM ClazzLogAttendanceRecord LEFT JOIN ClazzLogAttendanceRecordReplicate ON ClazzLogAttendanceRecordReplicate.clarPk = ClazzLogAttendanceRecord.clazzLogAttendanceRecordUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ScheduleReplicate ( schedulePk BIGINT NOT NULL, scheduleVersionId BIGINT NOT NULL DEFAULT 0, scheduleDestination BIGINT NOT NULL, schedulePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (schedulePk, scheduleDestination)) "
        _stmtList +=
            " CREATE INDEX index_ScheduleReplicate_schedulePk_scheduleDestination_scheduleVersionId ON ScheduleReplicate (schedulePk, scheduleDestination, scheduleVersionId) "
        _stmtList +=
            " CREATE INDEX index_ScheduleReplicate_scheduleDestination_schedulePending ON ScheduleReplicate (scheduleDestination, schedulePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_21_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (21, NEW.scheduleUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_21_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_21_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_21_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (21, OLD.scheduleUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_21_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_21_fn(); "
        _stmtList +=
            "CREATE VIEW Schedule_ReceiveView AS  SELECT Schedule.*, ScheduleReplicate.* FROM Schedule LEFT JOIN ScheduleReplicate ON ScheduleReplicate.schedulePk = Schedule.scheduleUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS HolidayCalendarReplicate ( hcPk BIGINT NOT NULL, hcVersionId BIGINT NOT NULL DEFAULT 0, hcDestination BIGINT NOT NULL, hcPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (hcPk, hcDestination)) "
        _stmtList +=
            " CREATE INDEX index_HolidayCalendarReplicate_hcPk_hcDestination_hcVersionId ON HolidayCalendarReplicate (hcPk, hcDestination, hcVersionId) "
        _stmtList +=
            " CREATE INDEX index_HolidayCalendarReplicate_hcDestination_hcPending ON HolidayCalendarReplicate (hcDestination, hcPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_28_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (28, NEW.umCalendarUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_28_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_28_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_28_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (28, OLD.umCalendarUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_28_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_28_fn(); "
        _stmtList +=
            "CREATE VIEW HolidayCalendar_ReceiveView AS  SELECT HolidayCalendar.*, HolidayCalendarReplicate.* FROM HolidayCalendar LEFT JOIN HolidayCalendarReplicate ON HolidayCalendarReplicate.hcPk = HolidayCalendar.umCalendarUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS HolidayReplicate ( holidayPk BIGINT NOT NULL, holidayVersionId BIGINT NOT NULL DEFAULT 0, holidayDestination BIGINT NOT NULL, holidayPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (holidayPk, holidayDestination)) "
        _stmtList +=
            " CREATE INDEX index_HolidayReplicate_holidayPk_holidayDestination_holidayVersionId ON HolidayReplicate (holidayPk, holidayDestination, holidayVersionId) "
        _stmtList +=
            " CREATE INDEX index_HolidayReplicate_holidayDestination_holidayPending ON HolidayReplicate (holidayDestination, holidayPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_99_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (99, NEW.holUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_99_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_99_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_99_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (99, OLD.holUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_99_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_99_fn(); "
        _stmtList +=
            "CREATE VIEW Holiday_ReceiveView AS  SELECT Holiday.*, HolidayReplicate.* FROM Holiday LEFT JOIN HolidayReplicate ON HolidayReplicate.holidayPk = Holiday.holUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonReplicate ( personPk BIGINT NOT NULL, personVersionId BIGINT NOT NULL DEFAULT 0, personDestination BIGINT NOT NULL, personPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (personPk, personDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonReplicate_personPk_personDestination_personVersionId ON PersonReplicate (personPk, personDestination, personVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonReplicate_personDestination_personPending ON PersonReplicate (personDestination, personPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_9_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (9, NEW.personUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_9_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_9_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_9_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (9, OLD.personUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_9_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_9_fn(); "
        _stmtList +=
            "CREATE VIEW Person_ReceiveView AS  SELECT Person.*, PersonReplicate.* FROM Person LEFT JOIN PersonReplicate ON PersonReplicate.personPk = Person.personUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION person_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO Person(personUid, username, firstNames, lastName, emailAddr, phoneNum, gender, active, admin, personNotes, fatherName, fatherNumber, motherName, motherNum, dateOfBirth, personAddress, personOrgId, personGroupUid, personMasterChangeSeqNum, personLocalChangeSeqNum, personLastChangedBy, personLct, personCountry, personType) VALUES (NEW.personUid, NEW.username, NEW.firstNames, NEW.lastName, NEW.emailAddr, NEW.phoneNum, NEW.gender, NEW.active, NEW.admin, NEW.personNotes, NEW.fatherName, NEW.fatherNumber, NEW.motherName, NEW.motherNum, NEW.dateOfBirth, NEW.personAddress, NEW.personOrgId, NEW.personGroupUid, NEW.personMasterChangeSeqNum, NEW.personLocalChangeSeqNum, NEW.personLastChangedBy, NEW.personLct, NEW.personCountry, NEW.personType) ON CONFLICT (personUid) DO UPDATE SET username = EXCLUDED.username, firstNames = EXCLUDED.firstNames, lastName = EXCLUDED.lastName, emailAddr = EXCLUDED.emailAddr, phoneNum = EXCLUDED.phoneNum, gender = EXCLUDED.gender, active = EXCLUDED.active, admin = EXCLUDED.admin, personNotes = EXCLUDED.personNotes, fatherName = EXCLUDED.fatherName, fatherNumber = EXCLUDED.fatherNumber, motherName = EXCLUDED.motherName, motherNum = EXCLUDED.motherNum, dateOfBirth = EXCLUDED.dateOfBirth, personAddress = EXCLUDED.personAddress, personOrgId = EXCLUDED.personOrgId, personGroupUid = EXCLUDED.personGroupUid, personMasterChangeSeqNum = EXCLUDED.personMasterChangeSeqNum, personLocalChangeSeqNum = EXCLUDED.personLocalChangeSeqNum, personLastChangedBy = EXCLUDED.personLastChangedBy, personLct = EXCLUDED.personLct, personCountry = EXCLUDED.personCountry, personType = EXCLUDED.personType ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER person_remote_insert_trig INSTEAD OF INSERT ON Person_ReceiveView FOR EACH ROW EXECUTE PROCEDURE person_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzReplicate ( clazzPk BIGINT NOT NULL, clazzVersionId BIGINT NOT NULL DEFAULT 0, clazzDestination BIGINT NOT NULL, clazzPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (clazzPk, clazzDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzReplicate_clazzPk_clazzDestination_clazzVersionId ON ClazzReplicate (clazzPk, clazzDestination, clazzVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzReplicate_clazzDestination_clazzPending ON ClazzReplicate (clazzDestination, clazzPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_6_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (6, NEW.clazzUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_6_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_6_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_6_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (6, OLD.clazzUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_6_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_6_fn(); "
        _stmtList +=
            "CREATE VIEW Clazz_ReceiveView AS  SELECT Clazz.*, ClazzReplicate.* FROM Clazz LEFT JOIN ClazzReplicate ON ClazzReplicate.clazzPk = Clazz.clazzUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION clazz_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO Clazz(clazzUid, clazzName, clazzDesc, attendanceAverage, clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid, isClazzActive, clazzLocationUid, clazzStartTime, clazzEndTime, clazzFeatures, clazzSchoolUid, clazzMasterChangeSeqNum, clazzLocalChangeSeqNum, clazzLastChangedBy, clazzLct, clazzTimeZone, clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid, clazzCode) VALUES (NEW.clazzUid, NEW.clazzName, NEW.clazzDesc, NEW.attendanceAverage, NEW.clazzHolidayUMCalendarUid, NEW.clazzScheuleUMCalendarUid, NEW.isClazzActive, NEW.clazzLocationUid, NEW.clazzStartTime, NEW.clazzEndTime, NEW.clazzFeatures, NEW.clazzSchoolUid, NEW.clazzMasterChangeSeqNum, NEW.clazzLocalChangeSeqNum, NEW.clazzLastChangedBy, NEW.clazzLct, NEW.clazzTimeZone, NEW.clazzStudentsPersonGroupUid, NEW.clazzTeachersPersonGroupUid, NEW.clazzPendingStudentsPersonGroupUid, NEW.clazzParentsPersonGroupUid, NEW.clazzCode) ON CONFLICT (clazzUid) DO UPDATE SET clazzName = EXCLUDED.clazzName, clazzDesc = EXCLUDED.clazzDesc, attendanceAverage = EXCLUDED.attendanceAverage, clazzHolidayUMCalendarUid = EXCLUDED.clazzHolidayUMCalendarUid, clazzScheuleUMCalendarUid = EXCLUDED.clazzScheuleUMCalendarUid, isClazzActive = EXCLUDED.isClazzActive, clazzLocationUid = EXCLUDED.clazzLocationUid, clazzStartTime = EXCLUDED.clazzStartTime, clazzEndTime = EXCLUDED.clazzEndTime, clazzFeatures = EXCLUDED.clazzFeatures, clazzSchoolUid = EXCLUDED.clazzSchoolUid, clazzMasterChangeSeqNum = EXCLUDED.clazzMasterChangeSeqNum, clazzLocalChangeSeqNum = EXCLUDED.clazzLocalChangeSeqNum, clazzLastChangedBy = EXCLUDED.clazzLastChangedBy, clazzLct = EXCLUDED.clazzLct, clazzTimeZone = EXCLUDED.clazzTimeZone, clazzStudentsPersonGroupUid = EXCLUDED.clazzStudentsPersonGroupUid, clazzTeachersPersonGroupUid = EXCLUDED.clazzTeachersPersonGroupUid, clazzPendingStudentsPersonGroupUid = EXCLUDED.clazzPendingStudentsPersonGroupUid, clazzParentsPersonGroupUid = EXCLUDED.clazzParentsPersonGroupUid, clazzCode = EXCLUDED.clazzCode ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER clazz_remote_insert_trig INSTEAD OF INSERT ON Clazz_ReceiveView FOR EACH ROW EXECUTE PROCEDURE clazz_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzEnrolmentReplicate ( cePk BIGINT NOT NULL, ceVersionId BIGINT NOT NULL DEFAULT 0, ceDestination BIGINT NOT NULL, cePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (cePk, ceDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzEnrolmentReplicate_cePk_ceDestination_ceVersionId ON ClazzEnrolmentReplicate (cePk, ceDestination, ceVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzEnrolmentReplicate_ceDestination_cePending ON ClazzEnrolmentReplicate (ceDestination, cePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_65_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (65, NEW.clazzEnrolmentUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_65_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_65_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_65_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (65, OLD.clazzEnrolmentUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_65_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_65_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzEnrolment_ReceiveView AS  SELECT ClazzEnrolment.*, ClazzEnrolmentReplicate.* FROM ClazzEnrolment LEFT JOIN ClazzEnrolmentReplicate ON ClazzEnrolmentReplicate.cePk = ClazzEnrolment.clazzEnrolmentUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION clazzenrolment_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ClazzEnrolment(clazzEnrolmentUid, clazzEnrolmentPersonUid, clazzEnrolmentClazzUid, clazzEnrolmentDateJoined, clazzEnrolmentDateLeft, clazzEnrolmentRole, clazzEnrolmentAttendancePercentage, clazzEnrolmentActive, clazzEnrolmentLeavingReasonUid, clazzEnrolmentOutcome, clazzEnrolmentLocalChangeSeqNum, clazzEnrolmentMasterChangeSeqNum, clazzEnrolmentLastChangedBy, clazzEnrolmentLct) VALUES (NEW.clazzEnrolmentUid, NEW.clazzEnrolmentPersonUid, NEW.clazzEnrolmentClazzUid, NEW.clazzEnrolmentDateJoined, NEW.clazzEnrolmentDateLeft, NEW.clazzEnrolmentRole, NEW.clazzEnrolmentAttendancePercentage, NEW.clazzEnrolmentActive, NEW.clazzEnrolmentLeavingReasonUid, NEW.clazzEnrolmentOutcome, NEW.clazzEnrolmentLocalChangeSeqNum, NEW.clazzEnrolmentMasterChangeSeqNum, NEW.clazzEnrolmentLastChangedBy, NEW.clazzEnrolmentLct) ON CONFLICT (clazzEnrolmentUid) DO UPDATE SET clazzEnrolmentPersonUid = EXCLUDED.clazzEnrolmentPersonUid, clazzEnrolmentClazzUid = EXCLUDED.clazzEnrolmentClazzUid, clazzEnrolmentDateJoined = EXCLUDED.clazzEnrolmentDateJoined, clazzEnrolmentDateLeft = EXCLUDED.clazzEnrolmentDateLeft, clazzEnrolmentRole = EXCLUDED.clazzEnrolmentRole, clazzEnrolmentAttendancePercentage = EXCLUDED.clazzEnrolmentAttendancePercentage, clazzEnrolmentActive = EXCLUDED.clazzEnrolmentActive, clazzEnrolmentLeavingReasonUid = EXCLUDED.clazzEnrolmentLeavingReasonUid, clazzEnrolmentOutcome = EXCLUDED.clazzEnrolmentOutcome, clazzEnrolmentLocalChangeSeqNum = EXCLUDED.clazzEnrolmentLocalChangeSeqNum, clazzEnrolmentMasterChangeSeqNum = EXCLUDED.clazzEnrolmentMasterChangeSeqNum, clazzEnrolmentLastChangedBy = EXCLUDED.clazzEnrolmentLastChangedBy, clazzEnrolmentLct = EXCLUDED.clazzEnrolmentLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER clazzenrolment_remote_insert_trig INSTEAD OF INSERT ON ClazzEnrolment_ReceiveView FOR EACH ROW EXECUTE PROCEDURE clazzenrolment_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LeavingReasonReplicate ( lrPk BIGINT NOT NULL, lrVersionId BIGINT NOT NULL DEFAULT 0, lrDestination BIGINT NOT NULL, lrPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (lrPk, lrDestination)) "
        _stmtList +=
            " CREATE INDEX index_LeavingReasonReplicate_lrPk_lrDestination_lrVersionId ON LeavingReasonReplicate (lrPk, lrDestination, lrVersionId) "
        _stmtList +=
            " CREATE INDEX index_LeavingReasonReplicate_lrDestination_lrPending ON LeavingReasonReplicate (lrDestination, lrPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_410_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (410, NEW.leavingReasonUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_410_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_410_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_410_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (410, OLD.leavingReasonUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_410_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_410_fn(); "
        _stmtList +=
            "CREATE VIEW LeavingReason_ReceiveView AS  SELECT LeavingReason.*, LeavingReasonReplicate.* FROM LeavingReason LEFT JOIN LeavingReasonReplicate ON LeavingReasonReplicate.lrPk = LeavingReason.leavingReasonUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryReplicate ( cePk BIGINT NOT NULL, ceVersionId BIGINT NOT NULL DEFAULT 0, ceDestination BIGINT NOT NULL, cePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (cePk, ceDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryReplicate_cePk_ceDestination_ceVersionId ON ContentEntryReplicate (cePk, ceDestination, ceVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryReplicate_ceDestination_cePending ON ContentEntryReplicate (ceDestination, cePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_42_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (42, NEW.contentEntryUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_42_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_42_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_42_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (42, OLD.contentEntryUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_42_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_42_fn(); "
        _stmtList +=
            "CREATE VIEW ContentEntry_ReceiveView AS  SELECT ContentEntry.*, ContentEntryReplicate.* FROM ContentEntry LEFT JOIN ContentEntryReplicate ON ContentEntryReplicate.cePk = ContentEntry.contentEntryUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION contententry_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ContentEntry(contentEntryUid, title, description, entryId, author, publisher, licenseType, licenseName, licenseUrl, sourceUrl, thumbnailUrl, lastModified, primaryLanguageUid, languageVariantUid, contentFlags, leaf, publik, ceInactive, completionCriteria, minScore, contentTypeFlag, contentOwner, contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum, contentEntryLastChangedBy, contentEntryLct) VALUES (NEW.contentEntryUid, NEW.title, NEW.description, NEW.entryId, NEW.author, NEW.publisher, NEW.licenseType, NEW.licenseName, NEW.licenseUrl, NEW.sourceUrl, NEW.thumbnailUrl, NEW.lastModified, NEW.primaryLanguageUid, NEW.languageVariantUid, NEW.contentFlags, NEW.leaf, NEW.publik, NEW.ceInactive, NEW.completionCriteria, NEW.minScore, NEW.contentTypeFlag, NEW.contentOwner, NEW.contentEntryLocalChangeSeqNum, NEW.contentEntryMasterChangeSeqNum, NEW.contentEntryLastChangedBy, NEW.contentEntryLct) ON CONFLICT (contentEntryUid) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description, entryId = EXCLUDED.entryId, author = EXCLUDED.author, publisher = EXCLUDED.publisher, licenseType = EXCLUDED.licenseType, licenseName = EXCLUDED.licenseName, licenseUrl = EXCLUDED.licenseUrl, sourceUrl = EXCLUDED.sourceUrl, thumbnailUrl = EXCLUDED.thumbnailUrl, lastModified = EXCLUDED.lastModified, primaryLanguageUid = EXCLUDED.primaryLanguageUid, languageVariantUid = EXCLUDED.languageVariantUid, contentFlags = EXCLUDED.contentFlags, leaf = EXCLUDED.leaf, publik = EXCLUDED.publik, ceInactive = EXCLUDED.ceInactive, completionCriteria = EXCLUDED.completionCriteria, minScore = EXCLUDED.minScore, contentTypeFlag = EXCLUDED.contentTypeFlag, contentOwner = EXCLUDED.contentOwner, contentEntryLocalChangeSeqNum = EXCLUDED.contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum = EXCLUDED.contentEntryMasterChangeSeqNum, contentEntryLastChangedBy = EXCLUDED.contentEntryLastChangedBy, contentEntryLct = EXCLUDED.contentEntryLct; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER contententry_remote_insert_trig INSTEAD OF INSERT ON ContentEntry_ReceiveView FOR EACH ROW EXECUTE PROCEDURE contententry_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryContentCategoryJoinReplicate ( ceccjPk BIGINT NOT NULL, ceccjVersionId BIGINT NOT NULL DEFAULT 0, ceccjDestination BIGINT NOT NULL, ceccjPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (ceccjPk, ceccjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryContentCategoryJoinReplicate_ceccjPk_ceccjDestination_ceccjVersionId ON ContentEntryContentCategoryJoinReplicate (ceccjPk, ceccjDestination, ceccjVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryContentCategoryJoinReplicate_ceccjDestination_ceccjPending ON ContentEntryContentCategoryJoinReplicate (ceccjDestination, ceccjPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_3_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (3, NEW.ceccjUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_3_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_3_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_3_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (3, OLD.ceccjUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_3_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_3_fn(); "
        _stmtList +=
            "CREATE VIEW ContentEntryContentCategoryJoin_ReceiveView AS  SELECT ContentEntryContentCategoryJoin.*, ContentEntryContentCategoryJoinReplicate.* FROM ContentEntryContentCategoryJoin LEFT JOIN ContentEntryContentCategoryJoinReplicate ON ContentEntryContentCategoryJoinReplicate.ceccjPk = ContentEntryContentCategoryJoin.ceccjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryParentChildJoinReplicate ( cepcjPk BIGINT NOT NULL, cepcjVersionId BIGINT NOT NULL DEFAULT 0, cepcjDestination BIGINT NOT NULL, cepcjPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (cepcjPk, cepcjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryParentChildJoinReplicate_cepcjPk_cepcjDestination_cepcjVersionId ON ContentEntryParentChildJoinReplicate (cepcjPk, cepcjDestination, cepcjVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryParentChildJoinReplicate_cepcjDestination_cepcjPending ON ContentEntryParentChildJoinReplicate (cepcjDestination, cepcjPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_7_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (7, NEW.cepcjUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_7_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_7_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_7_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (7, OLD.cepcjUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_7_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_7_fn(); "
        _stmtList +=
            "CREATE VIEW ContentEntryParentChildJoin_ReceiveView AS  SELECT ContentEntryParentChildJoin.*, ContentEntryParentChildJoinReplicate.* FROM ContentEntryParentChildJoin LEFT JOIN ContentEntryParentChildJoinReplicate ON ContentEntryParentChildJoinReplicate.cepcjPk = ContentEntryParentChildJoin.cepcjUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION contententryparentchildjoin_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ContentEntryParentChildJoin(cepcjParentContentEntryUid, cepcjChildContentEntryUid, childIndex, cepcjUid, cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum, cepcjLastChangedBy, cepcjLct) VALUES (NEW.cepcjParentContentEntryUid, NEW.cepcjChildContentEntryUid, NEW.childIndex, NEW.cepcjUid, NEW.cepcjLocalChangeSeqNum, NEW.cepcjMasterChangeSeqNum, NEW.cepcjLastChangedBy, NEW.cepcjLct) ON CONFLICT (cepcjUid) DO UPDATE SET cepcjParentContentEntryUid = EXCLUDED.cepcjParentContentEntryUid, cepcjChildContentEntryUid = EXCLUDED.cepcjChildContentEntryUid, childIndex = EXCLUDED.childIndex, cepcjLocalChangeSeqNum = EXCLUDED.cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum = EXCLUDED.cepcjMasterChangeSeqNum, cepcjLastChangedBy = EXCLUDED.cepcjLastChangedBy, cepcjLct = EXCLUDED.cepcjLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER contententryparentchildjoin_remote_insert_trig INSTEAD OF INSERT ON ContentEntryParentChildJoin_ReceiveView FOR EACH ROW EXECUTE PROCEDURE contententryparentchildjoin_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentEntryRelatedEntryJoinReplicate ( cerejPk BIGINT NOT NULL, cerejVersionId BIGINT NOT NULL DEFAULT 0, cerejDestination BIGINT NOT NULL, cerejPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (cerejPk, cerejDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryRelatedEntryJoinReplicate_cerejPk_cerejDestination_cerejVersionId ON ContentEntryRelatedEntryJoinReplicate (cerejPk, cerejDestination, cerejVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentEntryRelatedEntryJoinReplicate_cerejDestination_cerejPending ON ContentEntryRelatedEntryJoinReplicate (cerejDestination, cerejPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_8_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (8, NEW.cerejUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_8_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_8_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_8_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (8, OLD.cerejUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_8_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_8_fn(); "
        _stmtList +=
            "CREATE VIEW ContentEntryRelatedEntryJoin_ReceiveView AS  SELECT ContentEntryRelatedEntryJoin.*, ContentEntryRelatedEntryJoinReplicate.* FROM ContentEntryRelatedEntryJoin LEFT JOIN ContentEntryRelatedEntryJoinReplicate ON ContentEntryRelatedEntryJoinReplicate.cerejPk = ContentEntryRelatedEntryJoin.cerejUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentCategorySchemaReplicate ( ccsPk BIGINT NOT NULL, ccsVersionId BIGINT NOT NULL DEFAULT 0, ccsDestination BIGINT NOT NULL, ccsPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (ccsPk, ccsDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentCategorySchemaReplicate_ccsPk_ccsDestination_ccsVersionId ON ContentCategorySchemaReplicate (ccsPk, ccsDestination, ccsVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentCategorySchemaReplicate_ccsDestination_ccsPending ON ContentCategorySchemaReplicate (ccsDestination, ccsPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_2_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (2, NEW.contentCategorySchemaUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_2_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_2_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_2_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (2, OLD.contentCategorySchemaUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_2_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_2_fn(); "
        _stmtList +=
            "CREATE VIEW ContentCategorySchema_ReceiveView AS  SELECT ContentCategorySchema.*, ContentCategorySchemaReplicate.* FROM ContentCategorySchema LEFT JOIN ContentCategorySchemaReplicate ON ContentCategorySchemaReplicate.ccsPk = ContentCategorySchema.contentCategorySchemaUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContentCategoryReplicate ( ccPk BIGINT NOT NULL, ccVersionId BIGINT NOT NULL DEFAULT 0, ccDestination BIGINT NOT NULL, ccPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (ccPk, ccDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContentCategoryReplicate_ccPk_ccDestination_ccVersionId ON ContentCategoryReplicate (ccPk, ccDestination, ccVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContentCategoryReplicate_ccDestination_ccPending ON ContentCategoryReplicate (ccDestination, ccPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_1_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (1, NEW.contentCategoryUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_1_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_1_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_1_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (1, OLD.contentCategoryUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_1_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_1_fn(); "
        _stmtList +=
            "CREATE VIEW ContentCategory_ReceiveView AS  SELECT ContentCategory.*, ContentCategoryReplicate.* FROM ContentCategory LEFT JOIN ContentCategoryReplicate ON ContentCategoryReplicate.ccPk = ContentCategory.contentCategoryUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LanguageReplicate ( languagePk BIGINT NOT NULL, languageVersionId BIGINT NOT NULL DEFAULT 0, languageDestination BIGINT NOT NULL, languagePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (languagePk, languageDestination)) "
        _stmtList +=
            " CREATE INDEX index_LanguageReplicate_languagePk_languageDestination_languageVersionId ON LanguageReplicate (languagePk, languageDestination, languageVersionId) "
        _stmtList +=
            " CREATE INDEX index_LanguageReplicate_languageDestination_languagePending ON LanguageReplicate (languageDestination, languagePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_13_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (13, NEW.langUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_13_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_13_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_13_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (13, OLD.langUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_13_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_13_fn(); "
        _stmtList +=
            "CREATE VIEW Language_ReceiveView AS  SELECT Language.*, LanguageReplicate.* FROM Language LEFT JOIN LanguageReplicate ON LanguageReplicate.languagePk = Language.langUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LanguageVariantReplicate ( lvPk BIGINT NOT NULL, lvVersionId BIGINT NOT NULL DEFAULT 0, lvDestination BIGINT NOT NULL, lvPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (lvPk, lvDestination)) "
        _stmtList +=
            " CREATE INDEX index_LanguageVariantReplicate_lvPk_lvDestination_lvVersionId ON LanguageVariantReplicate (lvPk, lvDestination, lvVersionId) "
        _stmtList +=
            " CREATE INDEX index_LanguageVariantReplicate_lvDestination_lvPending ON LanguageVariantReplicate (lvDestination, lvPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_10_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (10, NEW.langVariantUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_10_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_10_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_10_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (10, OLD.langVariantUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_10_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_10_fn(); "
        _stmtList +=
            "CREATE VIEW LanguageVariant_ReceiveView AS  SELECT LanguageVariant.*, LanguageVariantReplicate.* FROM LanguageVariant LEFT JOIN LanguageVariantReplicate ON LanguageVariantReplicate.lvPk = LanguageVariant.langVariantUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonGroupReplicate ( pgPk BIGINT NOT NULL, pgVersionId BIGINT NOT NULL DEFAULT 0, pgDestination BIGINT NOT NULL, pgPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (pgPk, pgDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupReplicate_pgPk_pgDestination_pgVersionId ON PersonGroupReplicate (pgPk, pgDestination, pgVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupReplicate_pgDestination_pgPending ON PersonGroupReplicate (pgDestination, pgPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_43_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (43, NEW.groupUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_43_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_43_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_43_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (43, OLD.groupUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_43_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_43_fn(); "
        _stmtList +=
            "CREATE VIEW PersonGroup_ReceiveView AS  SELECT PersonGroup.*, PersonGroupReplicate.* FROM PersonGroup LEFT JOIN PersonGroupReplicate ON PersonGroupReplicate.pgPk = PersonGroup.groupUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION persongroup_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO PersonGroup(groupUid, groupMasterCsn, groupLocalCsn, groupLastChangedBy, groupLct, groupName, groupActive, personGroupFlag) VALUES (NEW.groupUid, NEW.groupMasterCsn, NEW.groupLocalCsn, NEW.groupLastChangedBy, NEW.groupLct, NEW.groupName, NEW.groupActive, NEW.personGroupFlag) ON CONFLICT (groupUid) DO UPDATE SET groupMasterCsn = EXCLUDED.groupMasterCsn, groupLocalCsn = EXCLUDED.groupLocalCsn, groupLastChangedBy = EXCLUDED.groupLastChangedBy, groupLct = EXCLUDED.groupLct, groupName = EXCLUDED.groupName, groupActive = EXCLUDED.groupActive, personGroupFlag = EXCLUDED.personGroupFlag ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER persongroup_remote_insert_trig INSTEAD OF INSERT ON PersonGroup_ReceiveView FOR EACH ROW EXECUTE PROCEDURE persongroup_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonGroupMemberReplicate ( pgmPk BIGINT NOT NULL, pgmVersionId BIGINT NOT NULL DEFAULT 0, pgmDestination BIGINT NOT NULL, pgmPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (pgmPk, pgmDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupMemberReplicate_pgmPk_pgmDestination_pgmVersionId ON PersonGroupMemberReplicate (pgmPk, pgmDestination, pgmVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonGroupMemberReplicate_pgmDestination_pgmPending ON PersonGroupMemberReplicate (pgmDestination, pgmPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_44_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (44, NEW.groupMemberUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_44_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_44_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_44_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (44, OLD.groupMemberUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_44_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_44_fn(); "
        _stmtList +=
            "CREATE VIEW PersonGroupMember_ReceiveView AS  SELECT PersonGroupMember.*, PersonGroupMemberReplicate.* FROM PersonGroupMember LEFT JOIN PersonGroupMemberReplicate ON PersonGroupMemberReplicate.pgmPk = PersonGroupMember.groupMemberUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION persongroupmember_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO PersonGroupMember(groupMemberUid, groupMemberActive, groupMemberPersonUid, groupMemberGroupUid, groupMemberMasterCsn, groupMemberLocalCsn, groupMemberLastChangedBy, groupMemberLct) VALUES (NEW.groupMemberUid, NEW.groupMemberActive, NEW.groupMemberPersonUid, NEW.groupMemberGroupUid, NEW.groupMemberMasterCsn, NEW.groupMemberLocalCsn, NEW.groupMemberLastChangedBy, NEW.groupMemberLct) ON CONFLICT (groupMemberUid) DO UPDATE SET groupMemberActive = EXCLUDED.groupMemberActive, groupMemberPersonUid = EXCLUDED.groupMemberPersonUid, groupMemberGroupUid = EXCLUDED.groupMemberGroupUid, groupMemberMasterCsn = EXCLUDED.groupMemberMasterCsn, groupMemberLocalCsn = EXCLUDED.groupMemberLocalCsn, groupMemberLastChangedBy = EXCLUDED.groupMemberLastChangedBy, groupMemberLct = EXCLUDED.groupMemberLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER persongroupmember_remote_insert_trig INSTEAD OF INSERT ON PersonGroupMember_ReceiveView FOR EACH ROW EXECUTE PROCEDURE persongroupmember_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonPictureReplicate ( ppPk BIGINT NOT NULL, ppVersionId BIGINT NOT NULL DEFAULT 0, ppDestination BIGINT NOT NULL, ppPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (ppPk, ppDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonPictureReplicate_ppPk_ppDestination_ppVersionId ON PersonPictureReplicate (ppPk, ppDestination, ppVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonPictureReplicate_ppDestination_ppPending ON PersonPictureReplicate (ppDestination, ppPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_50_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (50, NEW.personPictureUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_50_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_50_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_50_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (50, OLD.personPictureUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_50_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_50_fn(); "
        _stmtList +=
            "CREATE VIEW PersonPicture_ReceiveView AS  SELECT PersonPicture.*, PersonPictureReplicate.* FROM PersonPicture LEFT JOIN PersonPictureReplicate ON PersonPictureReplicate.ppPk = PersonPicture.personPictureUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContainerReplicate ( containerPk BIGINT NOT NULL, containerVersionId BIGINT NOT NULL DEFAULT 0, containerDestination BIGINT NOT NULL, containerPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (containerPk, containerDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContainerReplicate_containerPk_containerDestination_containerVersionId ON ContainerReplicate (containerPk, containerDestination, containerVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContainerReplicate_containerDestination_containerPending ON ContainerReplicate (containerDestination, containerPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_51_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (51, NEW.containerUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_51_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_51_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_51_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (51, OLD.containerUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_51_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_51_fn(); "
        _stmtList +=
            "CREATE VIEW Container_ReceiveView AS  SELECT Container.*, ContainerReplicate.* FROM Container LEFT JOIN ContainerReplicate ON ContainerReplicate.containerPk = Container.containerUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS VerbEntityReplicate ( vePk BIGINT NOT NULL, veVersionId BIGINT NOT NULL DEFAULT 0, veDestination BIGINT NOT NULL, vePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (vePk, veDestination)) "
        _stmtList +=
            " CREATE INDEX index_VerbEntityReplicate_vePk_veDestination_veVersionId ON VerbEntityReplicate (vePk, veDestination, veVersionId) "
        _stmtList +=
            " CREATE INDEX index_VerbEntityReplicate_veDestination_vePending ON VerbEntityReplicate (veDestination, vePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_62_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (62, NEW.verbUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_62_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_62_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_62_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (62, OLD.verbUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_62_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_62_fn(); "
        _stmtList +=
            "CREATE VIEW VerbEntity_ReceiveView AS  SELECT VerbEntity.*, VerbEntityReplicate.* FROM VerbEntity LEFT JOIN VerbEntityReplicate ON VerbEntityReplicate.vePk = VerbEntity.verbUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS XObjectEntityReplicate ( xoePk BIGINT NOT NULL, xoeVersionId BIGINT NOT NULL DEFAULT 0, xoeDestination BIGINT NOT NULL, xoePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (xoePk, xoeDestination)) "
        _stmtList +=
            " CREATE INDEX index_XObjectEntityReplicate_xoePk_xoeDestination_xoeVersionId ON XObjectEntityReplicate (xoePk, xoeDestination, xoeVersionId) "
        _stmtList +=
            " CREATE INDEX index_XObjectEntityReplicate_xoeDestination_xoePending ON XObjectEntityReplicate (xoeDestination, xoePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_64_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (64, NEW.xObjectUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_64_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_64_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_64_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (64, OLD.xObjectUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_64_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_64_fn(); "
        _stmtList +=
            "CREATE VIEW XObjectEntity_ReceiveView AS  SELECT XObjectEntity.*, XObjectEntityReplicate.* FROM XObjectEntity LEFT JOIN XObjectEntityReplicate ON XObjectEntityReplicate.xoePk = XObjectEntity.xObjectUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StatementEntityReplicate ( sePk BIGINT NOT NULL, seVersionId BIGINT NOT NULL DEFAULT 0, seDestination BIGINT NOT NULL, sePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (sePk, seDestination)) "
        _stmtList +=
            " CREATE INDEX index_StatementEntityReplicate_sePk_seDestination_seVersionId ON StatementEntityReplicate (sePk, seDestination, seVersionId) "
        _stmtList +=
            " CREATE INDEX index_StatementEntityReplicate_seDestination_sePending ON StatementEntityReplicate (seDestination, sePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_60_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (60, NEW.statementUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_60_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_60_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_60_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (60, OLD.statementUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_60_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_60_fn(); "
        _stmtList +=
            "CREATE VIEW StatementEntity_ReceiveView AS  SELECT StatementEntity.*, StatementEntityReplicate.* FROM StatementEntity LEFT JOIN StatementEntityReplicate ON StatementEntityReplicate.sePk = StatementEntity.statementUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ContextXObjectStatementJoinReplicate ( cxosjPk BIGINT NOT NULL, cxosjVersionId BIGINT NOT NULL DEFAULT 0, cxosjDestination BIGINT NOT NULL, cxosjPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (cxosjPk, cxosjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ContextXObjectStatementJoinReplicate_cxosjPk_cxosjDestination_cxosjVersionId ON ContextXObjectStatementJoinReplicate (cxosjPk, cxosjDestination, cxosjVersionId) "
        _stmtList +=
            " CREATE INDEX index_ContextXObjectStatementJoinReplicate_cxosjDestination_cxosjPending ON ContextXObjectStatementJoinReplicate (cxosjDestination, cxosjPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_66_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (66, NEW.contextXObjectStatementJoinUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_66_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_66_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_66_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (66, OLD.contextXObjectStatementJoinUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_66_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_66_fn(); "
        _stmtList +=
            "CREATE VIEW ContextXObjectStatementJoin_ReceiveView AS  SELECT ContextXObjectStatementJoin.*, ContextXObjectStatementJoinReplicate.* FROM ContextXObjectStatementJoin LEFT JOIN ContextXObjectStatementJoinReplicate ON ContextXObjectStatementJoinReplicate.cxosjPk = ContextXObjectStatementJoin.contextXObjectStatementJoinUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS AgentEntityReplicate ( aePk BIGINT NOT NULL, aeVersionId BIGINT NOT NULL DEFAULT 0, aeDestination BIGINT NOT NULL, aePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (aePk, aeDestination)) "
        _stmtList +=
            " CREATE INDEX index_AgentEntityReplicate_aePk_aeDestination_aeVersionId ON AgentEntityReplicate (aePk, aeDestination, aeVersionId) "
        _stmtList +=
            " CREATE INDEX index_AgentEntityReplicate_aeDestination_aePending ON AgentEntityReplicate (aeDestination, aePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_68_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (68, NEW.agentUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_68_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_68_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_68_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (68, OLD.agentUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_68_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_68_fn(); "
        _stmtList +=
            "CREATE VIEW AgentEntity_ReceiveView AS  SELECT AgentEntity.*, AgentEntityReplicate.* FROM AgentEntity LEFT JOIN AgentEntityReplicate ON AgentEntityReplicate.aePk = AgentEntity.agentUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION agententity_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO AgentEntity(agentUid, agentMbox, agentMbox_sha1sum, agentOpenid, agentAccountName, agentHomePage, agentPersonUid, statementMasterChangeSeqNum, statementLocalChangeSeqNum, statementLastChangedBy, agentLct) VALUES (NEW.agentUid, NEW.agentMbox, NEW.agentMbox_sha1sum, NEW.agentOpenid, NEW.agentAccountName, NEW.agentHomePage, NEW.agentPersonUid, NEW.statementMasterChangeSeqNum, NEW.statementLocalChangeSeqNum, NEW.statementLastChangedBy, NEW.agentLct) ON CONFLICT (agentUid) DO UPDATE SET agentMbox = EXCLUDED.agentMbox, agentMbox_sha1sum = EXCLUDED.agentMbox_sha1sum, agentOpenid = EXCLUDED.agentOpenid, agentAccountName = EXCLUDED.agentAccountName, agentHomePage = EXCLUDED.agentHomePage, agentPersonUid = EXCLUDED.agentPersonUid, statementMasterChangeSeqNum = EXCLUDED.statementMasterChangeSeqNum, statementLocalChangeSeqNum = EXCLUDED.statementLocalChangeSeqNum, statementLastChangedBy = EXCLUDED.statementLastChangedBy, agentLct = EXCLUDED.agentLct; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER agententity_remote_insert_trig INSTEAD OF INSERT ON AgentEntity_ReceiveView FOR EACH ROW EXECUTE PROCEDURE agententity_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StateEntityReplicate ( sePk BIGINT NOT NULL, seVersionId BIGINT NOT NULL DEFAULT 0, seDestination BIGINT NOT NULL, sePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (sePk, seDestination)) "
        _stmtList +=
            " CREATE INDEX index_StateEntityReplicate_sePk_seDestination_seVersionId ON StateEntityReplicate (sePk, seDestination, seVersionId) "
        _stmtList +=
            " CREATE INDEX index_StateEntityReplicate_seDestination_sePending ON StateEntityReplicate (seDestination, sePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_70_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (70, NEW.stateUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_70_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_70_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_70_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (70, OLD.stateUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_70_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_70_fn(); "
        _stmtList +=
            "CREATE VIEW StateEntity_ReceiveView AS  SELECT StateEntity.*, StateEntityReplicate.* FROM StateEntity LEFT JOIN StateEntityReplicate ON StateEntityReplicate.sePk = StateEntity.stateUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS StateContentEntityReplicate ( scePk BIGINT NOT NULL, sceVersionId BIGINT NOT NULL DEFAULT 0, sceDestination BIGINT NOT NULL, scePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (scePk, sceDestination)) "
        _stmtList +=
            " CREATE INDEX index_StateContentEntityReplicate_scePk_sceDestination_sceVersionId ON StateContentEntityReplicate (scePk, sceDestination, sceVersionId) "
        _stmtList +=
            " CREATE INDEX index_StateContentEntityReplicate_sceDestination_scePending ON StateContentEntityReplicate (sceDestination, scePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_72_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (72, NEW.stateContentUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_72_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_72_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_72_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (72, OLD.stateContentUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_72_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_72_fn(); "
        _stmtList +=
            "CREATE VIEW StateContentEntity_ReceiveView AS  SELECT StateContentEntity.*, StateContentEntityReplicate.* FROM StateContentEntity LEFT JOIN StateContentEntityReplicate ON StateContentEntityReplicate.scePk = StateContentEntity.stateContentUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS XLangMapEntryReplicate ( xlmePk BIGINT NOT NULL, xlmeVersionId BIGINT NOT NULL DEFAULT 0, xlmeDestination BIGINT NOT NULL, xlmePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (xlmePk, xlmeDestination)) "
        _stmtList +=
            " CREATE INDEX index_XLangMapEntryReplicate_xlmePk_xlmeDestination_xlmeVersionId ON XLangMapEntryReplicate (xlmePk, xlmeDestination, xlmeVersionId) "
        _stmtList +=
            " CREATE INDEX index_XLangMapEntryReplicate_xlmeDestination_xlmePending ON XLangMapEntryReplicate (xlmeDestination, xlmePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_74_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (74, NEW.statementLangMapUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_74_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_74_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_74_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (74, OLD.statementLangMapUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_74_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_74_fn(); "
        _stmtList +=
            "CREATE VIEW XLangMapEntry_ReceiveView AS  SELECT XLangMapEntry.*, XLangMapEntryReplicate.* FROM XLangMapEntry LEFT JOIN XLangMapEntryReplicate ON XLangMapEntryReplicate.xlmePk = XLangMapEntry.statementLangMapUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SchoolReplicate ( schoolPk BIGINT NOT NULL, schoolVersionId BIGINT NOT NULL DEFAULT 0, schoolDestination BIGINT NOT NULL, schoolPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (schoolPk, schoolDestination)) "
        _stmtList +=
            " CREATE INDEX index_SchoolReplicate_schoolPk_schoolDestination_schoolVersionId ON SchoolReplicate (schoolPk, schoolDestination, schoolVersionId) "
        _stmtList +=
            " CREATE INDEX index_SchoolReplicate_schoolDestination_schoolPending ON SchoolReplicate (schoolDestination, schoolPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_164_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (164, NEW.schoolUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_164_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_164_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_164_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (164, OLD.schoolUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_164_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_164_fn(); "
        _stmtList +=
            "CREATE VIEW School_ReceiveView AS  SELECT School.*, SchoolReplicate.* FROM School LEFT JOIN SchoolReplicate ON SchoolReplicate.schoolPk = School.schoolUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SchoolMemberReplicate ( smPk BIGINT NOT NULL, smVersionId BIGINT NOT NULL DEFAULT 0, smDestination BIGINT NOT NULL, smPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (smPk, smDestination)) "
        _stmtList +=
            " CREATE INDEX index_SchoolMemberReplicate_smPk_smDestination_smVersionId ON SchoolMemberReplicate (smPk, smDestination, smVersionId) "
        _stmtList +=
            " CREATE INDEX index_SchoolMemberReplicate_smDestination_smPending ON SchoolMemberReplicate (smDestination, smPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_200_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (200, NEW.schoolMemberUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_200_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_200_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_200_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (200, OLD.schoolMemberUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_200_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_200_fn(); "
        _stmtList +=
            "CREATE VIEW SchoolMember_ReceiveView AS  SELECT SchoolMember.*, SchoolMemberReplicate.* FROM SchoolMember LEFT JOIN SchoolMemberReplicate ON SchoolMemberReplicate.smPk = SchoolMember.schoolMemberUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS CommentsReplicate ( commentsPk BIGINT NOT NULL, commentsVersionId BIGINT NOT NULL DEFAULT 0, commentsDestination BIGINT NOT NULL, commentsPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (commentsPk, commentsDestination)) "
        _stmtList +=
            " CREATE INDEX index_CommentsReplicate_commentsPk_commentsDestination_commentsVersionId ON CommentsReplicate (commentsPk, commentsDestination, commentsVersionId) "
        _stmtList +=
            " CREATE INDEX index_CommentsReplicate_commentsDestination_commentsPending ON CommentsReplicate (commentsDestination, commentsPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_208_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (208, NEW.commentsUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_208_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_208_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_208_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (208, OLD.commentsUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_208_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_208_fn(); "
        _stmtList +=
            "CREATE VIEW Comments_ReceiveView AS  SELECT Comments.*, CommentsReplicate.* FROM Comments LEFT JOIN CommentsReplicate ON CommentsReplicate.commentsPk = Comments.commentsUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ReportReplicate ( reportPk BIGINT NOT NULL, reportVersionId BIGINT NOT NULL DEFAULT 0, reportDestination BIGINT NOT NULL, reportPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (reportPk, reportDestination)) "
        _stmtList +=
            " CREATE INDEX index_ReportReplicate_reportPk_reportDestination_reportVersionId ON ReportReplicate (reportPk, reportDestination, reportVersionId) "
        _stmtList +=
            " CREATE INDEX index_ReportReplicate_reportDestination_reportPending ON ReportReplicate (reportDestination, reportPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_101_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (101, NEW.reportUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_101_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_101_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_101_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (101, OLD.reportUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_101_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_101_fn(); "
        _stmtList +=
            "CREATE VIEW Report_ReceiveView AS  SELECT Report.*, ReportReplicate.* FROM Report LEFT JOIN ReportReplicate ON ReportReplicate.reportPk = Report.reportUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SiteReplicate ( sitePk BIGINT NOT NULL, siteVersionId BIGINT NOT NULL DEFAULT 0, siteDestination BIGINT NOT NULL, sitePending BOOL NOT NULL DEFAULT true, PRIMARY KEY (sitePk, siteDestination)) "
        _stmtList +=
            " CREATE INDEX index_SiteReplicate_sitePk_siteDestination_siteVersionId ON SiteReplicate (sitePk, siteDestination, siteVersionId) "
        _stmtList +=
            " CREATE INDEX index_SiteReplicate_siteDestination_sitePending ON SiteReplicate (siteDestination, sitePending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_189_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (189, NEW.siteUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_189_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_189_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_189_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (189, OLD.siteUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_189_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_189_fn(); "
        _stmtList +=
            "CREATE VIEW Site_ReceiveView AS  SELECT Site.*, SiteReplicate.* FROM Site LEFT JOIN SiteReplicate ON SiteReplicate.sitePk = Site.siteUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION site_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO Site(siteUid, sitePcsn, siteLcsn, siteLcb, siteLct, siteName, guestLogin, registrationAllowed, authSalt) VALUES (NEW.siteUid, NEW.sitePcsn, NEW.siteLcsn, NEW.siteLcb, NEW.siteLct, NEW.siteName, NEW.guestLogin, NEW.registrationAllowed, NEW.authSalt) ON CONFLICT (siteUid) DO UPDATE SET sitePcsn = EXCLUDED.sitePcsn, siteLcsn = EXCLUDED.siteLcsn, siteLcb = EXCLUDED.siteLcb, siteLct = EXCLUDED.siteLct, siteName = EXCLUDED.siteName, guestLogin = EXCLUDED.guestLogin, registrationAllowed = EXCLUDED.registrationAllowed, authSalt = EXCLUDED.authSalt; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER site_remote_insert_trig INSTEAD OF INSERT ON Site_ReceiveView FOR EACH ROW EXECUTE PROCEDURE site_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LearnerGroupReplicate ( lgPk BIGINT NOT NULL, lgVersionId BIGINT NOT NULL DEFAULT 0, lgDestination BIGINT NOT NULL, lgPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (lgPk, lgDestination)) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupReplicate_lgPk_lgDestination_lgVersionId ON LearnerGroupReplicate (lgPk, lgDestination, lgVersionId) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupReplicate_lgDestination_lgPending ON LearnerGroupReplicate (lgDestination, lgPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_301_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (301, NEW.learnerGroupUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_301_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_301_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_301_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (301, OLD.learnerGroupUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_301_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_301_fn(); "
        _stmtList +=
            "CREATE VIEW LearnerGroup_ReceiveView AS  SELECT LearnerGroup.*, LearnerGroupReplicate.* FROM LearnerGroup LEFT JOIN LearnerGroupReplicate ON LearnerGroupReplicate.lgPk = LearnerGroup.learnerGroupUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS LearnerGroupMemberReplicate ( lgmPk BIGINT NOT NULL, lgmVersionId BIGINT NOT NULL DEFAULT 0, lgmDestination BIGINT NOT NULL, lgmPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (lgmPk, lgmDestination)) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupMemberReplicate_lgmPk_lgmDestination_lgmVersionId ON LearnerGroupMemberReplicate (lgmPk, lgmDestination, lgmVersionId) "
        _stmtList +=
            " CREATE INDEX index_LearnerGroupMemberReplicate_lgmDestination_lgmPending ON LearnerGroupMemberReplicate (lgmDestination, lgmPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_300_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (300, NEW.learnerGroupMemberUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_300_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_300_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_300_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (300, OLD.learnerGroupMemberUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_300_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_300_fn(); "
        _stmtList +=
            "CREATE VIEW LearnerGroupMember_ReceiveView AS  SELECT LearnerGroupMember.*, LearnerGroupMemberReplicate.* FROM LearnerGroupMember LEFT JOIN LearnerGroupMemberReplicate ON LearnerGroupMemberReplicate.lgmPk = LearnerGroupMember.learnerGroupMemberUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS GroupLearningSessionReplicate ( glsPk BIGINT NOT NULL, glsVersionId BIGINT NOT NULL DEFAULT 0, glsDestination BIGINT NOT NULL, glsPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (glsPk, glsDestination)) "
        _stmtList +=
            " CREATE INDEX index_GroupLearningSessionReplicate_glsPk_glsDestination_glsVersionId ON GroupLearningSessionReplicate (glsPk, glsDestination, glsVersionId) "
        _stmtList +=
            " CREATE INDEX index_GroupLearningSessionReplicate_glsDestination_glsPending ON GroupLearningSessionReplicate (glsDestination, glsPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_302_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (302, NEW.groupLearningSessionUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_302_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_302_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_302_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (302, OLD.groupLearningSessionUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_302_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_302_fn(); "
        _stmtList +=
            "CREATE VIEW GroupLearningSession_ReceiveView AS  SELECT GroupLearningSession.*, GroupLearningSessionReplicate.* FROM GroupLearningSession LEFT JOIN GroupLearningSessionReplicate ON GroupLearningSessionReplicate.glsPk = GroupLearningSession.groupLearningSessionUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS SiteTermsReplicate ( stPk BIGINT NOT NULL, stVersionId BIGINT NOT NULL DEFAULT 0, stDestination BIGINT NOT NULL, stPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (stPk, stDestination)) "
        _stmtList +=
            " CREATE INDEX index_SiteTermsReplicate_stPk_stDestination_stVersionId ON SiteTermsReplicate (stPk, stDestination, stVersionId) "
        _stmtList +=
            " CREATE INDEX index_SiteTermsReplicate_stDestination_stPending ON SiteTermsReplicate (stDestination, stPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_272_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (272, NEW.sTermsUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_272_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_272_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_272_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (272, OLD.sTermsUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_272_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_272_fn(); "
        _stmtList +=
            "CREATE VIEW SiteTerms_ReceiveView AS  SELECT SiteTerms.*, SiteTermsReplicate.* FROM SiteTerms LEFT JOIN SiteTermsReplicate ON SiteTermsReplicate.stPk = SiteTerms.sTermsUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzContentJoinReplicate ( ccjPk BIGINT NOT NULL, ccjVersionId BIGINT NOT NULL DEFAULT 0, ccjDestination BIGINT NOT NULL, ccjPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (ccjPk, ccjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzContentJoinReplicate_ccjPk_ccjDestination_ccjVersionId ON ClazzContentJoinReplicate (ccjPk, ccjDestination, ccjVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzContentJoinReplicate_ccjDestination_ccjPending ON ClazzContentJoinReplicate (ccjDestination, ccjPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_134_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (134, NEW.ccjUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_134_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_134_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_134_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (134, OLD.ccjUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_134_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_134_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzContentJoin_ReceiveView AS  SELECT ClazzContentJoin.*, ClazzContentJoinReplicate.* FROM ClazzContentJoin LEFT JOIN ClazzContentJoinReplicate ON ClazzContentJoinReplicate.ccjPk = ClazzContentJoin.ccjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonParentJoinReplicate ( ppjPk BIGINT NOT NULL, ppjVersionId BIGINT NOT NULL DEFAULT 0, ppjDestination BIGINT NOT NULL, ppjPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (ppjPk, ppjDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonParentJoinReplicate_ppjPk_ppjDestination_ppjVersionId ON PersonParentJoinReplicate (ppjPk, ppjDestination, ppjVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonParentJoinReplicate_ppjDestination_ppjPending ON PersonParentJoinReplicate (ppjDestination, ppjPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_512_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (512, NEW.ppjUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_512_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_512_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_512_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (512, OLD.ppjUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_512_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_512_fn(); "
        _stmtList +=
            "CREATE VIEW PersonParentJoin_ReceiveView AS  SELECT PersonParentJoin.*, PersonParentJoinReplicate.* FROM PersonParentJoin LEFT JOIN PersonParentJoinReplicate ON PersonParentJoinReplicate.ppjPk = PersonParentJoin.ppjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ScopedGrantReplicate ( sgPk BIGINT NOT NULL, sgVersionId BIGINT NOT NULL DEFAULT 0, sgDestination BIGINT NOT NULL, sgPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (sgPk, sgDestination)) "
        _stmtList +=
            " CREATE INDEX index_ScopedGrantReplicate_sgPk_sgDestination_sgVersionId ON ScopedGrantReplicate (sgPk, sgDestination, sgVersionId) "
        _stmtList +=
            " CREATE INDEX index_ScopedGrantReplicate_sgDestination_sgPending ON ScopedGrantReplicate (sgDestination, sgPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_48_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (48, NEW.sgUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_48_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_48_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_48_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (48, OLD.sgUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_48_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_48_fn(); "
        _stmtList +=
            "CREATE VIEW ScopedGrant_ReceiveView AS  SELECT ScopedGrant.*, ScopedGrantReplicate.* FROM ScopedGrant LEFT JOIN ScopedGrantReplicate ON ScopedGrantReplicate.sgPk = ScopedGrant.sgUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION sg_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ScopedGrant(sgUid, sgPcsn, sgLcsn, sgLcb, sgLct, sgTableId, sgEntityUid, sgPermissions, sgGroupUid, sgIndex, sgFlags) VALUES (NEW.sgUid, NEW.sgPcsn, NEW.sgLcsn, NEW.sgLcb, NEW.sgLct, NEW.sgTableId, NEW.sgEntityUid, NEW.sgPermissions, NEW.sgGroupUid, NEW.sgIndex, NEW.sgFlags) ON CONFLICT(sgUid) DO UPDATE SET sgLct = EXCLUDED.sgLct, sgPermissions = EXCLUDED.sgPermissions ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER sg_remote_insert_trig INSTEAD OF INSERT ON ScopedGrant_ReceiveView FOR EACH ROW EXECUTE PROCEDURE sg_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ErrorReportReplicate ( erPk BIGINT NOT NULL, erVersionId BIGINT NOT NULL DEFAULT 0, erDestination BIGINT NOT NULL, erPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (erPk, erDestination)) "
        _stmtList +=
            " CREATE INDEX index_ErrorReportReplicate_erPk_erDestination_erVersionId ON ErrorReportReplicate (erPk, erDestination, erVersionId) "
        _stmtList +=
            " CREATE INDEX index_ErrorReportReplicate_erDestination_erPending ON ErrorReportReplicate (erDestination, erPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_419_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (419, NEW.errUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_419_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_419_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_419_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (419, OLD.errUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_419_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_419_fn(); "
        _stmtList +=
            "CREATE VIEW ErrorReport_ReceiveView AS  SELECT ErrorReport.*, ErrorReportReplicate.* FROM ErrorReport LEFT JOIN ErrorReportReplicate ON ErrorReportReplicate.erPk = ErrorReport.errUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzAssignmentReplicate ( caPk BIGINT NOT NULL, caVersionId BIGINT NOT NULL DEFAULT 0, caDestination BIGINT NOT NULL, caPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (caPk, caDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentReplicate_caPk_caDestination_caVersionId ON ClazzAssignmentReplicate (caPk, caDestination, caVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentReplicate_caDestination_caPending ON ClazzAssignmentReplicate (caDestination, caPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_520_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (520, NEW.caUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_520_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_520_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_520_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (520, OLD.caUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_520_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_520_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzAssignment_ReceiveView AS  SELECT ClazzAssignment.*, ClazzAssignmentReplicate.* FROM ClazzAssignment LEFT JOIN ClazzAssignmentReplicate ON ClazzAssignmentReplicate.caPk = ClazzAssignment.caUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS ClazzAssignmentContentJoinReplicate ( cacjPk BIGINT NOT NULL, cacjVersionId BIGINT NOT NULL DEFAULT 0, cacjDestination BIGINT NOT NULL, cacjPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (cacjPk, cacjDestination)) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentContentJoinReplicate_cacjPk_cacjDestination_cacjVersionId ON ClazzAssignmentContentJoinReplicate (cacjPk, cacjDestination, cacjVersionId) "
        _stmtList +=
            " CREATE INDEX index_ClazzAssignmentContentJoinReplicate_cacjDestination_cacjPending ON ClazzAssignmentContentJoinReplicate (cacjDestination, cacjPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_521_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (521, NEW.cacjUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_521_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_521_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_521_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (521, OLD.cacjUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_521_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_521_fn(); "
        _stmtList +=
            "CREATE VIEW ClazzAssignmentContentJoin_ReceiveView AS  SELECT ClazzAssignmentContentJoin.*, ClazzAssignmentContentJoinReplicate.* FROM ClazzAssignmentContentJoin LEFT JOIN ClazzAssignmentContentJoinReplicate ON ClazzAssignmentContentJoinReplicate.cacjPk = ClazzAssignmentContentJoin.cacjUid "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS PersonAuth2Replicate ( paPk BIGINT NOT NULL, paVersionId BIGINT NOT NULL DEFAULT 0, paDestination BIGINT NOT NULL, paPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (paPk, paDestination)) "
        _stmtList +=
            " CREATE INDEX index_PersonAuth2Replicate_paPk_paDestination_paVersionId ON PersonAuth2Replicate (paPk, paDestination, paVersionId) "
        _stmtList +=
            " CREATE INDEX index_PersonAuth2Replicate_paDestination_paPending ON PersonAuth2Replicate (paDestination, paPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_678_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (678, NEW.pauthUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_678_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_678_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_678_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (678, OLD.pauthUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_678_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_678_fn(); "
        _stmtList +=
            "CREATE VIEW PersonAuth2_ReceiveView AS  SELECT PersonAuth2.*, PersonAuth2Replicate.* FROM PersonAuth2 LEFT JOIN PersonAuth2Replicate ON PersonAuth2Replicate.paPk = PersonAuth2.pauthUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION personauth2_remote_insert_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO PersonAuth2(pauthUid, pauthMechanism, pauthAuth, pauthLcsn, pauthPcsn, pauthLcb, pauthLct) VALUES (NEW.pauthUid, NEW.pauthMechanism, NEW.pauthAuth, NEW.pauthLcsn, NEW.pauthPcsn, NEW.pauthLcb, NEW.pauthLct) ON CONFLICT (pauthUid) DO UPDATE SET pauthMechanism = EXCLUDED.pauthMechanism, pauthAuth = EXCLUDED.pauthAuth, pauthLcsn = EXCLUDED.pauthLcsn, pauthPcsn = EXCLUDED.pauthPcsn, pauthLcb = EXCLUDED.pauthLcb, pauthLct = EXCLUDED.pauthLct ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER personauth2_remote_insert_trig INSTEAD OF INSERT ON PersonAuth2_ReceiveView FOR EACH ROW EXECUTE PROCEDURE personauth2_remote_insert_fn() "
        _stmtList +=
            " CREATE TABLE IF NOT EXISTS UserSessionReplicate ( usPk BIGINT NOT NULL, usVersionId BIGINT NOT NULL DEFAULT 0, usDestination BIGINT NOT NULL, usPending BOOL NOT NULL DEFAULT true, PRIMARY KEY (usPk, usDestination)) "
        _stmtList +=
            " CREATE INDEX index_UserSessionReplicate_usPk_usDestination_usVersionId ON UserSessionReplicate (usPk, usDestination, usVersionId) "
        _stmtList +=
            " CREATE INDEX index_UserSessionReplicate_usDestination_usPending ON UserSessionReplicate (usDestination, usPending) "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_upd_679_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (679, NEW.usUid, 1) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 1; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_upd_679_trig AFTER UPDATE OR INSERT ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_upd_679_fn(); "
        _stmtList +=
            " CREATE OR REPLACE FUNCTION ch_del_679_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO ChangeLog(chTableId, chEntityPk, chType) VALUES (679, OLD.usUid, 2) ON CONFLICT(chTableId, chEntityPk) DO UPDATE SET chType = 2; RETURN NULL; END ${'$'}${'$'} LANGUAGE plpgsql "
        _stmtList +=
            " CREATE TRIGGER ch_del_679_trig AFTER DELETE ON RepEntity FOR EACH ROW EXECUTE PROCEDURE ch_del_679_fn(); "
        _stmtList +=
            "CREATE VIEW UserSession_ReceiveView AS  SELECT UserSession.*, UserSessionReplicate.* FROM UserSession LEFT JOIN UserSessionReplicate ON UserSessionReplicate.usPk = UserSession.usUid "
        _stmtList +=
            "CREATE OR REPLACE FUNCTION usersession_remote_ins_fn() RETURNS TRIGGER AS ${'$'}${'$'} BEGIN INSERT INTO UserSession(usUid, usPcsn, usLcsn, usLcb, usLct, usPersonUid, usClientNodeId, usStartTime, usEndTime, usStatus, usReason, usAuth, usSessionType) VALUES (NEW.usUid, NEW.usPcsn, NEW.usLcsn, NEW.usLcb, NEW.usLct, NEW.usPersonUid, NEW.usClientNodeId, NEW.usStartTime, NEW.usEndTime, NEW.usStatus, NEW.usReason, NEW.usAuth, NEW.usSessionType) /*postgres ON CONFLICT (usUid) DO UPDATE SET usStatus = EXCLUDED.usStatus, usEndTime = EXCLUDED.usEndTime, usReason = EXCLUDED.usReason */ ; IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN RETURN NEW; ELSE RETURN OLD; END IF; END ${'$'}${'$'} LANGUAGE plpgsql"
        _stmtList +=
            " CREATE TRIGGER usersession_remote_ins_trig INSTEAD OF INSERT ON UserSession_ReceiveView FOR EACH ROW EXECUTE PROCEDURE usersession_remote_ins_fn() "
    }
    db.execSqlBatch(_stmtList.toTypedArray())
}


val UmAppDatabaseReplicationMigration91_92  = DoorMigrationSync(91, 92){ db ->
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
    }else {
        db.dropOldPostgresTriggers()
        db.dropOldPostgresFunctions()

        db.execSQL("CREATE TABLE IF NOT EXISTS ChangeLog (  chTableId  INTEGER  NOT NULL , chEntityPk  BIGINT  NOT NULL , chType  INTEGER  NOT NULL , PRIMARY KEY (chTableId, chEntityPk) )")

        db.execSQL("CREATE TABLE IF NOT EXISTS ReplicationStatus (  tableId  INTEGER  NOT NULL , priority  INTEGER  NOT NULL , nodeId  BIGINT  NOT NULL , lastRemoteChangeTime  BIGINT  NOT NULL , lastFetchReplicationCompleteTime  BIGINT  NOT NULL , lastLocalChangeTime  BIGINT  NOT NULL , lastSendReplicationCompleteTime  BIGINT  NOT NULL , repStatusId  SERIAL  PRIMARY KEY  NOT NULL )")
        db.execSQL("CREATE UNIQUE INDEX table_node_idx ON ReplicationStatus (tableId, nodeId)")
    }

    db.addReplicationEntities()
}