package com.ustadmobile.port.android.view;

import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SaleDetailSignaturePresenter;
import com.ustadmobile.core.view.SaleDetailSignatureView;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class SaleDetailSignatureActivity extends UstadBaseActivity implements SaleDetailSignatureView {

    private Toolbar toolbar;
    private SaleDetailSignaturePresenter mPresenter;
    SignaturePad mSignaturePad;


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_record_signature);

        //Toolbar:
        toolbar = findViewById(R.id.activity_record_signature_toolbar);
        toolbar.setTitle(getText(R.string.add_signature));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Call the Presenter
        mPresenter = new SaleDetailSignaturePresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        mSignaturePad = findViewById(R.id.activity_record_signature_signaturepad);


        //FAB and its listener
        FloatingTextButton accept = findViewById(R.id.activity_record_signature_fab_accept);
        accept.setOnClickListener(v -> mPresenter.handleClickAccept());

        FloatingTextButton cancel = findViewById(R.id.activity_record_signature_fab_clear);
        cancel.setOnClickListener(v -> clearSignature());

        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {}

            @Override
            public void onSigned() {
                String signSvg = mSignaturePad.getSignatureSvg();
                mPresenter.updateSignatureSvg(signSvg);
            }

            @Override
            public void onClear() {
                mPresenter.updateSignatureSvg("");
            }
        });

    }

    private void clearSignature(){

        mSignaturePad.setBackground(null);
        mSignaturePad.clear();
    }


    @Override
    public void updateSaleOnView(Sale sale) {
        //Updare Signature on view.

        if(sale != null){
            String saleSignature = sale.getSaleSignature();
            if(saleSignature!=null && !saleSignature.isEmpty()){

                try{

                    SVG svg = SVG.getFromString(saleSignature);

                    Picture signPic = svg.renderToPicture();

                    int picW = signPic.getWidth();
                    int picH = signPic.getHeight();
                    Picture adjustedPic = signPic;
                    if(picH>picW){
                        adjustedPic = rotatePicture(0f , signPic);
                    }
                    PictureDrawable pd = new PictureDrawable(adjustedPic);

                    runOnUiThread(() -> mSignaturePad.setBackground(pd));
                }catch (SVGParseException spe) {
                    spe.printStackTrace();
                }
            }
        }

    }

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
}
