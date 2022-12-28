package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.Site

@ShallowCopy
expect fun Site.shallowCopy(
    block: Site.() -> Unit,
): Site