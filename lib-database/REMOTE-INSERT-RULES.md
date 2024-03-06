
Person
    If new, check remoteNode has permission to create new
    If existing, check remoteNode has update permission on person


Clazz
    If new, check remoteNode has permission to create new
    If existing, check remoteNode has update permission on clazz

CoursePermission
    If remoteNode is course owner, accept
    If remoteNode has course edit permission, accept
    Else, reject

SystemPermission
    If remoteNode has admin permission, accept
    Else reject. 

UserSession:
    Check signed by public key for Person
   
