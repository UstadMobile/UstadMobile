package com.ustadmobile.core.util.isimpleresponse

import com.ustadmobile.core.util.stringvalues.IStringValues

class StringSimpleTextResponse(
    override val headers: IStringValues = IStringValues.empty(),

    override val responseCode: Int = 200,

    override val responseMessage: String? = null,

    override val responseBody: String? = null,
): ISimpleTextResponse
