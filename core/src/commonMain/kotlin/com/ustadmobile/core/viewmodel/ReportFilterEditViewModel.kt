package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.UidAndLabel

data class ReportFilterEditUiState(

    val fieldError: String? = null,

    val conditionsError: String? = null,

    val valuesError: String? = null,

    val fieldsEnabled: Boolean = true,

    var reportFilter: ReportFilter? = null,

    var uidAndLabelList: List<UidAndLabel> = emptyList(),

    val createNewFilter: String = "",

    val reportFilterValueVisible: Boolean = true,

    val reportFilterBetweenValueVisible: Boolean = true,

    val reportFilterUidAndLabelListVisible: Boolean = true

)