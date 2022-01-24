package com.ustadmobile.core.schedule

interface ClazzLogCreatorManager {

    fun requestClazzLogCreation(clazzUidFilter: Long, endpointUrl: String, fromTime: Long, toTime: Long)


    companion object {


        const val DAY_IN_MS = (1000 * 60 * 60 * 24)

        const val INPUT_ENDPOINTURL = "dbName"

        const val INPUT_FROMTIME = "fromTime"

        const val INPUT_TOTIME = "toTime"

        const val INPUT_CLAZZUID = "clazzUidFilter"


    }

}