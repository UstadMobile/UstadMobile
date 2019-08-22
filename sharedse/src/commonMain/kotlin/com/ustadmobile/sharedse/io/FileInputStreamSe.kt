package com.ustadmobile.sharedse.io

import kotlinx.io.InputStream

expect open class FileInputStreamSe : InputStream {

    constructor(file: FileSe)

}
