package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.Alert2AlertType;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Alert;
import com.ustadmobile.port.sharedse.persistence.proxy.AlertType;

@DatabaseTable(tableName = "alert2alert_type")
public class Alert2AlertTypeEntity implements Alert2AlertType {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_ALERT = "alert";
	@DatabaseField(columnName = COLNAME_ALERT, foreign = true, foreignAutoRefresh = true)
	private AlertEntity alert;
	static public final String COLNAME_ALERT_TYPE = "alert_type";
	@DatabaseField(columnName = COLNAME_ALERT_TYPE, foreign = true, foreignAutoRefresh = true)
	private AlertTypeEntity alertType;

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

	public AlertType getAlertType() {
		return alertType;
	}

	public void setAlertType(AlertType alertType) {
		this.alertType = (AlertTypeEntity) alertType;
	}
}