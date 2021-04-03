package com.ustadmobile.props

import react.RProps

interface MainComponentProps: RProps {
    var initialView: String
    var onThemeChange: () -> Unit
}