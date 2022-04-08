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
import com.ustadmobile.mui.theme.UMColor
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.clean
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.renderListSectionTitle
import com.ustadmobile.view.ext.renderRawHtmlOnIframe
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.em
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.px
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ParentalConsentManagementComponent (mProps: UmProps): UstadEditComponent<PersonParentJoinWithMinorPerson>(mProps),
    ParentalConsentManagementView {

    private var mPresenter: ParentalConsentManagementPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, PersonParentJoinWithMinorPerson>?
        get() = mPresenter

    override val viewNames: List<String>
        get() = listOf(ParentalConsentManagementView.VIEW_NAME)

    private var relationshipLabel = FieldLabel(text = getString(MessageID.relationship))

    override var infoText: String? = null
        get() = field
        set(value) {
            setState {
                field = "I confirm that I am the parent or legal guardian of the following child:\\n \\n Name: Test1 123\\n Date of birth: 8-3-2018\\n \\n If I consent to my child using Ustad Mobile, then the collection of and processing of personally identifiable information will be done in compliance with the privacy policy and terms and conditions below.\\n \\n Consent can be revoked anytime by using the link that was emailed. If you choose not to consent, any data provided by your child will be deleted within 24 hours"
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
                            umTypography(infoText?.clean(), variant = TypographyVariant.body1) {
                                css{
                                    fontSize = (1.2).em
                                }
                            }
                        }

                        renderListSectionTitle(getString(MessageID.terms_and_policies))

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
                                        Pair(it.optionId.toString(), it.description)
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
                                    size = ButtonSize.large,
                                    color = UMColor.secondary,
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
                                    size = ButtonSize.large,
                                    color = UMColor.secondary,
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
                                    size = ButtonSize.large,
                                    color = UMColor.secondary,
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