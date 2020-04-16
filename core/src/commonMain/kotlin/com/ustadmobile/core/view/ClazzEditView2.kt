package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Schedule

interface ClazzEditView2: UstadViewWithProgress {

    var clazzSchedules: DoorMutableLiveData<List<Schedule>>?

    var clazz: Clazz?

    var fieldsEnabled: Boolean

}