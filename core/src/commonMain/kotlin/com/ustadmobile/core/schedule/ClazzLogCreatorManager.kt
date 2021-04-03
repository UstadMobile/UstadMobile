package com.ustadmobile.core.schedule

interface ClazzLogCreatorManager {

    fun requestClazzLogCreation(clazzUid: Long, endpointUrl: String, fromTime: Long, toTime: Long)

}