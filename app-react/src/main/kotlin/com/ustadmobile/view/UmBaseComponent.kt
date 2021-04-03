package com.ustadmobile.view

import com.ustadmobile.core.view.UstadView
import kotlinx.coroutines.Runnable
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

open class UmBaseComponent <P: RProps,S: RState>(props: P): RComponent<P, S>(props), UstadView  {

    override fun RBuilder.render() {}

    override var loading: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        TODO("Not yet implemented")
    }

    override fun runOnUiThread(r: Runnable?) {
        TODO("Not yet implemented")
    }
}