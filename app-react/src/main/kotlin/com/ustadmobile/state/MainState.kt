package com.ustadmobile.state

interface MainState: UmBaseState {
    var currentView: String
    var responsiveDrawerOpen: Boolean
    var isRTLSupport: Boolean
}