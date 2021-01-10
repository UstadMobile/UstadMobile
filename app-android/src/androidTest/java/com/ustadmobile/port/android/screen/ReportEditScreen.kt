package com.ustadmobile.port.android.screen

import android.content.Context
import android.view.View
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.fragment.findNavController
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.google.android.material.textfield.TextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.port.android.view.ReportEditFragment
import com.ustadmobile.test.port.android.KNestedScrollView
import com.ustadmobile.test.port.android.util.setDateField
import com.ustadmobile.test.port.android.util.setMessageIdOption
import org.hamcrest.Matcher

object ReportEditScreen : KScreen<ReportEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_report_edit
    override val viewClass: Class<*>?
        get() = ReportEditFragment::class.java

    val reportTitleInput = KTextInputLayout { withId(R.id.fragment_report_edit_title_layout) }

    val xAxisValue = KTextView { withId(R.id.fragment_edit_report_dialog_xaxis_text) }

    val fromDateTextInput = KTextInputLayout { withId(R.id.activity_report_edit_fromDate_textinputlayout) }

    val toDateTextInput = KTextInputLayout { withId(R.id.activity_report_edit_toDate_textinputlayout) }

    val seriesRecycler: KRecyclerView = KRecyclerView({
        withId(R.id.activity_report_edit_series_list)
    }, itemTypeBuilder = {
        itemType(::Series)
    })




    class Series(parent: Matcher<View>) : KRecyclerItem<Series>(parent) {

        val seriesNameTextInput = KTextInputLayout(parent) { withId(R.id.item_report_series_title_text_inputlayout) }

        val seriesNameTextView = KTextView(parent) { withId(R.id.item_report_series_title_text) }

        val deleteButton = KImageView(parent) { withId(R.id.item_report_series_delete_button) }

        val dataSetTextInput = KTextInputLayout(parent) { withId(R.id.item_edit_report_dialog_data_set_textinputlayout) }

        val dataSetTextView = KTextView(parent) { withId(R.id.item_edit_report_dialog_data_set_text) }

        val visualTypeTextInput = KTextInputLayout(parent) { withId(R.id.item_edit_report_dialog_visual_type_textinputlayout) }

        val visualTypeTextView = KTextView(parent) { withId(R.id.item_edit_report_dialog_visual_type_text) }

        val subgroupTextInput = KTextInputLayout(parent) { withId(R.id.item_edit_report_dialog_subgroup_textinputlayout) }

        val subgroupTextView = KTextView(parent) { withId(R.id.item_edit_report_dialog_subgroup_text) }

        val filterRecycler = KRecyclerView(parent, { withId(R.id.item_report_edit_filter_list) }, { itemType(::Filter) })

    }

    class Filter(parent: Matcher<View>) : KRecyclerItem<Filter>(parent) {

        val filterName = KTextView(parent) { withId(R.id.item_clazz_simple_line1_text)}

        val filterDeleteButton = KImageView(parent) { withId(R.id.item_clazz_simple_secondary_menu_imageview)}
    }


    val nestScroll = KNestedScrollView {
        withId(R.id.fragment_report_edit_edit_scroll)
    }

    fun fillFields(fragmentScenario: FragmentScenario<ReportEditFragment>? = null,
                   report: ReportWithSeriesWithFilters,
                   reportOnForm: ReportWithSeriesWithFilters? = ReportWithSeriesWithFilters(),
                   setFieldsRequiringNavigation: Boolean = true,
                   impl: UstadMobileSystemImpl, context: Context, testContext: TestContext<Unit>) {

        report.reportTitle?.takeIf { it != reportOnForm?.reportTitle }?.also {
            reportTitleInput {
                edit {
                    clearText()
                    typeText(it)
                }
            }
        }

        closeSoftKeyboard()

        report.xAxis.takeIf { it != reportOnForm?.xAxis }?.also {
            testContext.flakySafely {
                setMessageIdOption(xAxisValue,
                        impl.getString(ReportEditPresenter.XAxisOptions.values().find { report -> report.optionVal == it }!!.messageId, context))
            }
        }

        report.fromDate.takeIf { it != reportOnForm?.fromDate }?.also {
            setDateField(R.id.activity_report_edit_fromDate_textinputlayout, it)
        }
        report.toDate.takeIf { it != reportOnForm?.toDate }?.also {
            setDateField(R.id.activity_report_edit_toDate_textinputlayout, it)
        }

        seriesRecycler {

            var seriesCount = 0
            children<Series> {

                val series = report.reportSeriesWithFiltersList?.get(seriesCount)
                val seriesOnForm = reportOnForm?.reportSeriesWithFiltersList?.get(seriesCount)
                seriesCount++

                if (series == null) {
                    deleteButton.click()
                    return@seriesRecycler
                }

                series.reportSeriesName?.takeIf { it != seriesOnForm?.reportSeriesName }?.also {
                    seriesNameTextInput {
                        edit {
                            clearText()
                            typeText(it)
                        }
                    }
                }

                series.reportSeriesDataSet.takeIf { it != seriesOnForm?.reportSeriesDataSet }?.also {

                    testContext.flakySafely {
                        setMessageIdOption(this@children.dataSetTextView,
                                impl.getString(ReportEditPresenter.DataSetOptions.values()
                                        .find { report -> report.optionVal == it }!!.messageId, context))
                    }
                }

                series.reportSeriesVisualType.takeIf { it != seriesOnForm?.reportSeriesVisualType }?.also {

                    testContext.flakySafely {
                        setMessageIdOption(this@children.visualTypeTextView,
                                impl.getString(ReportEditPresenter.VisualTypeOptions.values()
                                        .find { report -> report.optionVal == it }!!.messageId, context))
                    }
                }

                series.reportSeriesSubGroup.takeIf { it != seriesOnForm?.reportSeriesSubGroup }?.also {

                    testContext.flakySafely {
                        setMessageIdOption(this@children.subgroupTextView,
                                impl.getString(ReportEditPresenter.SubGroupOptions.values()
                                        .find { report -> report.optionVal == it }!!.messageId, context))
                    }
                }

                filterRecycler {

                    var filterCount = 0
                    children<Filter> {

                        val filters = series.reportSeriesFilters?.get(filterCount)
                        val filtersOnForm = seriesOnForm?.reportSeriesFilters?.get(filterCount)
                        filterCount++

                        if(filters == null){
                            filterDeleteButton.click()
                            return@filterRecycler
                        }

                    }


                }



            }

        }

        if (!setFieldsRequiringNavigation) {
            return
        }

        /* fragmentScenario?.onFragment { fragment ->
             fragment.takeIf { reportOnForm. != null }
                     ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                     ?.set("ReportFilter", defaultGson().toJson(listOf(reportFilter)))
         }
 */
    }


}
