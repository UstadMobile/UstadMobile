package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityAuditLogSelectionBinding
import com.ustadmobile.core.controller.AuditLogSelectionPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.AuditLogSelectionView
import com.ustadmobile.port.android.impl.UmDropDownOption
import com.ustadmobile.port.android.view.DropDownListAutoCompleteTextView
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.android.synthetic.main.layout_toolbar.view.*
import java.util.*

class AuditLogSelectionActivity : UstadBaseActivity(), AuditLogSelectionView,
        //SelectClazzesDialogFragment.ClazzSelectDialogListener,
        SelectMultipleTreeDialogFragment.MultiSelectTreeDialogListener,
        SelectTwoDatesDialogFragment.CustomTimePeriodDialogListener,
        DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<UmDropDownOption> {

    private var mPresenter: AuditLogSelectionPresenter? = null
    private var mBiding: ActivityAuditLogSelectionBinding? = null
    private var timePeriodPresets: List<UmDropDownOption> ? = null


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
        mBiding = ActivityAuditLogSelectionBinding.inflate(layoutInflater)
        setContentView(mBiding?.root)
        mBiding?.root?.toolbar?.title = getText(R.string.audit_log)
        setSupportActionBar(mBiding?.root?.toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        mPresenter = AuditLogSelectionPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        updateClassesIfEmpty()
        updateLocationIfEmpty()
        updatePeopleIfEmpty()
        updateActorIfEmpty()


        mBiding?.timePeriodChoices?.onDropDownListItemSelectedListener = this

        mBiding?.activityAuditLogClassesEdittext?.setOnClickListener { v -> mPresenter!!.goToSelectClassesDialog() }

        mBiding?.activityAuditLogLocationEdittext?.setOnClickListener { v -> mPresenter!!.goToLocationDialog() }

        mBiding?.activityAuditLogPersonEdittext?.setOnClickListener { v -> mPresenter!!.goToPersonDialog() }

        mBiding?.activityAuditLogChangedByEdittext?.setOnClickListener { v -> mPresenter!!.goToActorDialog() }

        mBiding?.activityAuditLogSelectionFab?.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton() }


    }

//    override fun onSelectClazzesResult(selectedClazzes: HashMap<String, Long>?) {
//        val classesSelectedString = StringBuilder()
//        val selectedClazzesNameIterator = selectedClazzes!!.keys.iterator()
//        while (selectedClazzesNameIterator.hasNext()) {
//            classesSelectedString.append(selectedClazzesNameIterator.next())
//            if (selectedClazzesNameIterator.hasNext()) {
//                classesSelectedString.append(", ")
//            }
//        }
//        val selectedClassesList = ArrayList(selectedClazzes.values)
//        mPresenter!!.selectedClasses = selectedClassesList
//
//        updateClazzesSelected(classesSelectedString.toString())
//    }

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

        timePeriodPresets = options.values.toTypedArray().map {
            UmDropDownOption(it)
        }
        mBiding?.optionItems = timePeriodPresets

    }

    override fun updateLocationsSelected(locations: String) {
        mBiding?.activityAuditLogLocationEdittext?.text = locations
        if (locations == "") {
            updateLocationIfEmpty()
        }
    }

    override fun updateClazzesSelected(clazzes: String) {
        mBiding?.activityAuditLogClassesEdittext?.text = clazzes
        if (clazzes == "") {
            updateClassesIfEmpty()
        }
    }

    override fun updatePeopleSelected(people: String) {
        mBiding?.activityAuditLogPersonEdittext?.text = people
        if (people == "") {
            updatePeopleIfEmpty()
        }
    }

    override fun updateActorsSelected(actors: String) {
        mBiding?.activityAuditLogChangedByEdittext?.text = actors
        if (actors == "") {
            updateActorIfEmpty()
        }
    }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: UmDropDownOption) {
        val index = timePeriodPresets?.indexOf(selectedOption)
        if(index != null){
            mPresenter!!.handleTimePeriodSelected(index)
        }
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {}

    override fun onDestroy() {
        super.onDestroy()
        mPresenter = null
        mBiding = null
        timePeriodPresets = null
    }
}
