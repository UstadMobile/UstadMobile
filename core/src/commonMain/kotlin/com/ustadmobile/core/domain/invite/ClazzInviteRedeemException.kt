package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.util.ExceptionWithStringResource
import dev.icerock.moko.resources.StringResource

class ClazzInviteRedeemException(
    message: String,
    override val stringResource: StringResource,
): Exception(message), ExceptionWithStringResource
