package com.ustadmobile.lib.db.composites

@Suppress("unused") //some items reserved for future use.
enum class TransferJobItemStatus(val value: Int) {
    QUEUED(1), IN_PROGRESS(11), COMPLETE(21), FAILED(22), CANCELLED(23);

    companion object {

        const val STATUS_QUEUED_INT = 1

        const val STATUS_IN_PROGRESS_INT = 11

        const val STATUS_COMPLETE_INT = 21

        const val STATUS_FAILED = 22

        const val STATUS_CANCELLED = 23

        fun valueOf(value: Int): TransferJobItemStatus {
            return entries.firstOrNull { it.value == value } ?: QUEUED
        }

    }
}