package com.ustadmobile.view

import com.ustadmobile.core.controller.SessionListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SessionListView
import com.ustadmobile.lib.db.entities.PersonWithSessionsDisplay
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.spacingUnits
import com.ustadmobile.mui.components.umTypography
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.*
import com.ustadmobile.view.ext.*
import kotlinx.css.FlexDirection
import kotlinx.css.LinearDimension
import kotlinx.css.padding
import mui.material.AvatarVariant
import mui.material.styles.TypographyVariant
import react.RBuilder
import styled.css
import styled.styledSpan


class SessionListComponent(props: UmProps): UstadListComponent<PersonWithSessionsDisplay,
        PersonWithSessionsDisplay>(props), SessionListView {

    private var mPresenter: SessionListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithSessionsDisplay>?
        get() = mPresenter

    override val displayTypeRepo: Any?
        get() = dbRepo?.userSessionDao


    override fun RBuilder.renderListItem(item: PersonWithSessionsDisplay) {
        umGridContainer {
            val padding = LinearDimension("4px")
            css{
                padding(top = padding, bottom = padding)
            }

            umItem(GridSize.cells3 , GridSize.cells1){
                umItemThumbnail(isContentCompleteImage(item),
                    width = 50, avatarVariant = AvatarVariant.circular)
            }

            umItem(GridSize.cells9, GridSize.cells11){
                umItem(GridSize.cells12){
                    umTypography(setContentComplete(systemImpl, item) +
                            " ${item.duration.formatToStringHoursMinutesSeconds(systemImpl)}",
                        variant = TypographyVariant.h6){
                        css (StyleManager.alignTextToStart)
                    }
                }

                umItem(GridSize.cells12){
                    umTypography("${item.startDate.toDate()?.standardFormat()} " +
                            "- ${item.startDate.toDate()?.formattedInHoursAndMinutes()}",
                        variant = TypographyVariant.body1,
                        paragraph = true){
                        css(StyleManager.alignTextToStart)
                    }
                }

                umItem(GridSize.cells12, flexDirection = FlexDirection.row){
                    styledSpan {
                        css{
                            padding( right = 4.spacingUnits)
                        }

                        umTypography(getString(MessageID.percentage_score)
                            .format("${item.resultScoreScaled * 100}"),
                            variant = TypographyVariant.body1,
                            paragraph = true){
                            css(StyleManager.alignTextToStart)
                        }
                    }

                    styledSpan {
                        css{
                            padding( right = 4.spacingUnits)
                        }

                        umTypography("(${item.resultScore} / ${item.resultMax})",
                            variant = TypographyVariant.body1,
                            paragraph = true){
                            css(StyleManager.alignTextToStart)
                        }
                    }
                }
            }
        }
    }

    override fun handleClickEntry(entry: PersonWithSessionsDisplay) {
        mPresenter?.onClickPersonWithSessionDisplay(entry)
    }

    override var personWithContentTitle: String? = null
        set(value) {
            field = value
            ustadComponentTitle = value
        }


    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = false
        mPresenter = SessionListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun onFabClicked() {}

    override fun RBuilder.renderAddContentOptionsDialog() {}


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}