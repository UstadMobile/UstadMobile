package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin

@ShallowCopy
expect fun PersonWithPersonParentJoin.shallowCopy(
    block: PersonWithPersonParentJoin.() -> Unit,
): PersonWithPersonParentJoin
