package com.ustadmobile.core.domain.compress.list

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressProgressUpdate
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.domain.compress.audio.CompressAudioUseCase
import com.ustadmobile.core.domain.compress.image.CompressImageUseCase
import com.ustadmobile.core.domain.compress.video.CompressVideoUseCase
import com.ustadmobile.core.io.ext.toDoorUri
import com.ustadmobile.core.util.ext.fileExtensionOrNull
import com.ustadmobile.libcache.headers.MimeTypeHelper
import com.ustadmobile.libcache.uuid.randomUuid
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * Use case that will go through a list of items and compress all those that can be compressed. This
 * is useful when importing zip-based formats (e.g. xAPI, H5P, etc) to compress embedded media (e.g.
 * videos, audio, images, etc). This is not intended to handle zip compression itself (which is done
 * by the cache) e.g. gzip etc. for css, html, javascript and other text files.
 */
class CompressListUseCase(
    private val compressVideoUseCase: CompressVideoUseCase?,
    private val compressImageUseCase: CompressImageUseCase?,
    private val compressAudioUseCase: CompressAudioUseCase? = null,
    private val mimeTypeHelper: MimeTypeHelper,
    private val filesystem: FileSystem = SystemFileSystem,
) {

    data class ItemToCompress(
        val path: Path,
        val name: String,
        val mimeType: String?
    )

    data class ItemResult(
        val originalItem: ItemToCompress,
        val compressedResult: CompressResult?,
    ) {

        val localUri: String
            get() = compressedResult?.uri ?: originalItem.path.toDoorUri().toString()

        val mimeType: String?
            get() = compressedResult?.mimeType ?: originalItem.mimeType

    }


    suspend operator fun invoke(
        items: List<ItemToCompress>,
        params: CompressParams,
        workDir: Path,
        onProgress: CompressUseCase.OnCompressProgress? = null,
    ) : List<ItemResult> {

        val sizes = items.associate {
            Pair(it.path.toString(),  filesystem.metadataOrNull(it.path)?.size ?: 0)
        }
        val totalSize = sizes.values.sum()

        var completedItemsSize = 0L

        //Do not compress
        if(params.compressionLevel == CompressionLevel.NONE) {
            return items.map {
                ItemResult(
                    originalItem = it,
                    compressedResult = null,
                )
            }
        }

        val results = items.map { item ->
            val mimeType = item.mimeType ?: item.name.fileExtensionOrNull()?.let {
                mimeTypeHelper.guessByExtension(it)
            }

            val compressor = when {
                mimeType != null && mimeType.startsWith("video/") -> {
                    compressVideoUseCase
                }
                mimeType != null && mimeType.startsWith("image/") && mimeType != "image/svg+xml"-> {
                    compressImageUseCase
                }
                mimeType != null && mimeType.startsWith("audio/") && mimeType != "audio/midi" -> {
                    compressAudioUseCase
                }

                else -> null
            }

            val compressResult = if(compressor != null) {
                val toPath = Path(workDir, randomUuid())
                compressor(
                    fromUri = item.path.toDoorUri().toString(),
                    toUri = toPath.toDoorUri().toString(),
                    params = params,
                    onProgress = {
                        onProgress?.invoke(
                            CompressProgressUpdate(
                                fromUri = "",
                                completed = completedItemsSize + it.completed,
                                total = totalSize
                            )
                        )
                    }
                )
            }else {
                null
            }

            completedItemsSize += (sizes[item.path.toString()] ?: 0L)
            onProgress?.invoke(
                CompressProgressUpdate(
                    fromUri = "",
                    completed = completedItemsSize,
                    total = totalSize,
                )
            )

            ItemResult(
                originalItem = item,
                compressedResult = compressResult,
            )
        }

        return results
    }
}