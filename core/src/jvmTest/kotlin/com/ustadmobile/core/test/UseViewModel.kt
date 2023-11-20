package com.ustadmobile.core.test

import com.ustadmobile.core.viewmodel.UstadViewModel
import io.github.aakira.napier.Napier

inline fun <T: UstadViewModel> T.use(
    block: (T) -> Unit
) {
    try {
        block(this)
    }catch(e: Exception) {
        Napier.e("ViewModel.use: caught exception", e)
    }finally {
        close()
    }
}
