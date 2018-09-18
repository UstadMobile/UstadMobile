package com.ustadmobile.port.android.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.PersonEditPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.PersonDetailViewField;
import com.ustadmobile.core.view.PersonEditView;
import com.ustadmobile.port.android.generated.MessageIDMap;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_DATE;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_DROPDOWN;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_FIELD;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_HEADER;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_PHONE_NUMBER;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_TEXT;

public class PersonEditActivity extends UstadBaseActivity implements PersonEditView {

    private Toolbar toolbar;
    private LinearLayout mLinearLayout;

    private PersonEditPresenter mPresenter;

    public static final int DEFAULT_PADDING = 16;
    public static final int DEFAULT_PADDING_HEADER_BOTTOM = 16;
    public static final int DEFAULT_DIVIDER_HEIGHT = 2;
    public static final int DEFAULT_TEXT_PADDING_RIGHT = 4;
    public static final String BLANK_ICON = "ic_blank_24dp";
    public static final int HEADER_TEXT_SIZE = 12;
    public static final int LABEL_TEXT_SIZE = 10;
    public static final String COLOR_GREY= "#B3B3B3";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Set layout:
        setContentView(R.layout.activity_person_edit);

        //Toolbar
        toolbar = findViewById(R.id.activity_person_edit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the header & fields layout
        mLinearLayout = findViewById(R.id.activity_person_edit_fields_linear_layout);

        //Call the presenter
        mPresenter = new PersonEditPresenter(this, UMAndroidUtil.bundleToHashtable(
                getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB
        FloatingTextButton dab = findViewById(R.id.activity_person_edit_fab_done);
        dab.setOnClickListener(v -> mPresenter.handleClickDone());

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


    public void setEditField(int fieldType, String label, int labelId,
                             String iconName, boolean editMode,
                             LinearLayout thisLinearLayout, Object thisValue){


        //Set icon if not present (for margins to align ok)
        if(iconName == null || iconName.length() == 0){
            iconName = BLANK_ICON;
        }

        LinearLayout.LayoutParams dividerLayout =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        DEFAULT_DIVIDER_HEIGHT);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams parentParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

        LinearLayout hll = new LinearLayout(this);
        hll.setLayoutParams(parentParams);
        hll.setOrientation(LinearLayout.HORIZONTAL);


        switch (fieldType){
            case FIELD_TYPE_HEADER:
                //Add The Divider
                View divider = new View(this);
                divider.setLayoutParams(dividerLayout);
                divider.setBackgroundColor(Color.parseColor(COLOR_GREY));
                thisLinearLayout.addView(divider);

                //Add the Header
                TextView header = new TextView(this);
                header.setText(label.toUpperCase());
                header.setTextSize(HEADER_TEXT_SIZE);
                header.setPadding(DEFAULT_PADDING,0,0,DEFAULT_PADDING_HEADER_BOTTOM);
                thisLinearLayout.addView(header);
                break;

            case FIELD_TYPE_TEXT:
            case FIELD_TYPE_FIELD:

                //Add the icon
                int iconResId = getResourceId(iconName, "drawable", getPackageName());
                ImageView icon = new ImageView(this);
                icon.setImageResource(iconResId);
                icon.setPadding(DEFAULT_PADDING,0,DEFAULT_TEXT_PADDING_RIGHT,0);
                hll.addView(icon);

                TextInputLayout til = new TextInputLayout(this);

                View cl = findViewById(R.id.activity_person_edit_fields_linear_layout);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;


                TextInputLayout.LayoutParams tilp = new TextInputLayout.LayoutParams(width,
                        TextInputLayout.LayoutParams.MATCH_PARENT);

                ViewGroup.LayoutParams editTextParams =
                        new LinearLayout.LayoutParams(
                                width,
                                ViewGroup.LayoutParams.MATCH_PARENT);

                EditText et = new EditText(this);
                et.setLayoutParams(editTextParams);
                et.setHint(label);
                et.setText(thisValue.toString());

                til.addView(et, tilp);

                hll.addView(til);


                //Add the value entry to the linear layout
                thisLinearLayout.addView(hll);

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

    @Override
    public void setField(int index, PersonDetailViewField field, Object value) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String label = null;
        int labelId = 0;
        if(field.getMessageLabel() != 0) {
            label = impl.getString(field.getMessageLabel(), getContext());
            labelId = MessageIDMap.ID_MAP.get(field.getMessageLabel());
        }

        setEditField(field.getFieldType(), label, labelId,field.getIconName(),
                true, mLinearLayout, value);

    }
}
