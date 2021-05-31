package com.ustadmobile.core.util

import com.ustadmobile.lib.db.entities.ScopedGrantAndName

interface ScopedGrantOneToManyListener {

    fun onClickAddNewScopedGrant()

    fun onClickEditScopedGrant(scopedGrantAndName: ScopedGrantAndName)

    fun onClickDeleteScopedGrant(scopedGrantAndName: ScopedGrantAndName)

}