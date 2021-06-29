package com.ustadmobile.port.android.screen

import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ContentEntryEdit2Fragment
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView

object ContentEntryEditScreen : KScreen<ContentEntryEditScreen>() {


    override val layoutId: Int?
        get() = R.layout.fragment_content_entry_edit2
    override val viewClass: Class<*>?
        get() = ContentEntryEdit2Fragment::class.java

    val importButton = KButton { withId(R.id.content_entry_select_file) }

    val storageOption = KTextView { withId(R.id.storage_option) }

    val entryTitleTextInput = KTextInputLayout { withId(R.id.entry_title) }

    val descTextInput = KTextInputLayout { withId(R.id.entry_description) }

}