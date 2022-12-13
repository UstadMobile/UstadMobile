package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.BitmaskFlag

@ShallowCopy
expect fun BitmaskFlag.shallowCopy(
    block: BitmaskFlag.() -> Unit,
): BitmaskFlag