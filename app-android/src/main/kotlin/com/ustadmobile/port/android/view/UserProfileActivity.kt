package com.ustadmobile.port.android.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.UserProfilePresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.UserProfileView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.staging.port.android.view.CircleTransform
import id.zelory.compressor.Compressor
import java.io.*

class UserProfileActivity : UstadBaseActivity(), UserProfileView {

    override fun setLoggedPerson(person: Person) {
    }

    override fun loadProfileIcon(profile: String) {
    }


    private var toolbar: Toolbar? = null
    private var mPresenter: UserProfilePresenter? = null

    private var changePasswordLL: LinearLayout? = null
    private var languageLL: LinearLayout? = null
    private var logoutLL: LinearLayout? = null
    private var myWomenEntLL: LinearLayout? = null

    private var languageSet: TextView? = null
    internal lateinit var pictureEdit: ImageView
    internal lateinit var personEditImage: ImageView

    internal var IMAGE_MAX_HEIGHT = 1024
    internal var IMAGE_MAX_WIDTH = 1024
    internal var IMAGE_QUALITY = 75

    private var imagePathFromCamera: String? = null
    private lateinit var lastSyncedTV : TextView

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

    override fun showLanguageOptions() {

    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_user_profile)

        //Toolbar:
        toolbar = findViewById(R.id.activity_user_profile_toolbar)
        toolbar!!.title = getText(R.string.app_name)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        changePasswordLL = findViewById(R.id.activity_user_profile_change_password_ll)
        languageLL = findViewById(R.id.activity_user_profile_language_ll)
        logoutLL = findViewById(R.id.activity_user_profile_logout_ll)
        myWomenEntLL = findViewById(R.id.activity_user_profile_my_women_entrepreneurs_ll)
        languageSet = findViewById(R.id.activity_user_profile_language_selection)
        pictureEdit = findViewById(R.id.activity_user_profile_edit)
        personEditImage = findViewById(R.id.activity_user_profile_user_image)
        lastSyncedTV = findViewById(R.id.activity_user_prodile_last_synced)

