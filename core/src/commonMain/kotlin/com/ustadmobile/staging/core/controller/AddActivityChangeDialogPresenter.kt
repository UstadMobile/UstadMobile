package com.ustadmobile.core.controller


import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddActivityChangeDialogView
import com.ustadmobile.lib.db.entities.ClazzActivityChange
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * The AddActivityChangeDialog Presenter. Usually triggered when editing a single Clazz Activity
 * This presenter is responsible for persisting an edited/new ClazzActivity.
 */
class AddActivityChangeDialogPresenter(context: Any, arguments: Map<String, String>?,
                                       view: AddActivityChangeDialogView,
                                       val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<AddActivityChangeDialogView>(context, arguments!!, view) {

    //The current Clazz Activity Change that this Clazz Activity will be assigned to.
    private var currentChange: ClazzActivityChange? = null

    //Map of all the measurement type options's uid AND its position. Useful when we know what
    //position from the view('s spinner) was selected so we can find the corresponding measurement
    // type.
    private var measurementToUOM: HashMap<Int, Int>? = null

    //The Database repo
    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)


        //Create a new activity change
        if (currentChange == null) {
            currentChange = ClazzActivityChange()
        }

        //We know all the measurement type. So we just populate them here..
        val measurementTypes = ArrayList<String>()
        measurementToUOM = HashMap()
        measurementTypes.add(impl.getString(MessageID.frequency, context))
        measurementToUOM!![0] = ClazzActivityChange.UOM_FREQUENCY
        measurementTypes.add(impl.getString(MessageID.duration, context))
        measurementToUOM!![1] = ClazzActivityChange.UOM_DURATION
        measurementTypes.add(impl.getString(MessageID.yes_no, context))
        measurementToUOM!![2] = ClazzActivityChange.UOM_BINARY

        var measurementTypesArray = measurementTypes.toTypedArray<String>()

        //.. and set them on the view.
        view.setMeasurementDropdownPresets(measurementTypesArray)
    }

    /**
     * Method that gets called when user clicks "Add" on the dialog (primary).
     * This will persist the information added about this new Activity
     */
    fun handleAddActivityChange() {

        val clazzActivityChangeDao = repository.clazzActivityChangeDao
        currentChange!!.isClazzActivityChangeActive = true //set active

        GlobalScope.launch {
            clazzActivityChangeDao.insertAsync(currentChange!!)
            view.finish()
        }
    }

    /**
     * Method that gets called when user clicks "Cancel" on the dialog (dismiss)
     */
    fun handleCancelActivityChange() {
        currentChange = null
    }

    /**
     * Updates the unit of measurement selected for the clazz activity.
     * @param position  The position of item selected from the drop down.
     */
    fun handleMeasurementSelected(position: Int) {
        currentChange!!.clazzActivityUnitOfMeasure = measurementToUOM!![position]!!
    }

    /**
     * Updates the title of the clazz activity.
     * @param title The activity title
     */
    fun handleTitleChanged(title: String) {
        currentChange!!.clazzActivityChangeTitle = title
    }
}
