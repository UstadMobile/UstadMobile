package com.ustadmobile.core.view;


import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;

/**
 * Core View. Screen is for AddSaleProductToSaleCategory's View
 */
public interface AddSaleProductToSaleCategoryView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "AddSaleProductToSaleCategory";

    //Any argument keys:
    String ARG_SALE_PRODUCT_CATEGORY_TO_ASSIGN_TO_UID="ArgSaleProductCategoryToAssignToUid";
    String ARG_ADD_TO_CATEGORY_TYPE_ITEM="ArgAddToCategoryTypeItem";
    String ARG_ADD_TO_CATEGORY_TYPE_CATEGORY="argAddToCategoryTypeCategory";

    /**
     * Method to finish the screen / view.
     */
    void finish();

    /**
     * Product list
     * @param listProvider SameNameWithImage product provider
     */
    void setListProvider(UmProvider<SaleNameWithImage> listProvider);

    void setAddtitle(String title);

    void setToolbarTitle(String title);
}

