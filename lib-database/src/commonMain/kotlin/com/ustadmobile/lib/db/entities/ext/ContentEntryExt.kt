package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ContentEntry

@ShallowCopy
expect fun ContentEntry.shallowCopy(
    block: ContentEntry.() -> Unit,
): ContentEntry
