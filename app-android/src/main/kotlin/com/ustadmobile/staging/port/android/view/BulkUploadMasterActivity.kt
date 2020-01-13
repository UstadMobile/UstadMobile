package com.ustadmobile.staging.port.android.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.BulkUploadMasterPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.BulkUploadMasterView
import com.ustadmobile.port.android.view.UstadBaseActivity
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.*
import java.util.*

class BulkUploadMasterActivity : UstadBaseActivity(), BulkUploadMasterView {


    private var filePathFromFilePicker: String? = null
    private var selectedPathUri : Uri? = null
    private var mPresenter: BulkUploadMasterPresenter? = null
    private var mProgressBar: ProgressBar? = null
    private var fab: FloatingTextButton? = null
    private var selectFileButton: Button? = null
    private var timeZoneSpinner: AppCompatSpinner? = null
    private var errorsLL: LinearLayout? = null
    private var errorHeading: TextView? = null
    private var heading: TextView? = null
    private var allErrors: MutableList<String>? = null
    private var toolbar: Toolbar ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        allErrors = ArrayList()

        //Set layout
        setContentView(R.layout.activity_bulk_upload_master)

        //Toolbar
        toolbar = findViewById(R.id.activity_bulk_upload_master_toolbar)
        toolbar!!.setTitle(R.string.bulk_upload_master)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Timezone spinner
        timeZoneSpinner = findViewById(R.id.activity_bulk_upload_master_timezone_spinner)
        timeZoneSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                (parent.getChildAt(0) as TextView).setTextColor(Color.BLACK)
                mPresenter!!.setTimeZoneSelected(position, id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        //Call the presenter
        mPresenter = BulkUploadMasterPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Button
        selectFileButton = findViewById(R.id.activity_bulk_upload_master_upload_button)
        selectFileButton!!.setOnClickListener { v -> chooseFileFromDevice() }

        //Errors and warnings:
        errorsLL = findViewById(R.id.activity_bulk_upload_master_errors_ll)
        errorHeading = findViewById(R.id.activity_bulk_upload_master_errors_heading)
        errorsLL!!.removeAllViews()

        errorsLL!!.visibility = View.INVISIBLE
        errorHeading!!.visibility = View.INVISIBLE

        //Heading TextView
        heading = findViewById(R.id.activity_bulk_upload_select_file_text)

        //Progress bar
        mProgressBar = findViewById(R.id.activity_bulk_upload_master_progressbar)
        mProgressBar!!.isIndeterminate = false
        mProgressBar!!.scaleY = 3f

        //FAB
        fab = findViewById(R.id.activity_bulk_upload_master_fab)
//        fab!!.setOnClickListener { v -> parseFile(filePathFromFilePicker!!) }
        fab!!.setOnClickListener { v -> parseThisUri() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setInProgress(inProgress: Boolean) {
        mProgressBar!!.visibility = if (inProgress) View.VISIBLE else View.GONE
        fab!!.isEnabled = !inProgress
        fab!!.visibility = if (inProgress) View.INVISIBLE else View.VISIBLE
        selectFileButton!!.isEnabled = inProgress
        selectFileButton!!.visibility = if (inProgress) View.INVISIBLE else View.VISIBLE
        selectFileButton!!.background.alpha = if (inProgress) 128 else 255
    }

    override fun updateProgressValue(line: Int, nlines: Int) {
        runOnUiThread {
            val value = line * 100 / nlines
            mProgressBar!!.progress = value
        }
    }

    override fun getAllErrors(): MutableList<String>? {
        return allErrors
    }

    override fun addError(message: String, error: Boolean) {
        if (!allErrors!!.contains(message)) {
            allErrors!!.add(message)
            runOnUiThread {
                errorsLL!!.visibility = View.VISIBLE
                errorHeading!!.visibility = View.VISIBLE
                val errorView = TextView(applicationContext)
                errorView.text = message
                if (error) {
                    setErrorHeading(MessageID.errors)
                    errorView.setTextColor(Color.RED)
                }
                errorsLL!!.addView(errorView)
            }
        }

    }

    override fun addError(message: String) {
        addError(message, false)
    }

    override fun setErrorHeading(messageId: Int) {
        val impl = UstadMobileSystemImpl.instance
        val message = impl.getString(messageId, applicationContext)
        runOnUiThread { errorHeading!!.text = message }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == FILE_PERMISSION_REQUEST) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted - call the method here
                chooseFileFromDevice()
            } else {
                // permission denied, you may keep on requesting it or just finish the activity
            }
            return
        }
    }

    override fun setTimeZonesList(timeZoneIds: List<String>) {

        val timeZoneAdapter = ArrayAdapter(applicationContext,
                android.R.layout.simple_spinner_item, timeZoneIds)
        timeZoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        timeZoneSpinner!!.adapter = timeZoneAdapter

        val index = timeZoneIds.indexOf(TimeZone.getDefault().id)
        timeZoneSpinner!!.setSelection(index)

    }

    override fun chooseFileFromDevice() {

        if (ContextCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@BulkUploadMasterActivity,
                    arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                    FILE_PERMISSION_REQUEST)
            return
        }

        val mimeTypes = arrayOf("text/*")

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        } else {
            val mimeTypesStr = StringBuilder()
            for (mimeType in mimeTypes) {
                mimeTypesStr.append(mimeType).append("|")
            }
            intent.type = mimeTypesStr.substring(0, mimeTypesStr.length - 1)
        }
        startActivityForResult(Intent.createChooser(intent, "Select CSV File"),
                FILE_PERMISSION_REQUEST)
    }

    fun parseThisUri(){
        setInProgress(true)
        if(selectedPathUri == null ){
            showMessage(getText(R.string.select_file).toString())
        }else{
            readUri()
        }
    }
    override fun parseFile(filePath: String) {
        setInProgress(true)
        if (filePath == null || filePath.isEmpty()) {
            showMessage(getText(R.string.select_file).toString())
        } else {
            val sourceFile = File(filePath)
            readFile(sourceFile)
        }
    }

    fun readUri(){
        val stream: InputStream = getContentResolver()!!.openInputStream(selectedPathUri!!)!!
        val br = BufferedReader(InputStreamReader(stream))
        val lines = ArrayList<String>()
        var line: String? = null
        while ( {line = br.readLine(); line}() != null) {
            lines.add(line!!)
        }

        mPresenter!!.lines = lines
        mPresenter!!.setCurrentPosition(0) //skip first line
        mPresenter!!.startParsing()
    }


    fun readFile(sourceFile: File) {
        try {
            val reader = BufferedReader(FileReader(sourceFile))
            var line : String ?=null
            val lines = ArrayList<String>()
            while ( {line = reader.readLine(); line}() != null) {
                lines.add(line!!)
            }

            mPresenter!!.lines = lines
            mPresenter!!.setCurrentPosition(0) //skip first line
            mPresenter!!.startParsing()

        } catch (e: FileNotFoundException) {
            showMessage("File not found")
            e.printStackTrace()
        } catch (e: IOException) {
            showMessage("Unable to process the file")
            e.printStackTrace()
        }

    }

    override fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(
                    applicationContext,
                    message,
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                FILE_PERMISSION_REQUEST -> {
                    selectedPathUri = data!!.data

                    //TODO: KMP Re did this. Check and test,etc.
                    val sourceFile = File(data!!.data!!.path)

//                    val sourceFile = File(Objects.requireNonNull(
//                            UmAndroidUriUtil.getPath(this, selectedUri)))
                    //Do something with your file
                    filePathFromFilePicker = sourceFile.getAbsolutePath()
                    val fileSelectedString = getText(R.string.file_selected).toString() + " " +
                            sourceFile.getName()
                    heading!!.text = fileSelectedString
                }
            }
        }
    }



    companion object {

        private val FILE_PERMISSION_REQUEST = 400
    }

}
