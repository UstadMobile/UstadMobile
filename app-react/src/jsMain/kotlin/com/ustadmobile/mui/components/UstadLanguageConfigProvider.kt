package com.ustadmobile.mui.components

import com.ustadmobile.core.impl.config.SupportedLanguagesConfig
import react.FC
import react.PropsWithChildren
import react.createContext

val UstadLanguageConfigContext = createContext<SupportedLanguagesConfig>()

external interface UstadLanguageConfigProviderProps: PropsWithChildren {

    var languagesConfig: SupportedLanguagesConfig

}

val UstadLanguageConfigProvider = FC<UstadLanguageConfigProviderProps> { props ->
    UstadLanguageConfigContext(props.languagesConfig) {
        + props.children
    }
}
