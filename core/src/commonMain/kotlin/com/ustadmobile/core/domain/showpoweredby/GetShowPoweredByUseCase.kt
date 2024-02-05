package com.ustadmobile.core.domain.showpoweredby

class GetShowPoweredByUseCase(private val showPoweredBy: Boolean) {

    operator fun invoke(): Boolean = showPoweredBy

}