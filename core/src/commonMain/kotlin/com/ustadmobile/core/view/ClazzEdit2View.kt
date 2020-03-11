package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Schedule

interface ClazzEdit2View: UstadViewWithProgress {

    var clazzSchedules: DoorMutableLiveData<List<Schedule>>?

    var clazz: Clazz?

    var fieldsEnabled: Boolean

    var loading: Boolean

    fun finish()

    companion object {

        const val VIEW_NAME = "ClazzEdit2"

    }

}