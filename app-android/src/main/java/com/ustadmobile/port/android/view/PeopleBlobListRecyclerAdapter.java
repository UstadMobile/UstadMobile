package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CommonHandlerPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.PersonPictureDao;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture;

import java.io.File;
import java.util.Hashtable;

/**
 * The Recycler adapter for blobs of people list used in SEL's recognition, selection, nomination
 * lists. A blob here is basically a square with the image of the person and an optional text.
 * Click handlers can be implemented via a common CommonHandlerPresenter.
 *
 */
public class PeopleBlobListRecyclerAdapter extends
        PagedListAdapter<PersonWithPersonPicture, PeopleBlobListRecyclerAdapter.PeopleViewHolder> {

    Context theContext;
    CommonHandlerPresenter mPresenter;
    private boolean hideNames = false;

    private Hashtable<Integer, String> colorMap = new Hashtable<>();


    class PeopleViewHolder extends RecyclerView.ViewHolder {
        PeopleViewHolder(View itemView) {
            super(itemView);
        }
    }

    PeopleBlobListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<PersonWithPersonPicture> diffCallback,
                                              Context context, CommonHandlerPresenter presenter) {
        super(diffCallback);
        theContext = context;
        mPresenter = presenter;
    }

    PeopleBlobListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<PersonWithPersonPicture> diffCallback,
                                            Context context, CommonHandlerPresenter presenter,
                                            boolean namesHidden) {
        super(diffCallback);
        theContext = context;
        mPresenter = presenter;
        hideNames = namesHidden;
    }

    /**
     * This method inflates the card layout (to parent view given) and returns it.
     *
     * @param parent View given.
     * @param viewType View Type not used here.
     * @return New ViewHolder for the ClazzStudent type
     */
    @NonNull
    @Override
    public PeopleBlobListRecyclerAdapter.PeopleViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View clazzStudentListItem =
                LayoutInflater.from(theContext).inflate(
                        R.layout.item_peopleblob, parent, false);
        return new PeopleBlobListRecyclerAdapter.PeopleViewHolder(clazzStudentListItem);
    }

    /**
     * Transform a bitmap ?
     */
    public class CropSquareTransformation implements Transformation {
        @Override public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override public String key() { return "square()"; }
    }

    /**
     * Updates image from the path given to it to the view.
     *
     * @param imagePath         The path of the image (local to the device)
     * @param personImageView   The View where to set the image.
     */
    private void updateImageOnView(String imagePath, ImageView personImageView){
        Uri profileImage = Uri.fromFile(new File(imagePath));

        //Picasso.with(theContext)
        Picasso.get()
                .load(profileImage)
                .transform(new CropSquareTransformation())
                .resize(90,90)
                .centerCrop()
                .into(personImageView);

        File profilePic = new File(imagePath);
        //Picasso.with(theContext)
        Picasso.get()
                .load(profilePic)
                .transform(new CropSquareTransformation())
                .resize(90,90)
                .centerCrop()
                .into(personImageView);
    }

    /**
     * In Order:
     *      1.  Gets student name for every item, gets image and sets it to the card.
     *      2.  Depending on constructor, hides or shows the name
     *      3.  Adds an onClick listener on the item Card so that it changes color depending on the
     *          selection.
 *          4.  Also calls presenter's primary action method (ie: for nominations)
     *
     * @param holder    The holder
     * @param position  The position in the recycler view.
     */
    @Override
    public void onBindViewHolder(
            @NonNull PeopleBlobListRecyclerAdapter.PeopleViewHolder holder, int position) {

        PersonWithPersonPicture thisPerson = getItem(position);

        String studentName;
        if (thisPerson == null) {
            studentName = "Student";
        } else {
            studentName = thisPerson.getFirstNames() + " " +
                    thisPerson.getLastName();
        }

        ImageView studentImage = holder.itemView
                .findViewById(R.id.item_peopleblob_image);

        assert thisPerson != null;

        long personPictureUid = thisPerson.getPersonPictureUid();
        if(personPictureUid != 0L) {
            String imgPath = UmAppDatabase.getInstance(theContext).getPersonPictureDao()
                    .getAttachmentPath(personPictureUid);
            updateImageOnView(imgPath, studentImage);
        }

        TextView studentEntry = holder.itemView
                .findViewById(R.id.item_peopleblob_name);

        if (!hideNames) {
            studentEntry.setText(studentName);
        }

        CardView personCard = holder.itemView.findViewById(R.id.item_peoplblob_card);

        personCard.setOnClickListener(v -> {

            if (colorMap.containsKey(position)) {
                if (colorMap.get(position).equals("selected")) {
                    if (hideNames){
                        studentEntry.setText("");
                    }else {
                        personCard.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    }
                    colorMap.put(position, "unselected");
                } else {
                    if (hideNames){
                        studentEntry.setText(studentName);
                    }else {
                        personCard.setBackgroundColor(Color.parseColor("#FF6666"));

                    }
                    colorMap.put(position, "selected");
                }
            } else {
                colorMap.put(position, "selected");
                if (hideNames){
                    studentEntry.setText(studentName);
                }else {
                    personCard.setBackgroundColor(Color.parseColor("#FF6666"));
                }
            }


            mPresenter.handleCommonPressed(thisPerson.getPersonUid());
        });


    }
}
