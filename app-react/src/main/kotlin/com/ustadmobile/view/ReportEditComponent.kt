package com.ustadmobile.view

import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ObjectMessageIdOption
import com.ustadmobile.core.util.ext.toDisplayString
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.lib.db.entities.DateRangeMoment
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.FieldLabel
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.*
import kotlinx.css.*
import mui.material.FormControlVariant
import react.RBuilder
import react.setState
import styled.css
import styled.styledSpan

class ReportEditComponent (mProps: UmProps): UstadEditComponent<ReportWithSeriesWithFilters>(mProps),
    ReportEditView {

    private var mPresenter: ReportEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ReportWithSeriesWithFilters>?
        get() = mPresenter

    private var showSeriesDeleteButton = false

    private var reportTitleLabel = FieldLabel(text = getString(MessageID.xapi_options_report_title))

    private var reportDescLabel = FieldLabel(text = getString(MessageID.description))

    private var seriesYAxisLabel = FieldLabel(text = getString(MessageID.xapi_options_y_axes))

    private var seriesTitleLabel = FieldLabel(text = getString(MessageID.title))

    private var seriesVisualLabel = FieldLabel(text = getString(MessageID.xapi_options_visual_type))

    private var seriesSubGroupLabel = FieldLabel(text = getString(MessageID.xapi_options_subgroup))

    private var xAxisLabel = FieldLabel(text = getString(MessageID.xapi_options_x_axes))

    private var timeRangeLabel = FieldLabel(text = getString(MessageID.time_range))

    private var seriesList: List<ReportSeries> = listOf()

    override var visualTypeOptions: List<ReportEditPresenter.VisualTypeMessageIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var xAxisOptions: List<ReportEditPresenter.XAxisMessageIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var subGroupOptions: List<ReportEditPresenter.SubGroupByMessageIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var yAxisOptions: List<ReportEditPresenter.YAxisMessageIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var dateRangeOptions: List<ObjectMessageIdOption<DateRangeMoment>>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var selectedDateRangeMoment: DateRangeMoment? = null
        get() = field
        set(value) {
            setState {
                field = value
                if(value == null) return@setState
                entity?.fromDate = value.fromMoment.fixedTime
                entity?.fromRelTo = value.fromMoment.relTo
                entity?.fromRelOffSet = value.fromMoment.relOffSet
                entity?.fromRelUnit = value.fromMoment.relUnit

                entity?.toDate = value.toMoment.fixedTime
                entity?.toRelTo = value.toMoment.relTo
                entity?.toRelOffSet = value.toMoment.relOffSet
                entity?.toRelUnit = value.toMoment.relUnit
            }
        }

    override var titleErrorText: String? = null
        get() = field
        set(value) {
            setState {
                field = value
                reportTitleLabel = reportTitleLabel.copy(errorText = value)
            }
        }

    override var fieldsEnabled: Boolean = false
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
                showSeriesDeleteButton = (value?.reportSeriesWithFiltersList?.size ?: 0) > 1
                seriesList = value?.reportSeriesWithFiltersList ?: listOf()
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        setEditTitle(MessageID.create_a_new_report, MessageID.edit_report)
        mPresenter = ReportEditPresenter(this, arguments, this,
            di, this)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        umGridContainer {
            css{
                +fieldsOnlyFormScreen
                +defaultPaddingTop
            }

            umItem(GridSize.cells12){

                umTextField(label = "${reportTitleLabel.text}",
                    helperText = reportTitleLabel.errorText,
                    value = entity?.reportTitle,
                    error = reportTitleLabel.error,
                    fullWidth = true,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.reportTitle = it
                            titleErrorText = null
                        }
                    })


                umTextField(label = "${reportDescLabel.text}",
                    value = entity?.reportDescription,
                    error = reportDescLabel.error,
                    disabled = !fieldsEnabled,
                    helperText = reportDescLabel.errorText,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.reportDescription = it
                        }
                    })

                umGridContainer(columnSpacing =GridSpacing.spacing4) {

                    umItem(GridSize.cells12, GridSize.cells6){
                        umTextFieldSelect(
                            "${xAxisLabel.text}",
                            entity?.xAxis.toString(),
                            xAxisLabel.errorText ?: "",
                            error = xAxisLabel.error,
                            values = xAxisOptions?.map {
                                Pair(it.code.toString(), it.toString())
                            }?.toList(),
                            onChange = {
                                setState {
                                    entity?.xAxis = it.toInt()
                                }
                            }
                        )
                    }

                    umItem(GridSize.cells12, GridSize.cells6){
                        umTextFieldSelect(
                            "${timeRangeLabel.text}",
                            entity?.reportDateRangeSelection.toString(),
                            timeRangeLabel.errorText ?: "",
                            error = timeRangeLabel.error,
                            values = dateRangeOptions?.map {
                                Pair(it.code.toString(), it.toString())
                            }?.toList(),
                            onChange = {
                                setState {
                                    entity?.reportDateRangeSelection = it.toInt()
                                }
                                if(it.toInt() == ReportEditPresenter.DateRangeOptions.NEW_CUSTOM_RANGE.code) {
                                    mPresenter?.handleDateRangeChange()
                                }
                                val option = IdOption("",it.toInt())
                                mPresenter?.handleDateRangeSelected(option)
                                mPresenter?.handleXAxisSelected(option)
                            }
                        )
                    }
                }

                if(seriesList.isNotEmpty()){
                    umItem(GridSize.cells12){
                        css(horizontalList)
                        for (series in seriesList){
                           renderSeriesItem(series, seriesList.size > 1 && showSeriesDeleteButton)
                        }
                    }
                }

                umItem(GridSize.cells12) {
                    umListItem(button = true){
                        attrs.onClick = {
                            mPresenter?.handleClickAddSeries()
                        }
                        renderCreateNewItemOnList(getString(MessageID.xapi_options_series))
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

    private fun RBuilder.renderSeriesItem(series: ReportSeries, showDelete : Boolean){
        umGridContainer {
            umItem(GridSize.cells12) {
                umGridContainer {
                    umItem(GridSize.cells12, flexDirection = FlexDirection.row) {
                        umTextField(label = "${seriesTitleLabel.text}",
                            helperText = seriesTitleLabel.errorText,
                            value = series.reportSeriesName,
                            error = seriesTitleLabel.error,
                            fullWidth = true,
                            disabled = !fieldsEnabled,
                            variant = FormControlVariant.outlined,
                            onChange = {
                                setState {
                                    series.reportSeriesName = it
                                }
                            }){
                            css{
                                width = LinearDimension(if(!showDelete) "100%" else "95%")
                            }
                        }

                        if(showDelete){
                            styledSpan {
                                css{
                                    margin(left = 3.spacingUnits, top = 2.spacingUnits)
                                    width = 40.px
                                }

                                umIconButton("close", onClick = {
                                    mPresenter?.handleRemoveSeries(series)
                                })
                            }
                        }
                    }
                }

                umGridContainer(columnSpacing =  GridSpacing.spacing4) {
                    umItem(GridSize.cells12,GridSize.cells4){
                        umTextFieldSelect(
                            "${seriesYAxisLabel.text}",
                            series.reportSeriesYAxis.toString(),
                            seriesYAxisLabel.errorText ?: "",
                            error = seriesYAxisLabel.error,
                            values = yAxisOptions?.map {
                                Pair(it.code.toString(), it.toString())
                            }?.toList(),
                            onChange = {
                                setState {
                                    series.reportSeriesYAxis = it.toInt()
                                }
                            }
                        )
                    }

                    umItem(GridSize.cells12,GridSize.cells4){
                        umTextFieldSelect(
                            "${seriesVisualLabel.text}",
                            series.reportSeriesVisualType.toString(),
                            seriesVisualLabel.errorText ?: "",
                            error = seriesVisualLabel.error,
                            values = visualTypeOptions?.map {
                                Pair(it.code.toString(), it.toString())
                            }?.toList(),
                            onChange = {
                                setState {
                                    series.reportSeriesVisualType = it.toInt()
                                }
                            }
                        )
                    }

                    umItem(GridSize.cells12,GridSize.cells4){
                        umTextFieldSelect(
                            "${seriesSubGroupLabel.text}",
                            series.reportSeriesSubGroup.toString(),
                            seriesSubGroupLabel.errorText ?: "",
                            error = seriesSubGroupLabel.error,
                            values = subGroupOptions?.map {
                                Pair(it.code.toString(), it.toString())
                            }?.toList(),
                            onChange = {
                                setState {
                                    series.reportSeriesSubGroup = it.toInt()
                                }
                            }
                        )
                    }

                }
            }

            umItem(GridSize.cells12) {
                renderListSectionTitle(getString(MessageID.filter))
            }

            val filters = series.reportSeriesFilters ?: listOf()

            if(filters.isNotEmpty()){
                umItem(GridSize.cells12){
                    css(horizontalList)
                    for(filter in filters){
                        umListItem(button = true) {
                            attrs.onClick = {
                                Util.stopEventPropagation(it)
                                mPresenter?.handleOnFilterClicked(filter)
                            }

                            renderListItemWithTitleDescriptionAndRightAction(
                                filter.toDisplayString(systemImpl, Any()), "delete",
                                withAction = true){
                                Util.stopEventPropagation(it)
                                mPresenter?.handleRemoveFilter(filter)
                            }
                        }
                    }
                }
            }

            umItem(GridSize.cells12){
                umListItem(button = true) {
                    attrs.onClick = {
                        Util.stopEventPropagation(it)
                        mPresenter?.handleOnFilterClicked(ReportFilter().apply {
                            reportFilterSeriesUid = series.reportSeriesUid
                        })
                    }
                    renderCreateNewItemOnList(getString(MessageID.filter))
                }
            }
        }
    }

}