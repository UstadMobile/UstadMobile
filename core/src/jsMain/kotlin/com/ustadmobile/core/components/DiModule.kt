package com.ustadmobile.core.components

import org.kodein.di.DI
import react.FC
import react.PropsWithChildren
import react.createContext

val DIContext = createContext<DI>()

external interface DIProps: PropsWithChildren {
    var di: DI
}

val DIModule = FC<DIProps> { props ->
    console.log("DIMOdule DI=${props.di}")
    DIContext(props.di) {
        +props.children
    }
}