/*
 * Copyright (c) 2016—2017 Andrei Tomashpolskiy and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ustadmobile.core.torrent

import bt.data.StorageUnit
import bt.data.file.FileCacheKey
import bt.data.file.FileSystemStorageUnit
import bt.data.file.OpenFileCache
import bt.net.buffer.ByteBufferView
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.Path

class SharedFileSystemStorageUnit private constructor(private val cache: OpenFileCache, private val file: Path, private val capacity: Long) : StorageUnit {

    private val key: FileCacheKey = FileCacheKey(file, capacity)
    private val sbc: SeekableByteChannel? = null

    internal constructor(cache: OpenFileCache, root: Path, path: String?, capacity: Long) : this(cache, root.resolve(path), capacity) {}

    constructor(cache: OpenFileCache, file: Path) : this(cache, file, getSize(file)) {}

    override fun readBlock(buffer: ByteBuffer, offset: Long): Int {
        return if (!cache.existsOnFileSystem(key)) {
            -1
        } else cache.readBlock(key, buffer, offset)
    }

    override fun readBlockFully(buffer: ByteBuffer, offset: Long) {
        cache.readBlockFully(key, buffer, offset)
    }

    override fun writeBlock(buffer: ByteBuffer, offset: Long): Int {
        return cache.writeBlock(key, buffer, offset)
    }

    override fun writeBlockFully(buffer: ByteBuffer, offset: Long) {
        cache.writeBlockFully(key, buffer, offset)
    }

    override fun writeBlock(buffer: ByteBufferView, offset: Long): Int {
        return cache.writeBlock(key, buffer, offset)
    }

    override fun writeBlockFully(buffer: ByteBufferView, offset: Long) {
        cache.writeBlockFully(key, buffer, offset)
    }

    override fun capacity(): Long {
        return capacity
    }

    override fun size(): Long {
        return getSize(file)
    }

    override fun toString(): String {
        return "($capacity B) $file"
    }

    @Throws(IOException::class)
    override fun close() {
        cache.close(key)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(FileSystemStorageUnit::class.java)

        /**
         * Reads the size of the passed in file
         *
         * @param file the file to read the size of
         * @return the file size
         */
        private fun getSize(file: Path): Long {
            return try {
                if (Files.exists(file)) Files.size(file) else 0
            } catch (e: IOException) {
                throw UncheckedIOException("Unexpected I/O error", e)
            }
        }
    }

}