package com.ustadmobile.core.impl.nav

/**
 * Results being "returned" have a destination view naem (to which the navigation will be popped back)
 * and a key (used to pass the data)
 *
 * @param viewName the ViewName that we will pop back to when the result has been selected
 * @param key the key that is used for the NavResultReturner for the screen to collect the result.
 */
class NavResultDest(
    val viewName: String,
    val key: String,
) {
}