package com.ustadmobile.core.schedule

interface ClazzLogCreatorManager {

    fun requestClazzLogCreation(clazzUidFilter: Long, endpointUrl: String, fromTime: Long, toTime: Long)

}