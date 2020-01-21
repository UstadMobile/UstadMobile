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
    private lateinit var personPictureIV : ImageView
    private lateinit var mPresenter : CommonInventorySelectionPresenter<*>

    private var personWithInventory : PersonWithInventory? = null
    private var saleItemUid : Long = 0
    private var remainingDelivery : Int = -1

    interface OnStockSelectedListener {
        fun onStockSelected(view: PersonWithInventorySelectionView)
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, personInventory: PersonWithInventory,
                presenter: CommonInventorySelectionPresenter<*>, remaining: Int) : super(context) {
        personWithInventory = personInventory
        mPresenter = presenter
        remainingDelivery = remaining
        init()
    }

    constructor(context: Context, personInventory: PersonWithInventory,
                presenter: CommonInventorySelectionPresenter<*>, itemUid: Long, remaining: Int) : super(context) {
        personWithInventory = personInventory
        mPresenter = presenter
        remainingDelivery = remaining
        saleItemUid = itemUid
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

            val inventoryCount = personWithInventory!!.inventoryCount
            val selected = personWithInventory!!.inventorySelected
            val deliveredCountTotal = personWithInventory!!.inventoryCountDeliveredTotal
            val deliveryCountThis = personWithInventory!!.inventoryCountDelivered

            //TODO: Also edittext max limit somehow

            if(mPresenter!!.inventorySelection) {
                if(remainingDelivery >= 0){
                    seekBar.max = remainingDelivery
                }else {
                    seekBar.max = personWithInventory!!.inventoryCount
                }

                if(personWithInventory!!.inventorySelected > 0){

                }
                //Show stock
                stockTV.visibility = View.VISIBLE

                var sbct = ""
                sbct = inventoryCount.toString() + " " +
                        context.getText(R.string.in_stock)
                if(mPresenter!!.saleItemPreOrder){
                    sbct = inventoryCount.toString() + " " +
                        context.getText(R.string.in_stock)
                } else if(selected > -1 && mPresenter!!.deliveryMode){
                    sbct = selected.toString() + " " +
                            context.getText(R.string.in_stock_selected) + ", " +
                            deliveredCountTotal + " " + context.getText(R.string.already_delivered)

                    if(!mPresenter!!.newDelivery){
                        sbct += ", " + deliveryCountThis + " " + context.getText(R.string.delivered_here)
                    }
                }
                stockTV.setText(sbct)


            }else{
                if(remainingDelivery >= 0){
                    if(remainingDelivery > inventoryCount) {
                        seekBar.max = remainingDelivery
                    }
                }else {
                    seekBar.max = SEEKBAR_MAX
                }
                //Hide stock
                stockTV.visibility = View.INVISIBLE
            }


            if(mPresenter!!.deliveryMode){

                if(mPresenter!!.newDelivery){
                    if( selected > -1 && remainingDelivery < 0){
                        seekBar.max = selected
                        //Don't let people put the number
                        seekBarET.isEnabled = false
                    }

                    if(selected < remainingDelivery){
                        seekBar.max = selected
                        //seekBarET.isEnabled = false
                    }

                    if(remainingDelivery > -1 && deliveredCountTotal > -1 ){
                        var left = selected - deliveredCountTotal
                        seekBar.max = left
                    }

                    if(mPresenter.saleItemPreOrder){
                        if(remainingDelivery < inventoryCount){
                            seekBar.max = remainingDelivery
                        }else {
                            seekBar.max = inventoryCount
                        }
                    }

                }

                if(!mPresenter!!.newDelivery){
                    if( selected > -1){
                        //Freeze selection
                        seekBarET.setText(selected.toString())
                        seekBarET.isEnabled = false
                        seekBar.progress = selected
                        seekBar.max = selected +  personWithInventory!!.inventoryCount
                        seekBar.setEnabled(false)
                    }


                    if(deliveryCountThis > -1){
                        seekBar.max = selected
                        seekBar.progress = deliveryCountThis
                        seekBar.isEnabled = false

                        seekBarET.setText(deliveryCountThis.toString())
                        seekBarET.isEnabled = false
                    }
                }
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


        }


    }

    fun updateProfilePictureOnView(){
        var imgPath = ""
        GlobalScope.async(Dispatchers.Main) {

            val personPictureDaoRepo =
                    UmAccountManager.getRepositoryForActiveAccount(this).personPictureDao
            val personPictureDao =
                    UmAccountManager.getActiveDatabase(this).personPictureDao

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
