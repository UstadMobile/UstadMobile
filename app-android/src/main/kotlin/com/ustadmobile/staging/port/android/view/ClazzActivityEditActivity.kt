package com.ustadmobile.staging.port.android.view


import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzActivityEditPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzActivityEditView
import com.ustadmobile.core.view.ClazzActivityEditView.Companion.THUMB_BAD
import com.ustadmobile.core.view.ClazzActivityEditView.Companion.THUMB_GOOD
import com.ustadmobile.core.view.ClazzActivityEditView.Companion.THUMB_OFF
import com.ustadmobile.lib.db.entities.ClazzActivityChange
import com.ustadmobile.port.android.view.UstadBaseActivity
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*


/**
 * This Activity is responsible for adding a new activity change for a particular class. The type of
 * feedback on the activity change and its metrics depend on the type of activity change selected.
 * The ClazzActivityEdit activity. This Activity extends UstadBaseActivity and implements
 * ClazzActivityEditView
 */
class ClazzActivityEditActivity : UstadBaseActivity(), ClazzActivityEditView {

    private var toolbar: Toolbar? = null
    private var mPresenter: ClazzActivityEditPresenter? = null

    //The Activity change options drop down / spinner.
    internal lateinit var activityChangeSpinner: AppCompatSpinner
    internal lateinit var trueFalseSpinner: Spinner

    //The list of activity changes as a string list (used to populate the drop down / spinner)
    internal lateinit var changesPresets: Array<String>

    //The unit of measure / length of time metric drop down / spinner that will be populated
    internal lateinit var unitOfMeasureEditText: EditText

    //Good and Bad thumbs up
    internal lateinit var thumbsUp: ImageView
    internal lateinit var thumbsDown: ImageView

    //Unit of measure title
    internal lateinit var unitOfMeasureTitle: TextView

    //Notes in the ClazzActivity
    internal lateinit var notesET: TextView

    //Date heading
    internal lateinit var dateHeading: TextView

    //Back and Forward date
    internal lateinit var backDate: AppCompatImageButton
    internal lateinit var forwardDate: AppCompatImageButton

    //FAB
    internal lateinit var fab: FloatingTextButton

