package com.ustadmobile.core.contentformats.media

import kotlinx.serialization.Serializable

/**
 * This class is stores the information required to display multimedia (e.g. video or audio).
 * Currently this includes only the media url itself and the mime type.
 *
 * Information on available subtitle tracks etc. will be added.
 *
 * The ContentEntryVersion.cevUrl for a media file will point to a json file with this data (
 * e.g. this class serialized), not the URL of the video/audio etc. itself.
 */
@Serializable
class MediaContentInfo(
    //Currently only one source is supported, this might change in future.
    val sources: List<MediaSource>,
    val subtitles: List<SubtitleTrack> = emptyList(),
)
