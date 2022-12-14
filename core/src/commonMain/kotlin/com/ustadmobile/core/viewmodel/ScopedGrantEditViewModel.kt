package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.lib.db.entities.ScopedGrant

data class ScopedGrantEditUiState(

    val entity: ScopedGrant? = null,

    val bitmaskList: List<BitmaskFlag> = listOf()
)