package com.ustadmobile.state

interface MainComponentState: UmBaseState {
    var currentView: String
    var responsiveDrawerOpen: Boolean
}