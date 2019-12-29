package com.ustadmobile.port.android.view

import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectProducersPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.SelectProducersView
import com.ustadmobile.lib.db.entities.PersonWithInventory
import com.ustadmobile.port.android.view.PersonEditActivity.Companion.DEFAULT_PADDING
import com.ustadmobile.port.android.view.PersonEditActivity.Companion.dpToPx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File


class SelectProducersActivity : UstadBaseActivity(), SelectProducersView {

    private var toolbar: Toolbar? = null
    private var mPresenter: SelectProducersPresenter? = null
    private var mLinearLayout: LinearLayout? = null

    private var sortSpinner: Spinner? = null
    internal lateinit var sortSpinnerPresets: Array<String?>

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_done, menu)

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
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.menu_done -> {
                mPresenter!!.handleClickSave()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_select_producers)

        //Toolbar:
        toolbar = findViewById(R.id.activity_select_producers_toolbar)
        toolbar!!.title = getText(R.string.select_producers)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        sortSpinner = findViewById(R.id.activity_select_producers_spinner)

        //RecyclerView
        mLinearLayout = findViewById(
                R.id.activity_select_producers_linearlayout)

        //Call the Presenter
        mPresenter = SelectProducersPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Sort spinner handler
        sortSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleChangeSortOrder(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
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


    override fun updateProducersOnView(producers: List<PersonWithInventory>) {

        mLinearLayout!!.removeAllViews()

        for(producer in producers){

            val personWithInventorySelection = PersonWithInventorySelectionView(this, producer, mPresenter!!, -1)
            mLinearLayout!!.addView(personWithInventorySelection)

        }
    }



    fun updateProducersOnView2(producers: List<PersonWithInventory>) {

        mLinearLayout!!.removeAllViews()


        val matchParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)

        val wrapParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        val personPictureWidth = PersonEditActivity.dpToPx(48)
        val iconParams = ViewGroup.LayoutParams(personPictureWidth, personPictureWidth)

        val pixelOffset = dpToPx(48 + 5 * DEFAULT_PADDING )
        //Calculate the width of the screen.
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val widthWithPadding = displayWidth - pixelOffset

        val seekParams = ViewGroup.LayoutParams(widthWithPadding,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        for(producer in producers){

            val producerName = producer.fullName()
            val inventoryCount = producer.inventoryCount

            //Add the Header
            val header = TextView(this)
            header.text = producerName
            header.setTextColor(ContextCompat.getColor(this, R.color.primaryTextColor))
            header.textSize = PersonEditActivity.HEADER_TEXT_SIZE.toFloat()
            header.setPadding(DEFAULT_PADDING,
                    PersonEditActivity.DEFAULT_PADDING_HEADER_BOTTOM, 0, 0)
            mLinearLayout!!.addView(header)


            //Horizontal layout: Icon Area space + field (Horizontal)
            val fieldHLayout = LinearLayout(this)
            fieldHLayout.layoutParams = matchParams
            fieldHLayout.orientation = LinearLayout.HORIZONTAL


            val iconName = "ic_person_black_24dp"

            val iconResId = getResourceId(iconName, "drawable", packageName)
            val icon = AppCompatImageView(this)
            icon.layoutParams = iconParams

            //icon.setImageAlpha(0)
            icon.setImageResource(iconResId)
            icon.setPadding(DEFAULT_PADDING, 0,
                    PersonEditActivity.DEFAULT_TEXT_PADDING_RIGHT, 0)
            fieldHLayout.addView(icon)

            val seekBar = SeekBar(this)
            if(mPresenter!!.inventorySelection) {
                seekBar.max = inventoryCount
            }else{
                seekBar.max = SEEKBAR_MAX
            }
            seekBar.progress = 0
            seekBar.layoutParams = seekParams
            seekBar.setPadding(DEFAULT_PADDING * 2,
                    DEFAULT_PADDING * 3,
                    0,
                    0)



            val seekValueET = EditText(this)
            seekValueET.setTextColor(ContextCompat.getColor(this, R.color.primaryTextColor))
            seekValueET.setInputType(InputType.TYPE_CLASS_NUMBER)
            seekValueET.textSize = PersonEditActivity.HEADER_TEXT_SIZE.toFloat()
            seekValueET.layoutParams = wrapParams
            seekValueET.textSize = 16F
            seekValueET.setText("0")
            seekValueET.setPadding(DEFAULT_PADDING * 2,DEFAULT_PADDING,DEFAULT_PADDING,DEFAULT_PADDING)
            seekValueET.setKeyListener(DigitsKeyListener.getInstance("0123456789"))

            seekValueET.addTextChangedListener(object: TextWatcher{
                override fun afterTextChanged(p0: Editable?) {
                    try {
                        val value = p0.toString().toInt()
                        mPresenter!!.updateWeCount(producer.personUid, value, 0)
                    }catch (nfe: NumberFormatException){
                        mPresenter!!.updateWeCount(producer!!.personUid, 0, 0)
                    }

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            })

            seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

                override fun onStopTrackingTouch(arg0: SeekBar) { }

                override fun onStartTrackingTouch(arg0: SeekBar) {}

                override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {
                    seekValueET.setText(arg1.toString())
                }
            })

            val seekBarWithCountLL = LinearLayout(this)
            seekBarWithCountLL.orientation = LinearLayout.VERTICAL
            seekBarWithCountLL.addView(seekBar)

            val seekBarCounter = TextView(this)
            seekBarCounter.setPadding(DEFAULT_PADDING*2,0,0,0)
            seekBarCounter.textSize = 12F

            if(mPresenter!!.inventorySelection) {
                val sbct = inventoryCount.toString() + " " + getText(R.string.in_stock)
                seekBarCounter.setText(sbct)
                seekBarWithCountLL.addView(seekBarCounter)
            }

            fieldHLayout.addView(seekBarWithCountLL)
            fieldHLayout.addView(seekValueET)

            mLinearLayout!!.addView(fieldHLayout)



            var imgPath = ""
            GlobalScope.async(Dispatchers.Main) {

                val personPictureDaoRepo =
                        UmAccountManager.getRepositoryForActiveAccount(this).personPictureDao
                val personPictureDao =
                        UmAppDatabase.getInstance(this).personPictureDao

                val personPictureLocal = personPictureDao.findByPersonUidAsync(producer.personUid)
                imgPath = personPictureDaoRepo!!.getAttachmentPath(personPictureLocal!!)!!

                if (!imgPath!!.isEmpty())
                    setPictureOnView(imgPath, icon!!)
                else
                    icon.setImageResource(R.drawable.ic_person_black_new_24dp)

                val personPictureEntity = personPictureDaoRepo!!.findByPersonUidAsync(producer.personUid)
                imgPath = personPictureDaoRepo!!.getAttachmentPath(personPictureEntity!!)!!

                if(personPictureLocal != personPictureEntity) {
                    if (!imgPath!!.isEmpty())
                        setPictureOnView(imgPath, icon!!)
                    else
                        icon.setImageResource(R.drawable.ic_person_black_new_24dp)
                }
            }

        }
    }


    private fun setPictureOnView(imagePath: String, theImage: ImageView) {

        val imageUri = Uri.fromFile(File(imagePath))

        Picasso
                .get()
                .load(imageUri)
                .resize(0, dpToPxImagePerson())
                .noFade()
                .into(theImage)
    }

    private fun dpToPxImagePerson(): Int {
        return (IMAGE_PERSON_THUMBNAIL_WIDTH *
                Resources.getSystem().displayMetrics.density).toInt()
    }

    override fun updateSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(this,
                R.layout.item_simple_spinner_gray, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner!!.adapter = adapter
    }

    companion object {
        private val IMAGE_PERSON_THUMBNAIL_WIDTH = 48

        private val SEEKBAR_MAX = 150
    }
}
