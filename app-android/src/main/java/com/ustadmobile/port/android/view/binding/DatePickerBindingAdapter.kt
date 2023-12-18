package com.ustadmobile.port.android.view.binding


/**
 * Shorthand to check if this Long represents a date that has really been set by the user, or is just
 * a default. 0 and Long.MAX_VALUE are reserved defaults. MAX_VALUE is used for end times to simplify
 * queries.
 */
val Long.isSet: Boolean
    get() = this != 0L && this != Long.MAX_VALUE
