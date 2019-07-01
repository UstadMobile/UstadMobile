package com.ustadmobile.core.view;


import com.ustadmobile.lib.db.entities.SaleProduct;

import java.util.List;

/**
 * SelectMultipleTreeDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SelectMultipleProductTypeTreeDialogView extends UstadView {

    String VIEW_NAME = "SelectMultipleProductTypeTreeDialog";

    String ARG_PRODUCT_SELECTED_SET = "ProductSelected";

    void populateTopProductType(List<SaleProduct> locations);

    void setTitle(String title);

    /**
     * For Android: closes the activity.
     */
    void finish();

}
