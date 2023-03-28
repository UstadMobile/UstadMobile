package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.DeviceSession

data class NetworkNodeListUiState(

    val selectedChipId: Int = 7,

    val fieldsEnabled: Boolean = true,

    val deviceFilterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MessageID.all, 7),
    ),

    val devices: List<DeviceSession> = emptyList()

)