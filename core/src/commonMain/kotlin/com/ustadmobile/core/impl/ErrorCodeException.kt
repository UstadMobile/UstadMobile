package com.ustadmobile.core.impl

class ErrorCodeException(val errorCode: Int, message: String, cause: Exception? = null) : Exception(message, cause){
}