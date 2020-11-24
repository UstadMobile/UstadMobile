package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.PersonGroupWithMemberCount

interface PersonGroupListItemListener {

    fun onClickGroup(group: PersonGroupWithMemberCount)

}