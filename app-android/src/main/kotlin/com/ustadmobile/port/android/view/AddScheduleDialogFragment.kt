package com.ustadmobile.port.android.view

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.AddScheduleDialogPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.AddScheduleDialogView
import com.ustadmobile.core.view.DismissableDialog
import com.ustadmobile.lib.db.entities.Schedule
import io.reactivex.annotations.NonNull
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * The Android View for adding a schedule to Class while editing it.
 */
class AddScheduleDialogFragment : UstadDialogFragment(), AddScheduleDialogView,
        AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener,
        DialogInterface.OnShowListener, View.OnClickListener, DismissableDialog {
    override val viewContext: Any
        get() = context!!

    internal lateinit var errorMessageTextView: TextView
    internal lateinit var fromET: EditText
    internal lateinit var toET: EditText
    internal lateinit var scheduleSpinner: Spinner
    internal lateinit var daySpinner: Spinner
    internal lateinit var mPresenter: AddScheduleDialogPresenter
    internal lateinit var dialog: AlertDialog
    internal lateinit var rootView: View
    internal lateinit var schedulePresets: Array<String>
    internal lateinit var dayPresets: Array<String>

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = Objects.requireNonNull<Context>(context).getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val myCalendar = Calendar.getInstance()
        val myCalendar2 = Calendar.getInstance()

        assert(inflater != null)

        rootView = inflater.inflate(R.layout.fragment_add_schedule_dialog, null)

        errorMessageTextView = rootView.findViewById(R.id.fragment_add_schedule_dialog_error_message)
        fromET = rootView.findViewById(R.id.fragment_add_schedule_dialog_from_time)
        toET = rootView.findViewById(R.id.fragment_add_schedule_dialog_to_time)
        scheduleSpinner = rootView.findViewById(R.id.fragment_add_schedule_dialog_schedule_spinner)
        daySpinner = rootView.findViewById(R.id.fragment_add_schedule_dialog_day_spinner)

        //Date format to show in the Date picker
        val justTheTimeFormat: SimpleDateFormat

        if (DateFormat.is24HourFormat(context)) {
            justTheTimeFormat = SimpleDateFormat("HH:mm")

        } else {
            justTheTimeFormat = SimpleDateFormat("hh:mm a")

        }

        //A Time picker listener that sets the from time.
        val timeF = { view:TimePicker, hourOfDay:Int, minute:Int ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            myCalendar.set(Calendar.MINUTE, minute)
            val timeOnET = justTheTimeFormat.format(myCalendar.time)

            mPresenter.handleScheduleFromTimeSelected(((hourOfDay * 60 + minute) * 60 * 1000).toLong())
            fromET.setText(timeOnET)
        }
        //Default view: not focusable.
        fromET.isFocusable = false

        //From time on click -> opens a timer picker.
        fromET.setOnClickListener { v ->
            TimePickerDialog(context, timeF,
                    myCalendar.get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(context)).show()
        }

        //A Time picker listener that sets the to time.
        val timeT = { view: TimePicker, hourOfDay:Int, minute:Int ->
            myCalendar2.set(Calendar.HOUR_OF_DAY, hourOfDay)
            myCalendar2.set(Calendar.MINUTE, minute)
            val timeOnET = justTheTimeFormat.format(myCalendar2.time)

            mPresenter.handleScheduleToTimeSelected(((hourOfDay * 60 + minute) * 60 * 1000).toLong())
            toET.setText(timeOnET)
        }
        //Default view: not focusable.
        toET.isFocusable = false


        //To time on click -> opens a time picker.
        toET.setOnClickListener { v ->
            TimePickerDialog(
                    context, timeT, myCalendar2.get(Calendar.HOUR),
                    myCalendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(context)).show()
        }

        //Schedule spinner's on click listener
        scheduleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter.handleScheduleSelected(position, id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //Do nothing here.
            }
        }

        daySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter.handleDaySelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        val positiveOCL = { _:DialogInterface, _:Int-> mPresenter.handleAddSchedule() }

        val negativeOCL = { _:DialogInterface, _:Int -> mPresenter.handleCancelSchedule() }

        val builder = AlertDialog.Builder(Objects.requireNonNull<Context>(context))
        builder.setTitle(R.string.add_session)
        builder.setView(rootView)
        builder.setPositiveButton(R.string.add, positiveOCL)
        builder.setNegativeButton(R.string.cancel, negativeOCL)
        dialog = builder.create()
        dialog.setOnShowListener(this)

        mPresenter = AddScheduleDialogPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(arguments))

        val scheduleAL = ArrayList<String>()
        scheduleAL.add(getText(R.string.daily).toString())
        scheduleAL.add(getText(R.string.weekly).toString())

        var s = scheduleAL.toTypedArray<String>()

        val dayAL = ArrayList<String>()
        dayAL.add(getText(R.string.sunday).toString())
        dayAL.add(getText(R.string.monday).toString())
        dayAL.add(getText(R.string.tuesday).toString())
        dayAL.add(getText(R.string.wednesday).toString())
        dayAL.add(getText(R.string.thursday).toString())
        dayAL.add(getText(R.string.friday).toString())
        dayAL.add(getText(R.string.saturday).toString())

        var d = dayAL.toTypedArray<String>()


        setScheduleDropdownPresets(s)
        setDayDropdownPresets(d)

        return dialog
    }

    override fun onClick(dialog: DialogInterface, which: Int) {}

    override fun onShow(dialog: DialogInterface) {}

    override fun onClick(v: View) {}

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {}

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun finish() {}

    /**
     * Adds given list of schedule presets to the Dialog Spinner
     *
     * @param presets   a string array of the presets in order.
     */
    override fun setScheduleDropdownPresets(presets: Array<String>) {
        this.schedulePresets = presets
        val adapter = ArrayAdapter(Objects.requireNonNull<Context>(context),
                android.R.layout.simple_spinner_item, schedulePresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        scheduleSpinner.adapter = adapter

        //Set the schedule spinner to have the last item always selected by default (in this case
        // it is "Weekly" option).
        scheduleSpinner.setSelection(presets.size - 1)
    }

    /**
     * Adds given list of day presets to the Dialog Spinner
     * @param presets   a string array of the presets in order.
     */
    override fun setDayDropdownPresets(presets: Array<String>) {
        this.dayPresets = presets
        val adapter = ArrayAdapter(Objects.requireNonNull<Context>(context),
                android.R.layout.simple_spinner_item, dayPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daySpinner.adapter = adapter
    }

    override fun setError(errorMessage: String) {}

    override fun hideDayPicker(hide: Boolean) {
        if (hide) {
            rootView.findViewById<View>(R.id.fragment_add_schedule_dialog_day_heading).visibility = View.INVISIBLE
            daySpinner.visibility = View.INVISIBLE
        } else {
            rootView.findViewById<View>(R.id.fragment_add_schedule_dialog_day_heading).visibility = View.VISIBLE
            daySpinner.visibility = View.VISIBLE
        }
    }

    override fun updateFields(schedule: Schedule) {

        runOnUiThread(Runnable{
            val justTheTimeFormat: SimpleDateFormat
            if (DateFormat.is24HourFormat(context)) {
                justTheTimeFormat = SimpleDateFormat("HH:mm")
            } else {
                justTheTimeFormat = SimpleDateFormat("hh:mm a")
            }


            val startTimeLong = schedule.sceduleStartTime
            val endTimeLong = schedule.scheduleEndTime

            val formatter = SimpleDateFormat.getTimeInstance(java.text.DateFormat.SHORT)
            //start time
            val startMins = startTimeLong / (1000 * 60)
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, (startMins / 60).toInt())
            cal.set(Calendar.MINUTE, (startMins % 60).toInt())
            val startTime = formatter.format(cal.time)

            //end time
            //start time
            val endMins = endTimeLong / (1000 * 60)
            cal.set(Calendar.HOUR_OF_DAY, (endMins / 60).toInt())
            cal.set(Calendar.MINUTE, (endMins % 60).toInt())
            val endTime = formatter.format(cal.time)


            val fromCal = Calendar.getInstance()
            val toCal = Calendar.getInstance()

            fromCal.set(Calendar.HOUR_OF_DAY,
                    TimeUnit.MILLISECONDS.toHours(schedule.sceduleStartTime).toInt())
            fromCal.set(Calendar.MINUTE,
                    TimeUnit.MILLISECONDS.toMinutes(schedule.sceduleStartTime).toInt())


            toCal.set(Calendar.HOUR_OF_DAY,
                    TimeUnit.MILLISECONDS.toHours(schedule.scheduleEndTime).toInt())
            toCal.set(Calendar.MINUTE,
                    TimeUnit.MILLISECONDS.toMinutes(schedule.scheduleEndTime).toInt())

            val timeOnET = justTheTimeFormat.format(schedule.sceduleStartTime)
            //String timeOnET = justTheTimeFormat.format(fromCal.getTime());
            val timeOnToET = justTheTimeFormat.format(schedule.scheduleEndTime)
            //String timeOnToET = justTheTimeFormat.format(toCal.getTime());

            //            fromET.setText(timeOnET);
            //            toET.setText(timeOnToET);

            fromET.setText(startTime)
            toET.setText(endTime)

            scheduleSpinner.setSelection(schedule.scheduleFrequency - 1)
            daySpinner.setSelection(schedule.scheduleDay - 1)
        })


    }
}
