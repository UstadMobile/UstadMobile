package com.ustadmobile.view

import com.ustadmobile.core.view.UstadView
import com.ccfraser.muirwik.components.mSnackbar
import kotlinx.coroutines.Runnable
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

open class UmBaseComponent <P: RProps,S: RState>(props: P): RComponent<P, S>(props), UstadView  {

    private var builder: RBuilder? = null

    override fun RBuilder.render() {
        builder = this
    }

    override var loading: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        builder?.mSnackbar (message = message)
    }

    override fun runOnUiThread(r: Runnable?) {
        r?.run()
    }

}