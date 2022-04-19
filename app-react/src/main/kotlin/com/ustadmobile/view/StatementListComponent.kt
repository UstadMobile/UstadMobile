package com.ustadmobile.view

import com.ustadmobile.core.controller.StatementListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.StatementListView
import com.ustadmobile.lib.db.entities.StatementWithSessionDetailDisplay
import com.ustadmobile.lib.db.entities.VerbEntity
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.*
import com.ustadmobile.view.ext.setStatementQuestionAnswer
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import com.ustadmobile.view.ext.umItemThumbnail
import kotlinx.css.FlexDirection
import kotlinx.css.LinearDimension
import kotlinx.css.padding
import react.RBuilder
import styled.css
import styled.styledSpan


class StatementListComponent(props: UmProps): UstadListComponent<StatementWithSessionDetailDisplay,
        StatementWithSessionDetailDisplay>(props), StatementListView {

    private var mPresenter: StatementListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in StatementWithSessionDetailDisplay>?
        get() = mPresenter

    override val displayTypeRepo: Any?
        get() = dbRepo?.statementDao

    override fun RBuilder.renderListItem(item: StatementWithSessionDetailDisplay) {
        umGridContainer {
            val padding = LinearDimension("4px")
            css{
                padding(top = padding, bottom = padding)
            }

            umItem(GridSize.cells3 , GridSize.cells1){
                umItemThumbnail(VERB_ICON_MAP[item.statementVerbUid.toInt()] ?: "",
                    width = 50, avatarVariant = AvatarVariant.circle)
            }

            umItem(GridSize.cells9, GridSize.cells11){
                umGridContainer {
                    umItem(GridSize.cells12){
                        umTypography(item.verbDisplay,
                            variant = TypographyVariant.h6){
                            css (StyleManager.alignTextToStart)
                        }
                    }

                    umItem(GridSize.cells12){
                        umTypography(item.objectDisplay,
                            variant = TypographyVariant.body1,
                            paragraph = true){
                            css(StyleManager.alignTextToStart)
                        }
                    }

                    if(item.statementVerbUid != VerbEntity.VERB_ANSWERED_UID){
                        umItem(GridSize.cells12){
                            umTypography(setStatementQuestionAnswer(item),
                                variant = TypographyVariant.body1,
                                paragraph = true){
                                css(StyleManager.alignTextToStart)
                            }
                        }
                    }


                    umItem(GridSize.cells12){
                        umGridContainer {
                            umItem(GridSize.cells12, GridSize.cells3, flexDirection = FlexDirection.row) {
                                styledSpan {
                                    css{
                                        padding(right = 1.spacingUnits)
                                    }
                                    umIcon("calendar_today", fontSize = IconFontSize.small)
                                }

                                umTypography("${item.timestamp.toDate()?.standardFormat()} " +
                                        "- ${item.timestamp.toDate()?.formattedInHoursAndMinutes()}",
                                    variant = TypographyVariant.body1,
                                    paragraph = true){
                                    css(StyleManager.alignTextToStart)
                                }
                            }
                            if(item.resultDuration > 1000){
                                umItem(GridSize.cells12, GridSize.cells3, flexDirection = FlexDirection.row) {
                                    styledSpan {
                                        css{
                                            padding(right = 1.spacingUnits)
                                        }
                                        umIcon("timer", fontSize = IconFontSize.small)
                                    }

                                    umTypography(item.resultDuration.formatToStringHoursMinutesSeconds(systemImpl),
                                        variant = TypographyVariant.body1,
                                        paragraph = true){
                                        css(StyleManager.alignTextToStart)
                                    }
                                }
                            }
                        }
                    }

                    umItem(GridSize.cells12, flexDirection = FlexDirection.row){
                        styledSpan {
                            css{
                                padding(right = 1.spacingUnits)
                            }
                            umIcon("check", fontSize = IconFontSize.small)
                        }

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

                        if(item.resultScoreMax > 0){
                            styledSpan {
                                css{
                                    padding( right = 4.spacingUnits)
                                }

                                umTypography("(${item.resultScoreRaw} / ${item.resultScoreMax})",
                                    variant = TypographyVariant.body1,
                                    paragraph = true){
                                    css(StyleManager.alignTextToStart)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun handleClickEntry(entry: StatementWithSessionDetailDisplay) {}

    override var personWithContentTitle: String? = null
        set(value) {
            field = value
            ustadComponentTitle = value
        }


    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = false
        mPresenter = StatementListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun onFabClicked() {}

    override fun RBuilder.renderAddContentOptionsDialog() {}


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }

    companion object {
        val VERB_ICON_MAP = mapOf(
            VerbEntity.VERB_COMPLETED_UID.toInt() to "fact_check",
            VerbEntity.VERB_PROGRESSED_UID.toInt() to "show_chart",
            VerbEntity.VERB_ATTEMPTED_UID.toInt() to "ballot",
            VerbEntity.VERB_INTERACTED_UID.toInt() to "touch_app",
            VerbEntity.VERB_ANSWERED_UID.toInt() to "contact_support",
            VerbEntity.VERB_SATISFIED_UID.toInt() to "checklist",
            VerbEntity.VERB_PASSED_UID.toInt() to "checklist",
            VerbEntity.VERB_FAILED_UID.toInt() to "close")
    }
}