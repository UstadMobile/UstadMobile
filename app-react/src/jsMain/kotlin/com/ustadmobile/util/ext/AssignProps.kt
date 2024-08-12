package com.ustadmobile.util.ext

import js.objects.Object.Companion.getOwnPropertyNames

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
    getOwnPropertyNames(this).filter(filter).forEach { propName ->
        receiverDynamic[propName] = thisDynamic[propName]
    }
}
