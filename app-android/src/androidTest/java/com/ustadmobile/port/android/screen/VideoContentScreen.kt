package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.VideoContentFragment

object VideoContentScreen : KScreen<VideoContentScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_video_content

    override val viewClass: Class<*>?
        get() = VideoContentFragment::class.java

    val desc = KTextView { withId(R.id.activity_video_player_description)}

    val exoPlayer = KView { withId(R.id.activity_video_player_view)}

    val playerControls = KView { withId(R.id.player_view_controls)}

    val exoPlayButton = KButton {
        withId(R.id.exo_play)
        isDescendantOfA {
            withId(R.id.player_view_controls)
        }
    }

}