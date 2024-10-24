package com.ustadmobile.wrappers.jsjodatime

@JsModule("@js-joda/timezone")
@JsNonModule
external object JsJodaTimeZoneModule

//Used by Javascript as per https://github.com/Kotlin/kotlinx-datetime#note-about-time-zones-in-js
@Suppress("unused")
val jsJodaTz = JsJodaTimeZoneModule
