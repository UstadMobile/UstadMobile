package com.ustadmobile.core.util.isimplerequest

import com.ustadmobile.core.util.stringvalues.IStringValues

class StringSimpleTextRequest(
    override val headers: IStringValues =IStringValues.empty(),
    override val method: String = "GET",
    override val path: String,
    override val body: String? = null,
): ISimpleTextRequest
