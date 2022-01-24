package com.ustadmobile.port.android.view

import androidx.fragment.app.Fragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.mockito.kotlin.*
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
                mock { }, viewList,
                mapOf("DummyView" to DummyFragment::class.java))

        val fragCreated = listPagerAdapter.createFragment(0)
        Assert.assertNotNull("Fragment was instantiated", fragCreated)
        Assert.assertEquals("Page count is as per list", viewList.size, listPagerAdapter.itemCount)
        Assert.assertEquals("Argument set as expected", "value",
                fragCreated.arguments?.getString("arg"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun givenUriNotMapped_whenGetItemCalled_thenShouldThrowIllegalArgumentException() {
        val viewList = listOf("NotMappedView?arg=value")

        val listPagerAdapter = ViewNameListFragmentPagerAdapter(mock {  },
                mock{}, viewList, mapOf("DummyView" to DummyFragment::class.java))

        listPagerAdapter.createFragment(0)
    }


}