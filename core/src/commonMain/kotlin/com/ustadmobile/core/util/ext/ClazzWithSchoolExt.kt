package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzWithSchool

fun ClazzWithSchool.findClazzTimeZone(): String{
    if(clazzTimeZone.isNullOrBlank()){
        if(school != null && school?.schoolTimeZone.isNullOrBlank()){
            return "UTC"
        }else{
            return school?.schoolTimeZone?:"UTC"
        }
    }else{
        return clazzTimeZone?:"UTC"
    }
}

fun ClazzWithSchool.effectiveTimeZone() = clazzTimeZone ?: school?.schoolTimeZone ?: "UTC"