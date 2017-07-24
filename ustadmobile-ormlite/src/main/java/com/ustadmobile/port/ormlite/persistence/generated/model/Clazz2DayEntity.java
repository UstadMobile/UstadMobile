package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.Clazz2Day;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Clazz;
import com.ustadmobile.port.sharedse.persistence.proxy.Day;

@DatabaseTable(tableName = "clazz2day")
public class Clazz2DayEntity implements Clazz2Day {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_CLAZZ = "clazz";
	@DatabaseField(columnName = COLNAME_CLAZZ, foreign = true, foreignAutoRefresh = true)
	private ClazzEntity clazz;
	static public final String COLNAME_DAY = "day";
	@DatabaseField(columnName = COLNAME_DAY, foreign = true, foreignAutoRefresh = true)
	private DayEntity day;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public Clazz getClazz() {
		return clazz;
	}

	public void setClazz(Clazz clazz) {
		this.clazz = (ClazzEntity) clazz;
	}

	public Day getDay() {
		return day;
	}

	public void setDay(Day day) {
		this.day = (DayEntity) day;
	}
}