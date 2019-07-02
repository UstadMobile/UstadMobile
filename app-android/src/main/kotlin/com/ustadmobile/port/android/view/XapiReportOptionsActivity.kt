package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
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

    private lateinit var didChipGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xapi_report_options)

        visualTypeSpinner = findViewById(R.id.type_spinner)
        yAxisSpinner = findViewById(R.id.yaxis_spinner)
        xAxisSpinner = findViewById(R.id.xaxis_spinner)
        subGroupSpinner = findViewById(R.id.sub_group_spinner)
        autoCompleteView = findViewById(R.id.autoCompleteTextView)
        didChipGroup = findViewById(R.id.didChipGroup)

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


    override fun fillVisualChartType(translatedGraphList: List<String>) {
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, translatedGraphList)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        visualTypeSpinner.adapter = dataAdapter
    }

    override fun fillYAxisData(translatedYAxisList: List<String>) {
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, translatedYAxisList)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yAxisSpinner.adapter = dataAdapter
    }

    override fun fillXAxisAndSubGroupData(translatedXAxisList: List<String>) {
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, translatedXAxisList)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        xAxisSpinner.adapter = dataAdapter

        val subgroupAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, translatedXAxisList)
        subgroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subGroupSpinner.adapter = subgroupAdapter
    }

    override fun fillDidData(didList: List<String>) {
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, didList)
        autoCompleteView.setAdapter(dataAdapter)
        autoCompleteView.setOnItemClickListener { parent, _, position, _ ->


            val selected = parent.getItemAtPosition(position) as String
            addChipToGroup(selected, didChipGroup,autoCompleteView.text)

        }
    }


    private fun addChipToGroup(person: String, chipGroup: ChipGroup, text: Editable) {
        //val chip = LayoutInflater.from(this).inflate(R.layout.view_chip, chipGroup, false) as Chip

        val chip = ChipDrawable.createFromResource(this, R.xml.drawable_chip)
        chip.setText(person)
        chip.setBounds(0, 0, chip.intrinsicWidth, chip.intrinsicHeight)
        val span = ImageSpan(chip)
        text.setSpan(span, 0, person.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        // necessary to get single selection working
       // chipGroup.addView(chip as View)
        //chip.setOnCloseIconClickListener { chipGroup.removeView(chip as View) }
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