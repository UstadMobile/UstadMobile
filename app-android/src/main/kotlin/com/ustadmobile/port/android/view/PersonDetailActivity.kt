package com.ustadmobile.port.android.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.RoleAssignmentListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonDetailViewField
import com.ustadmobile.core.view.PersonEditView.Companion.IMAGE_MAX_HEIGHT
import com.ustadmobile.core.view.PersonEditView.Companion.IMAGE_MAX_WIDTH
import com.ustadmobile.core.view.PersonEditView.Companion.IMAGE_QUALITY
import com.ustadmobile.core.view.RoleAssignmentListView
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.EntityRoleWithGroupName
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_DATE
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_DROPDOWN
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_FIELD
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_HEADER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_PHONE_NUMBER
import com.ustadmobile.lib.db.entities.PersonField.Companion.FIELD_TYPE_TEXT
import com.ustadmobile.port.android.view.PersonEditActivity.Companion.ADD_PERSON_ICON
import com.ustadmobile.port.android.view.PersonEditActivity.Companion.dpToPx
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.File

/**
 * The PersonDetail activity - Shows the detail of a person (like a contact in an personAddress book)
 *
 * This Activity extends UstadBaseActivity and implements PersonDetailView
 */
class PersonDetailActivity : UstadBaseActivity(), PersonDetailView {
    private var mLinearLayout: LinearLayout? = null

    private var mRecyclerView: RecyclerView? = null
    private var mRecyclerView2: RecyclerView? = null

    private var mPresenter: PersonDetailPresenter? = null
    internal var personEditImage: ImageView ? = null
    private var fab: FloatingTextButton? = null
    internal var updateImageButton: Button? = null
    private var imagePathFromCamera: String? = null
    private var toolbar: Toolbar? = null

    private var mOptionsMenu: Menu? = null

    private var enrollInClassLL: LinearLayout? = null
    private var recordDropoutLL: LinearLayout? = null

    internal var customFieldsLL: LinearLayout? = null

    private var weGroupNameHeader: TextView? = null


    override fun updateToolbar(name: String) {
        toolbar!!.title = name
    }

    override fun updateWEGroupName(name: String) {
        if(weGroupNameHeader != null) {
            val impl = UstadMobileSystemImpl.instance
            val firstBit = impl.getString(MessageID.women_entrepreneurs_group, this)
            weGroupNameHeader!!.text = firstBit + ": " + name
            weGroupNameHeader!!.visibility = View.VISIBLE
        }
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Setting layout:
        setContentView(R.layout.activity_person_detail)

        //Toolbar
        toolbar = findViewById<Toolbar>(R.id.activity_person_detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mLinearLayout = findViewById<LinearLayout>(R.id.activity_person_detail_fields_linear_layout)

        //FAB
        fab = findViewById(R.id.activity_person_detail_fab_edit)

        //Load the Image
        personEditImage = findViewById<ImageView>(R.id.activity_person_detail_student_image)

        //Update image button
        updateImageButton = findViewById<Button>(R.id.activity_person_detail_student_image_button2)

        updateImageButton!!.setOnClickListener { view -> addImageFromCamera() }

        enrollInClassLL = findViewById(R.id.activity_person_detail_action_ll_enroll_in_class_ll)
        recordDropoutLL = findViewById(R.id.activity_person_detail_action_ll_record_dropout_ll)

        customFieldsLL = findViewById(R.id.activity_person_detail_custom_fields_ll)

        //Call the Presenter
        mPresenter = PersonDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        fab!!.setOnClickListener { mPresenter!!.handleClickEdit() }

        val callParentTextView = findViewById<TextView>(R.id.activity_person_detail_action_call_parent_text)
        val textParentTextView = findViewById<TextView>(R.id.activity_person_detail_action_text_parent_text)
        val callParentImageView = findViewById<ImageView>(R.id.activity_person_detail_action_call_parent_icon)
        val textParentImageView = findViewById<ImageView>(R.id.activity_person_detail_action_text_parent_icon)
        val enrollInClassTextView = findViewById<TextView>(R.id.activity_person_detail_action_enroll_in_class_text)
        val enrollInClassImageView = findViewById<ImageView>(R.id.activity_person_detail_action_enroll_in_class_icon)

        callParentImageView.setOnClickListener { mPresenter!!.handleClickCallParent() }
        callParentTextView.setOnClickListener { mPresenter!!.handleClickCallParent() }

        textParentImageView.setOnClickListener { mPresenter!!.handleClickTextParent() }
        textParentTextView.setOnClickListener { mPresenter!!.handleClickTextParent() }

        enrollInClassImageView.setOnClickListener { mPresenter!!.handleClickEnrollInClass() }
        enrollInClassTextView.setOnClickListener { mPresenter!!.handleClickEnrollInClass() }

        recordDropoutLL!!.setOnClickListener { mPresenter!!.handleClickRecordDropout() }

    }

    fun getResourceId(pVariableName: String?, pResourcename: String, pPackageName: String): Int {
        try {
            return resources.getIdentifier(pVariableName, pResourcename, pPackageName)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }

    }

    override fun clearAllFields() {
        runOnUiThread { mLinearLayout!!.removeAllViews() }

    }

    override fun showFAB(show: Boolean) {
        runOnUiThread {
            fab!!.isEnabled = show
            fab!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun addImageFromCamera() {
        if (ContextCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@PersonDetailActivity,
                    arrayOf<String>(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST)
            return
        }
        startCameraIntent()
    }

    override fun showEnrollInClass(show: Boolean) {
        //Goldozi: Not showing enroll in clazz
        //runOnUiThread { enrollInClassLL!!.visibility = if (show) View.VISIBLE else View.GONE }

    }

    override fun showDropout(show: Boolean) {
        runOnUiThread { recordDropoutLL!!.visibility = if (show) View.VISIBLE else View.GONE }

    }


    //this is how you check permission grant task result.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                startCameraIntent()
            }
        }
    }

