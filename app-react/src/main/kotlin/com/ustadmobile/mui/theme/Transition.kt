package com.ustadmobile.mui.theme

external interface Easing {
    var easeInOut: String
    var easeOut: String
    var easeIn: String
    var sharp: String
}

external interface Duration {
    var shortest: Int
    var shorter: Int
    var short: Int
    var standard: Int
    var complex: Int
    var enteringScreen: Int
    var leavingScreen: Int
}
external interface Transitions {
    var easing: Easing
    var duration: Duration
}

external interface TransitionsOptions {
    var easing: Any? get() = definedExternally; set(value) = definedExternally
    var duration: Any? get() = definedExternally; set(value) = definedExternally
}