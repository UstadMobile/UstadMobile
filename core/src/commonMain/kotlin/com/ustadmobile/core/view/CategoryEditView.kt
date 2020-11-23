package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Category


interface CategoryEditView: UstadEditView<Category> {

    companion object {

        const val VIEW_NAME = "CategoryEditView"

    }

}