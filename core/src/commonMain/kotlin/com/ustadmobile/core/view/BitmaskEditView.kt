package com.ustadmobile.core.view

import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.LongWrapper
import com.ustadmobile.door.lifecycle.LiveData


interface BitmaskEditView: UstadEditView<LongWrapper> {

    var bitmaskList: LiveData<List<BitmaskFlag>>?

    companion object {

        const val VIEW_NAME = "BitmaskEditView"

    }

}