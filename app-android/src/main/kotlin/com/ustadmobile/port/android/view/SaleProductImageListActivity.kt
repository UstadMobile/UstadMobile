package com.ustadmobile.port.android.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.toughra.ustadmobile.R

import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.core.controller.SaleProductImageListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.SaleProductImageListView
import com.ustadmobile.lib.db.entities.SaleProductPicture
import id.zelory.compressor.Compressor
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.io.*


class SaleProductImageListActivity : UstadBaseActivity(), SaleProductImageListView {

    private var toolbar: Toolbar? = null
    private var mPresenter: SaleProductImageListPresenter? = null

    // If you have a recycler view 
    private var mRecyclerView: RecyclerView? = null

    private var imagePathFromCamera: String? = null


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
        setContentView(R.layout.activity_saleproductimagelist)

        //Toolbar:
        toolbar = findViewById(R.id.activity_saleproductimagelist_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_saleproductimagelist_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = SaleProductImageListPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_saleproductimagelist_fab)

        fab.setOnClickListener { mPresenter!!.handleClickAddPicture() }


    }

    override fun setListProvider(factory: DataSource.Factory<Int, SaleProductPicture>) {
        val recyclerAdapter = SaleProductImageListRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                applicationContext)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        data.observe(this, Observer<PagedList<SaleProductPicture>> { recyclerAdapter.submitList(it) })

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_save, menu)
        return true
    }

    override fun showGetImageAlertDialog() {
        val adb = AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("Select image from Camera or Gallery")

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

    private fun addImageFromCamera() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST)
            return
        }
        startCameraIntent()
    }

    private fun addImageFromGallery() {
        //READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
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
        val output = File(dir, "saleproductimage"+ mPresenter!!.productUid.toString() +
                (0..9999).random() + "_image.png")
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
        startActivityForResult(cameraIntent, CAMERA_IMAGE_CAPTURE_REQUEST)
    }

    //this is how you check permission grant task result.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> if (grantResults.size > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraIntent()
            }
            GALLERY_REQUEST_CODE -> if (grantResults.size > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGalleryIntent()
            }
        }
    }

    /**
     * Compress the image set using Compressor.
     *
     */
    private fun compressImage() {
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_IMAGE_CAPTURE_REQUEST -> {

                    //Compress the image:
                    compressImage()

                    val imageFile = File(imagePathFromCamera)
                    mPresenter!!.handleNewCompressedImage(imageFile.canonicalPath)
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
                    try {

                        mPresenter!!.handleNewCompressedImage(galleryFile.canonicalPath)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    private fun sendMessage(messageId: Int) {
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


    companion object {

        private val CAMERA_PERMISSION_REQUEST = 114
        private val CAMERA_IMAGE_CAPTURE_REQUEST = 113
        private val GALLERY_REQUEST_CODE = 115

        internal var IMAGE_MAX_HEIGHT = 1024
        internal var IMAGE_MAX_WIDTH = 1024
        internal var IMAGE_QUALITY = 75

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleProductPicture> = object
            : DiffUtil.ItemCallback<SaleProductPicture>() {
            override fun areItemsTheSame(oldItem: SaleProductPicture,
                                         newItem: SaleProductPicture): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SaleProductPicture,
                                            newItem: SaleProductPicture): Boolean {
                return oldItem == newItem
            }
        }

    }
}
