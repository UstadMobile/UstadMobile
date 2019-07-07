package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.SelectMultipleProductTypeTreeDialogView;
import com.ustadmobile.lib.db.entities.SaleProduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static com.ustadmobile.core.controller.ReportOptionsDetailPresenter.convertCSVStringToLongList;
import static com.ustadmobile.core.view.SelectMultipleProductTypeTreeDialogView.ARG_PRODUCT_SELECTED_SET;

/**
 * The SelectMultipleTreeDialog Presenter.
 */
public class SelectMultipleProductTypeTreeDialogPresenter
        extends CommonEntityHandlerPresenter<SelectMultipleProductTypeTreeDialogView> {

    private HashMap<String, Long> selectedOptions;

    private List<Long> selectedProductTypeUidsList;

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public SelectMultipleProductTypeTreeDialogPresenter(Object context, Hashtable arguments,
                                                        SelectMultipleProductTypeTreeDialogView view) {
        super(context, arguments, view);

        if (arguments.containsKey(ARG_PRODUCT_SELECTED_SET)) {
            String productTypesArrayString = arguments.get(ARG_PRODUCT_SELECTED_SET).toString();
            selectedProductTypeUidsList = convertCSVStringToLongList(productTypesArrayString);
        }

        selectedOptions = new HashMap<>();
        getTopProductTypes();

    }

    static ArrayList<Long> convertLongArray(long[] array) {
        ArrayList<Long> result = new ArrayList<Long>(array.length);
        for (long item : array)
            result.add(item);
        return result;
    }

    public HashMap<String, Long> getSelectedOptions() {
        return selectedOptions;
    }

    private void getTopProductTypes() {
        SaleProductParentJoinDao parentJoinDao = repository.getSaleProductParentJoinDao();
        parentJoinDao.findTopSaleProductsAsync(new UmCallback<List<SaleProduct>>() {
            @Override
            public void onSuccess(List<SaleProduct> result) {
                view.populateTopProductType(result);
            }

            @Override
            public void onFailure(Throwable exception) { exception.printStackTrace(); }
        });
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }


    public void handleClickPrimaryActionButton() {
        view.finish();
    }


    public List<Long> getSelectedProductTypeUidsList() {
        if (selectedProductTypeUidsList == null) {
            return new ArrayList<>();
        }
        return selectedProductTypeUidsList;
    }

    @Override
    public void entityChecked(String entityName, Long entityUid, boolean checked) {
        if (checked) {
            selectedOptions.put(entityName, entityUid);
        } else {
            selectedOptions.remove(entityName);
        }
    }

}
