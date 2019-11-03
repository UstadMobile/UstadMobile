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
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
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

import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleProductDetailPresenter
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.view.SaleProductDetailView
import com.ustadmobile.lib.db.entities.SaleProduct
import com.ustadmobile.lib.db.entities.SaleProductSelected
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Objects

import id.zelory.compressor.Compressor

class SaleProductDetailActivity : UstadBaseActivity(), SaleProductDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: SaleProductDetailPresenter? = null
    private var cRecyclerView: RecyclerView? = null

    private var menu: Menu? = null

    internal lateinit var titleEng: EditText
    internal lateinit var descEng: EditText
    internal lateinit var titleDari: EditText
    internal lateinit var descDari: EditText
    internal lateinit var titlePashto: EditText
    internal lateinit var descPastho: EditText
    internal lateinit var categoryTitle: TextView

    internal lateinit var productImageView: ImageView
    internal var IMAGE_MAX_HEIGHT = 1024
    internal var IMAGE_MAX_WIDTH = 1024
    internal var IMAGE_QUALITY = 75

    private var imagePathFromCamera: String? = null


    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_save, menu)

        menu.findItem(R.id.menu_save).isVisible = true
        return true
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == android.R.id.home) {
            onBackPressed()
            return true

        } else if (i == R.id.menu_save) {
            mPresenter!!.handleClickSave()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Don't show me the keyboard
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        //Setting layout:
        setContentView(R.layout.activity_sale_product_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_sale_product_detail_toolbar)
        toolbar!!.title = getText(R.string.create_new_subcategory)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Categories RecyclerView
        cRecyclerView = findViewById(R.id.activity_sale_product_detail_categories_rv)
        val mRecyclerLayoutManager = LinearLayoutManager(this)
        cRecyclerView!!.layoutManager = mRecyclerLayoutManager

        titleEng = findViewById(R.id.activity_sale_product_detail_title_english)
        titleDari = findViewById(R.id.activity_sale_product_detail_title_dari)
        titlePashto = findViewById(R.id.activity_sale_product_detail_title_pashto)

        descEng = findViewById(R.id.activity_sale_product_detail_desc_english)
        descDari = findViewById(R.id.activity_sale_product_detail_desc_dari)
        descPastho = findViewById(R.id.activity_sale_product_detail_desc_pashto)

        categoryTitle = findViewById(R.id.activity_sale_product_detail_category_title)

        productImageView = findViewById(R.id.activity_sale_product_detail_imageview)

        //Call the Presenter
        mPresenter = SaleProductDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Listeners
        titleEng.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateTitleEng(s.toString())
            }
        })
        titleDari.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateTitleDari(s.toString())
            }
        })
        titlePashto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateTitlePashto(s.toString())
            }
        })

        descEng.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateDescEng(s.toString())
            }
        })
        descDari.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateDescDari(s.toString())
            }
        })
        descPastho.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateDescPashto(s.toString())
            }
        })

        productImageView.setOnClickListener { v -> showGetImageAlertDialog() }

    }

    fun showGetImageAlertDialog() {

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

    override fun setListProvider(factory: DataSource.Factory<Int, SaleProductSelected>) {
        val recyclerAdapter = SaleProductCategorySelectorRecyclerAdapter(DIFF_CALLBACK, mPresenter!!,
                this)

        // get the provider, set , observe, etc.
        val data =
                LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        data.observe(this,
                Observer<PagedList<SaleProductSelected>> { recyclerAdapter.submitList(it) })

        //set the adapter
        cRecyclerView!!.adapter = recyclerAdapter
    }

    override fun updateToolbarTitle(titleName: String) {
        runOnUiThread { toolbar!!.title = titleName }
    }

    override fun updateCategoryTitle(titleName: String) {
        runOnUiThread { categoryTitle.text = titleName }
    }

    override fun initFromSaleProduct(saleProduct: SaleProduct) {
        if (saleProduct != null) {
            if (saleProduct.saleProductName != null && !saleProduct.saleProductName!!.isEmpty()) {
                titleEng.setText(saleProduct.saleProductName)
            }
            if (saleProduct.saleProductNameDari != null && !saleProduct.saleProductNameDari!!.isEmpty()) {
                titleDari.setText(saleProduct.saleProductNameDari)
            }
            if (saleProduct.saleProductNamePashto != null && !saleProduct.saleProductNamePashto!!.isEmpty()) {
                titlePashto.setText(saleProduct.saleProductNamePashto)
            }
            if (saleProduct.saleProductDesc != null && !saleProduct.saleProductDesc!!.isEmpty()) {
                descEng.setText(saleProduct.saleProductDesc)
            }
            if (saleProduct.saleProductDescDari != null && !saleProduct.saleProductDescDari!!.isEmpty()) {
                descDari.setText(saleProduct.saleProductDescDari)
            }
            if (saleProduct.saleProductDescPashto != null && !saleProduct.saleProductDescPashto!!.isEmpty()) {
                descPastho.setText(saleProduct.saleProductDescPashto)
            }

            if (saleProduct.saleProductCategory) {
                updateCategoryTitle(getText(R.string.parent_categories).toString())
            } else {
                updateCategoryTitle(getText(R.string.categories).toString())
            }

            if (!saleProduct.saleProductName!!.isEmpty())
                updateToolbarTitle(saleProduct.saleProductName!!)

        }
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
        val output = File(dir, mPresenter!!.currentSaleProduct!!.saleProductUid.toString() + "_image.png")
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
                    try {

                        mPresenter!!.handleCompressedImage(galleryFile.canonicalPath)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    override fun updateImageOnView(imagePath: String) {
        imagePathFromCamera = imagePath
        val output = File(imagePath)


        val iconDimen = dpToPx(150)

        if (output.exists()) {
            val profileImage = Uri.fromFile(output)

            runOnUiThread {
                Picasso
                        .get()
                        .load(profileImage)
                        .resize(iconDimen, iconDimen)
                        .centerCrop()
                        .into(productImageView)

                //Click on image - open dialog to show bigger picture
                productImageView.setOnClickListener{view ->
                        mPresenter!!.openPictureDialog(imagePath)};
            }

        }
    }


    override fun addImageFromCamera() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@SaleProductDetailActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST)
            return
        }
        startCameraIntent()
    }

    override fun addImageFromGallery() {
        //READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@SaleProductDetailActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    GALLERY_REQUEST_CODE)
            return
        }

        startGalleryIntent()
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

    companion object {

        private val CAMERA_PERMISSION_REQUEST = 104
        private val CAMERA_IMAGE_CAPTURE_REQUEST = 103
        private val GALLERY_REQUEST_CODE = 105

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleProductSelected> = object : DiffUtil.ItemCallback<SaleProductSelected>() {
            override fun areItemsTheSame(oldItem: SaleProductSelected,
                                         newItem: SaleProductSelected): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SaleProductSelected,
                                            newItem: SaleProductSelected): Boolean {
                return oldItem == newItem
            }
        }
    }

}
