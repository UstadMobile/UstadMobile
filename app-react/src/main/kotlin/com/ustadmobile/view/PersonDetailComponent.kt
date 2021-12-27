package com.ustadmobile.view

import com.ustadmobile.core.controller.PersonConstants.GENDER_MESSAGE_ID_MAP
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.outcomeToString
import com.ustadmobile.core.util.ext.roleToString
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultFullWidth
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.*
import com.ustadmobile.mui.components.*
import kotlinx.css.LinearDimension
import kotlinx.css.marginTop
import kotlinx.css.padding
import org.w3c.dom.events.Event
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class PersonDetailComponent(mProps: UmProps): UstadDetailComponent<PersonWithPersonParentJoin>(mProps),
    PersonDetailView {

    private var mPresenter: PersonDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override val viewName: String
        get() = PersonDetailView.VIEW_NAME

    private var classList: List<ClazzEnrolmentWithClazzAndAttendance>? = null

    private val observer = ObserverFnWrapper<List<ClazzEnrolmentWithClazzAndAttendance>>{
        setState {
            classList = it
        }
    }

    override var clazzes: DoorDataSourceFactory<Int, ClazzEnrolmentWithClazzAndAttendance>? = null
        get() = field
        set(value) {
            field = value
            val liveData = value?.getData(0,Int.MAX_VALUE)
            liveData?.removeObserver(observer)
            liveData?.observe(this, observer)
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

    override var entity: PersonWithPersonParentJoin? = null
        get() = field
        set(value) {
            ustadComponentTitle = value?.firstNames +" "+ value?.lastName
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = PersonDetailPresenter(this,arguments,this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                +contentContainer
                +defaultPaddingTop
            }
            umGridContainer {
                umItem(GridSize.cells12){
                    umGridContainer(GridSpacing.spacing4) {
                        createProfileAction("call", getString(MessageID.call), GridSize.cells4, GridSize.cells2,
                            entity?.phoneNum != null){
                            onClickCall(entity?.phoneNum)
                        }
                        createProfileAction("message",getString(MessageID.text), GridSize.cells4, GridSize.cells2,
                            entity?.phoneNum != null){
                            onClickSMS(entity?.phoneNum)
                        }
                        createProfileAction("email",getString(MessageID.email), GridSize.cells4, GridSize.cells2,
                            entity?.emailAddr != null){
                            onClickEmail(entity?.emailAddr)
                        }
                        createProfileAction("vpn_key",getString(MessageID.change_password), GridSize.cells6, GridSize.cells3,
                            changePasswordVisible){
                            mPresenter?.handleChangePassword()
                        }
                        createProfileAction("person_add",getString(MessageID.create_account), GridSize.cells6, GridSize.cells3,
                            showCreateAccountVisible){
                            mPresenter?.handleCreateAccount()
                        }
                    }
                }

                umItem(GridSize.cells12){
                    umDivider {
                        css{
                            +defaultFullWidth
                            +defaultMarginTop
                        }
                    }
                }

                umItem(GridSize.cells12){
                    umGridContainer(GridSpacing.spacing6) {

                        umItem(GridSize.cells12, GridSize.cells4){
                            css{
                                marginTop = LinearDimension("12px")
                            }
                            umEntityAvatar(showIcon = false) {}
                        }

                        umItem(GridSize.cells12, GridSize.cells8) {
                            css{
                                +defaultMarginTop
                                padding = "16px"
                            }
                            umGridContainer(GridSpacing.spacing4){

                                umItem(GridSize.cells12){
                                    umTypography(getString(MessageID.basic_details),
                                        variant = TypographyVariant.caption){
                                        css(alignTextToStart)
                                    }
                                }

                                umItem(GridSize.cells12){
                                    createInformation("event",
                                        entity?.dateOfBirth.toDate().standardFormat(),
                                        getString(MessageID.birthday)
                                    )

                                    val genderMessageId = GENDER_MESSAGE_ID_MAP[entity?.gender ?: 0]
                                    createInformation("person", if(genderMessageId == null) "" else getString(genderMessageId),
                                        getString(MessageID.field_person_gender)
                                    )

                                    createInformation("badge", entity?.personOrgId,
                                        getString(MessageID.organization_id)
                                    )

                                    createInformation("account_circle", entity?.username,
                                        getString(MessageID.username)
                                    )
                                }

                                umItem(GridSize.cells12){
                                    umTypography(getString(MessageID.contact_details),
                                        variant = TypographyVariant.caption){
                                        css(alignTextToStart)
                                    }
                                }

                                umItem(GridSize.cells12){
                                    createInformation("call",entity?.phoneNum,getString(MessageID.phone_number))
                                    createInformation("email",entity?.emailAddr,getString(MessageID.email))
                                    createInformation("place",entity?.personAddress,getString(MessageID.address))
                                }


                                if(classList != null && classList?.isNotEmpty() == true){

                                    umItem(GridSize.cells12){
                                        createListSectionTitle(getString(MessageID.classes))

                                        classList?.let { clazzes ->
                                            child(ClazzEnrolmentWithClazzSimpleListComponent::class){
                                                attrs.entries = clazzes
                                                attrs.onEntryClicked = { clazz ->
                                                    mPresenter?.handleClickClazz(clazz)
                                                }
                                            }
                                        }
                                    }
                                }
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
    }

    class ClazzEnrolmentWithClazzSimpleListComponent(mProps: SimpleListProps<ClazzEnrolmentWithClazzAndAttendance>):
        UstadSimpleList<SimpleListProps<ClazzEnrolmentWithClazzAndAttendance>>(mProps){

        override fun RBuilder.renderListItem(item: ClazzEnrolmentWithClazzAndAttendance, onClick: (Event) -> Unit) {
            umGridContainer {
                attrs.onClick = {
                    onClick.invoke(it.nativeEvent)
                }
                val title = "${item.clazz?.clazzName} (${item.roleToString(this, systemImpl)}) " +
                        "- ${item.outcomeToString(this,  systemImpl)}"

                val enrollmentPeriod = "${Date(item.clazzEnrolmentDateJoined).standardFormat()} " +
                        "- ${Date(item.clazzEnrolmentDateLeft).standardFormat()}"


                createListItemWithAttendance("people", title, enrollmentPeriod,
                    item.attendance, getString(MessageID.x_percent_attended))
            }
        }
    }
}