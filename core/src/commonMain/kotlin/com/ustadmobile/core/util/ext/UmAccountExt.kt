package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.UmAccount



val UmAccount.userAtServer: String
    get() = "$username@$endpointUrl"

