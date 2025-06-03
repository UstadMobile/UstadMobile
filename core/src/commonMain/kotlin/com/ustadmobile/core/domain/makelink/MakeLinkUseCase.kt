package com.ustadmobile.core.domain.makelink

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.toQueryString

/**
 * Create a link for a given destination and endpoint that can be shared. Links are always in the
 * form of:
 *
 * http(s)://endpoint/divider/#/DestName?arg1=value1&...
 *
 */
class MakeLinkUseCase(
    private val learningSpace: LearningSpace,
) {

    operator fun invoke(destName: String, args: Map<String, String>): String {
        return (UMFileUtil.joinPaths(
            learningSpace.url,
            UstadMobileSystemCommon.LINK_ENDPOINT_VIEWNAME_DIVIDER
        ) + destName).appendQueryArgs(args.toQueryString())
    }

}