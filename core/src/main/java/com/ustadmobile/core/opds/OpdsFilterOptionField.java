package com.ustadmobile.core.opds;

/**
 * Created by mike on 12/16/17.
 */

public class OpdsFilterOptionField {

    public static final int TYPE_DROPDOWN = 0;

    private int filterType;

    private String filterName;

    private String[] filterOptions;


    /**
     *
     * @return
     */
    public int getFilterType() {
        return filterType;
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String[] getFilterOptions() {
        return filterOptions;
    }

    public void setFilterOptions(String[] filterOptions) {
        this.filterOptions = filterOptions;
    }
}
