# lib-entities

This module contains database entity POKO classes, with most annotations
being the standard room annotations.

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
@PrimaryKey(autoGenerate = true)
var clazzMemberUid: Long
```

**Join fields**: entity name that holds the relationship field, followed by the foreign key field name.

Eg. in ClazzMember.kt:

```
//Foreign key to connect to the Person table:
var clazzMemberPersonUid: Long
```

### Field nullability
Numerical and boolean types must be __non-nullable__ (e.g. as per Java 
primitive types). Strings must be __nullable__.

### Constructors 

The Kotlin primary constructor needs to be present on all entities. The
constructor must be usable with no arguments (e.g. no arguments or
a primary constructor where all parameters have default values).  Failure
to add a Kotlin primary constructor will cause a strange exception when
the Android Room annotation processor runs.

Works:
```
class TestEntity(var name: String? = null)
```

Works:
```
class TestEntity() {
    var name: String? = null
}
```


Does not work (has empty constructor, but is missing Kotlin primary constructor):
```
class TestEntity{

constructor() 

} 
```
