package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.PersonDetailPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.core.view.PersonDetailViewField;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.io.File;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_DATE;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_DROPDOWN;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_FIELD;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_HEADER;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_PHONE_NUMBER;
import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_TEXT;
import static com.ustadmobile.port.android.view.PersonEditActivity.ADD_PERSON_ICON;

/**
 * The PersonDetail activity.
 * <p>
 * This Activity extends UstadBaseActivity and implements PersonDetailView
 */
public class PersonDetailActivity extends UstadBaseActivity implements PersonDetailView {

    //Toolbar
    private Toolbar toolbar;
    private LinearLayout mLinearLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private PersonDetailPresenter mPresenter;
    String personName = "";
    ImageView personEditImage;

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

        //Load the Image
        personEditImage = findViewById(R.id.activity_person_detail_student_image);

        //Call the Presenter
        mPresenter = new PersonDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB
        FloatingTextButton fab = findViewById(R.id.activity_person_detail_fab_edit);
        fab.setOnClickListener(v -> mPresenter.handleClickEdit());

        TextView callParentTextView = findViewById(R.id.activity_person_detail_action_call_parent_text);
        TextView textParentTextView = findViewById(R.id.activity_person_detail_action_text_parent_text);
        ImageView callParentImageView =
                findViewById(R.id.activity_person_detail_action_call_parent_icon);
        ImageView textParentImageView =
                findViewById(R.id.activity_person_detail_action_text_parent_icon);
        TextView enrollInClassTextView =
                findViewById(R.id.activity_person_detail_action_enroll_in_class_text);
        ImageView enrollInClassImageView =
                findViewById(R.id.activity_person_detail_action_enroll_in_class_icon);

        callParentImageView.setOnClickListener(v -> mPresenter.handleClickCallParent());
        callParentTextView.setOnClickListener(v -> mPresenter.handleClickCallParent());

        textParentImageView.setOnClickListener(v -> mPresenter.handleClickTextParent());
        textParentTextView.setOnClickListener(v -> mPresenter.handleClickTextParent());

        enrollInClassImageView.setOnClickListener(v -> mPresenter.handleClickEnrollInClass());
        enrollInClassTextView.setOnClickListener(v -> mPresenter.handleClickEnrollInClass());

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
    public void clearAllFields() {
        mLinearLayout.removeAllViews();
    }

    @Override
    public void updateImageOnView(String imagePath){
        Uri profileImage = Uri.fromFile(new File(imagePath));

        Picasso.with(getApplicationContext()).load(profileImage).into(personEditImage);

        File profilePic = new File(imagePath);
        Picasso.with(getApplicationContext()).load(profilePic).into(personEditImage);
    }

    @Override
    public void setField(int index, PersonDetailViewField field, Object value) {
        if(value == null){
            value = "";
        }
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

                if(field.getMessageLabel() == MessageID.classes){
                    //Add a recyclerview of classes
                    mRecyclerView = new RecyclerView(this);

                    mRecyclerLayoutManager = new LinearLayoutManager(getApplicationContext());
                    mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

                    //Add the layout
                    mLinearLayout.addView(mRecyclerView);

                    //Generate the live data and set it
                    mPresenter.generateAssignedClazzesLiveData();
                }

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
                    iconName = ADD_PERSON_ICON;
                }

                int iconResId = getResourceId(iconName, "drawable", getPackageName());
                ImageView icon = new ImageView(this);
                icon.setImageResource(iconResId);
                if(iconName.equals(ADD_PERSON_ICON)){
                    icon.setAlpha(0);
                }
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
                    textIcon.setPadding(8,16, 32,16);
                    textIcon.setOnClickListener(v -> {
                        mPresenter.handleClickText(field.getActionParam());

                    });

                    ImageView callIcon = new ImageView(this);
                    callIcon.setImageResource(getResourceId(CALL_ICON_NAME,
                            "drawable", getPackageName()));
                    callIcon.setPadding(8,16, 32,16);
                    callIcon.setOnClickListener(v -> {
                        mPresenter.handleClickCall(field.getActionParam());

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

    @Override
    public void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider) {

        ClazzListRecyclerAdapter recyclerAdapter =
                new ClazzListRecyclerAdapter(DIFF_CALLBACK);
        DataSource.Factory<Integer, ClazzWithNumStudents> factory =
                (DataSource.Factory<Integer, ClazzWithNumStudents>)
                        clazzListProvider.getProvider();
        LiveData<PagedList<ClazzWithNumStudents>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        data.observe(this, recyclerAdapter::submitList);

        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void handleClickCall(String number) {
        startActivity(new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + number)));
    }

    @Override
    public void handleClickText(String number) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",
                number, null)));
    }

    /**
     * The Recycler Adapter
     */
    protected class ClazzListRecyclerAdapter
            extends PagedListAdapter<ClazzWithNumStudents,
                        ClazzListRecyclerAdapter.ClazzLogDetailViewHolder> {

        protected class ClazzLogDetailViewHolder extends RecyclerView.ViewHolder{
            protected ClazzLogDetailViewHolder(View itemView){
                super(itemView);
            }
        }

        protected ClazzListRecyclerAdapter(
                @NonNull DiffUtil.ItemCallback<ClazzWithNumStudents> diffCallback){
            super(diffCallback);
        }

        @NonNull
        @Override
        public ClazzLogDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

            View clazzLogDetailListItem =
                    LayoutInflater.from(getApplicationContext()).inflate(
                            R.layout.item_clazzlist_clazz_simple, parent, false);
            return new ClazzLogDetailViewHolder(clazzLogDetailListItem);
        }

        /**
         * This method sets the elements after it has been obtained for that item'th position.
         *
         * Every item in the recycler view will have set its colors if no attendance status is set.
         * every attendance button will have it-self mapped to tints on activation.
         *
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(@NonNull ClazzLogDetailViewHolder holder, int position){
            ClazzWithNumStudents thisClazz = getItem(position);

            ((TextView)holder.itemView.findViewById(R.id.item_clazzlist_clazz_simple_clazz_name))
                    .setText(thisClazz.getClazzName());

        }
    }

    // Diff callback.
    public static final DiffUtil.ItemCallback<ClazzWithNumStudents> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ClazzWithNumStudents>() {
                @Override
                public boolean areItemsTheSame(ClazzWithNumStudents oldItem,
                                               ClazzWithNumStudents newItem) {
                    return oldItem.getClazzUid() ==
                            newItem.getClazzUid();
                }

                @Override
                public boolean areContentsTheSame(ClazzWithNumStudents oldItem,
                                                  ClazzWithNumStudents newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
