package com.ustadmobile.port.android.view

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CommonInventorySelectionPresenter
import com.ustadmobile.core.controller.SelectProducersPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.PersonWithInventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File

/**
 * Created by mike on 9/22/17.
 */
class PersonWithInventorySelectionView : ConstraintLayout {

    private lateinit var producerNameTV : TextView
    private lateinit var stockTV : TextView
    private lateinit var seekBar : SeekBar
    private lateinit var seekBarET: EditText
    private lateinit var inStockTV : TextView
    private lateinit var personPictureIV : ImageView
    private var mPresenter : CommonInventorySelectionPresenter<*>? = null

    private var personWithInventory : PersonWithInventory? = null
    private var saleItemUid : Long = 0

    interface OnStockSelectedListener {
        fun onStockSelected(view: PersonWithInventorySelectionView)
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, personInventory: PersonWithInventory,
                presenter: CommonInventorySelectionPresenter<*>) : super(context) {
        personWithInventory = personInventory
        mPresenter = presenter
        init()
    }

    constructor(context: Context, personInventory: PersonWithInventory,
                presenter: CommonInventorySelectionPresenter<*>, itemUid: Long) : super(context) {
        personWithInventory = personInventory
        mPresenter = presenter
        saleItemUid = itemUid
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.view_producer_inventory_selection, this)
        producerNameTV = findViewById(R.id.view_producer_with_inventory_selection_name)
        stockTV = findViewById(R.id.view_producer_with_inventory_selection_stock_tv)
        seekBar = findViewById(R.id.view_producer_with_inventory_selection_seekbar)
        seekBarET = findViewById(R.id.view_producer_with_inventory_selection_selected_edittext)
        personPictureIV = findViewById(R.id.view_producer_with_inventory_selection_picture)

        if(personWithInventory != null){
            updateProfilePictureOnView()

            producerNameTV.setText(personWithInventory!!.fullName())

            seekBar.progress = 0

            if(mPresenter!!.inventorySelection) {
                seekBar.max = personWithInventory!!.inventoryCount
            }else{
                seekBar.max = SEEKBAR_MAX
            }

            seekBarET.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    try {
                        val value = p0.toString().toInt()
                        mPresenter!!.updateWeCount(personWithInventory!!.personUid, value,saleItemUid)
                    }catch (nfe: NumberFormatException){
                        mPresenter!!.updateWeCount(personWithInventory!!.personUid, 0,saleItemUid)
                    }

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            })

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                override fun onStopTrackingTouch(arg0: SeekBar) { }

                override fun onStartTrackingTouch(arg0: SeekBar) {}

                override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {
                    seekBarET.setText(arg1.toString())
                }
            })

            if(mPresenter!!.inventorySelection) {
                stockTV.visibility = View.VISIBLE
                val sbct = personWithInventory!!.inventoryCount.toString() + " " +
                        context.getText(R.string.in_stock)
                stockTV.setText(sbct)
            }else{
                stockTV.visibility = View.INVISIBLE
            }
        }


    }

    fun updateProfilePictureOnView(){
        var imgPath = ""
        GlobalScope.async(Dispatchers.Main) {

            val personPictureDaoRepo =
                    UmAccountManager.getRepositoryForActiveAccount(this).personPictureDao
            val personPictureDao =
                    UmAppDatabase.getInstance(this).personPictureDao

            val personPictureLocal = personPictureDao.findByPersonUidAsync(personWithInventory!!.personUid)
            imgPath = personPictureDaoRepo.getAttachmentPath(personPictureLocal!!)!!

            if (!imgPath!!.isEmpty())
                setPictureOnView(imgPath, personPictureIV)
            else
                personPictureIV.setImageResource(R.drawable.ic_person_black_new_24dp)

            val personPictureEntity = personPictureDaoRepo.findByPersonUidAsync(personWithInventory!!.personUid)
            imgPath = personPictureDaoRepo.getAttachmentPath(personPictureEntity!!)!!

            if(personPictureLocal != personPictureEntity) {
                if (!imgPath!!.isEmpty())
                    setPictureOnView(imgPath, personPictureIV)
                else
                    personPictureIV.setImageResource(R.drawable.ic_person_black_new_24dp)
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

    companion object {
        private val IMAGE_PERSON_THUMBNAIL_WIDTH = 48

        private val SEEKBAR_MAX = 150
    }

}
