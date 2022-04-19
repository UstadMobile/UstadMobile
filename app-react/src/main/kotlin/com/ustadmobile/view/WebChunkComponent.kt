package com.ustadmobile.view

import com.ustadmobile.core.controller.WebChunkPresenter
import com.ustadmobile.core.view.WebChunkView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import react.RBuilder

class WebChunkComponent(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps), WebChunkView  {

    private var mPresenter: WebChunkPresenter? = null

    override var entry: ContentEntry? = null
        get() = field
        set(value) {
            field = value
            ustadComponentTitle = value?.title
        }

    override var url: String = ""
        get() = field
        set(value) {
            loading = value.isEmpty()
            field = value
        }

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = false
        mPresenter = WebChunkPresenter(this,arguments, this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        if(url.isNotEmpty()){
            renderIframe(listOf(url), 1)
        }
    }

    override fun showNoAppFoundError(message: String, actionMessageId: Int, mimeType: String) {
        showSnackBar(message, {}, actionMessageId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entry = null
    }

}