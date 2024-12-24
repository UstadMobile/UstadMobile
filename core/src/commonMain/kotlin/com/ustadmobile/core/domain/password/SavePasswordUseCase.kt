package com.ustadmobile.core.domain.password

interface SavePasswordUseCase {
    suspend operator fun invoke(username:String,password:String)
}