package com.ustadmobile.sharedse.network

expect interface IHttpSessionSe {

    fun execute()

    fun getUri(): String
}