        //Call the Presenter
        mPresenter = UserProfilePresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this,
                UmAccountManager.getActiveDatabase(this), UstadMobileSystemImpl.instance)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        changePasswordLL!!.setOnClickListener { v -> mPresenter!!.handleClickChangePassword() }
        languageLL!!.setOnClickListener { v -> mPresenter!!.handleClickChangeLanguage() }
        logoutLL!!.setOnClickListener { v -> handleClickLogout() }
        myWomenEntLL!!.setOnClickListener{ v -> mPresenter!!.handleClickMyWomenEntrepreneurs()}


        pictureEdit.setOnClickListener { v -> showGetImageAlertDialog() }
    }

    override fun updateLastSyncedText(lastSynced: String) {
        lastSyncedTV.setText(lastSynced)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun setLanguageOption(languages: MutableList<String>){
        //TODO: future
    }

    fun showGetImageAlertDialog() {
        val adb = AlertDialog.Builder(this)
                .setTitle("")
                .setMessage(getText(R.string.select_image_from_camera_or_gallery))

                .setPositiveButton(R.string.camera) { dialog, which ->
                    addImageFromCamera()
                    dialog.dismiss()
                }

                .setNegativeButton(R.string.gallery) { dialog, which ->
                    addImageFromGallery()
                    dialog.dismiss()
                }

        adb.create()
        adb.show()
    }


    override fun updateImageOnView(imagePath: String, skipCached: Boolean) {
        imagePathFromCamera = imagePath
        val output = File(imagePath)

        val iconDimen = dpToPx(150)

        if (output.exists()) {
            val profileImage = Uri.fromFile(output)

            runOnUiThread {

                if(skipCached){
                    Picasso.get().invalidate(profileImage)

                    Picasso
                            .get()
                            .load(profileImage)
                            .transform(CircleTransform())
                            .resize(iconDimen, iconDimen)
                            .centerCrop()
                            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                            .into(personEditImage)

                }else{
                    Picasso
                            .get()
                            .load(profileImage)
                            .transform(CircleTransform())
                            .resize(iconDimen, iconDimen)
                            .centerCrop()
                            .into(personEditImage)
                }


                //Click on image - open dialog to show bigger picture
                personEditImage.setOnClickListener { view ->
                    mPresenter!!.openPictureDialog(imagePath) }
            }

        }
    }

    override fun addImageFromCamera() {
        if (ContextCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@UserProfileActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST)
            return
        }
        startCameraIntent()
    }


    override fun addImageFromGallery() {
        //READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@UserProfileActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    GALLERY_REQUEST_CODE)
            return
        }

        startGalleryIntent()
    }

    private fun startGalleryIntent() {
        //Create an Intent with action as ACTION_PICK
        val intent = Intent(Intent.ACTION_PICK)
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.type = "image/*"
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }


    /**
     * Starts the camera intent.
     */
    private fun startCameraIntent() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val dir = filesDir
        val output = File(dir, mPresenter!!.loggedInPersonUid.toString() + "_image.png")
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


    //this is how you check permission grant task result.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraIntent()
            }
            GALLERY_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGalleryIntent()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_IMAGE_CAPTURE_REQUEST -> {

                    //Compress the image:
                    compressImage()

                    val imageFile = File(imagePathFromCamera)
                    mPresenter!!.handleCompressedImage(imageFile.canonicalPath)
                }

                GALLERY_REQUEST_CODE -> {

                    val selectedImage = data!!.data


                    val picPath = doInBackground(selectedImage!!)
                    imagePathFromCamera = picPath
                    if (imagePathFromCamera == null) {
                        sendMessage(MessageID.unable_open_image)
                        return
                    }

                    //Compress the image:
                    compressImage()

                    val galleryFile = File(imagePathFromCamera)
                    mPresenter!!.handleCompressedImage(galleryFile.canonicalPath)
                }
            }
        }
    }

    override fun callFinishAffinity(){
        finishAffinity()
    }

    override fun restartUI() {
        onResume()
    }

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

    protected fun doInBackground(vararg fileUris: Uri): String? {
        var cursor: Cursor? = null
        var fileIn: InputStream? = null
        var tmpOut: OutputStream? = null
        var tmpFilePath: String? = null

        try {
            //As per https://developer.android.com/guide/topics/providers/document-provider
            cursor = contentResolver.query(fileUris[0], null, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val displayName = cursor.getString(cursor
                        .getColumnIndex(OpenableColumns.DISPLAY_NAME))
                val extension = UMFileUtil.getExtension(displayName)

                val tmpFile = File.createTempFile("SelectedFileTmp",
                        "-" + System.currentTimeMillis() + "." + extension)
                fileIn = contentResolver.openInputStream(fileUris[0])
                tmpOut = FileOutputStream(tmpFile)
                UMIOUtils.readFully(fileIn!!, tmpOut)
                tmpFilePath = tmpFile.absolutePath
            } else {

            }
        } catch (e: Exception) {
            e.printStackTrace()

        } finally {
            cursor?.close()

            UMIOUtils.closeInputStream(fileIn)
            UMIOUtils.closeOutputStream(tmpOut)
        }

        return tmpFilePath
    }

    /**
     * Compress the image set using Compressor.
     *
     */
    fun compressImage() {
        val imageFile = File(imagePathFromCamera)
        try {
            val c = Compressor(this)
                    .setMaxWidth(IMAGE_MAX_WIDTH)
                    .setMaxHeight(IMAGE_MAX_HEIGHT)
                    .setQuality(IMAGE_QUALITY)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(imageFile.path + "_" + imageFile.name)

            if (imageFile.exists()) {
                val compressedImageFile = c.compressToFile(imageFile)
                if (!imageFile.delete()) {
                    print("Could not delete " + imagePathFromCamera!!)
                }
                imagePathFromCamera = compressedImageFile.getAbsolutePath()
            }


        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun handleClickLogout() {
        finishAffinity()
        mPresenter!!.handleClickLogout()
    }

    override fun updateToolbarTitle(personName: String) {
        toolbar!!.title = personName
    }

    override fun setLanguageSet(lang: String) {
        languageSet!!.text = lang
    }

    companion object {


        private val CAMERA_PERMISSION_REQUEST = 104
        private val CAMERA_IMAGE_CAPTURE_REQUEST = 103
        private val GALLERY_REQUEST_CODE = 105

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }
}