package com.ustadmobile.core.domain.contententry.getsubtitletrackfromuri

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.door.DoorUri

class GetSubtitleTrackFromUriUseCaseJs(
    private val endpoint: Endpoint,
): GetSubtitleTrackFromUriUseCase {

    override suspend fun invoke(
        subtitleTrackUri: DoorUri,
        filename: String
    ): SubtitleTrack {
        TODO()
    }
}