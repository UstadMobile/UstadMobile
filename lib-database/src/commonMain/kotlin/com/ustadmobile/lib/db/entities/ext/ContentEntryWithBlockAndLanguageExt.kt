package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.ContentEntryWithBlockAndLanguage

@ShallowCopy
expect fun ContentEntryWithBlockAndLanguage.shallowCopy(
    block: ContentEntryWithBlockAndLanguage.() -> Unit,
): ContentEntryWithBlockAndLanguage