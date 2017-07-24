package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.UMPackage;
import com.j256.ormlite.field.DatabaseField;

@DatabaseTable(tableName = "u_mpackage")
public class UMPackageEntity implements UMPackage {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_NAME = "name";
	@DatabaseField(columnName = COLNAME_NAME)
	private String name;
	static public final String COLNAME_DESC = "desc";
	@DatabaseField(columnName = COLNAME_DESC)
	private String desc;
	static public final String COLNAME_MAX_STUDENTS = "max_students";
	@DatabaseField(columnName = COLNAME_MAX_STUDENTS)
	private int maxStudents;
	static public final String COLNAME_MAX_TEACHERS = "max_teachers";
	@DatabaseField(columnName = COLNAME_MAX_TEACHERS)
	private int maxTeachers;
	static public final String COLNAME_MAX_PUBLISHERS = "max_publishers";
	@DatabaseField(columnName = COLNAME_MAX_PUBLISHERS)
	private int maxPublishers;
	static public final String COLNAME_PRICE_RANGE_PER_MONTH = "price_range_per_month";
	@DatabaseField(columnName = COLNAME_PRICE_RANGE_PER_MONTH)
	private float priceRangePerMonth;

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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getMaxStudents() {
		return maxStudents;
	}

	public void setMaxStudents(int maxStudents) {
		this.maxStudents = maxStudents;
	}

	public int getMaxTeachers() {
		return maxTeachers;
	}

	public void setMaxTeachers(int maxTeachers) {
		this.maxTeachers = maxTeachers;
	}

	public int getMaxPublishers() {
		return maxPublishers;
	}

	public void setMaxPublishers(int maxPublishers) {
		this.maxPublishers = maxPublishers;
	}

	public float getPriceRangePerMonth() {
		return priceRangePerMonth;
	}

	public void setPriceRangePerMonth(float priceRangePerMonth) {
		this.priceRangePerMonth = priceRangePerMonth;
	}
}