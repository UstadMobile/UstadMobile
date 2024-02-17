package com.ustadmobile.libcache

import kotlinx.io.files.Path

/**
 * Paths under which response body data will be stored
 *
 * Note: All paths provided should be on the same medium e.g. it should be possible to move a file
 *       from tmpWorkPath to persistentPath or cachePath without needing to copy the data.
 *
 * @param tmpWorkPath a directory where temporary  work will be performed if needed e.g. copying to
 *        read the SHA-256 sum, compressing, etc. This is a transitory location but should be persistent.
 * @param persistentPath a directory under which entries will be stored if they are locked as to
 *        be retained.
 * @param cachePath a directory under which entries will be stored when they are not marked as to
 *        be retained. This is normally defined by the operating system e.g. Android has a specific
 *        cache directory for each app. This allows the user to see how much space is used for
 *        caching vs. saved content.
 */
data class CachePaths(
    val tmpWorkPath: Path,
    val persistentPath: Path,
    val cachePath: Path
)