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
    DIContext(props.di) {
        +props.children
    }
}