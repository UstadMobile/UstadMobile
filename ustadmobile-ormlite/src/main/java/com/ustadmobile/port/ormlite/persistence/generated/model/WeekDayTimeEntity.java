package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.WeekDayTime;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Day;

@DatabaseTable(tableName = "week_day_time")
public class WeekDayTimeEntity implements WeekDayTime {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_DAY = "day";
	@DatabaseField(columnName = COLNAME_DAY, foreign = true, foreignAutoRefresh = true)
	private DayEntity day;
	static public final String COLNAME_FROM_TIME = "from_time";
	@DatabaseField(columnName = COLNAME_FROM_TIME)
	private long fromTime;
	static public final String COLNAME_TO_TIME = "to_time";
	@DatabaseField(columnName = COLNAME_TO_TIME)
	private long toTime;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public Day getDay() {
		return day;
	}

	public void setDay(Day day) {
		this.day = (DayEntity) day;
	}

	public long getFromTime() {
		return fromTime;
	}

	public void setFromTime(long fromTime) {
		this.fromTime = fromTime;
	}

	public long getToTime() {
		return toTime;
	}

	public void setToTime(long toTime) {
		this.toTime = toTime;
	}
}