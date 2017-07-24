package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.CalendarHolidays;
import com.j256.ormlite.field.DatabaseField;

@DatabaseTable(tableName = "calendar_holidays")
public class CalendarHolidaysEntity implements CalendarHolidays {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}
}