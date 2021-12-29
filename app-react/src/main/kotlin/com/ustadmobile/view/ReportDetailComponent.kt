package com.ustadmobile.view

import com.ustadmobile.core.controller.ReportDetailPresenter
import com.ustadmobile.core.controller.StatementConstants.STATEMENT_RESULT_OPTIONS
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.ChartData
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.ReportTemplateListView
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.lib.db.entities.StatementEntityWithDisplayDetails
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager.screenWithChartOnLeft
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.format
import com.ustadmobile.util.ext.standardFormat
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.createTopMainAction
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.border
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ReportDetailComponent(mProps: UmProps): UstadDetailComponent<ReportWithSeriesWithFilters>(mProps),
    ReportDetailView {

    private var mPresenter: ReportDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override val viewName: String
        get() = ReportDetailView.VIEW_NAME

    override var saveAsTemplateVisible: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    private val seriesTitle: MutableList<String?> = mutableListOf()

    private val statementSeriesList: MutableList<List<StatementEntityWithDisplayDetails>> = mutableListOf()

    override var statementListDetails: List<DoorDataSourceFactory<Int, StatementEntityWithDisplayDetails>>? = null
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
            css(screenWithChartOnLeft)
            umGridContainer(columnSpacing = GridSpacing.spacing4) {
                umItem(GridSize.cells12, GridSize.cells6){
                    umGridContainer {

                        umItem(GridSize.cells12) {
                            createTopMainAction("exit_to_app",getString(MessageID.export), GridSize.cells4, GridSize.cells4){
                                //export to png
                            }

                            if(chartData?.reportWithFilters?.reportUid ?: 0 == 0L){
                                createTopMainAction("addchart",getString(MessageID.add_to).format(getString(MessageID.dashboard)), GridSize.cells4, GridSize.cells4){
                                    chartData?.reportWithFilters?.let {
                                        mPresenter?.handleOnClickAddFromDashboard(it)
                                        if (it.reportUid == 0L) {
                                            navController.popBackStack(ReportEditView.VIEW_NAME, true)
                                            navController.popBackStack(ReportTemplateListView.VIEW_NAME, true)
                                        }
                                    }
                                }
                            }

                            if(saveAsTemplateVisible){
                                createTopMainAction("post_add",getString(MessageID.save_as_template), GridSize.cells4, GridSize.cells4){
                                    chartData?.reportWithFilters?.let {
                                        mPresenter?.handleOnClickAddAsTemplate(it)
                                        showSnackBar(getString(MessageID.added))
                                    }
                                }
                            }

                        }
                    }
                }

                umItem(GridSize.cells12, GridSize.cells6){
                   umTableContainer {
                       umTable {
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
                                          border = "0px solid transparent"
                                      }
                                      umTableCell(colSpan = 4) {
                                          umTypography(seriesTitle[index]) {  }
                                      }
                                  }

                                  statements.forEach { statement ->
                                      umTableRow {
                                          umTableCell{
                                              umTypography(statement.person?.fullName()) {  }
                                          }

                                          umTableCell{
                                              umTypography(statement.xlangMapEntry?.valueLangMap) {  }
                                          }

                                          umTableCell{
                                              umTypography(getString(STATEMENT_RESULT_OPTIONS[statement.resultSuccess.toInt()] ?: 0)) {  }
                                          }

                                          umTableCell{
                                              umTypography(statement.timestamp.toDate().standardFormat()) {  }
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
        entity = null
    }
}