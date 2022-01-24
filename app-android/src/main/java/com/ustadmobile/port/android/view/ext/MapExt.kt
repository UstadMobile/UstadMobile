package com.ustadmobile.port.android.view.ext

import android.content.Context
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @param tabs is a list of views and their arguments e.g. "ViewName1?arg=value", "ViewName2", etc
 */
fun Map<String, Int>.createTabLayoutStrategy(tabs: List<String>,  context: Context): TabLayoutMediator.TabConfigurationStrategy {
    val titleMap = this.map { it.key to context.getString(it.value) }.toMap()
    return TabLayoutMediator.TabConfigurationStrategy{ tab, position ->
        val viewName = tabs[position].substringBefore('?')
        tab.text = titleMap[viewName]
    }
}
