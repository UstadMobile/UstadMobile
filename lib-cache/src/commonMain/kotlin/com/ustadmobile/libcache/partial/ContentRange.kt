package com.ustadmobile.libcache.partial

/**
 * Data required to process a content range response. Supports only one range.
 *
 * @param fromByte the from byte (inclusive)
 * @param toByte the to byte (inclusive)
 * @param totalBytes the total size of the document
 * @param contentLength the actual size of this response.
 */
class ContentRange(
    val fromByte: Long,
    val toByte: Long,
    val totalBytes: Long,
    val contentLength: Long,
) {

    val contentRangeResponseHeader: String
        get() = "bytes $fromByte-$toByte/$totalBytes"

    companion object {

        /**
         *  As per https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Range
         *
         *  @param header the Content-Range header from the http request
         *  @param totalContentLength the total length of the content
         */
        fun parseRangeHeader(
            header: String,
            totalContentLength: Long,
        ) : ContentRange {
            val headerTrimmed = header.trim()

            if(headerTrimmed.indexOf(",") != -1) {
                throw RangeRequestNotSatisfiableException("Multiple content-ranges are not supported")
            }

            val equalsPos = headerTrimmed.indexOf("=")
            if(equalsPos < 0)
                throw IllegalArgumentException("Malformed Content-Range: must have unit followed = eg. bytes=")

            val unit = headerTrimmed.substring(0, equalsPos)
            if(unit != "bytes")
                throw RangeRequestNotSatisfiableException("Content-Range: only supported unit is: bytes, $unit is not supported")

            //Full spec
            //See https://httpwg.org/specs/rfc7233.html#rule.ranges-specifier

            val dashIndex = headerTrimmed.indexOf("-")

            val firstBytePos = headerTrimmed.substring(equalsPos + 1, dashIndex).let {
                if(it.isNotBlank()) it.toLong() else -1
            }

            val lastBytePos = headerTrimmed.substring(dashIndex + 1).let {
                if(it.isNotBlank()) it.toLong() else -1
            }

            /*If only the lastBytes are specified, then this means return the last n bytes as per
             * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Range
             * "Alternatively, if it's unknown how large a resource is, the last n bytes can be requested using a suffix range of -n:
             * Range: bytes=-100"
             */
            return if(lastBytePos >= 0 && firstBytePos == -1L) {
                ContentRange(
                    fromByte = (totalContentLength - lastBytePos) + 1,
                    toByte = totalContentLength,
                    totalBytes = totalContentLength,
                    contentLength = lastBytePos,
                )
            }else {
                val lastByte = if(lastBytePos == -1L) totalContentLength - 1 else lastBytePos
                if(lastByte < firstBytePos) {
                    throw RangeRequestNotSatisfiableException("Cannot satisfy range: last byte " +
                            "$lastByte is before first byte $firstBytePos")
                }

                //Range is inclusive, starting from zero, so the last byte can be up to the total
                // content length minus 1
                if(lastBytePos >= totalContentLength) {
                    throw RangeRequestNotSatisfiableException("Cannot satisfy range: last byte is " +
                            "$lastByte, but totalSize is $totalContentLength")
                }

                ContentRange(
                    fromByte = firstBytePos,
                    toByte = lastByte,
                    totalBytes = totalContentLength,
                    contentLength = (lastByte - firstBytePos) + 1
                )
            }
        }
    }

}