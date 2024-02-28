package com.ustadmobile.core.domain.process

import kotlin.system.exitProcess

class CloseProcessUseCaseJvm: CloseProcessUseCase {

    override fun invoke() {
        exitProcess(0)
    }

}