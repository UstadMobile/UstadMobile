package com.ustadmobile.util.ext

import kotlinext.js.getOwnPropertyNames

/**
 * Like Object.assign, but with a filter so that some properties can be excluded. Will assign all own
 * property names that match the filter.
 */
fun Any.assignPropsTo(
    receiver: Any,
    filter: (String) -> Boolean = { true }
) {
    val thisDynamic = this.asDynamic()
    val receiverDynamic = receiver.asDynamic()
    this.getOwnPropertyNames().filter(filter).forEach { propName ->
        receiverDynamic[propName] = thisDynamic[propName]
    }
}
