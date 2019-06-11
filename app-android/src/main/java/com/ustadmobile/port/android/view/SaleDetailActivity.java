package com.ustadmobile.port.android.view;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.PictureDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.SaleDetailView;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleItemListDetail;
import com.ustadmobile.lib.db.entities.SalePayment;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.IOException;
import java.util.Objects;

public class SaleDetailActivity extends UstadBaseActivity implements SaleDetailView {

    private Toolbar toolbar;
    private SaleDetailPresenter mPresenter;
    private RecyclerView mRecyclerView;

    private Menu menu;

    private Spinner locationSpinner;
    private EditText discountET,orderNotesET;
    private CheckBox deliveredCB;
    private TextView orderTotal, totalAfterDiscount;
    private ConstraintLayout addItemCL;

    private TextView c1, c2, c3,c4,c5,c6;
    View hlineCalc;
    private long saleUid;

    private ImageButton recordVoiceNotesIB, playIB, stopIB;
    private static final int RECORD_AUDIO_PERMISSION_REQUEST = 108;
    private static String saleVoiceNoteFilePath = null;

    private MediaRecorder recorder = null;

    private MediaPlayer player = null;

    boolean mStartRecording = true;
    boolean mStartPlaying = true;

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private boolean recorded = false;
    private boolean fromFile = false;

    private TextView paymentTV;
    private ConstraintLayout addPaymentCL;
    private RecyclerView pRecyclerView;
    private TextView balanceDueTV, balanceTV, balanceCurrencyTV;
    private View paymentHLineBeforeRV, anotherOneHLine;

    //Signature
    private TextView signatureTitleTV;
    private ImageButton signatureIB;


    public static String getSaleVoiceNoteFilePath() {
        return saleVoiceNoteFilePath;
    }

