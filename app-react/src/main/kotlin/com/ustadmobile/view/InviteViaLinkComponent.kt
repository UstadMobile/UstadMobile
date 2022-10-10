package com.ustadmobile.view

import com.ustadmobile.core.controller.InviteViaLinkPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.InviteViaLinkView
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignCenterItems
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.Util.copyToClipboard
import com.ustadmobile.util.ext.format
import com.ustadmobile.view.ext.renderListItemWithIconAndTitle
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.marginTop
import mui.material.ButtonColor
import mui.material.ButtonVariant
import mui.material.Size
import mui.material.styles.TypographyVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class InviteViaLinkComponent(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps), InviteViaLinkView {

    private var mPresenter: InviteViaLinkPresenter? = null

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
        ustadComponentTitle = getString(MessageID.invite_with_link)
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

                umItem(GridSize.cells12){
                    umTypography(getString(MessageID.invite_link_desc).format(entityName ?: ""),
                        variant = TypographyVariant.body2){
                        css {
                            +StyleManager.defaultPaddingTopBottom
                            +StyleManager.alignTextToStart
                        }
                    }
                }

                umItem(GridSize.cells12){
                    renderListItemWithIconAndTitle("link",inviteLink ?: "")
                }

                umGridContainer(GridSpacing.spacing6) {
                    css(defaultDoubleMarginTop)
                    umItem(GridSize.cells12, GridSize.cells3){
                        css(alignCenterItems)
                        umButton(getString(MessageID.copy_link),
                            variant = ButtonVariant.contained,
                            color = ButtonColor.secondary,
                            size = Size.large,
                            startIcon = "content_copy", onClick = {
                                copyToClipboard(inviteLink ?: ""){
                                    showSnackBar(getString(MessageID.copied_to_clipboard))
                                }
                            })
                    }

                    umItem(GridSize.cells12, GridSize.cells3){
                        css(alignCenterItems)
                        umButton(getString(MessageID.copy_code),
                            variant = ButtonVariant.contained,
                            color = ButtonColor.secondary,
                            size = Size.large,
                            startIcon = "content_copy",
                            onClick = {
                                copyToClipboard(inviteCode ?: ""){
                                    showSnackBar(getString(MessageID.copied_to_clipboard))
                                }
                            })
                    }
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