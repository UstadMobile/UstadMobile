
Person
    If new, check remoteNode has permission to create new
    If existing, check remoteNode has update permission on person


Clazz
    If new, check remoteNode has permission to create new
    If existing, check remoteNode has update permission on clazz

PersonGroup
    If new uid:
        If related to person: check this is the groupUid specified by the person entity. Check that the remoteNode has permission to create new person.
        If related to clazz students/parents/teachers: check this is the groupUid specified by the clazz entity. Check the remoteNode has permission to create new clazz.

      If update:
         If related to person: check the remoteNode has update permission for related person
         if related to clazz: check the remoteNode has update permission for related clazz.

PersonGroupMember
    If no existing members for persongroup:
        As per PersonGroup if new uid

    If there are existing group members:
        As per update of PersonGroup (e.g. first PersonGroupMember insert MUST be the one that grants update permission to node if needed)

ScopedGrant
    If scoped to a specific table and entity:
        If remoteNodeId is owner of entity - accept
        If remoteNodeId has update permission on entity - accept
        Else - reject

    If not scoped:
        ScopedGrant must not exceed global permissions of remoteNodeId (eg. delegate what you can do is allowed, not more)

UserSession:
    Check signed by public key for Person
   
