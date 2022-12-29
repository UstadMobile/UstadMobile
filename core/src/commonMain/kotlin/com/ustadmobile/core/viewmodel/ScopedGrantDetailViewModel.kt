package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.lib.db.entities.ScopedGrant

data class ScopedGrantDetailUiState(
    val entity: ScopedGrant? = null,
    val bitmaskList: List<BitmaskFlag> = emptyList()
)