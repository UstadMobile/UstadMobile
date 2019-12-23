package com.ustadmobile.port.android.view


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Picture
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.PictureDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SaleDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.SaleDetailView
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleItemListDetail
import com.ustadmobile.lib.db.entities.SalePayment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

class SaleDetailActivity : SelectSaleTypeDialogFragment.SaleTypeDialogListener,
        UstadBaseActivity(), SaleDetailView,
        CustomerDetailDialogFragment.ChoosenCustomerListener {

    private var toolbar: Toolbar? = null
    private var mPresenter: SaleDetailPresenter? = null
    private var mRecyclerView: RecyclerView? = null

    private var menu: Menu? = null

    private lateinit var locationSpinner: Spinner
    private lateinit var discountET: EditText
    private lateinit var orderNotesET: EditText
    private lateinit var orderTotal: TextView
    private lateinit var totalAfterDiscount: TextView
    private lateinit var addItemCL: ConstraintLayout
    private lateinit var customerET: EditText
    private var customerUid : Long = 0L

    private lateinit var c1: TextView
    private lateinit var c2: TextView
    private lateinit var c3: TextView
    private lateinit var c4: TextView
    private lateinit var c5: TextView
    private lateinit var c6: TextView
    private lateinit var hlineCalc: View
    private var saleUid: Long = 0

    private lateinit var recordVoiceNotesIB: ImageButton
    private lateinit var playIB: ImageButton
    private lateinit var stopIB: ImageButton

    private var recorder: MediaRecorder?=null

    private var player: MediaPlayer? = null

    internal var mStartRecording = true
    internal var mStartPlaying = true

    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    private var recorded = false
    private var fromFile = false

    private var paymentTV: TextView? = null
    private lateinit var addPaymentCL: ConstraintLayout
    private var pRecyclerView: RecyclerView? = null
    private var balanceDueTV: TextView? = null
    private var balanceTV: TextView? = null
    private var balanceCurrencyTV: TextView? = null
    private var paymentHLineBeforeRV: View? = null
    private var anotherOneHLine: View? = null

    private lateinit var deliveriesTV : TextView
    private lateinit var addDeliveriesCL : ConstraintLayout
    private lateinit var dRecyclerView: RecyclerView

    override fun onStop() {
        super.onStop()
        if (recorder != null) {
            recorder!!.release()
            recorder = null
        }

        if (player != null) {
            player!!.release()
            player = null
        }

    }

    override fun onSaleTypeSelected(sale: Boolean) {
        if(sale){
            mPresenter!!.handleClickAddSaleItemSold()

        }else{
            mPresenter!!.handleClickAddSaleItemPreOrder()
        }
    }

    private fun requestPermission() {

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@SaleDetailActivity, permissions, RECORD_AUDIO_PERMISSION_REQUEST)
            return
        }
        startRecording()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_AUDIO_PERMISSION_REQUEST -> permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted){
            finish()
        }

    }

    private fun onRecord(start: Boolean) {

        if (start) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun onPlay(start: Boolean) {
        if (start) {
            startPlaying()

        } else {
            stopPlaying()
            stopIB!!.visibility = View.INVISIBLE
            playIB!!.visibility = View.VISIBLE
        }
    }

    fun playStopped() {
        stopIB!!.visibility = View.INVISIBLE
        playIB!!.visibility = View.VISIBLE
    }

    private fun startPlaying() {
        player = MediaPlayer()
        player!!.setOnCompletionListener { mp -> playStopped() }
        try {

            player!!.setDataSource(saleVoiceNoteFilePath)
            player!!.prepare()
            player!!.start()

            stopIB!!.visibility = View.VISIBLE
            playIB!!.visibility = View.INVISIBLE


        } catch (e: IOException) {
            e.printStackTrace()


        }

    }

    private fun stopPlaying() {
        player!!.release()
        player = null
    }

    private fun startRecording() {

        if (recorded) {

            playRecordDeleteSound()
            //TODO: handle delete video (null it on the Sale)
            mPresenter!!.handleDeleteVoiceNote()

            recordVoiceNotesIB!!.setImageResource(R.drawable.ic_mic_black_24dp)
            recordVoiceNotesIB!!.visibility = View.VISIBLE
            playIB!!.visibility = View.INVISIBLE
            stopIB!!.visibility = View.INVISIBLE


            recorded = false
        } else {

            recorder = MediaRecorder()
            recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder!!.setOutputFile(saleVoiceNoteFilePath)
            recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                recorder!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            playRecordStartSound()

            recordVoiceNotesIB!!.setImageResource(R.drawable.animation_recording)
            val frameAnimation = recordVoiceNotesIB!!.drawable as AnimationDrawable
            frameAnimation.start()
            recordVoiceNotesIB!!.visibility = View.VISIBLE

            recorder!!.start()

            playIB!!.visibility = View.INVISIBLE
            stopIB!!.setImageResource(R.drawable.ic_stop_black_24dp)
            stopIB!!.visibility = View.VISIBLE

            recorded = true
        }
    }

    private fun stopRecording() {
        if (recorder != null) {
            recorder!!.stop()
            recorder!!.release()
            recorder = null

            playRecordStopSound()

            recordVoiceNotesIB!!.setImageResource(R.drawable.ic_delete_black_24dp)
            recordVoiceNotesIB!!.visibility = View.VISIBLE

            playIB!!.visibility = View.VISIBLE
            playIB!!.setImageResource(R.drawable.ic_play_arrow_black_24dp)

            stopIB!!.visibility = View.INVISIBLE

            mPresenter!!.voiceNoteFileName = saleVoiceNoteFilePath
        }

    }

    private fun playRecordDeleteSound() {
        player = MediaPlayer.create(this, R.raw.delete)
        player!!.start()
    }

    private fun playRecordStartSound() {
        player = MediaPlayer.create(this, R.raw.videorecord)
        player!!.start()
    }

    fun playRecordStopSound() {
        player = MediaPlayer.create(this, R.raw.videostop)
        player!!.start()
    }

    override fun sendMessage(toast: String) {

        runOnUiThread {
            Toast.makeText(
                    this,
                    toast,
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun initiateRecording() {

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermission()
        } else {

            onRecord(mStartRecording)
            mStartRecording = !mStartRecording
        }

    }

    fun initiatePlayRecording() {
        onPlay(mStartPlaying)

        mStartPlaying = !mStartPlaying
    }

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_save, menu)


        showSaveButton(mPresenter!!.isShowSaveButton)
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

        //Setting layout:
        setContentView(R.layout.activity_sale_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_sale_detail_toolbar)
        toolbar!!.title = getText(R.string.record_sale)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_sale_detail_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        pRecyclerView = findViewById(
                R.id.activity_sale_detail_payments_recyclerview)
        val pRecyclerLayoutManager = LinearLayoutManager(this)
        pRecyclerView!!.layoutManager = pRecyclerLayoutManager

        locationSpinner = findViewById(R.id.activity_sale_detail_location_spinner)
        discountET = findViewById(R.id.activity_sale_detail_discount)
        discountET.setText("0")
        orderTotal = findViewById(R.id.activity_sale_detail_order_total)
        orderTotal.text = "0"
        totalAfterDiscount = findViewById(R.id.activity_sale_detail_order_after_discount_tota)
        totalAfterDiscount.text = "0"
        orderNotesET = findViewById(R.id.activity_sale_detail_order_notes)
        addItemCL = findViewById(R.id.activity_sale_detail_add_cl)
        recordVoiceNotesIB = findViewById(R.id.activity_sale_detail_order_notes_record_voice_note_ib)
        playIB = findViewById(R.id.activity_sale_detail_order_notes_play_image_button)
        stopIB = findViewById(R.id.activity_sale_detail_order_notes_delete_ib)
        customerET = findViewById(R.id.activity_sale_detail_customer_edittext)

        c1 = findViewById(R.id.textView21)
        c2 = findViewById(R.id.activity_sale_detail_disc_currency4)
        c3 = findViewById(R.id.activity_sale_detail_disc_currency)
        c4 = findViewById(R.id.activity_sale_detail_disc_currency3)
        c5 = findViewById(R.id.textView)
        c6 = findViewById(R.id.textView23)
        hlineCalc = findViewById(R.id.view2)

        paymentTV = findViewById(R.id.activity_sale_detail_payments_title)
        addPaymentCL = findViewById(R.id.activity_sale_detail_add_payments_cl)
        //private TextView balanceDueTV, balanceTV, balanceCurrencyTV;
        balanceDueTV = findViewById(R.id.activity_sale_detail_balance_due_textview)
        balanceTV = findViewById(R.id.activity_sale_detail_order_after_discount_tota3)
        balanceTV!!.text = "0"
        balanceCurrencyTV = findViewById(R.id.activity_sale_detail_disc_currency5)
        paymentHLineBeforeRV = findViewById(R.id.hlinebeforePaymentRV)
        anotherOneHLine = findViewById(R.id.anotherHLinePaymentRelated)

        deliveriesTV = findViewById(R.id.activity_sale_detail_delivieries_title)
        addDeliveriesCL = findViewById(R.id.activity_sale_detail_add_deliveries_cl)
        dRecyclerView = findViewById(R.id.activity_sale_detail_deliveries_recyclerview)

        val dRecyclerLayoutManager = LinearLayoutManager(this)
        dRecyclerView.layoutManager = dRecyclerLayoutManager

        //Call the Presenter
        mPresenter = SaleDetailPresenter(this,
                bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(bundleToMap(savedInstanceState))

        discountET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                var discount: Long = 0
                if (s != null && s.length > 0) {
                    discount = java.lang.Long.valueOf(s.toString())
                }
                mPresenter!!.handleDiscountChanged(discount)
            }
        })

        orderNotesET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.handleOrderNotesChanged(s.toString())
            }
        })


        addItemCL.setOnClickListener { v -> mPresenter!!.handleClickAddSaleItem() }

        addPaymentCL!!.setOnClickListener { v -> mPresenter!!.handleClickAddPayment() }

        addDeliveriesCL.setOnClickListener{mPresenter!!.handleClickAddDelivery() }

        customerET.setOnTouchListener(View.OnTouchListener { view, motionEvent ->

                if(motionEvent.action == MotionEvent.ACTION_DOWN) {
                    mPresenter!!.handleClickCustomer();
                }
                true

        })

        //Location spinner
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleLocationSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        recordVoiceNotesIB!!.setOnClickListener { v -> initiateRecording() }

        playIB!!.setOnClickListener { v -> initiatePlayRecording() }

        // Record to the external cache directory for visibility
        saleVoiceNoteFilePath = externalCacheDir!!.absolutePath
        saleVoiceNoteFilePath += "/audiorecordtest.3gp"

        stopIB!!.setOnClickListener { v -> stopRecording() }


        totalAfterDiscount!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateBalanceDueFromTotal(java.lang.Float.valueOf(s.toString()))

            }
        })
    }

    override fun updateCustomerNameOnView(customerName: String) {
        customerET.setText(customerName)
    }

    override fun onSelectCustomerListener(cUid: Long, customerName: String) {
        customerUid = cUid
        mPresenter!!.updateCustomerUid(customerUid)
    }

    override fun setListProvider(factory: DataSource.Factory<Int, SaleItemListDetail>) {
        val recyclerAdapter = SaleItemRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                this)

        //saleItemDao.findAllSaleItemListDetailActiveBySaleProvider
        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
                .saleItemDaoBoundaryCallbacks.findAllSaleItemListDetailActiveBySaleProvider(factory)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20)
                .setBoundaryCallback(boundaryCallback)
                .build()


        val customObserver = Observer{ o:PagedList<SaleItemListDetail>->

            recyclerAdapter.submitList(o)
            mPresenter!!.getTotalSaleOrderAndDiscountAndUpdateView(saleUid)
        }

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP, customObserver)
        }

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }


    override fun setPaymentProvider(factory: DataSource.Factory<Int, SalePayment>) {
        val recyclerAdapter = SalePaymentRecyclerAdapter(DIFF_CALLBACK_PAYMENT, mPresenter!!, this,
                this)

        //salePaymentDao.findBySaleProvider
        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
                .salePaymentDaoBoundaryCallbacks.findBySaleProvider(factory)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20)
                .setBoundaryCallback(boundaryCallback)
                .build()

        val customObserver = Observer{ o:PagedList<SalePayment> ->
            recyclerAdapter.submitList(o)
            mPresenter!!.getTotalPaymentsAndUpdateTotalView(saleUid)
        }

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP, customObserver)
        }

        //set the adapter
        pRecyclerView!!.adapter = recyclerAdapter

    }

    override fun setDeliveriesProvider(factory: DataSource.Factory<Int, SaleDelivery>) {
        val recyclerAdapter = SaleDeliveryRecyclerAdapter(DIFF_CALLBACK_DELIVERY, mPresenter!!, this,
                this)

        val boundaryCallback = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
                .saleDeliveryDaoBoundaryCallbacks.findAllDeliveriesBySaleUid(factory)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(factory, 20)
                .setBoundaryCallback(boundaryCallback)
                .build()

        val customObserver = Observer{ o:PagedList<SaleDelivery> ->
            recyclerAdapter.submitList(o)
        }

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP, customObserver)
        }

        //set the adapter
        dRecyclerView.adapter = recyclerAdapter

    }


    override fun setLocationPresets(locationPresets: Array<String>, selectedPosition: Int) {

        val adapter = ArrayAdapter(this,
                R.layout.item_simple_spinner, locationPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinner!!.adapter = adapter
        locationSpinner!!.setSelection(selectedPosition)

    }

    override fun updateOrderTotal(orderTotalValue: Long) {
        runOnUiThread {
            orderTotal!!.text = orderTotalValue.toString()
            updateOrderTotalAfterDiscountTotalChanged(orderTotalValue)
        }

    }

    override fun updateOrderTotalAfterDiscount(discountValue: Long) {
        if (orderTotal!!.text !== "") {
            val orderTotalValue = java.lang.Long.parseLong(orderTotal!!.text.toString())
            val totalAfterDiscountVal = orderTotalValue - discountValue
            totalAfterDiscount!!.text = totalAfterDiscountVal.toString()
        } else {
            totalAfterDiscount!!.text = "0"
        }
    }

    override fun updateOrderTotalAfterDiscountTotalChanged(total: Long) {
        if (total > 0) {
            orderTotal!!.text = total.toString()
        }
        var discount: Long = 0
        if (discountET!!.text != null && discountET!!.text.toString() != "") {
            discount = java.lang.Long.parseLong(discountET!!.text.toString())
        }
        updateOrderTotalAfterDiscount(discount)

    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPostResume() {
        super.onPostResume()
    }

    override fun updateSaleOnView(sale: Sale) {
        runOnUiThread {
            if (sale != null) {
                saleUid = sale.saleUid

                orderNotesET!!.setText(sale.saleNotes)
                var discountValue = "0"
                if (sale.saleDiscount > 0) {
                    discountValue = sale.saleDiscount.toString()
                }
                discountET!!.setText(discountValue)
                if (sale.saleSignature != null && !sale.saleSignature!!.isEmpty()) {
                    try {
                        val saleSignature = sale.saleSignature
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
//                            signatureIB!!.invalidateDrawable(signatureIB!!.background)
//                            signatureIB!!.background = pd
                        }


                    } catch (e: SVGParseException) {
                        e.printStackTrace()
                    }

                }

                if(sale.saleCustomerUid != 0L){

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

    override fun updatePaymentTotal(paymentTotal: Long) {
        //Actually, this is the balance due total not payment total.
    }

    override fun showSaveButton(show: Boolean) {
        if (menu != null) {
            val saveButton = menu!!.findItem(R.id.menu_save)
            if (saveButton != null) {
                saveButton.isVisible = show
            }
        }
    }

    override fun showCalculations(show: Boolean) {
        if (show) {
            toolbar!!.title = getText(R.string.sale_details)
        } else {
            toolbar!!.title = getText(R.string.record_sale)
        }

        c1!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        c2!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        c3!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        c4!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        c5!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        c6!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        discountET!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        discountET!!.isEnabled = show
        orderTotal!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        orderTotal!!.isEnabled = show
        totalAfterDiscount!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        totalAfterDiscount!!.isEnabled = show
        hlineCalc!!.visibility = if (show) View.VISIBLE else View.INVISIBLE

    }

    override fun showDelivered(show: Boolean) {
        //TODO: Remove or replace
    }

    override fun showNotes(show: Boolean) {
        orderNotesET!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        recordVoiceNotesIB!!.visibility = if (show) View.VISIBLE else View.INVISIBLE

    }

    override fun showDeliveries(show: Boolean) {
        runOnUiThread {
            deliveriesTV.visibility = if(show) View.VISIBLE else View.INVISIBLE
            addDeliveriesCL.visibility = if(show) View.VISIBLE else View.INVISIBLE
            dRecyclerView.visibility = if(show) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun showPayments(show: Boolean) {
        paymentTV!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        addPaymentCL!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        pRecyclerView!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        //private TextView balanceDueTV, balanceTV, balanceCurrencyTV;
        balanceCurrencyTV!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        balanceTV!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        balanceDueTV!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        paymentHLineBeforeRV!!.visibility = if (show) View.VISIBLE else View.INVISIBLE
        anotherOneHLine!!.visibility = if (show) View.VISIBLE else View.INVISIBLE

        deliveriesTV.visibility = if(show) View.VISIBLE else View.INVISIBLE
        addDeliveriesCL.visibility = if(show) View.VISIBLE else View.INVISIBLE
        dRecyclerView.visibility = if(show) View.VISIBLE else View.INVISIBLE

    }


    override fun updateSaleVoiceNoteOnView(fileName: String) {
        if (fileName != null && !fileName.isEmpty()) {
            saleVoiceNoteFilePath = fileName
            playIB!!.setImageResource(R.drawable.ic_play_arrow_black_24dp)
            playIB!!.visibility = View.VISIBLE
            stopIB!!.visibility = View.INVISIBLE
            stopIB!!.setImageResource(R.drawable.ic_stop_black_24dp)
            recordVoiceNotesIB!!.visibility = View.VISIBLE
            recordVoiceNotesIB!!.setImageResource(R.drawable.ic_delete_black_24dp)
            recorded = true
            fromFile = true
        }
    }

    override fun updateBalanceDue(balance: Long) {
        runOnUiThread { balanceTV!!.text = balance.toString() }

    }

    companion object {
        private val RECORD_AUDIO_PERMISSION_REQUEST = 108
        var saleVoiceNoteFilePath: String? = null


        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<SaleItemListDetail> = object : DiffUtil.ItemCallback<SaleItemListDetail>() {
            override fun areItemsTheSame(oldItem: SaleItemListDetail,
                                         newItem: SaleItemListDetail): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SaleItemListDetail,
                                            newItem: SaleItemListDetail): Boolean {
                return oldItem.saleItemUid == newItem.saleItemUid
            }
        }

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK_PAYMENT: ItemCallback<SalePayment> = object : DiffUtil.ItemCallback<SalePayment>() {
            override fun areItemsTheSame(oldItem: SalePayment,
                                         newItem: SalePayment): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SalePayment,
                                            newItem: SalePayment): Boolean {
                return oldItem.salePaymentUid == newItem.salePaymentUid
            }
        }

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK_DELIVERY: ItemCallback<SaleDelivery> = object : DiffUtil.ItemCallback<SaleDelivery>() {
            override fun areItemsTheSame(oldItem: SaleDelivery,
                                         newItem: SaleDelivery): Boolean {
                return oldItem.saleDeliveryUid == newItem.saleDeliveryUid
            }

            override fun areContentsTheSame(oldItem: SaleDelivery,
                                            newItem: SaleDelivery): Boolean {
                return oldItem == newItem
            }
        }
    }

}
