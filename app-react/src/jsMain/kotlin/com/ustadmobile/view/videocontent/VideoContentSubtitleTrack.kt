package com.ustadmobile.view.videocontent

import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.core.domain.contententry.ContentManifestMap
import react.FC
import react.Props
import react.dom.html.ReactHTML.track
import react.useMemo

external interface VideoContentSubtitleTrackProps: Props {

    var subtitleTrack: SubtitleTrack

    var manifestUrl: String?

    var manifestMap: ContentManifestMap?

}

val UstadVideoContentSubtitleTrack = FC<VideoContentSubtitleTrackProps> { props ->

    val manifestUrl = props.manifestUrl
    val manifestMap = props.manifestMap

    val subtitleSrc = useMemo(props.subtitleTrack, manifestUrl, manifestMap) {
        if(manifestMap != null && manifestUrl != null) {
            manifestMap.resolveUrl(manifestUrl, props.subtitleTrack.uri)
        }else {
            null
        }
    }

    if(subtitleSrc != null) {
        track {
            src = subtitleSrc
            kind = "subtitles"
            label = props.subtitleTrack.title
            srcLang = props.subtitleTrack.langCode
        }
    }
}
