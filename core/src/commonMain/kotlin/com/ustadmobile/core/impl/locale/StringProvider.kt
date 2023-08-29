package com.ustadmobile.core.impl.locale

import dev.icerock.moko.resources.StringResource

interface StringProvider {

    operator fun get(stringResource: StringResource): String

}