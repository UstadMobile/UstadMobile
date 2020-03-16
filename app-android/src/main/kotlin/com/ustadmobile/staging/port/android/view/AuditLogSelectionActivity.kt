package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.AuditLogSelectionPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.AuditLogSelectionView
import com.ustadmobile.port.android.view.*
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*

class AuditLogSelectionActivity : UstadBaseActivity(), AuditLogSelectionView,
        SelectClazzesDialogFragment.ClazzSelectDialogListener,
        SelectMultipleTreeDialogFragment.MultiSelectTreeDialogListener,
        SelectTwoDatesDialogFragment.CustomTimePeriodDialogListener,
        SelectPeopleDialogFragment.PersonSelectDialogListener {

    private var toolbar: Toolbar? = null
    private var mPresenter: AuditLogSelectionPresenter? = null
    private var timePeriodSpinner: Spinner? = null
    private var classesTextView: TextView? = null
    private var locationsTextView: TextView? = null
    private var personTextView: TextView? = null
    private var actorTextView: TextView? = null


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_audit_log_selection)

        //Toolbar:
        toolbar = findViewById(R.id.activity_audit_log_selection_toolbar)
        toolbar!!.title = getText(R.string.audit_log)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        timePeriodSpinner = findViewById(R.id.activity_audit_log_time_period_spinner)
        classesTextView = findViewById(R.id.activity_audit_log_classes_edittext)
        locationsTextView = findViewById(R.id.activity_audit_log_location_edittext)
        personTextView = findViewById(R.id.activity_audit_log_person_edittext)
        actorTextView = findViewById(R.id.activity_audit_log_changed_by_edittext)


        //Call the Presenter
        mPresenter = AuditLogSelectionPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        updateClassesIfEmpty()
        updateLocationIfEmpty()
        updatePeopleIfEmpty()
        updateActorIfEmpty()


        timePeriodSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleTimePeriodSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        classesTextView!!.setOnClickListener { v -> mPresenter!!.goToSelectClassesDialog() }

        locationsTextView!!.setOnClickListener { v -> mPresenter!!.goToLocationDialog() }

        personTextView!!.setOnClickListener { v -> mPresenter!!.goToPersonDialog() }

        actorTextView!!.setOnClickListener { v -> mPresenter!!.goToActorDialog() }


        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_audit_log_selection_fab)

        fab.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton() }


    }

    override fun onSelectClazzesResult(selectedClazzes: HashMap<String, Long>?) {
        val classesSelectedString = StringBuilder()
        val selectedClazzesNameIterator = selectedClazzes!!.keys.iterator()
        while (selectedClazzesNameIterator.hasNext()) {
            classesSelectedString.append(selectedClazzesNameIterator.next())
            if (selectedClazzesNameIterator.hasNext()) {
                classesSelectedString.append(", ")
            }
        }
        val selectedClassesList = ArrayList(selectedClazzes.values)
        mPresenter!!.selectedClasses = selectedClassesList

        updateClazzesSelected(classesSelectedString.toString())
    }

    override fun onLocationResult(selectedLocations: HashMap<String, Long>) {
        val selectedLocationsNameIterator = selectedLocations.keys.iterator()
        val locationsSelectedString = StringBuilder()
        while (selectedLocationsNameIterator.hasNext()) {
            locationsSelectedString.append(selectedLocationsNameIterator.next())
            if (selectedLocationsNameIterator.hasNext()) {
                locationsSelectedString.append(", ")
            }
        }
        val selectedLocationList = ArrayList(selectedLocations.values)
        mPresenter!!.selectedLocations = selectedLocationList

        updateLocationsSelected(locationsSelectedString.toString())
    }

    override fun onSelectPeopleListener(selected: HashMap<String, Long>?, actor: Boolean) {

        val selectedPeopleNamesIterator = selected!!.keys.iterator()
        val peopleSelectedString = StringBuilder()
        while (selectedPeopleNamesIterator.hasNext()) {
            peopleSelectedString.append(selectedPeopleNamesIterator.next())
            if (selectedPeopleNamesIterator.hasNext()) {
                peopleSelectedString.append(", ")
            }
        }
        val selectedPeopleList = ArrayList(selected.values)

        if (actor) {
            //Find out if its actors or people
            mPresenter!!.selectedActors = selectedPeopleList
            updateActorsSelected(peopleSelectedString.toString())
        } else {
            mPresenter!!.selectedPeople = selectedPeopleList
            updatePeopleSelected(peopleSelectedString.toString())
        }
    }

    override fun onCustomTimesResult(from: Long, to: Long) {
        mPresenter!!.fromTime = from
        mPresenter!!.toTime = to

        Toast.makeText(
                applicationContext,
                "Custom date from : $from to $to",
                Toast.LENGTH_SHORT
        ).show()

    }

    /** Will add "ALL" to Classes if called
     *
     */
    fun updateClassesIfEmpty() {
        updateClazzesSelected(getText(R.string.all).toString())
        mPresenter!!.selectedClasses = null
    }

    /** Will add "ALL" to Locations if called
     *
     */
    fun updateLocationIfEmpty() {
        updateLocationsSelected(getText(R.string.all).toString())
        mPresenter!!.selectedLocations = null
    }

    /** Will add "ALL" to People if called
     *
     */
    fun updatePeopleIfEmpty() {
        updatePeopleSelected(getText(R.string.all).toString())
        mPresenter!!.selectedPeople = null
    }

    /** Will add "ALL" to Actors if called
     *
     */
    fun updateActorIfEmpty() {
        updateActorsSelected(getText(R.string.all).toString())
        mPresenter!!.selectedActors = null
    }


    override fun populateTimePeriod(options: HashMap<Int, String>) {

        val timePeriodPresets = options.values.toTypedArray()
        val adapter = ArrayAdapter(applicationContext,
                R.layout.item_simple_spinner, timePeriodPresets)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timePeriodSpinner!!.adapter = adapter
    }

    override fun updateLocationsSelected(locations: String) {
        locationsTextView!!.text = locations
        if (locations == "") {
            updateLocationIfEmpty()
        }
    }

    override fun updateClazzesSelected(clazzes: String) {
        classesTextView!!.text = clazzes
        if (clazzes == "") {
            updateClassesIfEmpty()
        }
    }

    override fun updatePeopleSelected(people: String) {
        personTextView!!.text = people
        if (people == "") {
            updatePeopleIfEmpty()
        }
    }

    override fun updateActorsSelected(actors: String) {
        actorTextView!!.text = actors
        if (actors == "") {
            updateActorIfEmpty()
        }
    }
}
