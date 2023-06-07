package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.DiscussionPost

@ShallowCopy
expect fun DiscussionPost.shallowCopy(
    block: DiscussionPost.() -> Unit,
): DiscussionPost