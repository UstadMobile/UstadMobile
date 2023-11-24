package com.ustadmobile.mui.components

import com.ustadmobile.core.viewmodel.UstadViewModel
import react.FC
import react.PropsWithChildren
import react.createContext

/**
 * This is roughly modeled on the ViewModelUriHandlerLocalProvider used on Jetpack compose. The
 * context can be used by any child component that has links.
 */

typealias LinkOpener = (String) -> Unit

val LinkOpenerContext = createContext<LinkOpener>()

external interface ViewModelLinkOpenerProps : PropsWithChildren{

    var viewModel: UstadViewModel

}

val ViewModelLinkOpenerContext = FC<ViewModelLinkOpenerProps> { props ->
    LinkOpenerContext(props.viewModel::onClickLink) {
        + props.children
    }
}

