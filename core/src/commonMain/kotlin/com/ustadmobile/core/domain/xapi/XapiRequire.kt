package com.ustadmobile.core.domain.xapi

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.ustadmobile.door.DoorUri
import kotlinx.datetime.Instant
import kotlin.time.Duration


fun <T> xapiRequireNotNullOrThrow(required: T?, message: String, responseCode: Int = 400): T {
    return required ?: throw XapiException(responseCode, message)
}

/**
 * Require a duration as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#46-iso-8601-durations
 *
 * The field itself is optional, however, if it is non-null, it must be a valid 8601 duration
 */
fun xapiRequireDurationOrNullAsLong(duration: String?) : Long? {
    return duration?.let {
        try {
            Duration.parseIsoString(it).inWholeMilliseconds
        }catch(e: Throwable) {
            throw XapiException(400, "Invalid duration: ${e.message}", e)
        }
    }
}

fun xapiRequireTimestampAsLong(timestampStr: String): Long {
    try {
        return Instant.parse(timestampStr).toEpochMilliseconds()
    }catch(e: Throwable) {
        throw XapiException(400, "Invalid timestamp: ${e.message}", e)
    }
}

fun xapiRequireValidUuidOrNull(
    uuid: String?,
    errorMessage: String = "Invalid uuid",
) : Uuid? {
    try {
        return uuid?.let { uuidFrom(it) }
    }catch(e: Throwable) {
        throw XapiException(400, "$errorMessage: ${e.message}", e)
    }
}

fun xapiRequireValidUuidOrNullAsString(
    uuid: String?,
    errorMessage: String = "Invalid uuid",
) : String? {
    try {
        if(uuid != null)
            uuidFrom(uuid)

        return uuid
    }catch(e: Throwable) {
        throw XapiException(400, "$errorMessage: ${e.message}", e)
    }
}

fun xapiRequireValidIRIOrNull(iri: String?, errorMessage: String = "Invalid IRI:") : String? {
    if(iri != null) {
        try {
            DoorUri.parse(iri)
        }catch(e: Throwable) {
            throw XapiException(400, "$errorMessage: ${e.message}", e)
        }
    }

    return iri
}

fun xapiRequireValidIRI(iri: String?, errorMessage: String = "Invalid iri") : String {
    if(iri == null)
        throw XapiException(400, "$errorMessage: iri is null")

    xapiRequireValidIRIOrNull(iri)
    return iri
}


