
#!/bin/bash

# Exit on error
set -e

# Define LDAP admin credentials
LDAP_ADMIN_DN="cn=admin,dc=example,dc=com"
LDAP_ADMIN_PASSWORD="admin_password"
LDAP_CONTAINER="ldap"

# Create LDIF for user
cat <<EOF > user.ldif
dn: uid=jdoe,ou=users,dc=example,dc=com
objectClass: inetOrgPerson
uid: jdoe
cn: John Doe
sn: Doe
userPassword: $(slappasswd -s password123)
EOF

# Create LDIF for group
cat <<EOF > group.ldif
dn: cn=webapp-users,ou=groups,dc=example,dc=com
objectClass: groupOfNames
cn: webapp-users
member: uid=jdoe,ou=users,dc=example,dc=com
EOF

# Copy LDIF files into the LDAP container
docker cp user.ldif $LDAP_CONTAINER:/tmp/user.ldif
docker cp group.ldif $LDAP_CONTAINER:/tmp/group.ldif

# Add entries using ldapadd
docker exec -it $LDAP_CONTAINER ldapadd -x -D "$LDAP_ADMIN_DN" -w "$LDAP_ADMIN_PASSWORD" -f /tmp/user.ldif
docker exec -it $LDAP_CONTAINER ldapadd -x -D "$LDAP_ADMIN_DN" -w "$LDAP_ADMIN_PASSWORD" -f /tmp/group.ldif

# Clean up
rm user.ldif group.ldif
