package com.ustadmobile.core.util.ext

import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json


fun ContentEntryImportJob?.requireSourceAsDoorUri(): DoorUri {
    return this?.sourceUri?.let { DoorUri.parse(it) }
        ?: throw IllegalArgumentException("requireSourceAsDoorUri: SourceUri is null!")
}

fun ContentEntryImportJob.paramMap(json: Json): Map<String, String>? {
    return cjiParams?.let {
        json.decodeFromString(MapSerializer(String.serializer(), String.serializer()), it)
    }
}

fun ContentEntryImportJob.paramSubtitles(json: Json): List<SubtitleTrack>? {
    return paramMap(json)?.get(ContentEntryImportJob.PARAM_KEY_SUBTITLES)?.let {
        json.decodeFromString(ListSerializer(SubtitleTrack.serializer()), it)
    }
}
