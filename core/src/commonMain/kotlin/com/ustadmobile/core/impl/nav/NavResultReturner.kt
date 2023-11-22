package com.ustadmobile.core.impl.nav

import kotlinx.coroutines.flow.Flow

/**
 * NavResultReturner is responsible for "returning" results via the Navigation e.g. where one screen
 * returns a result to another screen.
 *
 * See CODING-STYLE.md file for a description of how this works.
 */
interface NavResultReturner {

    fun resultFlowForKey(key: String): Flow<NavResult>

    fun sendResult(result: NavResult)

}