package com.ustadmobile.view

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ReportListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.ChartData
import com.ustadmobile.core.util.ext.generateChartData
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.view.ReportListView
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.mui.components.spacingUnits
import com.ustadmobile.mui.components.umCircularProgress
import com.ustadmobile.mui.components.umTypography
import com.ustadmobile.util.StyleManager.alignCenterItems
import com.ustadmobile.util.StyleManager.alignTextToStart
import com.ustadmobile.util.StyleManager.maxLines
import com.ustadmobile.util.ThemeManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.renderChart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.serialization.builtins.ListSerializer
import mui.material.CircularProgressColor
import mui.material.styles.TypographyVariant
import org.kodein.di.direct
import org.kodein.di.instance
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ReportListComponent(mProps: UmProps):  UstadListComponent<Report, Report>(mProps),
    ReportListView {

    private var mPresenter: ReportListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.reportDao

    override val listPresenter: UstadListPresenter<*, in Report>?
        get() = mPresenter

    private val chartDataMap: MutableMap<Long, ChartData?> = mutableMapOf()

    override fun onCreateView() {
        super.onCreateView()
        linearLayout = false
        addNewEntryText = getString(MessageID.create_a_new_report)
        fabManager?.text = getString(MessageID.report)
        mPresenter = ReportListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun onDataListLoaded() {
        super.onDataListLoaded()
        GlobalScope.launch(Dispatchers.Main) {
            dataListItems.forEach { item ->
                val series = if (!item.reportSeries.isNullOrEmpty()) {
                    safeParseList(di, ListSerializer(ReportSeries.serializer()),
                        ReportSeries::class, item.reportSeries ?: "")
                } else {
                    listOf()
                }
                val accountManager: UstadAccountManager = di.direct.instance()
                val reportWithSeriesWithFilters = ReportWithSeriesWithFilters(item, series)

                val chartData = dbRepo?.generateChartData(reportWithSeriesWithFilters,
                    this, di.direct.instance(), accountManager.activeAccount.personUid)
                chartDataMap[item.reportUid] = chartData
                setState {  }
            }
        }
    }

    override fun RBuilder.renderListItem(item: Report) {
        val chartData = chartDataMap[item.reportUid]

        if(chartData == null){
            styledDiv {
                css {
                    +alignCenterItems
                    width = LinearDimension("100%")
                    height = 300.px
                    padding(top = LinearDimension("30%"))
                }

                umCircularProgress(
                    thickness = 5.0,
                    size = 80,
                    color = if(ThemeManager.isDarkModeActive())
                        CircularProgressColor.secondary else  CircularProgressColor.primary){}
            }
        }else {
            styledDiv {
                css{
                    position = Position.relative
                }
                renderChart(chartData, height = 200){
                    if(!it){
                        chartDataMap.remove(item.reportUid)
                    }
                }
            }
            styledDiv {
                css {
                    padding(2.spacingUnits)
                }

                umTypography(item.reportTitle, variant = TypographyVariant.h6){
                    css{
                        +alignTextToStart
                        maxLines(this,2)
                    }
                }
            }
        }
    }

    override fun handleClickEntry(entry: Report) {
        mPresenter?.handleClickEntry(entry)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}