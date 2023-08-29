package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ListFilterIdOption
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * Utility extension function to convert a list of pairs to a ListFilterIdOption by resolving
 * the MR.strings.to a string
 *
 * @receiver a list of Pairs where the first item is an Int representing the message id, and the second
 * item is an int representing the optionId
 */
fun List<Pair<Int, Int>>.toListFilterOptions(context: Any, di: DI) : List<ListFilterIdOption> {
    val systemImpl : UstadMobileSystemImpl = di.direct.instance()
    return map {
        ListFilterIdOption(systemImpl.getString(it.first, context), it.second)
    }
}