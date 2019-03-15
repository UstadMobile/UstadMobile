package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.app.Activity;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.GroupDetailPresenter;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

import java.io.File;

public class GroupDetailRecyclerAdapter extends
        PagedListAdapter<PersonWithEnrollment,
                GroupDetailRecyclerAdapter.GroupDetailViewHolder> {

    private static final int IMAGE_PERSON_THUMBNAIL_WIDTH = 26;
    Context theContext;
    Activity theActivity;
    GroupDetailPresenter mPresenter;

    @NonNull
    @Override
    public GroupDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_image_with_title_with_desc_and_dots, parent, false);
        return new GroupDetailViewHolder(list);

    }

    @Override
    public void onBindViewHolder(@NonNull GroupDetailViewHolder holder, int position) {

        PersonWithEnrollment personWithEnrollment = getItem(position);

        TextView studentNameTextView =
                holder.itemView.findViewById(R.id.item_image_with_title_with_desc_and_dots_title);
        TextView lastActiveTextView =
                holder.itemView.findViewById(R.id.item_image_with_title_with_desc_and_dots_desc);
        ImageView personPicture =
                holder.itemView.findViewById(R.id.item_image_with_title_with_desc_and_dots_image);
        AppCompatImageView menu =
                holder.itemView.findViewById(R.id.item_image_with_title_with_desc_and_dots_dots);


        //NAME:
        String firstName = "";
        String lastName = "";
        if(personWithEnrollment == null){
            return;
        }
        if(personWithEnrollment != null && personWithEnrollment.getFirstNames() != null){
            firstName = personWithEnrollment.getFirstNames();
        }
        if(personWithEnrollment != null && personWithEnrollment.getLastName() != null){
            lastName = personWithEnrollment.getLastName();
        }

        String studentName = firstName + " " + lastName;
        studentNameTextView.setText(studentName);
        long personUid = personWithEnrollment.getPersonUid();
        studentNameTextView.setOnClickListener(v -> mPresenter.handleClickStudent(personUid));

        //PICTURE : Add picture to person
        String imagePath = "";
        long personPictureUid = personWithEnrollment.getPersonPictureUid();
        if (personPictureUid != 0) {
            imagePath = UmAppDatabase.getInstance(theContext).getPersonPictureDao()
                    .getAttachmentPath(personPictureUid);
        }

        if(imagePath != null && !imagePath.isEmpty())
            setPictureOnView(imagePath, personPicture);
        else
            personPicture.setImageResource(R.drawable.ic_person_black_new_24dp);

        //Last Seen
        //TODO:
        lastActiveTextView.setText("");

        //Options to Edit/Delete every schedule in the list
        menu.setOnClickListener((View v) -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);
            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == R.id.edit) {
                    return true;
                } else if (i == R.id.delete) {
                    mPresenter.handleDeleteMember(personUid);
                    return true;
                } else {
                    return false;
                }
            });
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_item_schedule);

            popup.getMenu().findItem(R.id.edit).setVisible(false);

            //displaying the popup
            popup.show();
        });

    }

    private static int dpToPxImagePerson() {
        return (int) (IMAGE_PERSON_THUMBNAIL_WIDTH
                * Resources.getSystem().getDisplayMetrics().density);
    }

    private void setPictureOnView(String imagePath, ImageView theImage) {

        Uri imageUri = Uri.fromFile(new File(imagePath));

        Picasso
                .get()
                .load(imageUri)
                .resize(dpToPxImagePerson(), dpToPxImagePerson())
                .noFade()
                .into(theImage);
    }


    protected class GroupDetailViewHolder extends RecyclerView.ViewHolder {
        protected GroupDetailViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected GroupDetailRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<PersonWithEnrollment> diffCallback,
            GroupDetailPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theContext = context;
        theActivity = activity;
    }


}
