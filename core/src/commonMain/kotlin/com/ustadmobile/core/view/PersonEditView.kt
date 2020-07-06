package com.ustadmobile.core.view

import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazz
import com.ustadmobile.lib.db.entities.Person

interface PersonEditView: UstadEditView<Person> {

    var genderOptions: List<MessageIdOption>?

    var personPicturePath: String?

    var clazzList: DoorLiveData<List<ClazzMemberWithClazz>>?

    companion object {

        const val VIEW_NAME = "PersonEditView"

    }

}