    public static void setSaleVoiceNoteFilePath(String saleVoiceNoteFilePath) {
        SaleDetailActivity.saleVoiceNoteFilePath = saleVoiceNoteFilePath;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }

    }

    public void requestPermission(){

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, RECORD_AUDIO_PERMISSION_REQUEST);
            return;
        }



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case RECORD_AUDIO_PERMISSION_REQUEST:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    private void onRecord(boolean start) {

        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();

        } else {
            stopPlaying();
            stopIB.setVisibility(View.INVISIBLE);
            playIB.setVisibility(View.VISIBLE);
        }
    }

    public void playStopped(){
        stopIB.setVisibility(View.INVISIBLE);
        playIB.setVisibility(View.VISIBLE);
    }

    private void startPlaying(){
        player = new MediaPlayer();
        player.setOnCompletionListener(mp -> playStopped());
        try {

            player.setDataSource(saleVoiceNoteFilePath);
            player.prepare();
            player.start();

            stopIB.setVisibility(View.VISIBLE);
            playIB.setVisibility(View.INVISIBLE);


        } catch (IOException e) {
            e.printStackTrace();



        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {

        if(recorded){

            playRecordDeleteSound();
            //TODO: handle delete video (null it on the Sale)
            mPresenter.handleDeleteVoiceNote();

            recordVoiceNotesIB.setImageResource(R.drawable.ic_mic_black_24dp);
            recordVoiceNotesIB.setVisibility(View.VISIBLE);
            playIB.setVisibility(View.INVISIBLE);
            stopIB.setVisibility(View.INVISIBLE);


            recorded = false;
        }else {

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(saleVoiceNoteFilePath);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            playRecordStartSound();

            recordVoiceNotesIB.setImageResource(R.drawable.animation_recording);
            AnimationDrawable frameAnimation = (AnimationDrawable) recordVoiceNotesIB.getDrawable();
            frameAnimation.start();
            recordVoiceNotesIB.setVisibility(View.VISIBLE);

            recorder.start();

            playIB.setVisibility(View.INVISIBLE);
            stopIB.setImageResource(R.drawable.ic_stop_black_24dp);
            stopIB.setVisibility(View.VISIBLE);

            recorded = true;
        }
    }

    private void stopRecording() {
        if(recorder != null){
            recorder.stop();
            recorder.release();
            recorder = null;

            playRecordStopSound();

            recordVoiceNotesIB.setImageResource(R.drawable.ic_delete_black_24dp);
            recordVoiceNotesIB.setVisibility(View.VISIBLE);

            playIB.setVisibility(View.VISIBLE);
            playIB.setImageResource(R.drawable.ic_play_arrow_black_24dp);

            stopIB.setVisibility(View.INVISIBLE);

            mPresenter.setVoiceNoteFileName(saleVoiceNoteFilePath);
        }

    }

    private void playRecordDeleteSound() {
        player = MediaPlayer.create(this, R.raw.delete);
        player.start();
    }

    private void playRecordStartSound() {
        player = MediaPlayer.create(this, R.raw.videorecord);
        player.start();
    }

    public void playRecordStopSound(){
        player = MediaPlayer.create(this, R.raw.videostop);
        player.start();
    }

    public void initiateRecording(){

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }else {

            onRecord(mStartRecording);
            mStartRecording = !mStartRecording;
        }

    }

    public void initiatePlayRecording(){
        onPlay(mStartPlaying);

        mStartPlaying = !mStartPlaying;
    }

    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);

        showSaveButton(mPresenter.isShowSaveButton());
        return true;
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;

        } else if (i == R.id.menu_save) {
            mPresenter.handleClickSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_sale_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_sale_detail_toolbar);
        toolbar.setTitle(getText(R.string.record_sale));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_sale_detail_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        pRecyclerView = findViewById(
                R.id.activity_sale_detail_payments_recyclerview);
        RecyclerView.LayoutManager pRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        pRecyclerView.setLayoutManager(pRecyclerLayoutManager);


        locationSpinner = findViewById(R.id.activity_sale_detail_location_spinner);
        discountET = findViewById(R.id.activity_sale_detail_discount);
        discountET.setText("0");
        deliveredCB = findViewById(R.id.activity_sale_detail_delivered);
        orderTotal = findViewById(R.id.activity_sale_detail_order_total);
        orderTotal.setText("0");
        totalAfterDiscount = findViewById(R.id.activity_sale_detail_order_after_discount_tota);
        totalAfterDiscount.setText("0");
        orderNotesET = findViewById(R.id.activity_sale_detail_order_notes);
        addItemCL = findViewById(R.id.activity_sale_detail_add_cl);
        recordVoiceNotesIB = findViewById(R.id.activity_sale_detail_order_notes_record_voice_note_ib);
        playIB = findViewById(R.id.activity_sale_detail_order_notes_play_image_button);
        stopIB = findViewById(R.id.activity_sale_detail_order_notes_delete_ib);
        signatureTitleTV = findViewById(R.id.activity_sale_detail_signature_title);
        signatureIB = findViewById(R.id.activity_sale_detail_signature_button);

        c1 = findViewById(R.id.textView21);
        c2 = findViewById(R.id.activity_sale_detail_disc_currency4);
        c3 = findViewById(R.id.activity_sale_detail_disc_currency);
        c4 = findViewById(R.id.activity_sale_detail_disc_currency3);
        c5 = findViewById(R.id.textView);
        c6 = findViewById(R.id.textView23);
        hlineCalc = findViewById(R.id.view2);


        paymentTV = findViewById(R.id.activity_sale_detail_payments_title);
        addPaymentCL = findViewById(R.id.activity_sale_detail_add_payments_cl);
        //private TextView balanceDueTV, balanceTV, balanceCurrencyTV;
        balanceDueTV = findViewById(R.id.activity_sale_detail_balance_due_textview);
        balanceTV = findViewById(R.id.activity_sale_detail_order_after_discount_tota3);
        balanceTV.setText("0");
        balanceCurrencyTV = findViewById(R.id.activity_sale_detail_disc_currency5);
        paymentHLineBeforeRV = findViewById(R.id.hlinebeforePaymentRV);
        anotherOneHLine = findViewById(R.id.anotherHLinePaymentRelated);

        //Call the Presenter
        mPresenter = new SaleDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        discountET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                long discount = 0;
                if(s != null && s.length() > 0 ) {
                    discount = Long.valueOf(s.toString());
                }
                mPresenter.handleDiscountChanged(discount);


            }
        });

        orderNotesET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.handleOrderNotesChanged(s.toString());
            }
        });

        deliveredCB.setOnCheckedChangeListener((buttonView, isChecked) ->
                mPresenter.handleSetDelivered(isChecked));

        addItemCL.setOnClickListener(v -> mPresenter.handleClickAddSaleItem());

        addPaymentCL.setOnClickListener(v -> mPresenter.handleClickAddPayment());

        //Location spinner
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.handleLocationSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        recordVoiceNotesIB.setOnClickListener(v -> initiateRecording());

        playIB.setOnClickListener(v -> initiatePlayRecording());

        // Record to the external cache directory for visibility
        saleVoiceNoteFilePath = getExternalCacheDir().getAbsolutePath();
        saleVoiceNoteFilePath += "/audiorecordtest.3gp";

        stopIB.setOnClickListener(v -> stopRecording());


        totalAfterDiscount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.updateBalanceDueFromTotal(Float.valueOf(s.toString()));

            }
        });

        signatureIB.setOnClickListener(v -> mPresenter.handleClickAddSignature());
    }




    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<SaleItemListDetail> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SaleItemListDetail>() {
                @Override
                public boolean areItemsTheSame(SaleItemListDetail oldItem,
                                               SaleItemListDetail newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(SaleItemListDetail oldItem,
                                                  SaleItemListDetail newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<SalePayment> DIFF_CALLBACK_PAYMENT =
            new DiffUtil.ItemCallback<SalePayment>() {
                @Override
                public boolean areItemsTheSame(SalePayment oldItem,
                                               SalePayment newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(SalePayment oldItem,
                                                  SalePayment newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setListProvider(UmProvider<SaleItemListDetail> listProvider) {
        SaleItemRecyclerAdapter recyclerAdapter =
                new SaleItemRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SaleItemListDetail> factory =
                (DataSource.Factory<Integer, SaleItemListDetail>)
                        listProvider.getProvider();
        LiveData<PagedList<SaleItemListDetail>> data =
                new LivePagedListBuilder<>(factory, 20).build();

        Observer customObserver = o -> {
            recyclerAdapter.submitList((PagedList<SaleItemListDetail>) o);
            mPresenter.getTotalSaleOrderAndDiscountAndUpdateView(saleUid);
        };


        //Observe the data:
        //data.observe(this, recyclerAdapter::submitList);
        data.observe(this, customObserver);

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter);
    }


    @Override
    public void setPaymentProvider(UmProvider<SalePayment> paymentProvider) {
        SalePaymentRecyclerAdapter recyclerAdapter =
                new SalePaymentRecyclerAdapter(DIFF_CALLBACK_PAYMENT, mPresenter, this,
                        getApplicationContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SalePayment> factory =
                (DataSource.Factory<Integer, SalePayment>)
                        paymentProvider.getProvider();
        LiveData<PagedList<SalePayment>> data =
                new LivePagedListBuilder<>(factory, 20).build();

        Observer customObserver = o -> {
            recyclerAdapter.submitList((PagedList<SalePayment>) o);
            mPresenter.getTotalPaymentsAndUpdateTotalView(saleUid);
        };


        //Observe the data:
        //data.observe(this, recyclerAdapter::submitList);
        data.observe(this, customObserver);

        //set the adapter
        pRecyclerView.setAdapter(recyclerAdapter);

    }



    @Override
    public void setLocationPresets(String[] locationPresets, int selectedPosition) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.item_simple_spinner, locationPresets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
        locationSpinner.setSelection(selectedPosition);

    }

    @Override
    public void updateOrderTotal(long orderTotalValue) {
        runOnUiThread(() -> {
            orderTotal.setText(String.valueOf(orderTotalValue));
            updateOrderTotalAfterDiscountTotalChanged(orderTotalValue);
        });

    }

    @Override
    public void updateOrderTotalAfterDiscount(long discountValue) {
        if(orderTotal.getText() != "") {
            long orderTotalValue = Long.parseLong(orderTotal.getText().toString());
            long totalAfterDiscountVal = orderTotalValue - discountValue;
            totalAfterDiscount.setText(String.valueOf(totalAfterDiscountVal));
        }else{
            totalAfterDiscount.setText("0");
        }
    }

    @Override
    public void updateOrderTotalAfterDiscountTotalChanged(long total) {
        if(total > 0){
            orderTotal.setText(String.valueOf(total));
        }
        long discount = 0;
        if(discountET.getText() != null && !discountET.getText().toString().equals("")){
            discount = Long.parseLong(discountET.getText().toString());
        }
        updateOrderTotalAfterDiscount(discount);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public void updateSaleOnView(Sale sale) {
        runOnUiThread(() -> {
            if(sale != null){
                saleUid = sale.getSaleUid();
                deliveredCB.setChecked(sale.isSaleDone());
                orderNotesET.setText(sale.getSaleNotes());
                String discountValue = "0";
                if(sale.getSaleDiscount() > 0){
                    discountValue = String.valueOf(sale.getSaleDiscount());
                }
                discountET.setText(discountValue);
                if(sale.getSaleSignature() != null && !sale.getSaleSignature().isEmpty()){
                    try {
                        String saleSignature= sale.getSaleSignature();
                        SVG svg = SVG.getFromString(saleSignature);

                        //TODO: Crop signature
                        Picture signPic = svg.renderToPicture();

                        int picW = signPic.getWidth();
                        int picH = signPic.getHeight();
                        Picture adjustedPic = signPic;
                        if(picH>picW){
                            adjustedPic = rotatePicture(0f , signPic);
                        }
                        PictureDrawable  pd = new PictureDrawable(adjustedPic);

                        runOnUiThread(() -> signatureIB.setBackground(pd));


                    } catch (SVGParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

//    public static PictureDrawable crop(PictureDrawable image) {
//        int minY = 0, maxY = 0, minX = Integer.MAX_VALUE, maxX = 0;
//        boolean isBlank, minYIsDefined = false;
//
//        for (int y = 0; y < image.getHeight(); y++) {
//            isBlank = true;
//
//            for (int x = 0; x < image.getWidth(); x++) {
//                //Change condition to (raster.getSample(x, y, 3) != 0)
//                //for better performance
//                if (raster.getPixel(x, y, (int[]) null)[3] != 0) {
//                    isBlank = false;
//
//                    if (x < minX) minX = x;
//                    if (x > maxX) maxX = x;
//                }
//            }
//
//            if (!isBlank) {
//                if (!minYIsDefined) {
//                    minY = y;
//                    minYIsDefined = true;
//                } else {
//                    if (y > maxY) maxY = y;
//                }
//            }
//        }
//
//        return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
//    }

    public Picture rotatePicture(float degrees, Picture picture) {
        int width = picture.getWidth();
        int height = picture.getHeight();

        Picture rotatedPicture = new Picture();
        Canvas canvas = rotatedPicture.beginRecording(width, height);
        canvas.save();
        canvas.rotate(degrees, width, height);
        picture.draw(canvas);
        canvas.restore();
        rotatedPicture.endRecording();

        return rotatedPicture;
    }

    @Override
    public void updatePaymentTotal(long paymentTotal) {
        //Actually, this is the balance due total not payment total.
    }

    @Override
    public void showSaveButton(boolean show) {
        if(menu != null) {
            MenuItem saveButton = menu.findItem(R.id.menu_save);
            if (saveButton != null) {
                saveButton.setVisible(show);
            }
        }
    }

    @Override
    public void showCalculations(boolean show) {
        if(show){
            toolbar.setTitle(getText(R.string.sale_details));
        }else{
            toolbar.setTitle(getText(R.string.record_sale));
        }

        c1.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        c2.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        c3.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        c4.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        c5.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        c6.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        discountET.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        discountET.setEnabled(show);
        orderTotal.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        orderTotal.setEnabled(show);
        totalAfterDiscount.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        totalAfterDiscount.setEnabled(show);
        hlineCalc.setVisibility(show?View.VISIBLE:View.INVISIBLE);
    }

    @Override
    public void showDelivered(boolean show) {
        deliveredCB.setVisibility(show?View.VISIBLE:View.INVISIBLE);
    }

    @Override
    public void showNotes(boolean show) {
        orderNotesET.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        recordVoiceNotesIB.setVisibility(show?View.VISIBLE:View.INVISIBLE);

    }

    @Override
    public void showSignature(boolean show) {
        runOnUiThread(() -> {
            signatureIB.setVisibility(show?View.VISIBLE:View.INVISIBLE);
            signatureTitleTV.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        });
    }

    @Override
    public void showPayments(boolean show) {
        paymentTV.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        addPaymentCL.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        pRecyclerView.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        //private TextView balanceDueTV, balanceTV, balanceCurrencyTV;
        balanceCurrencyTV.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        balanceTV.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        balanceDueTV.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        paymentHLineBeforeRV.setVisibility(show?View.VISIBLE:View.INVISIBLE);
        anotherOneHLine.setVisibility(show?View.VISIBLE:View.INVISIBLE);

    }


    @Override
    public void updateSaleVoiceNoteOnView(String fileName) {
        if (fileName!=null&&!fileName.isEmpty()){
            saleVoiceNoteFilePath = fileName;
            playIB.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            playIB.setVisibility(View.VISIBLE);
            stopIB.setVisibility(View.INVISIBLE);
            stopIB.setImageResource(R.drawable.ic_stop_black_24dp);
            recordVoiceNotesIB.setVisibility(View.VISIBLE);
            recordVoiceNotesIB.setImageResource(R.drawable.ic_delete_black_24dp);
            recorded=true;
            fromFile = true;

        }
    }

    @Override
    public void updateBalanceDue(long balance) {
        runOnUiThread(() -> balanceTV.setText(String.valueOf(balance)));

    }

}
