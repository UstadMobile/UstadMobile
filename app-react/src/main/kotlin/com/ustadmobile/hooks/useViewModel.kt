package com.ustadmobile.hooks

import com.ustadmobile.core.viewmodel.ViewModel
import react.*
import react.router.dom.useSearchParams

/**
 * Use a ViewModel via useEffect and useState. The ViewModel will be cleared when the component is
 * unmounted. It will be recreated if the search param arguments change.
 */
fun <T:ViewModel> useViewModel(
    block: () -> T
): T {
    var firstRun by useState { true }

    var viewModel: T by useState {
        console.log("Creating ViewModel")
        block()
    }

    val searchParams by useSearchParams()

    useEffect(dependencies = arrayOf(searchParams)){
        if(!firstRun) {
            viewModel = block()
            console.log("Recreating ViewModel")
        }

        firstRun = false

        cleanup {
            console.log("Close ViewModel")
            viewModel.close()
        }
    }

    return viewModel
}
