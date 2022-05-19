import com.ustadmobile.core.schedule.getRawTimezoneOffset
import com.ustadmobile.core.schedule.getTimezoneOffset
import com.ustadmobile.core.util.defaultJsonSerializer
import kotlin.js.Date
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TimezoneHelperTest {
    @BeforeTest
    fun init(){
        defaultJsonSerializer()
    }

    @Test
    fun givenTimeZone_whenAskedForARawOffset_shouldProvideIt(){
        val offset = getRawTimezoneOffset("Asia/Dubai")
        assertEquals(240000,offset)
    }

    @Test
    fun givenTimeZoneAndUtcTime_whenAskedForAnOffset_shouldProvideIt(){
        val offset = getTimezoneOffset("Asia/Dubai", Date().getTime().toLong())
        assertEquals(240,offset)
    }

    @Test
    fun givenTimeZoneWithMinutesOffset_whenAskedForAnOffset_shouldProvideIt(){
        val offset = getTimezoneOffset("Asia/Calcutta", Date().getTime().toLong())
        assertEquals(330,offset)
    }
}