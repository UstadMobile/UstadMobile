package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.PersonGroupWithMemberCount

interface PersonGroupListView: UstadListView<PersonGroup, PersonGroupWithMemberCount> {

    companion object {
        const val VIEW_NAME = "PersonGroupListView"
    }

}