import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.getRawTimezoneOffset
import com.ustadmobile.core.schedule.getTimezoneOffset
import kotlin.js.Date
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TimezoneHelperTest {
    @BeforeTest
    fun init(){
        UstadMobileSystemImpl.instance.registerDefaultSerializer()
    }

    @Test
    fun givenTimeZone_whenAskedForARawOffset_shouldProvideIt(){
        val offset = getRawTimezoneOffset("Asia/Dubai")
        assertEquals(240,offset)
    }

    @Test
    fun givenTimeZoneAndUtcTime_whenAskedForAnOffset_shouldProvideIt(){
        val offset = getTimezoneOffset("Asia/Dubai", Date().getTime().toLong())
        assertEquals(4,offset)
    }
}