package com.ustadmobile.view

import com.ccfraser.muirwik.components.mSnackbar
import com.ccfraser.muirwik.components.themeContext
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.util.UmStyles
import kotlinx.browser.window
import kotlinx.coroutines.Runnable
import org.kodein.di.DI
import org.kodein.di.DIAware
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

open class UmBaseComponent <P: RProps,S: RState>(props: P): RComponent<P, S>(props), UstadView, DIAware {

    private var builder: RBuilder? = null

    val systemImpl =  UstadMobileSystemImpl.instance

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

    override val di: DI
        get() = window.asDynamic().di as DI

}