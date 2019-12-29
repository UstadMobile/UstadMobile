package com.ustadmobile.port.android.view

import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.github.gcacace.signaturepad.views.SignaturePad
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleDeliveryDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SaleDeliveryDetailView
import com.ustadmobile.core.view.SaleItemDetailView
import com.ustadmobile.lib.db.entities.PersonWithInventory
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleItemListDetail
import ru.dimorinny.floatingtextbutton.FloatingTextButton


class SaleDeliveryDetailActivity : UstadBaseActivity(), SaleDeliveryDetailView {

    private lateinit var toolbar: Toolbar
    private lateinit var mPresenter: SaleDeliveryDetailPresenter
    private lateinit var acceptFAB : FloatingTextButton
    private lateinit var clearFAB : FloatingTextButton
    private lateinit var signature : SignaturePad
    private lateinit var signatureWrapper: View
    private lateinit var deliveriesLL : LinearLayout

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
        setContentView(R.layout.activity_sale_delivery_detail)

        //Views:
        toolbar = findViewById(R.id.activity_sale_delivery_detail_toolbar)
        acceptFAB = findViewById(R.id.activity_sale_delivery_detail_fab_accept)
        clearFAB = findViewById(R.id.activity_sale_delivery_detail_fab_clear)
        signature = findViewById(R.id.activity_sale_delivery_detail_signaturepad)
        signatureWrapper = findViewById(R.id.activity_sale_delivery_detail_signature_wrapper)
        deliveriesLL = findViewById(R.id.activity_delivery_selection_linearlayout)

        //Set toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Call the Presenter
        mPresenter = SaleDeliveryDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
        mPresenter.inventorySelection = true

        signature.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {}

            override fun onSigned() {
                val signSvg = signature.getSignatureSvg()
                mPresenter.updateSignatureSvg(signSvg)
            }

            override fun onClear() {
                mPresenter.updateSignatureSvg("")
            }
        })
        acceptFAB.setOnClickListener{ handleClickAccept() }
        clearFAB.setOnClickListener{ clearSignature() }

    }

    private fun handleClickAccept(){

        val weCountMap = mPresenter.getWeCountMap()
        val impl = UstadMobileSystemImpl.instance
        var allgood = true
        for(saleItemUid in weCountMap.keys){

            val saleItemListDetail = mPresenter!!.saleItemToSaleItemListDetail.get(saleItemUid)
            val saleItemMap = weCountMap.get(saleItemUid)
            var totalSaleItemSelected = 0
            for(weUid in saleItemMap!!.keys){
                val weCount = saleItemMap!!.get(weUid)
                totalSaleItemSelected += weCount!!
            }
            val remaining = saleItemListDetail!!.saleItemQuantity - saleItemListDetail!!.deliveredCount
            if(totalSaleItemSelected > remaining){
                //Alert user that they have selected more than required.

                val alertText = getText(R.string.over_selected_delivery).toString() + " " +
                        saleItemListDetail.saleItemSaleProduct!!.getNameLocale(impl.getLocale(this))
                sendMessage(alertText)
                allgood = false
            }

        }
        if(allgood) {
            mPresenter.handleClickAccept()
        }
    }

    private fun sendMessage(toast: String) {

        Toast.makeText(
                this,
                toast,
                Toast.LENGTH_SHORT
        ).show()

    }

    private fun clearSignature() {
        signature.setBackground(null)
        signature.clear()
        mPresenter.updateSignatureSvg("")
    }

    override fun setUpView(saleItems: List<SaleItemListDetail>) {

        deliveriesLL.removeAllViews()
        for(item in saleItems){
            var products : List<PersonWithInventory>
            products  = emptyList()

            var entry = SaleDeliverySelectionView(this, products, item, mPresenter)
            deliveriesLL.addView(entry)
        }
    }

    override fun setUpAllViews(itemsWithProducers : HashMap<SaleItemListDetail, List<PersonWithInventory>>){
        deliveriesLL.removeAllViews()
        val keys = itemsWithProducers.keys
        for(item in keys){
            var products = itemsWithProducers.get(item)

            var entry = SaleDeliverySelectionView(this, products!!, item, mPresenter)
            deliveriesLL.addView(entry)
        }
    }

    override fun updateSaleDeliveryOnView(saleDelivery: SaleDelivery) {

        if (!saleDelivery.saleDeliverySignature.isEmpty()) {
            try {
                val saleSignature = saleDelivery.saleDeliverySignature
                val svg = SVG.getFromString(saleSignature)

                val signPic = svg.renderToPicture()

                val picW = signPic.getWidth()
                val picH = signPic.getHeight()
                var adjustedPic = signPic
                if (picH > picW) {
                    adjustedPic = rotatePicture(0f, signPic)
                }
                val pd = PictureDrawable(adjustedPic)

                runOnUiThread {
                    signature.invalidateDrawable(signature.background)
                    signature.background = pd
                }


            } catch (e: SVGParseException) {
                e.printStackTrace()
            }
        }
    }

    fun rotatePicture(degrees: Float, picture: Picture): Picture {
        val width = picture.width
        val height = picture.height

        val rotatedPicture = Picture()
        val canvas = rotatedPicture.beginRecording(width, height)
        canvas.save()
        canvas.rotate(degrees, width.toFloat(), height.toFloat())
        picture.draw(canvas)
        canvas.restore()
        rotatedPicture.endRecording()

        return rotatedPicture
    }


    companion object {


    }
}
