package com.ustadmobile.port.android.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
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
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.WeakHashMap;

import static com.ustadmobile.core.controller.PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_DATE;
import static com.ustadmobile.core.controller.PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_DROPDOWN;
import static com.ustadmobile.core.controller.PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_PHONE_NUMBER;
import static com.ustadmobile.core.controller.PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_TEXT;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting layout:
        setContentView(R.layout.activity_person_detail);

        //Toolbar
        toolbar = findViewById(R.id.activity_person_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLinearLayout = findViewById(R.id.activity_person_detail_fields_linearlayout);

        //Call the Presenter
        mPresenter = new PersonDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

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
    public void setField(int index, PersonDetailPresenter.PersonDetailViewField field, Object value) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String label = null;
        if(field.getMessageLabel() != 0) {
            label = impl.getString(field.getMessageLabel(), getContext());
        }

        switch(field.getFieldType()){
            case PersonDetailPresenterField.FIELD_TYPE_HEADER:

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
            case PersonDetailPresenterField.FIELD_TYPE_FIELD:

                if(field.getMessageLabel() == MessageID.first_names ||
                        field.getMessageLabel() == MessageID.last_name){


                    if(field.getMessageLabel() == MessageID.first_names){
                        personName  = value.toString();
                    }else{
                        personName = personName + " " + value.toString();
                    }
                    TextView name = findViewById(R.id.activity_person_detail_student_name);
                    name.setText(personName);
                    break;
                }

                LinearLayout hll = new LinearLayout(this);
                hll.setOrientation(LinearLayout.HORIZONTAL);
                hll.setPadding(16,16,16,16);

                int iconResId = getResourceId(field.getIconName(), "drawable", getPackageName());
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

    /**
     * Class : feed view pager adapter
     */
    private class PersonDetailViewPagerAdapter extends FragmentStatePagerAdapter {

        //Map of position and fragment
        WeakHashMap<Integer, UstadBaseFragment> positionMap;

        //Constructor creates the adapter
        public PersonDetailViewPagerAdapter(FragmentManager fm) {
            super(fm);
            positionMap = new WeakHashMap<>();
        }

        public void addFragments(int pos, Fragment fragment) {
            positionMap.put(pos, (UstadBaseFragment) fragment);
        }

        /**
         * Generate fragment for that page/position
         *
         * @param position
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            UstadBaseFragment thisFragment = positionMap.get(new Integer(position));
            if (thisFragment != null) {
                return thisFragment;
            } else {
                switch (position) {
                    default:
                        return null;
                }

            }
        }

        @Override
        public int getCount() {
            return positionMap.size();
        }
    }


}
