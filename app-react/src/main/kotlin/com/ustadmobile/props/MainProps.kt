package com.ustadmobile.props

import react.RProps

interface MainProps: RProps {
    var initialView: String
    var onThemeChange: () -> Unit
}