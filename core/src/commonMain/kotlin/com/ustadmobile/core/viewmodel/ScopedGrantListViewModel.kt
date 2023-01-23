package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ScopedGrantWithName

data class ScopedGrantListUiState(

    val scopedGrantList: List<ScopedGrantWithName> = emptyList(),

)