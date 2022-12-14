package com.ustadmobile.view

import com.ustadmobile.core.controller.ParentalConsentManagementPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.view.ParentalConsentManagementView
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinWithMinorPerson
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.renderListSectionTitle
import com.ustadmobile.view.ext.renderRawHtmlOnIframe
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.em
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.px
import mui.material.ButtonColor
import mui.material.ButtonVariant
import mui.material.Size
import mui.material.styles.TypographyVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ParentalConsentManagementComponent (mProps: UmProps): UstadEditComponent<PersonParentJoinWithMinorPerson>(mProps),
    ParentalConsentManagementView {

    private var mPresenter: ParentalConsentManagementPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, PersonParentJoinWithMinorPerson>?
        get() = mPresenter

    private var relationshipLabel = FieldLabel(text = getString(MessageID.relationship))

    override var infoText: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var siteTerms: SiteTerms? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var relationshipFieldOptions: List<IdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var relationshipFieldError: String? = null
        get() = field
        set(value) {
            field = value
            setState {
                relationshipLabel = relationshipLabel.copy(errorText = value)
            }
        }


    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var entity: PersonParentJoinWithMinorPerson? = null
        get() = field
        set(value) {
            setState {
                value?.ppjParentPersonUid = 0L
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = ParentalConsentManagementPresenter(this, arguments, this,
            this,di)
        ustadComponentTitle = getString(MessageID.parental_consent)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
        updateUiWithStateChangeDelay {
            fabManager?.visible = false
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                +contentContainer
                +defaultPaddingTop
            }

            umGridContainer(GridSpacing.spacing4) {
                umItem(GridSize.cells12, GridSize.cells8){
                    umGridContainer(GridSpacing.spacing4) {
                        umItem {
                            umTypography(infoText, variant = TypographyVariant.body1) {
                                css{
                                    fontSize = (1.2).em
                                }
                            }
                        }

                        renderListSectionTitle(getString(MessageID.terms_and_policies), leftMargin = 4)

                        umItem {
                            css(defaultMarginTop)
                            renderRawHtmlOnIframe(siteTerms?.termsHtml)
                        }
                    }
                }

                umItem(GridSize.cells12, GridSize.cells4){
                    umGridContainer(GridSpacing.spacing2) {

                        if(entity?.ppjParentPersonUid == 0L){
                            umItem {
                                umTextFieldSelect(
                                    "${relationshipLabel.text}",
                                    entity?.ppjRelationship.toString(),
                                    relationshipLabel.errorText ?: "",
                                    error = relationshipLabel.error,
                                    values = relationshipFieldOptions?.map {
                                        Pair(it.optionId.toString(), it.toString())
                                    }?.toList(),
                                    onChange = {
                                        setState {
                                            entity?.ppjRelationship = it.toInt()
                                            relationshipFieldError = null
                                        }
                                    }
                                )
                            }
                        }


                        if(entity?.ppjParentPersonUid == 0L){
                            umItem {
                                umButton(getString(MessageID.i_consent),
                                    size = Size.large,
                                    color = ButtonColor.secondary,
                                    variant = ButtonVariant.contained,
                                    onClick = {
                                        entity?.also {
                                            it.ppjStatus = PersonParentJoin.STATUS_APPROVED
                                            mPresenter?.handleClickSave(it)
                                        }
                                    }
                                ){
                                    css {
                                        +StyleManager.defaultFullWidth
                                        +StyleManager.defaultDoubleMarginTop
                                        height = 50.px
                                    }}
                            }
                        }


                        if(entity?.ppjParentPersonUid == 0L){
                            umItem {
                                umButton(getString(MessageID.i_do_not_consent),
                                    size = Size.large,
                                    color = ButtonColor.secondary,
                                    variant = ButtonVariant.outlined,
                                    onClick = {
                                        entity?.also {
                                            it.ppjStatus  = PersonParentJoin.STATUS_REJECTED
                                            mPresenter?.handleClickSave(it)
                                        }
                                    }
                                ){
                                    css {
                                        +StyleManager.defaultFullWidth
                                        +StyleManager.defaultDoubleMarginTop
                                        height = 50.px
                                    }}
                            }
                        }

                        if(entity?.ppjParentPersonUid != 0L){
                            umItem {
                                umButton(getString(MessageID.revoke_consent),
                                    size = Size.large,
                                    color = ButtonColor.secondary,
                                    variant = ButtonVariant.contained,
                                    onClick = {
                                        entity?.also {
                                            it.ppjStatus = if(it.ppjStatus == PersonParentJoin.STATUS_APPROVED) {
                                                PersonParentJoin.STATUS_REJECTED
                                            }else {
                                                PersonParentJoin.STATUS_APPROVED
                                            }
                                            mPresenter?.handleClickSave(it)
                                        }
                                    }
                                ){
                                    css {
                                        +StyleManager.defaultFullWidth
                                        +StyleManager.defaultDoubleMarginTop
                                        height = 50.px
                                    }}
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
        relationshipFieldError = null
    }

}