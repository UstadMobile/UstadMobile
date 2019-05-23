package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.lib.db.entities.SaleProduct;


/**
 * Core View. Screen is for SaleProductCategoryList's View
 */
public interface SaleProductCategoryListView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SaleProductCategoryList";

    //Any argument keys:
    String ARG_SALEPRODUCT_UID = "ArgSaleProductUid";

    /**
     * Method to finish the screen / view.
     */
    void finish();


    /**
     * Recycler view for Items
     * Sets the given provider to the view's provider adapter.
     *
     * @param listProvider The provider to set to the view
     */
    void setListProvider(UmProvider<SaleNameWithImage> listProvider);

    /**
     * Recycler view for Categories
     * @param listProvider
     */
    void setCategoriesListProvider(UmProvider<SaleNameWithImage> listProvider);

    void setMessageOnView(int messageCode);

    //eg: set toolbar
    void initFromSaleCategory(SaleProduct saleProductCategory);

    void updateSortPresets(String[] presets);

    void hideFAB(boolean hide);

}

