package com.ustadmobile.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.XapiReportOptionsPresenter
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.XLangMapEntryDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.XapiReportOptionsView
import kotlinx.coroutines.Job
import java.util.*


class XapiReportOptionsActivity : UstadBaseActivity(), XapiReportOptionsView {


    private lateinit var visualTypeSpinner: Spinner

    private lateinit var yAxisSpinner: Spinner

    private lateinit var xAxisSpinner: Spinner

    private lateinit var subGroupSpinner: Spinner

    private lateinit var didDataAdapter: ArrayAdapter<XLangMapEntryDao.Verb>

    private lateinit var didAutoCompleteView: AutoCompleteTextView

    private lateinit var didFlexBoxLayout: FlexboxLayout

    private lateinit var whoDataAdapter: ArrayAdapter<PersonDao.PersonNameAndUid>

    private lateinit var whoAutoCompleteView: AutoCompleteTextView

    private lateinit var whoFlexBoxLayout: FlexboxLayout

    private lateinit var presenter: XapiReportOptionsPresenter

    var whoTextChangedJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xapi_report_options)

        visualTypeSpinner = findViewById(R.id.type_spinner)
        yAxisSpinner = findViewById(R.id.yaxis_spinner)
        xAxisSpinner = findViewById(R.id.xaxis_spinner)
        subGroupSpinner = findViewById(R.id.sub_group_spinner)
        didAutoCompleteView = findViewById(R.id.didAutoCompleteTextView)
        didFlexBoxLayout = findViewById(R.id.didFlex)
        whoAutoCompleteView = findViewById(R.id.whoAutoCompleteTextView)
        whoFlexBoxLayout = findViewById(R.id.whoFlex)

        val toolbar = findViewById<Toolbar>(R.id.new_report_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "NewXapiReportOptions"

        presenter = XapiReportOptionsPresenter(viewContext,
                Objects.requireNonNull(UMAndroidUtil.bundleToMap(intent.extras)),
                this)
        presenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        whoDataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, listOf<PersonDao.PersonNameAndUid>())
        whoAutoCompleteView.setAdapter(whoDataAdapter)
        whoAutoCompleteView.addTextChangedListener(textWatcher)
        whoAutoCompleteView.setOnItemClickListener { parent, _, position, _ ->
            whoAutoCompleteView.text = null
            val selected = parent.getItemAtPosition(position) as PersonDao.PersonNameAndUid
            addChipToDidFlexLayout(selected.name, whoFlexBoxLayout, whoFlexBoxLayout.childCount - 1, selected.personUid)
        }

        didDataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, listOf<XLangMapEntryDao.Verb>())
        didAutoCompleteView.setAdapter(didDataAdapter)
        didAutoCompleteView.addTextChangedListener(textWatcher)
        didAutoCompleteView.setOnItemClickListener { parent, _, position, _ ->
            didAutoCompleteView.text = null
            val selected = parent.getItemAtPosition(position) as XLangMapEntryDao.Verb
            addChipToDidFlexLayout(selected.valueLangMap, didFlexBoxLayout, didFlexBoxLayout.childCount - 1, selected.verbLangMapUid)
        }

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

    override fun fillDidData(didList: List<XLangMapEntryDao.Verb>) {
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, didList)
        didAutoCompleteView.setAdapter(dataAdapter)
        didAutoCompleteView.setOnItemClickListener { parent, _, position, _ ->
            didAutoCompleteView.text = null
            val selected = parent.getItemAtPosition(position) as XLangMapEntryDao.Verb
            addChipToDidFlexLayout(selected.valueLangMap, didFlexBoxLayout, didFlexBoxLayout.childCount - 1, selected.verbLangMapUid)
        }
    }

    override fun updateWhoDataAdapter(whoList: List<PersonDao.PersonNameAndUid>) {
        whoDataAdapter.clear()
        whoDataAdapter.addAll(whoList)
        whoDataAdapter.notifyDataSetChanged()
    }

    override fun updateDidDataAdapter(didList: List<XLangMapEntryDao.Verb>) {
        didDataAdapter.clear()
        didDataAdapter.addAll(didList)
        didDataAdapter.notifyDataSetChanged()
    }


    private var textWatcher = object : TextWatcher {

        private var timer = Timer()
        private val DELAY: Long = 250 // milliseconds

        override fun afterTextChanged(s: Editable?) {
            timer.cancel()
            timer = Timer()
            timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            val hash = s.hashCode()
                            if (hash == whoAutoCompleteView.text.hashCode()) {
                                val name = whoAutoCompleteView.text.toString()
                                presenter.handleWhoDataTyped(name,whoFlexBoxLayout.children.filter {
                                    it is Chip
                                }.map {
                                    (it as Chip).tag as Long
                                }.toList())

                            } else if (hash == didAutoCompleteView.text.hashCode()) {
                                val verb = didAutoCompleteView.text.toString()
                                presenter.handleDidDataTyped(verb, didFlexBoxLayout.children.filter {
                                    it is Chip
                                }.map {
                                    (it as Chip).tag as Long
                                }.toList())
                            }
                        }
                    },
                    DELAY)

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }


    private fun addChipToDidFlexLayout(text: String, flexGroup: FlexboxLayout, count: Int, uid: Long) {
        val chip = LayoutInflater.from(this).inflate(R.layout.view_chip, flexGroup, false) as Chip
        chip.text = text
        chip.tag = uid
        flexGroup.addView(chip, count)
        chip.setOnCloseIconClickListener {
            flexGroup.removeView(chip as View)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_complete -> {
                presenter.handleViewReportPreview(
                        visualTypeSpinner.selectedItemPosition,
                        yAxisSpinner.selectedItemPosition,
                        xAxisSpinner.selectedItemPosition,
                        subGroupSpinner.selectedItemPosition,
                        didFlexBoxLayout.children.filter { it is Chip }.map {
                            (it as Chip).text.toString()
                        }.toList(),
                        whoFlexBoxLayout.children.filter { it is Chip }.map {
                            (it as Chip).tag as Long
                        }.toList())
                return true
            }

        }
        return true
    }


}