    /**
     * Starts the camera intent.
     */
    private fun startCameraIntent() {

        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        val dir = filesDir
        val output = File(dir, mPresenter!!.personUid.toString() + "_image.png")
        imagePathFromCamera = output.absolutePath

        val cameraImage = FileProvider.getUriForFile(applicationContext,
                "$packageName.provider", output)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImage)

        val resInfoList = packageManager.queryIntentActivities(cameraIntent,
                PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(packageName, cameraImage,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivityForResult(cameraIntent, CAMERA_IMAGE_CAPTURE_REQUEST)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_IMAGE_CAPTURE_REQUEST -> {

                    //Compress the image:
                    compressImage()

                    val imageFile = File(imagePathFromCamera)
                    mPresenter!!.handleCompressedImage(imageFile.absolutePath)
                }
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

    override fun showUpdateImageButton(show: Boolean) {
        runOnUiThread {
            updateImageButton!!.isEnabled = show
            updateImageButton!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun updateImageOnView(imagePath: String) {
        val output = File(imagePath)

        if (output.exists()) {
            val profileImage = Uri.fromFile(output)

            runOnUiThread {

                Picasso.get().invalidate(profileImage)
                Picasso
                        .get()
                        .load(profileImage)
                        .fit()
                        .centerCrop()
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .into(personEditImage)

                //Click on image - open dialog to show bigger picture
                personEditImage!!.setOnClickListener { view -> mPresenter!!.openPictureDialog(imagePath) }
            }

        }
    }

    override fun doneSettingFields() {
        //Add the final divider
        val divider2 = View(this)
        divider2.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        )
        divider2.setBackgroundColor(Color.parseColor("#B3B3B3"))
        //mLinearLayout!!.addView(divider2)
    }

    override fun setField(index: Int, field: PersonDetailViewField, value: Any?) {
        var value = value
        if (value == null) {
            value = ""
        }
        val impl = UstadMobileSystemImpl.instance
        var label: String? = null
        if (field.messageLabel != 0) {
            label = impl.getString(field.messageLabel, this)
        }

        when (field.fieldType) {
            FIELD_TYPE_HEADER -> {

                //Add The Divider
                val divider = View(this)
                divider.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                )
                divider.setBackgroundColor(Color.parseColor("#B3B3B3"))
                mLinearLayout!!.addView(divider)

                //Add the Header
                val header = TextView(this)
                assert(label != null)
                header.text = label!!.toUpperCase()
                header.textSize = 12f
                header.setPadding(16, 0, 0, 2)

                //Goldozi: Not showing header for classes
                if (field.messageLabel != MessageID.classes && field.messageLabel != MessageID.role_assignments) {
                    mLinearLayout!!.addView(header)
                }

                if(field.messageLabel == MessageID.role_assignments){
                    header.text = getText(R.string.no_women_embroiderers_set)
                    weGroupNameHeader = header
                    mLinearLayout!!.addView(weGroupNameHeader)
                }


                if (field.messageLabel == MessageID.classes) {

                    //Goldozi: No clazzes feature

//                    //Add a recyclerview of classes
//                    mRecyclerView = RecyclerView(this)
//
//                    val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
//                    mRecyclerView!!.setLayoutManager(mRecyclerLayoutManager)
//
//                    //Add the layout
//                    mLinearLayout!!.addView(mRecyclerView)
//
//                    //Generate the live data and set it
//                    mPresenter!!.generateAssignedClazzesLiveData()
                }

                if(field.messageLabel == MessageID.role_assignments) {
                    //Goldozi: No Role assignment feature
//                    //Add a recyclerview of Role assignments
//                    mRecyclerView2 = RecyclerView(this)
//
//                    val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
//                    mRecyclerView2!!.setLayoutManager(mRecyclerLayoutManager)
//
//                    //Add the layout
//                    mLinearLayout!!.addView(mRecyclerView2)
//
//                    //Generate the live data and set it
//                    mPresenter!!.generateAssignedRoleAssignments()



                }


            }
            FIELD_TYPE_TEXT, FIELD_TYPE_FIELD -> {

                val messageLabel = field.messageLabel
                //If this is just the full name, set it and continue
                if (messageLabel == MessageID.field_fullname) {
                    val name = findViewById<TextView>(R.id.activity_person_detail_student_name)
                    name.text = value.toString()
                    //TODO: KMP Check this flow replaced break with else and block.
                    //break
                }else{

                    val hll = LinearLayout(this)
                    hll.orientation = LinearLayout.HORIZONTAL
                    hll.setPadding(16, 16, 16, 16)


                    var iconName = field.iconName

                    if (iconName == null || iconName.length == 0) {
                        iconName = ADD_PERSON_ICON
                    }

                    val iconResId = getResourceId(iconName, "drawable", packageName)
                    val icon = AppCompatImageView(this)
                    icon.setImageResource(iconResId)
                    if (iconName == ADD_PERSON_ICON) icon.setAlpha(0)
                    icon.setPadding(16, 0, 4, 0)
                    hll.addView(icon)


                    val vll = LinearLayout(this)
                    vll.orientation = LinearLayout.VERTICAL
                    vll.setPadding(16, 0, 0, 0)

                    val fieldValue = TextView(this)
                    if (value.toString() === "") {
                        value = "-"
                    }
                    fieldValue.text = value.toString()
                    fieldValue.setPadding(16, 4, 4, 0)
                    vll.addView(fieldValue)

                    if (label != null) {
                        val fieldLabel = TextView(this)
                        fieldLabel.textSize = 10f
                        fieldLabel.text = label
                        fieldLabel.setPadding(16, 0, 4, 4)
                        vll.addView(fieldLabel)
                    }


                    //Add call and text buttons to father and mother detail
                    if (field.actionParam != null && field.actionParam!!.length > 0) {
                        val textIcon = AppCompatImageView(this)
                        textIcon.setImageResource(getResourceId(TEXT_ICON_NAME,
                                "drawable", packageName))
                        //textIcon.setPadding(8,16, 32,16);
                        textIcon.setOnClickListener({ v -> mPresenter!!.handleClickText(field.actionParam!!) })

                        val callIcon = AppCompatImageView(this)
                        callIcon.setImageResource(getResourceId(CALL_ICON_NAME,
                                "drawable", packageName))
                        callIcon.setPadding(32, 0, 0, 0)
                        callIcon.setOnClickListener({ v -> mPresenter!!.handleClickCall(field.actionParam!!) })

                        val heavyLayout = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1.0f
                        )
                        val fillIt = View(this)
                        fillIt.layoutParams = heavyLayout


                        vll.layoutParams = heavyLayout
                        hll.addView(vll)

                        val talkToMe = LinearLayout(this)
                        talkToMe.orientation = LinearLayout.HORIZONTAL
                        talkToMe.gravity = Gravity.RIGHT
                        talkToMe.weightSum = 2f
                        textIcon.setLayoutParams(heavyLayout)
                        callIcon.setLayoutParams(heavyLayout)
                        talkToMe.addView(textIcon)
                        talkToMe.addView(callIcon)
                        hll.addView(talkToMe)

                    } else {
                        hll.addView(vll)
                    }
                    mLinearLayout!!.addView(hll)
                }
            }
            FIELD_TYPE_DROPDOWN -> {
            }
            FIELD_TYPE_PHONE_NUMBER -> {
            }
            FIELD_TYPE_DATE -> {
            }
            else -> {
            }
        }

    }

    override fun addComponent(value: String, label: String) {
        var value = value

        val hll = LinearLayout(this)
        hll.orientation = LinearLayout.HORIZONTAL
        hll.setPadding(16, 16, 16, 16)


        val iconName = ADD_PERSON_ICON

        val iconResId = getResourceId(iconName, "drawable", packageName)
        val icon = AppCompatImageView(this)
        icon.setImageResource(iconResId)
        if (iconName == ADD_PERSON_ICON) icon.setAlpha(0)
        icon.setPadding(16, 0, 4, 0)
        hll.addView(icon)


        val vll = LinearLayout(this)
        vll.orientation = LinearLayout.VERTICAL
        vll.setPadding(16, 0, 0, 0)

        val fieldValue = TextView(this)
        if (value === "") {
            value = "-"
        }
        fieldValue.text = value
        fieldValue.setPadding(16, 4, 4, 0)
        vll.addView(fieldValue)

        if (label != null) {
            val fieldLabel = TextView(this)
            fieldLabel.textSize = 10f
            fieldLabel.text = label
            fieldLabel.setPadding(16, 0, 4, 4)
            vll.addView(fieldLabel)
        }

        hll.addView(vll)

        customFieldsLL!!.addView(hll)

    }

    override fun setClazzListProvider(factory: DataSource.Factory<Int, ClazzWithNumStudents>) {

        val recyclerAdapter = SimpleClazzListRecyclerAdapter(DIFF_CALLBACK, applicationContext)
        // A warning is expected
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<ClazzWithNumStudents>> { recyclerAdapter.submitList(it) })
        }

