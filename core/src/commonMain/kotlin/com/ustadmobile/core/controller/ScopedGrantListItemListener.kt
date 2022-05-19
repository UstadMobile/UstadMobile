package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.ScopedGrantWithName

interface ScopedGrantListItemListener {

    fun onClickScopedGrant(scopedGrant: ScopedGrantWithName)

}