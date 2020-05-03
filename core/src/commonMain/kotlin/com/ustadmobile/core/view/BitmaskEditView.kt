package com.ustadmobile.core.view

import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.LongWrapper
import com.ustadmobile.door.DoorLiveData


interface BitmaskEditView: UstadEditView<LongWrapper> {

    var bitmaskList: DoorLiveData<List<BitmaskFlag>>?

    companion object {

        const val VIEW_NAME = "BitmaskEditView"

    }

}