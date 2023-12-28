package com.ustadmobile.core.domain.blob

@Suppress("unused") //some items reserved for future use.
enum class TransferJobItemStatus(val value: Int) {
    QUEUED(1), IN_PROGRESS(11), COMPLETE(21), FAILED(22);

    companion object {

        fun valueOf(value: Int): TransferJobItemStatus {
            return entries.first { it.value == value }
        }

    }
}