package com.ustadmobile.libcache.io

import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

fun FileSystem.requireMetadata(path: Path) = metadataOrNull(path)
    ?: throw IllegalArgumentException("requiremetadata: no metadata for $path")
