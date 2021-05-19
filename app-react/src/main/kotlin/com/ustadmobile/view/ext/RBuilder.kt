package com.ustadmobile.view.ext

import com.ccfraser.muirwik.components.MAvatarVariant
import com.ccfraser.muirwik.components.mAvatar
import com.ccfraser.muirwik.components.mIcon
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.CssStyleManager.personListItemAvatar
import react.RBuilder
import styled.css

fun RBuilder.renderAvatar(attachmentId: Long, fallback: String){
    val src = null
    mAvatar(src,variant = MAvatarVariant.circular){
        css (personListItemAvatar)
        if(src == null) mIcon(fallback, className= "${CssStyleManager.name}-fallBackAvatar")
    }
}