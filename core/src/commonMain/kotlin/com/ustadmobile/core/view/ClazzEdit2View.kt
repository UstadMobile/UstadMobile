package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Schedule

interface ClazzEdit2View: UstadEditView<Clazz> {

    var clazzSchedules: DoorMutableLiveData<List<Schedule>>?

    companion object {

        const val VIEW_NAME = "ClazzEdit2"

    }

}