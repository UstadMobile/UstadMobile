package com.ustadmobile.core.util.ext

import com.ustadmobile.core.db.dao.TimeZoneEntityDao

/**
 * This is done as an extension function expect-actual because it depends on the underlying system
 * to access a list of timezones.
 */
actual fun TimeZoneEntityDao.insertSystemTimezones() {
}