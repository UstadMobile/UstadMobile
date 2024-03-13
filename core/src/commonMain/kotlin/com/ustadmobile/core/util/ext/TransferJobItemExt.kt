package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.TransferJobItem

val TransferJobItem.progressAsFloat: Float
    get() = tjTransferred.toFloat() / (tjTotalSize.toFloat().takeIf { it != 0f } ?: 1f)
