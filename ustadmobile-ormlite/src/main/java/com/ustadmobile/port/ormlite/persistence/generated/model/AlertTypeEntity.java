package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.AlertType;
import com.j256.ormlite.field.DatabaseField;
import java.util.Collection;

@DatabaseTable(tableName = "alert_type")
public class AlertTypeEntity implements AlertType {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_NAME = "name";
	@DatabaseField(columnName = COLNAME_NAME)
	private String name;
	static public final String COLNAME_ALERTS = "alerts";
	@DatabaseField(columnName = COLNAME_ALERTS, foreign = true, foreignAutoRefresh = true)
	private CollectionEntity alerts;

	public String getUUID() {
		return uUID;
	}

	public void setUUID(String uUID) {
		this.uUID = uUID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection getAlerts() {
		return alerts;
	}

	public void setAlerts(Collection alerts) {
		this.alerts = (CollectionEntity) alerts;
	}
}