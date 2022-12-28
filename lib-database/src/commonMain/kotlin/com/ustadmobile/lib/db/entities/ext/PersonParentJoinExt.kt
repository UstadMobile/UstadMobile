package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.PersonParentJoin

@ShallowCopy
expect fun PersonParentJoin.shallowCopy(
    block: PersonParentJoin.() -> Unit,
): PersonParentJoin