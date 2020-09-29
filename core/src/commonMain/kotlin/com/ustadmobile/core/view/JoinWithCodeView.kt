package com.ustadmobile.core.view

interface JoinWithCodeView: UstadView {

    var controlsEnabled: Boolean?

    var errorText: String?

    var code: String?

    fun finish()

    companion object {

        const val VIEW_NAME = "JoinWithCode"

    }
}