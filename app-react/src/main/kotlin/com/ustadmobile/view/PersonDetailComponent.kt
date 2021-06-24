package com.ustadmobile.view

import androidx.paging.DataSource
import com.ccfraser.muirwik.components.*
import com.ustadmobile.core.controller.PersonConstants.GENDER_MESSAGE_ID_MAP
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.personDetailComponentActionIcon
import com.ustadmobile.util.StyleManager.personDetailComponentActions
import com.ustadmobile.util.ext.formatDate
import com.ustadmobile.view.ext.*
import kotlinx.css.display
import kotlinx.css.marginTop
import kotlinx.css.px
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class PersonDetailComponent(mProps: RProps): UstadDetailComponent<PersonWithPersonParentJoin>(mProps),
    PersonDetailView {

    private lateinit var mPresenter: PersonDetailPresenter

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override val viewName: String
        get() = PersonDetailView.VIEW_NAME


    override var clazzes: DataSource.Factory<Int, ClazzEnrolmentWithClazzAndAttendance>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var changePasswordVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var showCreateAccountVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var rolesAndPermissions: DataSource.Factory<Int, EntityRoleWithNameAndRole>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var entity: PersonWithPersonParentJoin? = null
        get() = field
        set(value) {
            title = value?.firstNames +" "+ value?.lastName
            setState {
                field = value
            }
        }

    override fun onComponentReady() {
        super.onComponentReady()
        mPresenter = PersonDetailPresenter(this,arguments,this,di,this)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css(contentContainer)
            umGridContainer {
                umItem(MGridSize.cells12){
                    umGridContainer(MGridSpacing.spacing4) {
                        createAction("call",MessageID.call, MGridSize.cells4, MGridSize.cells2,
                            entity?.phoneNum != null){
                            handleCall(entity?.phoneNum)
                        }
                        createAction("message",MessageID.text, MGridSize.cells4, MGridSize.cells2,
                            entity?.phoneNum != null){
                            handleSMS(entity?.phoneNum)
                        }
                        createAction("email",MessageID.email, MGridSize.cells4, MGridSize.cells2,
                            entity?.emailAddr != null){
                            handleMail(entity?.emailAddr)
                        }
                        createAction("vpn_key",MessageID.change_password, MGridSize.cells6, MGridSize.cells3,
                            changePasswordVisible){
                            mPresenter.handleChangePassword()
                        }
                        createAction("person_add",MessageID.create_account, MGridSize.cells6, MGridSize.cells3,
                            showCreateAccountVisible){
                            mPresenter.handleCreateAccount()
                        }
                    }
                }

                umItem(MGridSize.cells12){
                    mDivider {
                        css{
                            +defaultFullWidth
                            +defaultMarginTop
                        }
                    }
                }

                umItem(MGridSize.cells12){
                    umGridContainer(MGridSpacing.spacing6) {
                        umItem(MGridSize.cells12, MGridSize.cells4){
                            css{
                                marginTop = 12.px
                            }
                            umEntityAvatar(showIcon = false) {}
                        }

                        umItem(MGridSize.cells12, MGridSize.cells8) {
                            css(defaultMarginTop)
                            umGridContainer(MGridSpacing.spacing4){

                                umItem(MGridSize.cells12){
                                    mTypography(getString(MessageID.basic_details),
                                        variant = MTypographyVariant.caption){
                                        css(alignTextToStart)
                                    }
                                }

                                umItem(MGridSize.cells12){
                                    createInformation("event",
                                        Date(entity?.dateOfBirth ?: 0).formatDate("dd/mm/yyyy"),
                                        MessageID.birthday)
                                    createInformation(null,
                                        getString(GENDER_MESSAGE_ID_MAP[entity?.gender] ?: 0),
                                        MessageID.field_person_gender)
                                    createInformation("badge", entity?.personOrgId, MessageID.organization_id)
                                    createInformation("account_circle", entity?.username, MessageID.username)
                                }

                                umItem(MGridSize.cells12){
                                    mTypography(getString(MessageID.contact_details),
                                        variant = MTypographyVariant.caption){
                                        css(alignTextToStart)
                                    }
                                }

                                umItem(MGridSize.cells12){
                                    createInformation("call",entity?.phoneNum,MessageID.phone_number)
                                    createInformation("email",entity?.emailAddr,MessageID.email)
                                    createInformation("place",entity?.personAddress,MessageID.address)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun RBuilder.createAction(icon: String, messageId: Int, xs: MGridSize,
                                      sm: MGridSize? = null, visible: Boolean = false,
                                      action:() -> Unit){
        umItem(xs, sm){
            css{
                display = displayProperty(visible, true)
            }
            mPaper(variant = MPaperVariant.elevation) {
                attrs.onClick = {
                    action()
                }
                css {
                    +personDetailComponentActions
                }
                mIcon(icon){
                    css{
                        +personDetailComponentActionIcon
                    }
                }
                mTypography(getString(messageId), variant = MTypographyVariant.body1, gutterBottom = true){
                    css(alignTextToStart)
                }
            }
        }
    }

    private fun RBuilder.createInformation(icon:String? = null, data: String?, label: Int){
        umGridContainer {
            css{
                +defaultMarginTop
                display = displayProperty(data != "0" || !data.isNullOrEmpty(), true)
            }
            umItem(MGridSize.cells2){
               if(icon != null){
                   mIcon(icon, className = "${StyleManager.name}-detailIconClass")
               }
            }

            umItem(MGridSize.cells10){
                mTypography("$data", variant = MTypographyVariant.body1){
                    css(alignTextToStart)
                }

                mTypography(getString(label),
                    variant = MTypographyVariant.body2){
                    css(alignTextToStart)
                }
            }
        }
    }

}