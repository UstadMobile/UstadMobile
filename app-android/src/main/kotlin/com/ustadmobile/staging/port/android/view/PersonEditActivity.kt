package com.ustadmobile.staging.port.android.view


import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.*
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.soywiz.klock.DateTime
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.PersonDetailViewField
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.PersonEditView.Companion.IMAGE_MAX_HEIGHT
import com.ustadmobile.core.view.PersonEditView.Companion.IMAGE_MAX_WIDTH
import com.ustadmobile.core.view.PersonEditView.Companion.IMAGE_QUALITY
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_DATE
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_DROPDOWN
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_FIELD
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_HEADER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_PASSWORD
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_PHONE_NUMBER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_TEXT
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_USERNAME
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.port.android.view.UstadBaseActivity
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * This activity is responsible for showing the edit page for a person. Used for editing a new
 * person as well as
 */
class PersonEditActivity : UstadBaseActivity(), PersonEditView {

    private lateinit var toolbar: Toolbar
    private lateinit var mLinearLayout: LinearLayout

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerView2: RecyclerView

    private lateinit var mPresenter: PersonEditPresenter

    private var imagePathFromCamera: String? = null

    internal lateinit var personEditImage: ImageView
    internal lateinit var customFieldsLL: LinearLayout

    internal lateinit var personEditImageButton: Button

    private var mProgressBar: ProgressBar? = null

    private var enrollingToClass = false

    var mOptionsMenu: Menu? = null

