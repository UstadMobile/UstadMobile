package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SelectSaleProductPresenter;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;

import java.io.File;

import static com.ustadmobile.lib.db.entities.SaleProductGroup.PRODUCT_GROUP_TYPE_PRODUCT;

public class SelectSaleProductRecyclerAdapter extends
        PagedListAdapter<SaleNameWithImage,
                SelectSaleProductRecyclerAdapter.SelectSaleProductViewHolder> {

    private static final int IMAGE_WITH = 26;
    Context theContext;
    Activity theActivity;
    SelectSaleProductPresenter mPresenter;

    @NonNull
    @Override
    public SelectSaleProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View list = LayoutInflater.from(theContext).inflate(
                R.layout.item_sale_product_blob, parent, false);
        return new SelectSaleProductViewHolder(list);

    }

    private static int dpToPxImagePerson() {
        return (int) (IMAGE_WITH
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

    @Override
    public void onBindViewHolder(@NonNull SelectSaleProductViewHolder holder, int position) {

        SaleNameWithImage entity = getItem(position);
        ImageView imageView = holder.itemView.findViewById(R.id.item_sale_product_blob_image);
        TextView name = holder.itemView.findViewById(R.id.item_sale_product_blob_title);

        //TODO: Get picture
        long pictureUid = entity.getPictureUid();
        String imagePath = "";
        if (pictureUid != 0) {
            imagePath = UmAppDatabase.getInstance(theContext).getSaleProductPictureDao()
                    .getAttachmentPath(pictureUid);
        }

        if(imagePath != null && !imagePath.isEmpty())
            setPictureOnView(imagePath, imageView);
        else
            imageView.setImageResource(R.drawable.ic_card_giftcard_black_24dp);

        name.setText(entity.getName());

        holder.itemView.setOnClickListener(v -> {
            if(entity.getType() == PRODUCT_GROUP_TYPE_PRODUCT) {
                mPresenter.handleClickProduct(entity.getProductUid());
            }
        });

    }

    protected class SelectSaleProductViewHolder extends RecyclerView.ViewHolder {
        protected SelectSaleProductViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected SelectSaleProductRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleNameWithImage> diffCallback,
            SelectSaleProductPresenter thePresenter,
            Activity activity,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        theContext = context;
    }


}
