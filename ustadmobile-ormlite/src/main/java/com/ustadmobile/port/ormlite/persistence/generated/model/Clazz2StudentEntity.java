package com.ustadmobile.port.ormlite.persistence.generated.model;

import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.port.sharedse.persistence.proxy.Clazz2Student;
import com.j256.ormlite.field.DatabaseField;
import com.ustadmobile.port.sharedse.persistence.proxy.Clazz;
import com.ustadmobile.port.sharedse.persistence.proxy.Person;

@DatabaseTable(tableName = "clazz2student")
public class Clazz2StudentEntity implements Clazz2Student {

	static public final String COLNAME_U_UID = "u_uid";
	@DatabaseField(columnName = COLNAME_U_UID, id = true)
	private String uUID;
	static public final String COLNAME_CLAZZ = "clazz";
	@DatabaseField(columnName = COLNAME_CLAZZ, foreign = true, foreignAutoRefresh = true)
	private ClazzEntity clazz;
	static public final String COLNAME_STUDENT = "student";
	@DatabaseField(columnName = COLNAME_STUDENT, foreign = true, foreignAutoRefresh = true)
	private PersonEntity student;

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

	public Person getStudent() {
		return student;
	}

	public void setStudent(Person student) {
		this.student = (PersonEntity) student;
	}
}