    /**
     * Handles option selected from the toolbar. Here it is handling back button pressed.
     *
     * @param item  The menu item pressed
     * @return  true if accounted for
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

    /**
     * In Order:
     * 1. Sets layout
     * 2. Sets toolbar
     * 3. Calls the presenter and its onCreate() < sets the spinner, fills data
     * 4. Sets the [Activity change] drop down / spinner 's on select listener -> to presenter
     * 5. Sets good / bad click listener -> to presenter
     * 6. Sets notes text edit listener -> to presenter
     * 7. Gets Unit of Measure title
     * 8. Sets the [Unit of measure] on text changed on select listener -> to presenter
     *
     * @param savedInstanceState    The application bundle
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_clazz_activity_edit)

        //Toolbar:
        toolbar = findViewById(R.id.activity_clazz_activity_edit_toolbar)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        //Get items
        thumbsUp = findViewById(R.id.activity_clazz_activity_edit_went_good)
        thumbsDown = findViewById(R.id.activity_clazz_activity_edit_went_bad)
        notesET = findViewById(R.id.activity_clazz_activity_edit_notes)
        unitOfMeasureTitle = findViewById(R.id.activity_clazz_activity_edit_change_uom_title)
        unitOfMeasureEditText = findViewById(R.id.activity_clazz_activity_edit_change_spinner2)
        dateHeading = findViewById(R.id.activity_class_activity_date_heading3)
        backDate = findViewById(R.id.activity_class_activity_date_go_back3)
        forwardDate = findViewById(R.id.activity_class_activity_date_go_forward3)
        fab = findViewById(R.id.activity_clazz_activity_edit_fab)
        trueFalseSpinner = findViewById(R.id.activity_clazz_activity_edit_change_measurement_spinner)
        activityChangeSpinner = findViewById(R.id.activity_clazz_activity_edit_change_spinner)

        //Call the Presenter
        mPresenter = ClazzActivityEditPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Change icon based on rtl in current language (eg: arabic)
        val isLeftToRight = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault())
        when (isLeftToRight) {
            ViewCompat.LAYOUT_DIRECTION_RTL -> {
                backDate.setImageDrawable(AppCompatResources.getDrawable(
                        applicationContext, R.drawable.ic_chevron_right_black_24dp))
                forwardDate.setImageDrawable(AppCompatResources.getDrawable(
                        applicationContext, R.drawable.ic_chevron_left_black_24dp))
            }
        }

        //FAB and its listener
        fab.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton() }

        activityChangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                //The change to id map starts from an offset
                mPresenter!!.handleChangeActivityChange(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        setTrueFalseDropdownPresets()
        trueFalseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                //Handle true/false value selected from the spinner.
                mPresenter!!.handleChangeTrueFalseMeasurement(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        //Thumbs listener
        thumbsUp.setOnClickListener { v -> mPresenter!!.handleChangeFeedback(true) }
        thumbsDown.setOnClickListener { v -> mPresenter!!.handleChangeFeedback(false) }

        //Notes listener
        notesET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.handleChangeNotes(s.toString())
            }
        })


        //Unit of Measure text listener
        unitOfMeasureEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.length > 0)
                    mPresenter!!.handleChangeUnitOfMeasure(java.lang.Long.valueOf(s.toString()))
            }
        })

        //Date switching
        backDate.setOnClickListener { v -> mPresenter!!.handleClickGoBackDate() }
        forwardDate.setOnClickListener { v -> mPresenter!!.handleClickGoForwardDate() }
    }


    /**
     * Updates the toolbar with title given.
     *
     * @param title     The string title
     */
    override fun updateToolbarTitle(title: String) {
        runOnUiThread {
            toolbar!!.title = title
            setSupportActionBar(toolbar)
            Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)
        }
    }

    fun setTrueFalseDropdownPresets() {

        val presetAL = ArrayList<String>()
        presetAL.add("True")
        presetAL.add("False")

        val trueFalsePresets = presetAL.toTypedArray()
        val adapter = ArrayAdapter(applicationContext,
                android.R.layout.simple_spinner_item, trueFalsePresets)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        trueFalseSpinner.adapter = adapter
    }

    /**
     * Updates the activity change drop down / spinner with values given.
     *
     * @param presets   The string array in order to be populated in the Activity change drop down /
     */
    override fun setClazzActivityChangesDropdownPresets(presets: Array<String>) {

        this.changesPresets = presets
        val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, changesPresets)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        activityChangeSpinner.adapter = adapter
    }

    override fun setUnitOfMeasureType(uomType: Long) {

        //Since gets called from presenter's thread, running on ui thread
        runOnUiThread {
            val uomTitle: String
            when (uomType.toInt()) {
                ClazzActivityChange.UOM_FREQUENCY -> {
                    uomTitle = getText(R.string.uom_frequency_title).toString()
                    unitOfMeasureEditText.inputType = InputType.TYPE_CLASS_NUMBER
                    setTrueFalseVisibility(false)
                }
                ClazzActivityChange.UOM_BINARY -> {
                    uomTitle = getText(R.string.uom_boolean_title).toString()
                    setTrueFalseVisibility(true)
                }
                ClazzActivityChange.UOM_DURATION -> {
                    uomTitle = getText(R.string.uom_duration_title).toString()
                    unitOfMeasureEditText.inputType = InputType.TYPE_CLASS_NUMBER
                    setTrueFalseVisibility(false)
                }
                else -> {
                    uomTitle = getText(R.string.uom_default_title).toString()
                    unitOfMeasureEditText.inputType = InputType.TYPE_CLASS_NUMBER
                    setTrueFalseVisibility(false)
                }
            }
            unitOfMeasureTitle.text = uomTitle

        }

    }

    override fun setActivityChangeOption(option: Long) {
        activityChangeSpinner.setSelection(option.toInt())
    }

    override fun setThumbs(thumbs: Int) {
        runOnUiThread {
            when (thumbs) {
                THUMB_OFF -> {
                    thumbsUp.clearColorFilter()
                    thumbsDown.clearColorFilter()
                }
                THUMB_GOOD -> {
                    thumbsUp.setColorFilter(Color.BLACK)
                    thumbsDown.clearColorFilter()
                }
                THUMB_BAD -> {
                    thumbsUp.clearColorFilter()
                    thumbsDown.setColorFilter(Color.BLACK)
                }
            }
        }
    }

    override fun setNotes(notes: String) {
        runOnUiThread { notesET.text = notes }
    }

    override fun setUOMText(uomText: String) {
        runOnUiThread { unitOfMeasureEditText.setText(uomText) }
    }

    override fun setMeasureBitVisibility(visible: Boolean) {
        runOnUiThread {
            if (visible) {
                unitOfMeasureTitle.visibility = View.VISIBLE
                unitOfMeasureEditText.visibility = View.VISIBLE
            } else {
                unitOfMeasureTitle.visibility = View.INVISIBLE
                unitOfMeasureEditText.visibility = View.INVISIBLE
            }
        }
    }

    override fun setTrueFalseVisibility(visible: Boolean) {
        runOnUiThread {
            if (visible) {
                trueFalseSpinner.visibility = View.VISIBLE
                unitOfMeasureEditText.visibility = View.INVISIBLE
            } else {
                trueFalseSpinner.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * Sets the dateString to the View
     *
     * @param dateString    The date in readable format that will be set to the ClazzLogDetail view
     */
    override fun updateDateHeading(dateString: String) {
        //Since its called from the presenter, need to run on ui thread.
        runOnUiThread { dateHeading.text = dateString }
    }

    override fun showFAB(show: Boolean) {
        if (show) {
            fab.visibility = View.VISIBLE
        } else {
            fab.visibility = View.INVISIBLE
        }
    }

    override fun setEditable(editable: Boolean) {

        activityChangeSpinner.isEnabled = editable
        notesET.isEnabled = editable
        unitOfMeasureEditText.isEnabled = editable
        thumbsUp.isEnabled = editable
        thumbsDown.isEnabled = editable
        unitOfMeasureEditText.isEnabled = editable
        trueFalseSpinner.isEnabled = editable

        showFAB(editable)

    }
}
