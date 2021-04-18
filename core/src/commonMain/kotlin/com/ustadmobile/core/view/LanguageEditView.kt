package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Language


interface LanguageEditView: UstadEditView<Language> {

    var langNameError: String?

    companion object {

        const val VIEW_NAME = "LanguageEditEditView"

    }

}