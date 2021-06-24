package com.ustadmobile.view

import com.ustadmobile.core.controller.WebChunkPresenter
import com.ustadmobile.core.view.WebChunkView
import com.ustadmobile.lib.db.entities.ContentEntry
import react.RBuilder
import react.RProps
import react.RState

class WebChunkComponent(mProps: RProps): UstadBaseComponent<RProps, RState>(mProps), WebChunkView  {

    private var mPresenter: WebChunkPresenter? = null

    override var entry: ContentEntry? = null
        get() = field
        set(value) {
            field = value
            title = value?.title
        }

    override var viewName: String? = null

    override var url: String = ""
        get() = field
        set(value) {
            loading = value.isEmpty()
            field = value
        }

    override fun onComponentReady() {
        super.onComponentReady()
        mPresenter = WebChunkPresenter(this,arguments, this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        renderIframe(listOf(url), 1)
    }

    override fun showNoAppFoundError(message: String, actionMessageId: Int, mimeType: String) {
        showSnackBar(message, {}, actionMessageId)
    }

}