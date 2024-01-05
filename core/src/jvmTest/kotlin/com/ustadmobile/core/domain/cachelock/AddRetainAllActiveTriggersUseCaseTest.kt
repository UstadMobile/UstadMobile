package com.ustadmobile.core.domain.cachelock

import com.ustadmobile.door.DoorDbType
import kotlin.test.Test

class AddRetainAllActiveTriggersUseCaseTest {

    @Test
    fun test(){
        AddRetainAllActiveUriTriggersUseCase().invoke(
            dbType = DoorDbType.SQLITE,
        ).forEach {
            println("add(\"\"\"$it;\"\"\")")
            println()
        }
    }
}