package com.ustadmobile.core.account

import com.ustadmobile.lib.db.entities.PersonParentJoin

data class AccountRegisterOptions(var makeAccountActive: Boolean = true, var parentJoin: PersonParentJoin? = null)
