# lib-entities

This module contains database entity POJO classes, annotated with
annotations from lib-database-annotation. These are roughly analagous
to entities used in the Room Persistence Framework.

__Important__: for the moment entities must have both Um annotations and 
the required Room annotations e.g. @UmEntity and @Entity. Boolean 
properties in Kotlin use the get prefix instead of is. Converting 
from Java to Kotlin might result in is being added to the fieldname
itself.

Correct:
```
var available: Boolean
```
Incorrect:
```
var isAvailable: Boolean
```


### Naming convention:

**UID**: camel case, entity name, followed by Uid
e.g. in ClazzMember.kt
```
@UmPrimaryKey
@PrimaryKey
var clazzMemberUid: Long
```

**Join fields**: entity name that holds the relationship field, followed by the foreign key field name.

Eg. in ClazzMember.kt:

```
//Foreign key to connect to the Person table:
var clazzMemberPersonUid: Long
```

### Primary Constructor 

primary constructor needs to be implemted on all entities 

```
class TestEntity(){

}

constructor() does not work 
```