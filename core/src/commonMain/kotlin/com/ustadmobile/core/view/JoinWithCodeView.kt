package com.ustadmobile.core.view

interface JoinWithCodeView: UstadView {

    var controlsEnabled: Boolean?

    var errorText: String?

    var code: String?

    fun finish()

    var buttonLabel: String?

    companion object {

        const val VIEW_NAME = "JoinWithCode"

    }
}