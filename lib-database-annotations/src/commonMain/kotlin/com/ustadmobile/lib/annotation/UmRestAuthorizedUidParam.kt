package com.ustadmobile.lib.database.annotation


/**
 * Indicates that the given parameter represents the person uid for the current, authorized user.
 * If this query is being sent over the network, we must validate their authorization.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class UmRestAuthorizedUidParam(val headerName: String = "X-Auth-Token")
