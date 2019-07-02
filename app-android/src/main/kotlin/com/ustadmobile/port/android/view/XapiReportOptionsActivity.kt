package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.XapiReportOptionsPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.XapiReportOptionsView
import java.util.*


class XapiReportOptionsActivity : UstadBaseActivity(), XapiReportOptionsView {

    private lateinit var visualTypeSpinner: Spinner

    private lateinit var yAxisSpinner: Spinner

    private lateinit var xAxisSpinner: Spinner

    private lateinit var subGroupSpinner: Spinner

    private lateinit var autoCompleteView: AutoCompleteTextView

    private lateinit var presenter: XapiReportOptionsPresenter

    private lateinit var didFlexBoxLayout: FlexboxLayout

    var didCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xapi_report_options)

        visualTypeSpinner = findViewById(R.id.type_spinner)
        yAxisSpinner = findViewById(R.id.yaxis_spinner)
        xAxisSpinner = findViewById(R.id.xaxis_spinner)
        subGroupSpinner = findViewById(R.id.sub_group_spinner)
        autoCompleteView = findViewById(R.id.didAutoCompleteTextView)
        didFlexBoxLayout = findViewById(R.id.didFlex)

        val toolbar = findViewById<Toolbar>(R.id.new_report_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "NewXapiReportOptions"

        presenter = XapiReportOptionsPresenter(viewContext,
                Objects.requireNonNull(UMAndroidUtil.bundleToMap(intent.extras)),
                this)
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_new_report, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setAdapterForSpinner(list: List<String>, spinner: Spinner) {
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, list)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = dataAdapter
    }


    override fun fillVisualChartType(translatedGraphList: List<String>) {
        setAdapterForSpinner(translatedGraphList, visualTypeSpinner)
    }

    override fun fillYAxisData(translatedYAxisList: List<String>) {
        setAdapterForSpinner(translatedYAxisList, yAxisSpinner)
    }

    override fun fillXAxisAndSubGroupData(translatedXAxisList: List<String>) {
        setAdapterForSpinner(translatedXAxisList, xAxisSpinner)
        setAdapterForSpinner(translatedXAxisList, subGroupSpinner)
    }

    override fun fillDidData(didList: List<String>) {
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, didList)
        autoCompleteView.setAdapter(dataAdapter)
        autoCompleteView.setOnItemClickListener { parent, _, position, _ ->
            autoCompleteView.text = null
            val selected = parent.getItemAtPosition(position) as String
            addChipToDidFlexLayout(selected, didFlexBoxLayout)

        }
    }

    private fun addChipToDidFlexLayout(text: String, flexGroup: FlexboxLayout) {
        val chip = LayoutInflater.from(this).inflate(R.layout.view_chip, flexGroup, false) as Chip
        chip.text = text
        flexGroup.addView(chip, didCount++)
        chip.setOnCloseIconClickListener {
            flexGroup.removeView(chip as View)
            --didCount
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_complete -> {
                presenter.handleViewReportPreview(
                        visualTypeSpinner.selectedItemPosition,
                        yAxisSpinner.selectedItemPosition,
                        xAxisSpinner.selectedItemPosition,
                        subGroupSpinner.selectedItemPosition)
                return true
            }

        }
        return true
    }


}