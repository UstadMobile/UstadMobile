package com.ustadmobile.libcache.io

import kotlinx.io.RawSource
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

/**
 * Get a specific range from the given path
 *
 * @param path the path to get a Source for as per FileSystem.source
 * @param fromByte the first byte to include (inclusive)
 * @param toByte the last byte to include (INCLUSIVE as per HTTP range requests)
 *
 * @return RawSource
 */
expect fun FileSystem.rangeSource(path: Path, fromByte: Long, toByte: Long): RawSource

/**
 * Get the last modified time on a file. Not currently included in metadata
 */
expect fun FileSystem.lastModified(path: Path): Long