        mRecyclerView!!.setAdapter(recyclerAdapter)
    }

    override fun setRoleAssignmentListProvider(factory: DataSource.Factory<Int, EntityRoleWithGroupName>) {

        val recyclerAdapter =
                RoleAssignmentListRecyclerAdapter(DIFF_CALLBACK_ENTITY_ROLE_WITH_GROUPNAME,
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

    override fun handleClickCall(number: String) {
        startActivity(Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:$number")))
    }

    override fun handleClickText(number: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",
                number, null)))
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
        val textInputLayoutParams = ViewGroup.LayoutParams(displayWidth,
                ViewGroup.LayoutParams.MATCH_PARENT)

        val widthWithPadding = displayWidth - dpToPx(28)
        //The EditText
        val fieldEditText = EditText(this)
        fieldEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
        val editTextParams = LinearLayout.LayoutParams(
                widthWithPadding,
                ViewGroup.LayoutParams.MATCH_PARENT)
        fieldEditText.layoutParams = editTextParams
        fieldEditText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        fieldEditText.setText(value)

        fieldEditText.hint = label.customFieldName

        fieldTextInputLayout.addView(fieldEditText, textInputLayoutParams)
        fieldTextInputLayout.setPadding(dpToPx(8), 0, 0, 0)
        customFieldsLL!!.addView(fieldTextInputLayout)
    }

    override fun addCustomFieldDropdown(label: CustomField, options: Array<String>, selected: Int) {
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
        customFieldsLL!!.addView(vll)
    }

    override fun clearAllCustomFields() {
        customFieldsLL!!.removeAllViews()
    }

    companion object {

        private val CAMERA_PERMISSION_REQUEST = 104
        private val CAMERA_IMAGE_CAPTURE_REQUEST = 103

        val CALL_ICON_NAME = "ic_call_bcd4_24dp"
        val TEXT_ICON_NAME = "ic_textsms_bcd4_24dp"

        /**
         * The DIFF CALLBACK
         */
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

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK_ENTITY_ROLE_WITH_GROUPNAME
                : DiffUtil.ItemCallback<EntityRoleWithGroupName> = object
            : DiffUtil.ItemCallback<EntityRoleWithGroupName>() {
            override fun areItemsTheSame(oldItem: EntityRoleWithGroupName,
                                         newItem: EntityRoleWithGroupName): Boolean {
                return oldItem.erUid == newItem.erUid
            }

            override fun areContentsTheSame(oldItem: EntityRoleWithGroupName,
                                            newItem: EntityRoleWithGroupName): Boolean {
                return oldItem.erUid == newItem.erUid
            }
        }
    }


}
