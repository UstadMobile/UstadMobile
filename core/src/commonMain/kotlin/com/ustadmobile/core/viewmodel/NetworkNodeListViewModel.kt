package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.DeviceSession

data class NetworkNodeListUiState(

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = 0,

    val deviceFilterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MessageID.all, 0),
        MessageIdOption2(MessageID.clients, 2),
        MessageIdOption2(MessageID.server, 1),
),

    val deviceName: String = "",

    val wifiSSID: String = "",

    val devices: List<DeviceSession> = emptyList()

)