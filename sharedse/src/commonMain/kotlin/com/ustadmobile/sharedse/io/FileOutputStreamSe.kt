package com.ustadmobile.sharedse.io

import kotlinx.io.OutputStream

expect class FileOutputStreamSe: OutputStream {

    constructor(file: FileSe)

    constructor(file: FileSe, append: Boolean)

}