package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.Alert2Clazz;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Alert;
import com.ustadmobile.port.sharedse.persistence.proxy.School;

@DatabaseTable(tableName = "alert2clazz")
public class Alert2ClazzEntity implements Alert2Clazz {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_ALERT = "alert";
	@DatabaseField(columnName = COLNAME_ALERT, foreign = true, foreignAutoRefresh = true)
	private AlertEntity alert;
	static public final String COLNAME_CLAZZ = "clazz";
	@DatabaseField(columnName = COLNAME_CLAZZ, foreign = true, foreignAutoRefresh = true)
	private SchoolEntity clazz;

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

	public School getClazz() {
		return clazz;
	}

	@Override
	public void setClazz(Class clazz) {
		//this.clazz = clazz;
	}

	public void setClazz(School clazz) {
		this.clazz = (SchoolEntity) clazz;
	}
}