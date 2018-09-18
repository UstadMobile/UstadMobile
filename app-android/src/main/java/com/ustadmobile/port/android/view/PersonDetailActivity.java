package com.ustadmobile.port.android.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.PersonDetailPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.core.view.PersonDetailViewField;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_DATE;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_DROPDOWN;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_FIELD;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_HEADER;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_PHONE_NUMBER;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_TEXT;

/**
 * The PersonDetail activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements PersonDetailView
 */
public class PersonDetailActivity extends UstadBaseActivity implements PersonDetailView {

    //Toolbar
    private Toolbar toolbar;
    private LinearLayout mLinearLayout;

    private PersonDetailPresenter mPresenter;
    String personName = "";

    public static final String CALL_ICON_NAME = "ic_call_bcd4_24dp";
    public static final String TEXT_ICON_NAME = "ic_textsms_bcd4_24dp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting layout:
        setContentView(R.layout.activity_person_detail);

        //Toolbar
        toolbar = findViewById(R.id.activity_person_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLinearLayout = findViewById(R.id.activity_person_detail_fields_linear_layout);

        //Call the Presenter
        mPresenter = new PersonDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB
        FloatingTextButton fab = findViewById(R.id.activity_person_detail_fab_edit);
        fab.setOnClickListener(v -> mPresenter.handleClickEdit());


    }

    public int getResourceId(String pVariableName, String pResourcename, String pPackageName)
    {
        try {
            return getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }



    @Override
    public void setField(int index, PersonDetailViewField field, Object value) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String label = null;
        if(field.getMessageLabel() != 0) {
            label = impl.getString(field.getMessageLabel(), getContext());
        }

        switch(field.getFieldType()){
            case FIELD_TYPE_HEADER:

                //Add The Divider
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                ));
                divider.setBackgroundColor(Color.parseColor("#B3B3B3"));
                mLinearLayout.addView(divider);

                //Add the Header
                TextView header = new TextView(this);
                header.setText(label.toUpperCase());
                header.setTextSize(12);
                header.setPadding(16,0,0,2);
                mLinearLayout.addView(header);
                break;
            case FIELD_TYPE_TEXT:
            case FIELD_TYPE_FIELD:

                int messageLabel = field.getMessageLabel();
                //If this is just the full name, set it and continue
                if(messageLabel == MessageID.field_fullname){
                    TextView name = findViewById(R.id.activity_person_detail_student_name);
                    name.setText(value.toString());
                    break;

                }

                LinearLayout hll = new LinearLayout(this);
                hll.setOrientation(LinearLayout.HORIZONTAL);
                hll.setPadding(16,16,16,16);

                String iconName = field.getIconName();

                if(iconName == null || iconName.length() == 0){
                    iconName = "ic_blank_24dp";
                }

                int iconResId = getResourceId(iconName, "drawable", getPackageName());
                ImageView icon = new ImageView(this);
                icon.setImageResource(iconResId);
                icon.setPadding(16,0,4,0);
                hll.addView(icon);


                LinearLayout vll = new LinearLayout(this);
                vll.setOrientation(LinearLayout.VERTICAL);
                vll.setPadding(16,0,0,0);

                TextView fieldValue = new TextView(this);
                fieldValue.setText(value.toString());
                fieldValue.setPadding(16,4,4,0);
                vll.addView(fieldValue);

                if (label != null) {
                    TextView fieldLabel = new TextView(this);
                    fieldLabel.setTextSize(10);
                    fieldLabel.setText(label);
                    fieldLabel.setPadding(16, 0, 4, 4);
                    vll.addView(fieldLabel);
                }

                hll.addView(vll);

                //Add call and text buttons to father and mother detail
                if(field.getActionParam() != null && field.getActionParam().length() > 0){
                    ImageView textIcon = new ImageView(this);
                    textIcon.setImageResource(getResourceId(TEXT_ICON_NAME,
                            "drawable", getPackageName()));
                    textIcon.setPadding(16,0, 16,0);
                    textIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPresenter.handleClickCall(field.getActionParam());
                        }
                    });

                    ImageView callIcon = new ImageView(this);
                    callIcon.setImageResource(getResourceId(CALL_ICON_NAME,
                            "drawable", getPackageName()));
                    callIcon.setPadding(16,0, 16,0);
                    callIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPresenter.handleClickText(field.getActionParam());
                        }
                    });

                    LinearLayout.LayoutParams heavyLayout = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1.0f
                    );
                    View fillIt = new View(this);
                    fillIt.setLayoutParams(heavyLayout);

                    hll.addView(fillIt);
                    hll.addView(textIcon);
                    hll.addView(callIcon);
                }

                mLinearLayout.addView(hll);

                break;
            case FIELD_TYPE_DROPDOWN:
                break;
            case FIELD_TYPE_PHONE_NUMBER:
                break;
            case FIELD_TYPE_DATE:
                break;
            default:
                break;
        }

    }

}
