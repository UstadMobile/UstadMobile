# lib-database

This module contains the java entity objects (POJOs) used for 
the database. 

Naming convention:

**UID**: camel case, entity name, followed by Uid 
e.g. in ClazzMember.java
```
@UmPrimaryKey
private long clazzMemberUid;
```

**Join fields**: entity name that holds the relationship field, followed by the foreign key field name.

Eg. in ClazzMember.java:

```
//Foreign key to connect to the Person table:
private long clazzMemberPersonUid;
```


