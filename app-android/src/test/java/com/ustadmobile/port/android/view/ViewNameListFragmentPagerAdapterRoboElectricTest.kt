package com.ustadmobile.port.android.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewNameListFragmentPagerAdapterRoboElectricTest {

    class DummyFragment: Fragment() {

    }

    @Test
    fun givenValidUriListAndFragmentMapping_whenGetItemCalled_thenShouldInstantiateFragmentAndSetArgs() {
        val viewList = listOf("DummyView?arg=value")

        val listPagerAdapter = ViewNameListFragmentPagerAdapter(mock {  },
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                viewList, mapOf("DummyView" to DummyFragment::class.java),
                mapOf("DummyView" to "TabTitle"))

        val fragCreated = listPagerAdapter.getItem(0)
        Assert.assertNotNull("Fragment was instantiated", fragCreated)
        Assert.assertEquals("Page count is as per list", viewList.size, listPagerAdapter.count)
        Assert.assertEquals("Tab name is returned as expected", "TabTitle",
                listPagerAdapter.getPageTitle(0))
        Assert.assertEquals("Argument set as expected", "value",
                fragCreated.arguments?.getString("arg"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun givenUriNotMapped_whenGetItemCalled_thenShouldThrowIllegalArgumentException() {
        val viewList = listOf("NotMappedView?arg=value")

        val listPagerAdapter = ViewNameListFragmentPagerAdapter(mock {  },
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                viewList, mapOf("DummyView" to DummyFragment::class.java),
                mapOf("DummyView" to "TabTitle"))

        listPagerAdapter.getItem(0)
    }


}