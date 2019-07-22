package com.ustadmobile.port.android.view

import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.github.gcacace.signaturepad.views.SignaturePad
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleDetailSignaturePresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SaleDetailSignatureView
import com.ustadmobile.lib.db.entities.Sale

class SaleDetailSignatureActivity : UstadBaseActivity(), SaleDetailSignatureView {

    private var toolbar: Toolbar? = null
    private var mPresenter: SaleDetailSignaturePresenter? = null
    internal lateinit var mSignaturePad: SignaturePad


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
        setContentView(R.layout.activity_record_signature)

        //Toolbar:
        toolbar = findViewById(R.id.activity_record_signature_toolbar)
        toolbar!!.title = getText(R.string.add_signature)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Call the Presenter
        mPresenter = SaleDetailSignaturePresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        mSignaturePad = findViewById(R.id.activity_record_signature_signaturepad)


        //FAB and its listener
        val accept = findViewById<View>(R.id.activity_record_signature_fab_accept)
        accept.setOnClickListener({ v -> mPresenter!!.handleClickAccept() })

        val cancel = findViewById<View>(R.id.activity_record_signature_fab_clear)
        cancel.setOnClickListener({ v -> clearSignature() })

        mSignaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {}

            override fun onSigned() {
                val signSvg = mSignaturePad.getSignatureSvg()
                mPresenter!!.updateSignatureSvg(signSvg)
            }

            override fun onClear() {
                mPresenter!!.updateSignatureSvg("")
            }
        })

    }

    private fun clearSignature() {

        mSignaturePad.setBackground(null)
        mSignaturePad.clear()
    }


    override fun updateSaleOnView(sale: Sale) {
        //Updare Signature on view.

        if (sale != null) {
            val saleSignature = sale.saleSignature
            if (saleSignature != null && !saleSignature.isEmpty()) {

                try {

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
                        mSignaturePad.setBackground(pd)
                    }
                } catch (spe: SVGParseException) {
                    spe.printStackTrace()
                }

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
}
