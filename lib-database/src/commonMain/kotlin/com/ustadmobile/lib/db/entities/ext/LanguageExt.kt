package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.Language

@ShallowCopy
expect fun Language.shallowCopy(
    block: Language.() -> Unit,
): Language