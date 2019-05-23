package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.entities.SaleProductSelected;


/**
 * Core View. Screen is for SaleProductDetail's View
 */
public interface SaleProductDetailView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SaleProductDetail";

    //Any argument keys:
    String ARG_NEW_TITLE = "ArgNewTitle";
    String ARG_NEW_CATEGORY = "ArgNewCategory";
    String ARG_SALE_PRODUCT_UID = "ArgSaleProductUid";
    String ARG_ASSIGN_TO_CATEGORY_UID = "ArgAssignToCategoryUid";


    /**
     * Method to finish the screen / view.
     */
    void finish();


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    void setListProvider(UmProvider<SaleProductSelected> listProvider);

    void updateToolbarTitle(String titleName);

    void updateCategoryTitle(String titleName);

    void updateImageOnView(String imagePath);

    void addImageFromCamera();

    void addImageFromGallery();

    void initFromSaleProduct(SaleProduct saleProduct);

    void sendMessage(int messageId);

}

