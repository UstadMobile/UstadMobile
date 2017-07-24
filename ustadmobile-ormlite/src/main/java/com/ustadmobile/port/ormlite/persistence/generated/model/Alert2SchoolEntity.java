package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.Alert2School;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Alert;
import com.ustadmobile.port.sharedse.persistence.proxy.School;

@DatabaseTable(tableName = "alert2school")
public class Alert2SchoolEntity implements Alert2School {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_ALERT = "alert";
	@DatabaseField(columnName = COLNAME_ALERT, foreign = true, foreignAutoRefresh = true)
	private AlertEntity alert;
	static public final String COLNAME_SCHOOL = "school";
	@DatabaseField(columnName = COLNAME_SCHOOL, foreign = true, foreignAutoRefresh = true)
	private SchoolEntity school;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public Alert getAlert() {
		return alert;
	}

	public void setAlert(Alert alert) {
		this.alert = (AlertEntity) alert;
	}

	public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = (SchoolEntity) school;
	}
}