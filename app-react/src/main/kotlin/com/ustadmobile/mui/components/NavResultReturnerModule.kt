package com.ustadmobile.mui.components

import com.ustadmobile.core.impl.nav.NavResultReturner
import com.ustadmobile.core.impl.nav.NavResultReturnerImpl
import react.FC
import react.PropsWithChildren
import react.createContext
import react.useMemo

val NavResultReturnerContext = createContext<NavResultReturner>()

val NavResultReturnerModule = FC<PropsWithChildren> { props ->
    val navResultReturner: NavResultReturner = useMemo(dependencies = emptyArray()) {
        NavResultReturnerImpl()
    }

    NavResultReturnerContext(navResultReturner) {
        + props.children
    }

}
