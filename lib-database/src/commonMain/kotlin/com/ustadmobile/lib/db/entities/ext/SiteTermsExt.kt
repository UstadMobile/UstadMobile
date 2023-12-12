package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.SiteTerms

@ShallowCopy
expect fun SiteTerms.shallowCopy(
    block: SiteTerms.() -> Unit
): SiteTerms
