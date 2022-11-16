package com.ustadmobile.core.test.viewmodeltest

import com.ustadmobile.core.viewmodel.ViewModel

fun <T: ViewModel> testViewModel(
    makeViewModel: ViewModelFactoryParams.() -> T,
    block: ViewModelTestBuilder<T>.() -> Unit
) {
    val viewModelTestBuilder = ViewModelTestBuilder(makeViewModel)
    try {
        block(viewModelTestBuilder)
    }finally {
        viewModelTestBuilder.viewModel.close()
    }
}