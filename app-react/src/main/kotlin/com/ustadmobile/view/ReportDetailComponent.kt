package com.ustadmobile.view

import com.ustadmobile.core.controller.ReportDetailPresenter
import com.ustadmobile.core.controller.StatementConstants
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.ChartData
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.ReportTemplateListView
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.lib.db.entities.StatementEntityWithDisplayDetails
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager.alignCenterItems
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.scrollOnMobile
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.exportToPng
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.util.getViewNameFromUrl
import com.ustadmobile.view.ext.renderChart
import com.ustadmobile.view.ext.renderTopMainAction
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.Overflow
import kotlinx.css.borderBottom
import kotlinx.css.borderTop
import kotlinx.css.overflow
import mui.material.styles.TypographyVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.json

class ReportDetailComponent(mProps: UmProps): UstadDetailComponent<ReportWithSeriesWithFilters>(mProps),
    ReportDetailView {

    private var mPresenter: ReportDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    var showChart = true

    override var saveAsTemplateVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private val seriesTitle: MutableList<String?> = mutableListOf()

    private val statementSeriesList: MutableList<List<StatementEntityWithDisplayDetails>> = mutableListOf()

    override var statementListDetails: List<DataSourceFactory<Int, StatementEntityWithDisplayDetails>>? = null
        set(value) {
            field = value
            if(value?.isNotEmpty() == true){
                repeat(value.size){ index ->
                    seriesTitle.add(chartData?.seriesData?.get(index)?.series?.reportSeriesName)
                    val liveData = value[index].getData(0,Int.MAX_VALUE)
                    val observerFnWrapper = ObserverFnWrapper<List<StatementEntityWithDisplayDetails>>{
                        statementSeriesList.add(it)
                        setState {}
                    }
                    liveData.removeObserver(observerFnWrapper)
                    liveData.observe(this, observerFnWrapper)
                }
            }
        }

    override var chartData: ChartData? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: ReportWithSeriesWithFilters? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
            ustadComponentTitle = value?.reportTitle
        }


    override fun onCreateView() {
        super.onCreateView()
        editButtonMode = EditButtonMode.FAB
        mPresenter = ReportDetailPresenter(this, arguments, this,
            di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css{
                +contentContainer
                +defaultMarginTop
                +scrollOnMobile
            }
            umGridContainer(GridSpacing.spacing2) {
                umItem(GridSize.cells12){
                    umItem(GridSize.cells12){
                        umGridContainer(GridSpacing.spacing4) {
                            if(showChart){
                                renderTopMainAction("exit_to_app",
                                    "${getString(MessageID.export)} ${getString(MessageID.report)}",
                                    GridSize.cells12,
                                    GridSize.cells4,
                                    visible = true){
                                    exportToPng("chat-area", entity?.reportTitle)
                                }
                            }

                            renderTopMainAction("addchart",
                                getString(MessageID.add_to).format(getString(MessageID.dashboard)),
                                GridSize.cells12, GridSize.cells4,
                                visible = chartData?.reportWithFilters?.reportUid ?: 0 == 0L){
                                chartData?.reportWithFilters?.let {
                                    mPresenter?.handleOnClickAddFromDashboard(it)
                                    if (it.reportUid == 0L) {
                                        navController.popBackStack(ReportEditView.VIEW_NAME, true)
                                        navController.popBackStack(ReportTemplateListView.VIEW_NAME, true)
                                    }
                                }
                            }

                            renderTopMainAction("post_add",
                                getString(MessageID.save_as_template),
                                GridSize.cells12, GridSize.cells4,
                                visible = saveAsTemplateVisible){
                                chartData?.reportWithFilters?.let {
                                    mPresenter?.handleOnClickAddAsTemplate(it)
                                    showSnackBar(getString(MessageID.added))
                                }
                            }
                        }

                        if(showChart){
                            umItem(GridSize.cells12){
                                css{
                                    +defaultDoubleMarginTop
                                    +alignCenterItems
                                }
                                //ID is used for exporting the chart
                                attrs.asDynamic().id = "chat-area"
                                renderChart(chartData){
                                    showChart = it
                                }
                            }
                        }
                    }
                }

                umItem(GridSize.cells12){
                    css{
                        overflow = Overflow.scroll
                    }
                    umPaper {
                        umTableContainer {
                            attrs.asDynamic().sx = json(Pair("maxHeight", "500px"))
                            umTable(stickyHeader = true) {
                                umTableHead {
                                    umTableRow {
                                        umTableCell {
                                            umTypography(getString(MessageID.person))
                                        }

                                        umTableCell {
                                            umTypography(getString(MessageID.xapi_verb_header))
                                        }

                                        umTableCell {
                                            umTypography(getString(MessageID.xapi_result_header))
                                        }

                                        umTableCell {
                                            umTypography(getString(MessageID.xapi_options_when))
                                        }
                                    }
                                }

                                statementSeriesList.forEachIndexed { index, statements ->
                                    umTableBody {
                                        umTableRow {
                                            css{
                                                borderBottom = "0px solid transparent"
                                                borderTop = "0px solid transparent"
                                            }
                                            umTableCell(colSpan = 4) {
                                                umTypography(seriesTitle[index],
                                                    variant = TypographyVariant.h6)
                                            }
                                        }

                                        statements.forEach { statement ->
                                            umTableRow {
                                                umTableCell{
                                                    umTypography(statement.person?.fullName(),
                                                        variant = TypographyVariant.body1)
                                                }

                                                umTableCell{
                                                    umTypography(statement.xlangMapEntry?.valueLangMap,
                                                        variant = TypographyVariant.body1)
                                                }

                                                umTableCell{
                                                    umTypography(getString(StatementConstants.STATEMENT_RESULT_OPTIONS[statement.resultSuccess.toInt()] ?: 0),
                                                        variant = TypographyVariant.body1)
                                                }

                                                umTableCell{
                                                    umTypography(statement.timestamp.toDate()?.standardFormat(),
                                                        variant = TypographyVariant.body1)
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

    override fun onFabClicked() {
        val report = entity
        if (report == null || report.reportUid == 0L) {
            getViewNameFromUrl()?.let {
                navController.popBackStack(it, true)
            }
        } else mPresenter?.handleClickEdit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }
}