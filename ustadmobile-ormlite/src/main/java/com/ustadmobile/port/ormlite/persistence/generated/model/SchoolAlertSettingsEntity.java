package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.SchoolAlertSettings;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.School;

@DatabaseTable(tableName = "school_alert_settings")
public class SchoolAlertSettingsEntity implements SchoolAlertSettings {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_SCHOOL = "school";
	@DatabaseField(columnName = COLNAME_SCHOOL, foreign = true, foreignAutoRefresh = true)
	private SchoolEntity school;
	static public final String COLNAME_CUT_OFF_TIME = "cut_off_time";
	@DatabaseField(columnName = COLNAME_CUT_OFF_TIME)
	private int cutOffTime;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = (SchoolEntity) school;
	}

	public int getCutOffTime() {
		return cutOffTime;
	}

	public void setCutOffTime(int cutOffTime) {
		this.cutOffTime = cutOffTime;
	}
}