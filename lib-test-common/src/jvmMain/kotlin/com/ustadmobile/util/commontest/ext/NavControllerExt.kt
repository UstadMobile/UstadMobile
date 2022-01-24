package com.ustadmobile.util.commontest.ext

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.impl.nav.UstadNavController
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import kotlin.reflect.KClass

/**
 * Utility function designed to make it easier to get a result that was saved when a presenter
 * being tested used finishWithResult.
 *
 * @param timeout The timeout to wait for the navigation that saves the result to happen
 * @param resultViewName to viewname to which the result is being saved (eg. as per
 * ARG_RESULT_DEST_VIEWNAME)
 * @param resultKey the key to which the result is being saved (e.g. as per ARG_RESULT_DEST_KEY)
 * @param resultClass Class object for the result type that will be used to deserialize it with Gson
 */
fun <T: Any> UstadNavController.awaitResult(timeout: Long,
                                            resultClass: KClass<T>,
                                            resultViewName: String,
                                            resultKey: String) : List<T> {
    verify(this, timeout(timeout)).popBackStack(eq(resultViewName), eq(false))
    val resultStr = currentBackStackEntry?.savedStateHandle?.get<String>(resultKey)
        ?: throw IllegalStateException("awaitResult: looking for $resultKey key on $resultViewName, " +
                "no current back stack entry or result is null")

    val gson: Gson = (this as DIAware).di.direct.instance()
    return gson.fromJson(resultStr, TypeToken.getParameterized(List::class.java,
        resultClass.java).type)

}