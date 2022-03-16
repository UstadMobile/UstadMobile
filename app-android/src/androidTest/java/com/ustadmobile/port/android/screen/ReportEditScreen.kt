package com.ustadmobile.port.android.screen

import android.content.Context
import android.view.View
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso
import io.github.kakaocup.kakao.common.views.KSwipeView
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toDisplayString
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.port.android.view.ReportEditFragment
import com.ustadmobile.test.port.android.KNestedScrollView
import com.ustadmobile.test.port.android.util.getApplicationDi
import com.ustadmobile.test.port.android.util.setMessageIdOption
import org.hamcrest.Matcher
import org.kodein.di.direct
import org.kodein.di.instance

object ReportEditScreen : KScreen<ReportEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_report_edit
    override val viewClass: Class<*>?
        get() = ReportEditFragment::class.java


    val nestedScroll = KSwipeView { withId(R.id.fragment_report_edit_edit_scroll) }

    val reportTitleInput = KTextInputLayout { withId(R.id.fragment_report_edit_title_layout) }

    val xAxisValue = KTextView { withId(R.id.fragment_edit_report_dialog_xaxis_text) }

    val dateRangeTextInput = KTextInputLayout { withId(R.id.fragment_edit_report_dialog_daterange_textinputlayout) }

    val seriesRecycler: KRecyclerView = KRecyclerView({
        withId(R.id.activity_report_edit_series_list)
    }, itemTypeBuilder = {
        itemType(::Series)
    })


    class Series(parent: Matcher<View>) : KRecyclerItem<Series>(parent) {

        val seriesNameTextInput = KTextInputLayout(parent) { withId(R.id.item_report_series_title_text_inputlayout) }

        val seriesNameTextView = KTextView(parent) { withId(R.id.item_report_series_title_text) }

        val deleteButton = KImageView(parent) { withId(R.id.item_report_series_delete_button) }

        val dataSetTextInput = KTextInputLayout(parent) { withId(R.id.item_edit_report_dialog_yaxis_textinputlayout) }

        val dataSetTextView = KTextView(parent) { withId(R.id.item_edit_report_dialog_yaxis_text) }

        val visualTypeTextInput = KTextInputLayout(parent) { withId(R.id.item_edit_report_dialog_visual_type_textinputlayout) }

        val visualTypeTextView = KTextView(parent) { withId(R.id.item_edit_report_dialog_visual_type_text) }

        val subgroupTextInput = KTextInputLayout(parent) { withId(R.id.item_edit_report_dialog_subgroup_textinputlayout) }

        val subgroupTextView = KTextView(parent) { withId(R.id.item_edit_report_dialog_subgroup_text) }

        val filterRecycler = KRecyclerView(parent, { withId(R.id.item_report_edit_filter_list) }, { itemType(::Filter) })

        val addFilterButton = KView(parent) { withId(R.id.item_edit_report_filter_add_layout)}

    }

    class Filter(parent: Matcher<View>) : KRecyclerItem<Filter>(parent) {

        val filterName = KTextView(parent) { withId(R.id.item_clazz_simple_line1_text) }

        val filterDeleteButton = KImageView(parent) {
            withId(R.id.item_clazz_simple_secondary_menu_imageview)
        }
    }


    val nestScroll = KNestedScrollView {
        withId(R.id.fragment_report_edit_edit_scroll)
    }

    fun fillFields(fragmentScenario: FragmentScenario<ReportEditFragment>? = null,
                   updatedReport: ReportWithSeriesWithFilters,
                   reportOnForm: ReportWithSeriesWithFilters? = ReportWithSeriesWithFilters(),
                   setFieldsRequiringNavigation: Boolean = true,
                   impl: UstadMobileSystemImpl, context: Context, testContext: TestContext<Unit>) {

        updatedReport.reportTitle?.takeIf { it != reportOnForm?.reportTitle }?.also {
            reportTitleInput {
                edit {
                    clearText()
                    typeText(it)
                }
            }
        }

        closeSoftKeyboard()

        updatedReport.xAxis.takeIf { it != reportOnForm?.xAxis }?.also {
            testContext.flakySafely {
                xAxisValue.setMessageIdOption(
                        impl.getString(ReportEditPresenter.XAxisOptions.values()
                                .find { report -> report.optionVal == it }!!.messageId, context))
            }
        }



        seriesRecycler {

            var seriesCount = 0
            children<Series> {

                this@ReportEditScreen.nestedScroll.swipeUp()

                val series = updatedReport.reportSeriesWithFiltersList?.getOrNull(seriesCount)
                val seriesOnForm = reportOnForm?.reportSeriesWithFiltersList?.getOrNull(seriesCount)
                seriesCount++

                if (series == null) {
                    deleteButton.click()
                    return@seriesRecycler
                }

                this@seriesRecycler.scrollTo {
                    withTag(seriesOnForm!!.reportSeriesUid)
                }

                series.reportSeriesName?.takeIf { it != seriesOnForm?.reportSeriesName }?.also {
                    seriesNameTextInput {
                        edit {
                            clearText()
                            typeText(it)
                            hasText(it)
                        }
                    }
                }
                Espresso.closeSoftKeyboard()

                series.reportSeriesYAxis.takeIf { it != seriesOnForm?.reportSeriesYAxis }?.also {

                    testContext.flakySafely {
                        this@children.dataSetTextView.setMessageIdOption(
                                impl.getString(ReportEditPresenter.YAxisOptions.values()
                                        .find { report -> report.optionVal == it }!!.messageId, context))
                    }
                }

                series.reportSeriesVisualType.takeIf { it != seriesOnForm?.reportSeriesVisualType }?.also {

                    testContext.flakySafely {
                        this@children.visualTypeTextView.setMessageIdOption(
                                impl.getString(ReportEditPresenter.VisualTypeOptions.values()
                                        .find { report -> report.optionVal == it }!!.messageId, context))
                    }
                }


                series.reportSeriesSubGroup.takeIf { it != seriesOnForm?.reportSeriesSubGroup }?.also {

                    testContext.flakySafely {
                        this@children.subgroupTextView.setMessageIdOption(
                                impl.getString(ReportEditPresenter.SubGroupOptions.values()
                                        .find { report -> report.optionVal == it }!!.messageId, context))
                    }
                }

                val sizeOfFilters = seriesOnForm?.reportSeriesFilters?.size ?: 0

                if (series.reportSeriesFilters?.isNotEmpty() == true) {

                    filterRecycler {

                        var filterCount = 0

                        hasSize(sizeOfFilters)
                        if(sizeOfFilters == 0){
                            return@filterRecycler
                        }

                        children<Filter> {

                            val filters = series.reportSeriesFilters?.getOrNull(filterCount)
                            val filtersOnForm = seriesOnForm?.reportSeriesFilters?.getOrNull(filterCount)
                            filterCount++

                            if (filters == null) {
                                filterDeleteButton.click()
                                return@filterRecycler
                            }

                            filterName {
                                val di = getApplicationDi()
                                hasText(filters.toDisplayString(di.direct.instance(), context))
                            }

                        }

                    }
                }

            }

        }

        if (!setFieldsRequiringNavigation) {
            return
        }


    }


}