    override fun sendMessage(messageId: Int) {
        val impl = UstadMobileSystemImpl.instance
        val toast = impl.getString(messageId, this)
        runOnUiThread {
            Toast.makeText(
                    this,
                    toast,
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun setEnrollToClass(enroll: Boolean) {
        enrollingToClass = enroll
    }

    override fun setInProgress(inProgress: Boolean) {
        mProgressBar!!.visibility = if (inProgress) View.VISIBLE else View.GONE
        disableFields(inProgress)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set layout:
        setContentView(R.layout.activity_person_edit)

        mProgressBar = findViewById(R.id.progressBar)
        mProgressBar!!.isIndeterminate = true
        mProgressBar!!.scaleY = 3f


        //Toolbar
        toolbar = findViewById(R.id.activity_person_edit_toolbar)
        toolbar.setTitle(getText(R.string.edit_person))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Get the header & fields layout
        mLinearLayout = findViewById(R.id.activity_person_edit_fields_linear_layout)

        customFieldsLL = findViewById(R.id.activity_person_edit_custom_fields_ll)

        //Call the presenter
        mPresenter = PersonEditPresenter(this, UMAndroidUtil.bundleToMap(
                intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        personEditImage = findViewById(R.id.activity_person_edit_student_image)
        personEditImage.setOnClickListener { v -> addImageFromCamera() }

        personEditImageButton = findViewById<Button>(R.id.activity_person_edit_student_image_button)
        personEditImageButton.setOnClickListener { v -> addImageFromCamera() }


    }

    /**
     * Gets resource id of a resource image like an image. Usually used to get the resource id
     * from an image
     *
     * @param pVariableName     The variable name of the resource eg: ic_person_24dp
     * @param pResourcename     The name of the type of resource eg: drawable
     * @param pPackageName      The package name calling the resource (usually getPackageName() from
     * an activity)
     * @return                  The resource ID
     */
    fun getResourceId(pVariableName: String, pResourcename: String, pPackageName: String): Int {
        try {
            return resources.getIdentifier(pVariableName, pResourcename, pPackageName)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }

    }

    private fun handleFieldEdited(fieldUid: Long,  dateLong: Long){
        mPresenter.handleFieldEdited(fieldUid, dateLong)
    }
    /**
     * Clears all fields.
     */
    override fun clearAllFields() {
        mLinearLayout.removeAllViews()
    }

    override fun disableFields(disable: Boolean){


        val doneButton = mOptionsMenu!!.findItem(R.id.menu_done)

        doneButton.isEnabled = !disable
        if(disable) {
            doneButton.icon.alpha = 130
            personEditImageButton.alpha = .133F
            personEditImage.alpha = .133F
        } else{
            doneButton.icon.alpha = 255
            personEditImageButton.alpha = 1F
            personEditImage.alpha = 1F
        }

        for(child in mLinearLayout.children) {
            if (child is TextView){
                child.isEnabled = !disable
            }
            if(child is LinearLayout){
                val childLL = child as LinearLayout
                for(cc in childLL.children){
                    if (cc is TextInputLayout) {
                        cc.editText!!.isEnabled = !disable
                    }

                    if (cc is TextView){
                        cc.isEnabled = !disable
                    }

                    if(cc is AppCompatImageView){
                      if(disable){
                          cc.alpha = .133F
                      }else{
                          cc.alpha = 1F
                      }
                    }
                }
            }
        }

    }

    /**
     * Adds edit fields to the linear layout given to it. This method will figure out which type
     * of field it is and add the corresponding necessary view component to it. This method is to
     * be called for every field applicable to that person in order of visibility as it will be
     * added linearly.
     *
     * @param fieldUid              The field Uid of this field. This is used to set it to the
     * presenter's update method : handleFieldEdit() that uses this
     * field uid to figure out which field to update.
     * @param fieldType             The field type is used to determine which field type this is as
     * defined in PersonDetailViewField class.
     * @param label                 The label of the field if required it will be added before the
     * view component is added. eg: "Profile" label before Name, DOB,
     * etc are added.
     * @param labelId               The label Id
     * @param iconName              The icon name - to add custom icons to the view (if any)
     * @param editMode              Edit mode - true if yes, false if no
     * @param thisLinearLayout      The linear layout where the edit view will be added to.
     * @param thisValue             The value of the edit view component to be pre populated.
     */
    fun setEditField(fieldUid: Long, fieldType: Int, label: String?,
                     labelId: Int, iconName: String?, editMode: Boolean,
                     thisLinearLayout: LinearLayout?, thisValue: Any?) {
        var iconName = iconName

        val parentParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)

        //Calculate the width of the screen.
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels

        //Horizontal layout: Icon Area space + field (Horizontal)
        val fieldHLayout = LinearLayout(this)
        fieldHLayout.layoutParams = parentParams
        fieldHLayout.orientation = LinearLayout.HORIZONTAL

        var fieldView: View? = null

        when (fieldType) {

            FIELD_TYPE_HEADER -> {
                //Add The Divider
                val divider = View(this)
                val dividerLayout = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        DEFAULT_DIVIDER_HEIGHT)
                divider.layoutParams = dividerLayout
                divider.setBackgroundColor(Color.parseColor(COLOR_GREY))

                //Add for classes
                val impl = UstadMobileSystemImpl.instance
                if (label == impl.getString(MessageID.classes, applicationContext)) {

                    thisLinearLayout!!.addView(divider)

                    //Add the Header
                    val header = TextView(this)
                    header.text = label!!.toUpperCase()
                    header.textSize = HEADER_TEXT_SIZE.toFloat()
                    header.setPadding(DEFAULT_PADDING, 0, 0, DEFAULT_PADDING_HEADER_BOTTOM)
                    thisLinearLayout.addView(header)

                    //Add Add new Class button
                    val addPersonToClazzHL = LinearLayout(this)
                    addPersonToClazzHL.layoutParams = parentParams
                    addPersonToClazzHL.orientation = LinearLayout.HORIZONTAL

                    //Add the icon
                    val addIconResId = getResourceId(ADD_PERSON_ICON,
                            "drawable", packageName)
                    val addIcon = AppCompatImageView(this)


                    addIcon.setImageResource(addIconResId)
                    addIcon.setPadding(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_TEXT_PADDING_RIGHT, 0)
                    addPersonToClazzHL.addView(addIcon)

                    //Add the button
                    val addPersonButton = Button(this)
                    addPersonButton.includeFontPadding = false
                    addPersonButton.minHeight = 0
                    addPersonButton.text = impl.getString(MessageID.add_person_to_class,
                            applicationContext)
                    addPersonButton.background = null
                    addPersonButton.setPadding(DEFAULT_PADDING, 0, 0, 0)
                    addPersonButton.setOnClickListener { v -> mPresenter.handleClickAddNewClazz() }
                    addPersonToClazzHL.addView(addPersonButton)

                    if(!enrollingToClass) {
                        mLinearLayout.addView(addPersonToClazzHL)
                    }

                    //Add a recycler view of classes
                    mRecyclerView = RecyclerView(this)
                    val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
                    val wrapParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
                    mRecyclerView.setLayoutManager(mRecyclerLayoutManager)
                    mRecyclerView.setLayoutParams(wrapParams)


                    //Add the layout
                    mLinearLayout.addView(mRecyclerView)

                    //Generate the live data and set it
                    mPresenter.generateAssignedClazzesLiveData()
                }else if (label == impl.getString(MessageID.role_assignments, applicationContext)) {

                    thisLinearLayout!!.addView(divider)

                    //Add the Header
                    val header = TextView(this)
                    header.text = label!!.toUpperCase()
                    header.textSize = HEADER_TEXT_SIZE.toFloat()
                    header.setPadding(DEFAULT_PADDING, 0, 0, DEFAULT_PADDING_HEADER_BOTTOM)
                    thisLinearLayout.addView(header)

                    //Add Add new Role Assignment button
                    val addPersonToClazzHL = LinearLayout(this)
                    addPersonToClazzHL.layoutParams = parentParams
                    addPersonToClazzHL.orientation = LinearLayout.HORIZONTAL

                    //Add the icon
                    val addIconResId = getResourceId(ADD_ICON,
                            "drawable", packageName)
                    //ImageView addIcon = new ImageView(this);
                    val addIcon = AppCompatImageView(this)


                    addIcon.setImageResource(addIconResId)
                    addIcon.setPadding(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_TEXT_PADDING_RIGHT,
                            0)
                    addPersonToClazzHL.addView(addIcon)

                    //Add the button
                    val addPersonButton = Button(this)
                    addPersonButton.includeFontPadding = false
                    addPersonButton.minHeight = 0
                    addPersonButton.text = impl.getString(MessageID.add_new_role_assignment,
                            applicationContext)
                    addPersonButton.background = null
                    addPersonButton.setPadding(DEFAULT_PADDING, 0, 0, 0)
                    addPersonButton.setOnClickListener {
                        v -> mPresenter.handleClickAddNewRoleAssignment() }
                    addPersonToClazzHL.addView(addPersonButton)


                    if(!enrollingToClass) {
                        mLinearLayout.addView(addPersonToClazzHL)
                    }


                    //Add a recycler view of classes
                    mRecyclerView2 = RecyclerView(this)
                    val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
                    val wrapParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
                    mRecyclerView2.setLayoutManager(mRecyclerLayoutManager)
                    mRecyclerView2.setLayoutParams(wrapParams)


                    //Add the layout
                    mLinearLayout.addView(mRecyclerView2)

                    //Generate the live data and set it
                    mPresenter.generateAssignedRoleAssignments()
                }else{

                    thisLinearLayout!!.addView(divider)

                    //Add the Header
                    val header = TextView(this)
                    header.text = label!!.toUpperCase()
                    header.textSize = HEADER_TEXT_SIZE.toFloat()
                    header.setPadding(DEFAULT_PADDING, 0, 0, DEFAULT_PADDING_HEADER_BOTTOM)
                    thisLinearLayout.addView(header)
                }
            }

            FIELD_TYPE_TEXT, FIELD_TYPE_FIELD, FIELD_TYPE_PHONE_NUMBER, FIELD_TYPE_DATE,
            FIELD_TYPE_PASSWORD, FIELD_TYPE_USERNAME -> {

                //width of editText to be width of screen - (icon put left right padding)
                val pixelOffset = dpToPx(ADD_PERSON_ICON_WIDTH + 2 * DEFAULT_PADDING)
                val widthWithPadding = displayWidth - pixelOffset

                //The field is an input type. So we are gonna add a TextInputLayout:
                val fieldTextInputLayout = TextInputLayout(this)
                //Edit Text is inside a TextInputLayout
                val textInputLayoutParams = LinearLayout.LayoutParams(widthWithPadding,
                        LinearLayout.LayoutParams.MATCH_PARENT)

                //Add the icon to the Horizontal layout
                //Set icon if not present (for margins to align ok)
                if (iconName == null || iconName.length == 0) iconName = ADD_PERSON_ICON

                val iconResId = getResourceId(iconName, "drawable", packageName)
                val icon = AppCompatImageView(this)
                if (iconName == ADD_PERSON_ICON) {
                    icon.setImageAlpha(0)
                }//else don't set the icon. Let it be blank
                icon.setImageResource(iconResId)
                icon.setPadding(DEFAULT_PADDING, 0, DEFAULT_TEXT_PADDING_RIGHT, 0)
                fieldHLayout.addView(icon)

                //The EditText next to the icon
                val fieldEditText = EditText(this)
                fieldEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
                val editTextParams = ViewGroup.LayoutParams(
                        widthWithPadding,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                fieldEditText.layoutParams = editTextParams
                if (label != null) {
                    fieldEditText.hint = label
                }
                if (thisValue != null) {
                    if(thisValue != null) {
                        fieldEditText.setText(thisValue.toString())
                    }
                }
                if (fieldType == FIELD_TYPE_PHONE_NUMBER) {
                    fieldEditText.inputType = InputType.TYPE_CLASS_PHONE
                }

                if (fieldType == FIELD_TYPE_DATE) {

                    //Get locale
                    val currentLocale = resources.configuration.locale

                    //Date pickers's on click listener - sets text
                    val date = { view: DatePicker, year: Int, month:Int, dayOfMonth:Int ->

                        val dateLong = UMCalendarUtil.getDateLongFromYMD(year, month+1, dayOfMonth)

                        fieldEditText.setText(UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                                dateLong, currentLocale))
                        handleFieldEdited(fieldUid, dateLong)

                    }

                    val cal = DateTime.now()
                    fieldEditText.isFocusable = false

                    //date listener - opens a new date picker.
                    val dateFieldPicker = DatePickerDialog(
                            this@PersonEditActivity,
                            R.style.CustomDatePickerDialogTheme,
                            date, cal.yearInt,
                            cal.month0, cal.dayOfMonth)
                    dateFieldPicker.datePicker.maxDate = System.currentTimeMillis()
                    dateFieldPicker.datePicker.spinnersShown = true
                    dateFieldPicker.datePicker.calendarViewShown = false
                    dateFieldPicker.datePicker.layoutMode = 1
                    fieldEditText.setOnClickListener { v -> dateFieldPicker.show() }


                }
                if (fieldType == FIELD_TYPE_TEXT) {
                    fieldEditText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS +
                            InputType.TYPE_TEXT_FLAG_CAP_WORDS
                }

                if(fieldType == FIELD_TYPE_USERNAME){
                    fieldEditText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

                    fieldEditText.filters = arrayOf(object : InputFilter {
                        override fun filter(source: CharSequence?, start: Int, end: Int,
                                            dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                            // eliminates single space
                            if (end == 1) {
                                if (Character.isWhitespace(source?.get(0)!!)) {
                                    return ""
                                }
                            }
                            return null
                        }
                    })
                }

                if( fieldType == FIELD_TYPE_PASSWORD){
                    fieldEditText.inputType = InputType.TYPE_CLASS_TEXT +
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                    fieldEditText.filters = arrayOf(object : InputFilter {
                        override fun filter(source: CharSequence?, start: Int, end: Int,
                                            dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                            // eliminates single space
                            if (end == 1) {
                                if (Character.isWhitespace(source?.get(0)!!)) {
                                    return ""
                                }
                            }
                            return null
                        }
                    })
                }

                if (fieldType != FIELD_TYPE_DATE) {
                    fieldEditText.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence, start: Int,
                                                       count: Int, after: Int) {
                        }

                        override fun onTextChanged(s: CharSequence, start: Int,
                                                   before: Int, count: Int) {
                        }

                        override fun afterTextChanged(s: Editable) {
                            if(s!=null) {
                                mPresenter!!.handleFieldEdited(fieldUid, s.toString())
                            }
                        }
                    })
                }

                fieldTextInputLayout.addView(fieldEditText, textInputLayoutParams)

                fieldView = fieldTextInputLayout
            }
            FIELD_TYPE_DROPDOWN -> {
            }

            else -> {
            }
        }//End of TEXT

        if (fieldView != null) {
            fieldHLayout.addView(fieldView)
        }

        mLinearLayout!!.addView(fieldHLayout)

    }


    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_done, menu)

        mOptionsMenu = menu

        return true
    }

    /**
     * Handles Action Bar menu button click.
     * @param item  The MenuItem clicked.
     * @return  Boolean if handled or not.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        // Handle item selection
        val i = item.itemId
        //If this activity started from other activity
        if (i == R.id.menu_done) {
            handleClickDone()

            return super.onOptionsItemSelected(item)
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun handleClickDone() {
        val customCount = customFieldsLL.childCount
        for (i in 0 until customCount) {
            val field = customFieldsLL.getChildAt(i)
            val fieldId = field.id
            var type = 0
            var valueObject: Any? = null
            if (field is TextInputLayout) {
                //Text custom field
                type = CustomField.FIELD_TYPE_TEXT
                val til = field as TextInputLayout
                if(til!=null && til.editText!!.text!=null) {
                    valueObject = til.editText!!.text.toString()
                }

            } else if (field is LinearLayout) {
                type = CustomField.FIELD_TYPE_DROPDOWN
                val s = field.getChildAt(1) as Spinner
                valueObject = s.selectedItemPosition
            }
            mPresenter!!.handleSaveCustomFieldValues(fieldId, type, valueObject!!)
        }

        mPresenter!!.handleClickDone()
    }

    /**
     * Sets the field of given parameters to the view.
     *
     * @param index The index where the field should go in the (Linear) layout
     * @param fieldUid  The field uid
     * @param field The PersonDetailViewField field representation that has its id, type label & options
     * @param value The value of the field to be set to the view.
     */
    override fun setField(index: Int, fieldUid: Long, field: PersonDetailViewField, value: Any?) {
        val impl = UstadMobileSystemImpl.instance
        var label: String? = null
        var labelId = 0
        if (field.messageLabel != 0) {
            label = impl.getString(field.messageLabel, applicationContext)
            labelId = MessageIDMap.ID_MAP[field.messageLabel]!!
        }

        setEditField(fieldUid, field.fieldType, label, labelId, field.iconName,
                true, mLinearLayout, value)

    }

    override fun setClazzListProvider(factory : DataSource.Factory<Int, ClazzWithNumStudents>) {
        val recyclerAdapter = SimpleClazzListRecyclerAdapter(DIFF_CALLBACK, applicationContext)

        val data = LivePagedListBuilder(factory, 20).build()

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<ClazzWithNumStudents>> { recyclerAdapter.submitList(it) })
        }

        mRecyclerView!!.setAdapter(recyclerAdapter)
    }

    override fun setRoleAssignmentListProvider(factory:
                                               DataSource.Factory<Int, EntityRoleWithGroupName>){

        val recyclerAdapter =
                RoleAssignmentListRecyclerAdapter(PersonDetailActivity.DIFF_CALLBACK_ENTITY_ROLE_WITH_GROUPNAME,
                        null, mPresenter!!, this, applicationContext)
        // A warning is expected
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<EntityRoleWithGroupName>> { recyclerAdapter.submitList(it) })
        }

        mRecyclerView2!!.setAdapter(recyclerAdapter)

    }

    override fun updateToolbarTitle(titleName: String) {
        toolbar.setTitle(titleName)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Starts the camera intent.
     */
    private fun startCameraIntent() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        val dir = filesDir
        val output = File(dir, mPresenter!!.personUid.toString() + "_image.png")
        imagePathFromCamera = output.absolutePath

        val cameraImage = FileProvider.getUriForFile(this,
                "$packageName.provider", output)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImage)


        val resInfoList = packageManager.queryIntentActivities(cameraIntent,
                PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(packageName, cameraImage,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }


        //cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION |Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(cameraIntent, CAMERA_IMAGE_CAPTURE_REQUEST)


    }

    //this is how you check permission grant task result.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraIntent()
            }
        }
    }

    /**
     * Compress the image set using Compressor.
     *
     */
    fun compressImage() {
        val imageFile = File(imagePathFromCamera)

        val c = Compressor(this)
                .setMaxWidth(IMAGE_MAX_WIDTH)
                .setMaxHeight(IMAGE_MAX_HEIGHT)
                .setQuality(IMAGE_QUALITY)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(imageFile.path + "_" + imageFile.name)

        val compressedImageFile = c.compressToFile(imageFile)
        if (!imageFile.delete()) {
            print("Could not delete " + imagePathFromCamera!!)
        }
        imagePathFromCamera = compressedImageFile.absolutePath


    }

    override fun updateImageOnView(imagePath: String) {
        runOnUiThread {
            val profileImage = Uri.fromFile(File(imagePath))


            Picasso.get().invalidate(profileImage)
            Picasso
                    .get()
                    .load(profileImage)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .into(personEditImage)

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_IMAGE_CAPTURE_REQUEST -> {

                    //Compress the image:
                    compressImage()

                    //set imagePathFromCamera to Person (persist)
                    updateImageOnView(imagePathFromCamera!!)
                    mPresenter!!.updatePersonPic(imagePathFromCamera!!)
                }
            }
        }
    }

    override fun addImageFromCamera() {
        if (ContextCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@PersonEditActivity,
                    arrayOf<String>(Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_PERMISSION_REQUEST)
            return
        }
        startCameraIntent()
    }


    override fun addCustomFieldText(label: CustomField, value: String) {
        //customFieldsLL

        //Calculate the width of the screen.
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels

        //The field is an input type. So we are gonna add a TextInputLayout:
        val fieldTextInputLayout = TextInputLayout(this)
        val viewId = View.generateViewId()
        mPresenter!!.addToMap(viewId, label.customFieldUid)
        fieldTextInputLayout.setId(viewId)
        //Edit Text is inside a TextInputLayout
        val textInputLayoutParams = LinearLayout.LayoutParams(displayWidth,
                LinearLayout.LayoutParams.MATCH_PARENT)

        val widthWithPadding = displayWidth - dpToPx(28)
        //The EditText
        val fieldEditText = EditText(this)
        fieldEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
        val editTextParams = LinearLayout.LayoutParams(
                widthWithPadding,
                LinearLayout.LayoutParams.MATCH_PARENT)
        fieldEditText.layoutParams = editTextParams
        fieldEditText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        fieldEditText.setText(value)

        fieldEditText.hint = label.customFieldName

        fieldTextInputLayout.addView(fieldEditText, textInputLayoutParams)
        fieldTextInputLayout.setPadding(dpToPx(8), 0, 0, 0)
        customFieldsLL.addView(fieldTextInputLayout)
    }


    override fun addCustomFieldDropdown (label: CustomField, options: Array<String?>,
                                        selected:Int) {
        //Calculate the width of the screen.
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels

        //The field is an input type. So we are gonna add a TextInputLayout:
        //Edit Text is inside a TextInputLayout
        val textInputLayoutParams = ViewGroup.LayoutParams(displayWidth,
                ViewGroup.LayoutParams.MATCH_PARENT)

        val widthWithPadding = displayWidth - dpToPx(28)

        //Spinner time
        val spinner = Spinner(this)
        val spinnerArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
                options)
        spinner.adapter = spinnerArrayAdapter

        spinner.setSelection(selected)

        //Spinner label
        val labelTV = TextView(this)
        labelTV.text = label.customFieldName

        val viewId = View.generateViewId()
        mPresenter!!.addToMap(viewId, label.customFieldUid)

        //VLL
        val vll = LinearLayout(this)
        vll.id = viewId
        vll.layoutParams = textInputLayoutParams
        vll.orientation = LinearLayout.VERTICAL

        vll.addView(labelTV)
        vll.addView(spinner)

        vll.setPadding(dpToPx(8), 0, 0, 0)
        customFieldsLL.addView(vll)
    }

    override fun clearAllCustomFields() {
        customFieldsLL.removeAllViews()
    }

    companion object {

        val DEFAULT_PADDING = 16
        val DEFAULT_PADDING_HEADER_BOTTOM = 16
        val DEFAULT_DIVIDER_HEIGHT = 2
        val DEFAULT_TEXT_PADDING_RIGHT = 4
        val ADD_PERSON_ICON = "ic_person_add_black_24dp"
        val ADD_ICON = "ic_add_black_24dp"
        val ADD_PERSON_ICON_WIDTH = 24
        val HEADER_TEXT_SIZE = 12
        val COLOR_GREY = "#B3B3B3"

        private val CAMERA_PERMISSION_REQUEST = 100
        private val CAMERA_IMAGE_CAPTURE_REQUEST = 101

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        // Diff callback.
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzWithNumStudents> = object
            : DiffUtil.ItemCallback<ClazzWithNumStudents>() {
            override fun areItemsTheSame(oldItem: ClazzWithNumStudents,
                                         newItem: ClazzWithNumStudents): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: ClazzWithNumStudents,
                                            newItem: ClazzWithNumStudents): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }
        }
    }

}
