package com.ustadmobile.core.domain.compress

/**
 * Generic Domain UseCase that can be implemented to handle compression of different types.
 */
interface CompressUseCase {

    fun interface OnCompressProgress {

        operator fun invoke(update: CompressProgressUpdate)

    }

    /**
     * @param fromUri the uri of the data to be compressed
     * @param toUri the uri where compressed data should be saved. This is a SUGGESTION, and
     *        compressors might append extensions etc. The CompressResult.toUri return is where
     *        the actual data is.
     * @param params Compression parameters
     * @param onProgress progress listener
     * @return CompressResult including the uri where the compressed data was stored, or null if
     *         the compressor decided not to compress (e.g. because the original file was already
     *         sufficiently compressed)
     */
    suspend operator fun invoke(
        fromUri: String,
        toUri: String? = null,
        params: CompressParams = CompressParams(),
        onProgress: OnCompressProgress? = null,
    ): CompressResult?

}