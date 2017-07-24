package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.UMCalendar2Holiday;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.UMCalendar;
import com.ustadmobile.port.sharedse.persistence.proxy.Holiday;

@DatabaseTable(tableName = "u_mcalendar2holiday")
public class UMCalendar2HolidayEntity implements UMCalendar2Holiday {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_CALENDAR = "calendar";
	@DatabaseField(columnName = COLNAME_CALENDAR, foreign = true, foreignAutoRefresh = true)
	private UMCalendarEntity calendar;
	static public final String COLNAME_HOLIDAY = "holiday";
	@DatabaseField(columnName = COLNAME_HOLIDAY, foreign = true, foreignAutoRefresh = true)
	private HolidayEntity holiday;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public UMCalendar getCalendar() {
		return calendar;
	}

	public void setCalendar(UMCalendar calendar) {
		this.calendar = (UMCalendarEntity) calendar;
	}

	public Holiday getHoliday() {
		return holiday;
	}

	public void setHoliday(Holiday holiday) {
		this.holiday = (HolidayEntity) holiday;
	}
}