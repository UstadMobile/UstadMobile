package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
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
import com.ustadmobile.core.controller.SaleProductCategoryListPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;

import java.io.File;

public class SelectSaleCategoryRecyclerAdapter extends
        PagedListAdapter<SaleNameWithImage,
                SelectSaleCategoryRecyclerAdapter.SelectSaleProductViewHolder> {

    private static final int IMAGE_WITH = 100;
    private Context theContext;
    private Activity theActivity;
    SaleProductCategoryListPresenter mPresenter;

    private boolean listCategory;
    private boolean showContextMenu;

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

        assert entity != null;
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

        //Options to Edit/Delete every schedule in the list
        if(showContextMenu) {
            dots.setOnClickListener((View v) -> {
                if (theActivity != null) {
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(theActivity.getApplicationContext(), v);
                    popup.setOnMenuItemClickListener(item -> {
                        int i = item.getItemId();
                        if (i == R.id.edit) {
                            mPresenter.handleClickEditCategory(entity.getProductUid());
                            return true;
                        } else if (i == R.id.delete) {
                            mPresenter.handleDeleteCategory(entity.getProductUid());
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

    SelectSaleCategoryRecyclerAdapter(
            @NonNull DiffUtil.ItemCallback<SaleNameWithImage> diffCallback,
            SaleProductCategoryListPresenter thePresenter,
            Activity activity,
            Boolean showContext,
            Boolean isCategory,
            Context context) {
        super(diffCallback);
        mPresenter = thePresenter;
        theActivity = activity;
        listCategory = isCategory;
        showContextMenu = showContext;
        theContext = context;
    }

}
