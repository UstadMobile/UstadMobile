package com.ustadmobile.core.view;

import com.ustadmobile.lib.db.entities.ClazzActivity;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzActivityWithChangeTitle;

import java.util.LinkedHashMap;

/**
 * ClazzActivityList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzActivityListView extends UstadView {

    String VIEW_NAME = "ClazzActivityList";

    String ACTIVITY_BAR_LABEL_DESC = "Activity Bar chart";
    int ACTIVITY_BAR_CHART_HEIGHT = 100;
    int ACTIVITY_BAR_CHART_AXIS_MINIMUM = 0;

    int CHART_DURATION_LAST_WEEK = 1;
    int CHART_DURATION_LAST_MONTH = 2;
    int CHART_DURATION_LAST_YEAR = 3;

    /**
     * Sets Current provider
     * <p>
     * This method's purpose is to set the provider given to it to the view.
     * On Android it will be set to the recycler view..
     *
     * @param listProvider The provider data
     */
    void setListProvider(UmProvider<ClazzActivityWithChangeTitle> listProvider);

    /**
     * For Android: closes the activity.
     */
    void finish();

    void updateActivityBarChart(LinkedHashMap<Float, Float> dataMap);

    void setClazzActivityChangesDropdownPresets(String[] presets);

    /**
     * Resets the report buttons
     */
    void resetReportButtons();


    void setFABVisibility(boolean visible);
}
