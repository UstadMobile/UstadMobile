package com.ustadmobile.util.test.ext

import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import javax.naming.InitialContext



fun InitialContext.bindJndiForActiveEndpoint(url: String) {
    val dbNameSanitized = sanitizeDbNameFromUrl(url)
    bindNewSqliteDataSourceIfNotExisting(dbNameSanitized)
}
