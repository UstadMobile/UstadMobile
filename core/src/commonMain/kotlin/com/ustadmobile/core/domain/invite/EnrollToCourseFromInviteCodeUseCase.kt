package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.util.UMFileUtil

class EnrollToCourseFromInviteCodeUseCase(
    private val clazzInviteRedeemUseCase: ClazzInviteRedeemUseCase
) {
    suspend operator fun invoke(
        viewUri:String,
        personUid:Long
    ){

            val questionIndex = viewUri.indexOf('?')
            val args = if(questionIndex > 0) {
                UMFileUtil.parseURLQueryString(viewUri.substring(questionIndex))
            }else {
                emptyMap()
            }
            clazzInviteRedeemUseCase(
                inviteCode = args.values.first(),
                isAccepting = true,
                personUid = personUid
            )
    }
}