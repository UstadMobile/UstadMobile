package com.ustadmobile.view

import com.ustadmobile.core.controller.InviteViaLinkPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.InviteViaLinkView
import com.ustadmobile.mui.components.*
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.hideOnMobile
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.Util.copyToClipboard
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.createItemWithIconAndTitle
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.marginTop
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class InviteViaLinkComponent(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps), InviteViaLinkView {

    private var mPresenter: InviteViaLinkPresenter? = null

    override val viewName: String
        get() = InviteViaLinkView.VIEW_NAME

    override var inviteLink: String? = null
        get() = field
        set(value) {
            setState{
                field = value
            }
        }
    override var entityName: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var inviteCode: String? = null
        get() = field
        set(value) {
            setState{
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        title = getString(MessageID.invite_with_link)
        mPresenter = InviteViaLinkPresenter(this, arguments, this, di)
        mPresenter?.onCreate(mapOf())
    }


    override fun RBuilder.render() {
        styledDiv {
            css {
                +contentContainer
                marginTop = 4.spacingUnits
            }
            umGridContainer(GridSpacing.spacing4) {

                umItem(sm = GridSize.cells12){
                    umTypography(getString(MessageID.invite_link_desc).format(entityName ?: ""),
                        variant = TypographyVariant.body2){
                        css {
                            +StyleManager.defaultPaddingTopBottom
                            +StyleManager.alignTextToStart
                        }
                    }
                }

                umItem(GridSize.cells12){
                    createItemWithIconAndTitle("link",inviteLink ?: "")
                }

                umItem(GridSize.cells1){
                    css(hideOnMobile)
                }

                umItem(GridSize.cells12, GridSize.cells3){
                    umButton(getString(MessageID.copy_link),
                        variant = ButtonVariant.contained,
                        color = UMColor.secondary,
                        size = ButtonSize.large,
                        startIcon = "content_copy", onClick = {
                        copyToClipboard(inviteLink ?: ""){
                            showSnackBar(getString(MessageID.copied_to_clipboard))
                        }
                    }){
                        css(defaultFullWidth)
                    }
                }

                umItem(GridSize.cells12, GridSize.cells3){
                    umButton(getString(MessageID.share_link),
                        variant = ButtonVariant.contained,
                        color = UMColor.secondary,
                        size = ButtonSize.large,
                        startIcon = "share",
                        onClick = {

                    }){
                        css(defaultFullWidth)
                    }
                }

                umItem(GridSize.cells12, GridSize.cells3){
                    umButton(getString(MessageID.copy_code),
                        variant = ButtonVariant.contained,
                        color = UMColor.secondary,
                        size = ButtonSize.large,
                        startIcon = "content_copy",
                        onClick = {
                        copyToClipboard(inviteLink ?: ""){
                            showSnackBar(getString(MessageID.copied_to_clipboard))
                        }
                    }){
                        css(defaultFullWidth)
                    }
                }

                umItem(GridSize.cells1){
                    css(hideOnMobile)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        inviteLink = null
        inviteCode = null
    }
}