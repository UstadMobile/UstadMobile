package com.ustadmobile.core.util.isimplerequest

import com.ustadmobile.core.util.stringvalues.IStringValues

interface ISimpleTextRequest {

    val headers: IStringValues

    val method: String

    val path: String

    val body: String?

}