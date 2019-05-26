package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SelectSaleProductPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;

import java.io.File;

import static com.ustadmobile.lib.db.entities.SaleProductGroup.PRODUCT_GROUP_TYPE_PRODUCT;

public class SelectSaleProductRecyclerAdapter extends PagedListAdapter<SaleNameWithImage,
                SelectSaleProductRecyclerAdapter.SelectSaleProductViewHolder> {

    private static final int IMAGE_WITH = 26;
    private Context theContext;
    private Activity theActivity;
    private Fragment theFragment;
    SelectSaleProductPresenter mPresenter;

    private boolean listCategory;
    private boolean isCatalog;

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
        ImageView dots = holder.itemView.findViewById(R.id.item_sale_product_blob_dots);

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

        if(isCatalog){
            dots.setVisibility(View.VISIBLE);
            //Options to Edit/Delete every schedule in the list
            dots.setOnClickListener((View v) -> {
                if(theActivity != null){
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);
                    popup.setOnMenuItemClickListener(item -> {
                        int i = item.getItemId();
                        if (i == R.id.edit) {
                            mPresenter.handleClickProduct(entity.getProductUid(), listCategory);
                            return true;
                        } else if (i == R.id.delete) {
                            mPresenter.handleDelteSaleProduct(entity.getProductUid(), listCategory);
                            return true;
                        } else {
                            return false;
                        }
                    });

                    //inflating menu from xml resource
                    popup.inflate(R.menu.menu_edit_delete);
                    popup.getMenu().findItem(R.id.edit).setVisible(true);
                    //displaying the popup
                    popup.show();
                }else if(theFragment != null){
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(theFragment.getContext(), v);
                    popup.setOnMenuItemClickListener(item -> {
                        int i = item.getItemId();
                        if (i == R.id.edit) {
                            mPresenter.handleClickProduct(entity.getProductUid(), listCategory);
                            return true;
                        } else if (i == R.id.delete) {
                            mPresenter.handleDelteSaleProduct(entity.getProductUid(), listCategory);
                            return true;
                        } else {
                            return false;
                        }
                    });

                    //inflating menu from xml resource
                    popup.inflate(R.menu.menu_edit_delete);
                    popup.getMenu().findItem(R.id.edit).setVisible(true);
                    //displaying the popup
                    popup.show();
                }

            });
        }else{
            dots.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(v -> {
            mPresenter.handleClickProduct(entity.getProductUid(), listCategory);
        });

    }

    class SelectSaleProductViewHolder extends RecyclerView.ViewHolder {
        SelectSaleProductViewHolder(View itemView) {
            super(itemView);
        }
    }

    SelectSaleProductRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleNameWithImage> diffCallback,
            SelectSaleProductPresenter thePresenter,
            Activity activity,
            Boolean isCategory,
            Boolean catalog,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        listCategory = isCategory;
        isCatalog = catalog;
        theContext = context;
    }

    SelectSaleProductRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleNameWithImage> diffCallback,
            SelectSaleProductPresenter thePresenter,
            Fragment fragment,
            Boolean isCategory,
            Boolean catalog,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theFragment = fragment;
        listCategory = isCategory;
        isCategory = catalog;
        theContext = context;
    }


}
