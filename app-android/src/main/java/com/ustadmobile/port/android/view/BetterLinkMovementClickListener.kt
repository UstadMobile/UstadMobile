package com.ustadmobile.port.android.view

import android.text.util.Linkify
import android.widget.TextView
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import me.saket.bettermovementmethod.BetterLinkMovementMethod

class BetterLinkMovementLinkClickListener(
    val systemImpl: UstadMobileSystemImpl,
    val accountManager: UstadAccountManager,
    val context: Any
){

    private val onClickListener: BetterLinkMovementMethod.OnLinkClickListener =
        BetterLinkMovementMethod.OnLinkClickListener{
                _,url ->
            systemImpl.handleClickLink(url, accountManager, context)

            true
        }

    private val onLongClickListener: BetterLinkMovementMethod.OnLinkLongClickListener =
        BetterLinkMovementMethod.OnLinkLongClickListener{
                _,_ ->
            true
        }


    fun addMovement(textView: TextView?){

        if(textView == null){
            return
        }

        textView.linksClickable = true

        textView.movementMethod =
            BetterLinkMovementMethod.newInstance().apply {
                this.setOnLinkClickListener(onClickListener)
                this.setOnLinkLongClickListener(onLongClickListener)

            }

        Linkify.addLinks(textView, Linkify.ALL)

    }
}