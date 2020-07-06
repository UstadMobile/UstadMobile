package com.ustadmobile.port.android.view.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMFileUtil
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

private fun makeBundleFromArgs(viewUri: String, index: Int): Bundle {
    val bundle = UMAndroidUtil.mapToBundle(UMFileUtil.parseURLQueryString(viewUri)) ?: throw IllegalStateException("Null bundle from mapToBundle")
    return bundle
}

/**
 * This FragmentPagerAdapter is designed to work with Ustad Mobile's views implemented in Fragments.
 * Given a list of viewUris (eg ViewName?arg=val) strings and a map of view names to fragment classes,
 * it will instantiate the correct fragment and set the arguments.
 *
 * @param viewList A list of ViewUris that should be shown as tabs (e.g. ["ViewName1?arg1=val", "ViewName2"])
 * @param viewNameToFragmentClassMap a map of the view names to the class objects for fragments that implement them
 * @param viewNameToPageTitleMap a map of the view names to the titles that should be used for the tabs (e.g. ViewName -> "Tab title")
 * @param bundleMakerFn A function that will take in the viewUri and index and provide the argument
 * bundle. By default this will simply parse out the argument bundle from the item in the viewList.
 * However if all views have the same arguments you can just provide a single premade bundle.
 */
open class ViewNameListFragmentPagerAdapter(fm: FragmentManager, behavior: Int,
                                            val viewList: List<String>,
                                            val viewNameToFragmentClassMap: Map<String, Class<out Fragment>>,
                                            val viewNameToPageTitleMap: Map<String, String>,
                                            val bundleMakerFn: (viewUri: String, index: Int) -> Bundle = ::makeBundleFromArgs) : FragmentPagerAdapter(fm, behavior) {

    override fun getItem(position: Int): Fragment {
        val viewName = viewList[position].substringBefore('?')
        return viewNameToFragmentClassMap[viewName]?.newInstance()?.also {
            it.arguments = bundleMakerFn(viewList[position], position)
        } ?: throw IllegalArgumentException("No fragment found for view $viewName")
    }

    override fun getCount() = viewList.size

    override fun getPageTitle(position: Int): CharSequence? {
        val viewName = viewList[position].substringBefore('?')
        return viewNameToPageTitleMap[viewName]
    }
}